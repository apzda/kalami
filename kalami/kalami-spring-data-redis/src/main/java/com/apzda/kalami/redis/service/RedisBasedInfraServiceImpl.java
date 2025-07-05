/*
 * Copyright 2023-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apzda.kalami.redis.service;

import cn.hutool.core.date.DateUtil;
import com.apzda.kalami.data.TempData;
import com.apzda.kalami.service.CounterService;
import com.apzda.kalami.service.DistributedLockService;
import com.apzda.kalami.service.TempStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
public class RedisBasedInfraServiceImpl implements CounterService, TempStorageService, DistributedLockService {

    private final static Pattern ID_PATTERN = Pattern.compile("^(.+?)@(.+)$");

    private static final Map<String, Lock> locks = new ConcurrentHashMap<>();

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final LoadingCache<String, Boolean> idCache = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(10))
        .build(new CacheLoader<>() {
            @Override
            @NonNull
            public Boolean load(@NonNull String key) {
                try {
                    val matcher = ID_PATTERN.matcher(key);
                    if (matcher.find()) {
                        val id = matcher.group(1);
                        val interval = Long.parseLong(matcher.group(2));
                        stringRedisTemplate.expire(id, interval + 1, TimeUnit.SECONDS);
                    }
                }
                catch (Exception e) {
                    log.warn("Cannot set expired time of the key of counter '{}': {}", key, e.getMessage());
                }
                return true;
            }
        });

    public RedisBasedInfraServiceImpl(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        Assert.notNull(stringRedisTemplate, "stringRedisTemplate");
        Assert.notNull(objectMapper, "objectMapper");
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public int count(@NonNull String key, long interval) {
        Assert.isTrue(interval > 0, "interval = " + interval + " <= 0");
        val a = DateUtil.currentSeconds() / interval;
        val id = "counter." + key + "." + a;
        try {
            var increment = stringRedisTemplate.opsForValue().increment(id);
            if (increment == null) {
                increment = Long.parseLong(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(id)));
            }
            return Math.toIntExact(increment);
        }
        catch (Exception e) {
            log.warn("Cannot get try count for {} - {}", id, e.getMessage());
            return Integer.MAX_VALUE;
        }
        finally {
            try {
                idCache.getUnchecked(id + "@" + interval);
            }
            catch (Exception e) {
                log.warn("Cannot set TTL of the key of counter '{}': {}", id, e.getMessage());
            }
        }
    }

    @Override
    public <T extends TempData> T save(@NonNull String id, @NonNull T data) throws Exception {
        val key = "storage." + id;
        val ca = objectMapper.writeValueAsString(data);
        val expired = data.getExpireTime();
        if (expired == null || expired.isZero() || expired.isNegative()) {
            stringRedisTemplate.opsForValue().set(key, ca);
        }
        else {
            stringRedisTemplate.opsForValue().set(key, ca, expired.toSeconds(), TimeUnit.SECONDS);
        }
        return data;
    }

    @Override
    @NonNull
    public <T extends TempData> Optional<T> load(@NonNull String id, @NonNull Class<T> tClass) {
        val key = "storage." + id;
        try {
            val value = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(value)) {
                return Optional.of(objectMapper.readValue(value, tClass));
            }
        }
        catch (Exception e) {
            log.error("Cannot load TempData({}): {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean exist(@NonNull String id) {
        val key = "storage." + id;
        try {
            return stringRedisTemplate.hasKey(key);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void remove(@NonNull String id) {
        val key = "storage." + id;
        try {
            stringRedisTemplate.delete(key);
        }
        catch (Exception e) {
            log.warn("Cannot delete TempData({}): {}", id, e.getMessage());
        }
    }

    @Override
    public void expire(@NonNull String id, @NonNull Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            return;
        }

        val key = "storage." + id;
        try {
            stringRedisTemplate.expire(key, duration);
        }
        catch (Exception e) {
            log.warn("Cannot expire TempData({}): {}", id, e.getMessage());
        }
    }

    @Override
    @NonNull
    public Duration getTtl(@NonNull String id) {
        val key = "storage." + id;
        try {
            val expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            return Duration.ofSeconds(expire);
        }
        catch (Exception e) {
            log.warn("Cannot get the Expire TempData({}): {}", id, e.getMessage());
        }
        return Duration.ZERO;
    }

    @Override
    @NonNull
    public Lock getLock(@NonNull String id) {
        val key = "lock." + id;

        return locks.computeIfAbsent(key, k -> new SimpleRedisLock(k, stringRedisTemplate));
    }

    @Override
    public void deleteLock(@NonNull String id) {
        val key = "lock." + id;
        locks.remove(key);
    }

    @SuppressWarnings("all")
    static class SimpleRedisLock extends ReentrantLock {

        private final static Duration DEFAULT_TIMEOUT = Duration.ofSeconds(300);

        private final StringRedisTemplate stringRedisTemplate;

        private final String lockName;

        private final ThreadLocal<String> holder = new ThreadLocal<>();

        SimpleRedisLock(String lockName, StringRedisTemplate redisTemplate) {
            super();
            this.lockName = lockName;
            this.stringRedisTemplate = redisTemplate;
        }

        @Override
        public boolean isLocked() {
            return Objects.equals(Thread.currentThread().getName(), holder.get()) && super.isLocked();
        }

        @Override
        public void unlock() {
            try {
                if (isLocked() && isHeldByCurrentThread()) {
                    super.unlock();
                    try {
                        stringRedisTemplate.delete(lockName);
                    }
                    catch (Exception e) {
                        log.error("Cannot unlock {}. Please unlock it manually: DEL {} - {}", lockName, lockName,
                                e.getMessage());
                    }
                }
            }
            catch (Exception ignored) {
                log.error("Cannot unlock {}", lockName);
            }
            finally {
                this.holder.remove();
            }
        }

        @Override
        public void lock() {
            if (!tryLock()) {
                throw new IllegalMonitorStateException(String.format("Cannot lock %s - %s", lockName));
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            if (!tryLock(DEFAULT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                throw new IllegalMonitorStateException(String.format("Cannot lock %s", lockName));
            }
        }

        @Override
        public boolean tryLock() {
            try {
                return tryLock(DEFAULT_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            }
            catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean tryLock(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
            val start = System.currentTimeMillis();
            if (Objects.equals(Thread.currentThread().getName(), holder.get()) && isHeldByCurrentThread()) {
                return true;
            }
            if (super.tryLock(timeout, unit)) {
                val end = System.currentTimeMillis();
                try {
                    lock_(Duration.ofMillis(unit.toMillis(timeout) - (end - start)));
                }
                catch (Exception e) {
                    log.warn(e.getMessage());
                    // 获取redis锁出错时仅释放本地锁
                    super.unlock();
                    return false;
                }
                return true;
            }

            return false;
        }

        private void lock_(Duration duration) throws InterruptedException {
            try {
                long count = Optional.ofNullable(duration).orElse(DEFAULT_TIMEOUT).toMillis() / 100;
                do {
                    val increment = stringRedisTemplate.opsForValue().increment(lockName);
                    if (increment != null && increment == 1) {
                        holder.set(Thread.currentThread().getName());
                        break;
                    }
                    count--;
                    if (count < 0) {
                        throw new InterruptedException(String.format("Timeout while waiting for lock %s", lockName));
                    }
                    TimeUnit.MILLISECONDS.sleep(100);
                }
                while (true);
            }
            catch (InterruptedException e) {
                throw e;
            }
            catch (Exception e) {
                throw new IllegalMonitorStateException(String.format("Cannot lock %s - %s", lockName, e.getMessage()));
            }
        }

    }

}
