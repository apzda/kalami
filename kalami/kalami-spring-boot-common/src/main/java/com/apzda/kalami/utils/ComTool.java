/*
 * Copyright 2025 the original author or authors.
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
package com.apzda.kalami.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.codec.PercentCodec;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.util.*;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.EasyExcel;
import com.apzda.kalami.tenant.TenantManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

/**
 * 公用工具类
 *
 * @author john <luxi520cn@163.com>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComTool {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final String YYYY_MM = "yyyy-MM";

    public static final String HH_MM_SS = "HH:mm:ss";

    public static final String YYYYMMDD = "yyyyMMdd";

    public static final String YYYYMM = "yyyyMM";

    public static final String HHMMSS = "HHmmss";

    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final String YYYYNMMYDDR = "yyyy年MM月dd日";

    public static final String YYYYMMDDHHMMSSNNN = "yyyyMMddHHmmssSSS";

    public static final String YYMMDDHHMMSSNNN = "yyMMddHHmmssSSS";

    public static final String YYYY_MM_DD_HH_MM_SS_NNN = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final String[] USER_AGENT_ARR = { "Android", "iPhone", "iPod", "iPad", "Windows Phone",
            "MQQBrowser" };

    public static final String ENV = "pro.active";

    public static final String ENV_PRD = "prd";

    public static final String ENV_DEV = "dev";

    public static final String ENV_SIT = "sit";

    public static final String EMPTY = "";

    private static final ConcurrentHashMap<String, DateTimeFormatter> DTF_MAP = new ConcurrentHashMap<>();

    public static String LOCAL_ADDR;

    static {
        DTF_MAP.put(YYYY_MM_DD_HH_MM_SS, DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
        DTF_MAP.put(YYYY_MM_DD, DateTimeFormatter.ofPattern(YYYY_MM_DD));
        DTF_MAP.put(YYYY_MM, DateTimeFormatter.ofPattern(YYYY_MM));
        DTF_MAP.put(HH_MM_SS, DateTimeFormatter.ofPattern(HH_MM_SS));
        DTF_MAP.put(YYYYMMDD, DateTimeFormatter.ofPattern(YYYYMMDD));
        DTF_MAP.put(YYYYMM, DateTimeFormatter.ofPattern(YYYYMM));
        DTF_MAP.put(HHMMSS, DateTimeFormatter.ofPattern(HHMMSS));
        DTF_MAP.put(YYYYMMDDHHMMSS, DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS));
        DTF_MAP.put(YYYYNMMYDDR, DateTimeFormatter.ofPattern(YYYYNMMYDDR));
        DTF_MAP.put(YYYYMMDDHHMMSSNNN, DateTimeFormatter.ofPattern(YYYYMMDDHHMMSSNNN));
        DTF_MAP.put(YYMMDDHHMMSSNNN, DateTimeFormatter.ofPattern(YYMMDDHHMMSSNNN));
        DTF_MAP.put(YYYY_MM_DD_HH_MM_SS_NNN, DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS_NNN));
        try {
            LOCAL_ADDR = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ignored) {
        }
    }

    /**
     * 查询当前租户ID
     */
    public static String getTenantId() {
        String tenant = TenantManager.tenantId();
        return tenant != null ? tenant : "0";
    }

    /**
     * 查询当前门店ID
     */
    public static Long getShopId() {
        String tenant = TenantManager.currentOrgId();
        return tenant != null ? Long.parseLong(tenant) : 0L;
    }

    /**
     * 集合拆分
     * @param collection 所有集合
     * @param size 拆分后子集的大小
     * @return 按大小拆分后的所有子集
     */
    public static <T> List<List<T>> split(Collection<T> collection, int size) {
        return CollectionUtil.split(collection, size);
    }

    /**
     * 异常重试处理
     * @param maxRetry 最大重试次数
     * @param interval 重试间隔(毫秒)
     */
    public static void exceptionRetry(Runnable runnable, int maxRetry, long interval) {
        int i = maxRetry;
        while (true) {
            try {
                runnable.run();
                break;
            }
            catch (Exception e) {
                i--;
                if (i < 0) {
                    throw new RuntimeException(format("超出最大重试次数{}", maxRetry), e);
                }
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(interval));
            }
        }
    }

    /**
     * 获取分片索引
     * @param primaryKey 数据主键
     * @param numberOfMachines 机器数量
     * @return 分配到机器索引-从0开始
     */
    public static int getShardIndex(String primaryKey, int numberOfMachines) {
        return Math.abs(fnvHash(primaryKey)) % numberOfMachines;
    }

    /**
     * fnvHash-效率高，分布均匀，适合数据分表使用
     * @param primaryKey 数据主键
     * @return 哈希值
     */
    public static int fnvHash(String primaryKey) {
        return HashUtil.fnvHash(primaryKey);
    }

    /**
     * 事件推送
     */
    public static void publishEvent(ApplicationEvent event) {
        SpringUtil.publishEvent(event);
    }

    /**
     * 查询ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }

    /**
     * 查询Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return SpringUtil.getBean(clazz);
    }

    /**
     * 查询同类型多个Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return SpringUtil.getBeansOfType(type);
    }

    /**
     * 查询配置内的属性
     */
    public static String getProperty(String key) {
        return SpringUtil.getProperty(key);
    }

    /**
     * 环境判断
     * @return 是否是对应环境
     */
    public static boolean isPrd() {
        return getEnv().equals(ENV_PRD);
    }

    /**
     * 环境判断
     * @return 是否是对应环境
     */
    public static boolean isSit() {
        return getEnv().equals(ENV_SIT);
    }

    /**
     * 环境判断
     * @return 是否是对应环境
     */
    public static boolean isDev() {
        return getEnv().equals(ENV_DEV);
    }

    /**
     * 获取环境变量标识
     */
    public static String getEnv() {
        String env = getProperty(ENV);
        return isNotBlank(env) ? env : ENV_DEV;
    }

    /**
     * 获取中文信息存储后的大约数据大小
     * @param data 字符串数据
     * @return 返回字节长度
     */
    public static int getDataLength(CharSequence data) {
        return data == null ? 0 : utf8Bytes(data).length;
    }

    /**
     * 使用EasyExcel导出EXCEL至文件
     * @param fullFilePath 文件完整路径
     */
    public static void exportExcel2File(Class<?> clazz, List<?> data, String fullFilePath) {
        EasyExcel.write(FileUtil.getOutputStream(FileUtil.file(fullFilePath)), clazz).sheet("sheet1").doWrite(data);
    }

    /**
     * 使用EasyExcel导出EXCEL
     */
    public static void exportExcel(Class<?> clazz, List<?> data, String fileName) {
        HttpServletResponse response = getHttpServletResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        try {
            EasyExcel.write(response.getOutputStream(), clazz).sheet("sheet1").doWrite(data);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前请求HttpServletResponse
     */
    public static HttpServletResponse getHttpServletResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    /**
     * 获取当前请求HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * 非空对象
     */
    public static boolean isNotNull(Object obj) {
        return ObjectUtil.isNotNull(obj);
    }

    /**
     * 空对象
     */
    public static boolean isNull(Object obj) {
        return ObjectUtil.isNull(obj);
    }

    /**
     * 非空字符串
     * @param str 字符串
     * @return 与isBlank相反
     */
    public static boolean isNotBlank(CharSequence str) {
        return StrUtil.isNotBlank(str);
    }

    /**
     * 是否是空字符串
     * @param str 字符串
     * @return 为null或全为空字符串返回true
     */
    public static boolean isBlank(CharSequence str) {
        return StrUtil.isBlank(str);
    }

    /**
     * 计算总页数
     */
    public static long computePages(long totalRecord, long size) {
        long totalPage = 0;
        if (size > 0) {
            totalPage = totalRecord / size;
            if (totalRecord % size != 0) {
                totalPage++;
            }
        }
        return totalPage;
    }

    /**
     * 创建订单号
     * @return 总长为24位=时间精准到毫秒15位+随机码9位
     */
    public static String createOrderCode() {
        return formatDateTime(LocalDateTime.now(), YYMMDDHHMMSSNNN) + randomNumbers(9);
    }

    /**
     * 判断两个Date对象是否是同年同月
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 是否是同年同月
     */
    public static boolean isSameYearAndMonth(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startDate);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(endDate);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }

    /**
     * 判断两个Date对象是否是同年同月
     * @param startDate 开始日期 yyyy-MM-dd
     * @param endDate 结束日期 yyyy-MM-dd
     * @return 是否是同年同月
     */
    public static boolean isSameYearAndMonth(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return sub(startDate, 7).equals(sub(endDate, 7));
    }

    /**
     * 获得匹配的字符串
     * @param regex 匹配的正则
     * @param content 被匹配的内容
     * @param groupIndex 匹配正则的分组序号
     * @return 匹配后得到的字符串，未匹配返回null
     */
    public static String matchGroup(String regex, CharSequence content, int groupIndex) {
        return ReUtil.get(regex, content, groupIndex);
    }

    /**
     * 内容是否匹配正则
     * @param regex 匹配的正则
     * @param content 被匹配的内容
     */
    public static boolean isMatch(String regex, CharSequence content) {
        return ReUtil.isMatch(regex, content);
    }

    /**
     * 读取单个文件到String
     */
    public static String readString(String fullFilePath, Charset charset) {
        return FileUtil.readString(fullFilePath, charset);
    }

    /**
     * 读取单个文件到String
     */
    public static String readUtf8String(String fullFilePath) {
        return FileUtil.readUtf8String(fullFilePath);
    }

    /**
     * 逐行读取处理
     */
    public static void readLines(String fullFilePath, Charset charset, LineHandler lineHandler) {
        FileUtil.readLines(FileUtil.file(fullFilePath), charset, lineHandler);
    }

    /**
     * 逐行读取处理
     */
    public static void readUtf8Lines(String fullFilePath, LineHandler lineHandler) {
        FileUtil.readUtf8Lines(FileUtil.file(fullFilePath), lineHandler);
    }

    /**
     * 向文件写入字节数组
     * @param data 字节数组
     * @param fullFilePath 文件完整路径
     * @param isAppend true覆盖原文件, false文件末尾追加
     */
    public static File fileWriteBytes(byte[] data, String fullFilePath, boolean isAppend) {
        return FileUtil.writeBytes(data, FileUtil.touch(fullFilePath), 0, data.length, isAppend);
    }

    /**
     * 将输入流写入至文件
     * @param in 输入流
     * @param fullFilePath 文件完整路径
     * @param isCloseIn 是否关闭输入流
     */
    public static File fileWriteFromStream(InputStream in, String fullFilePath, boolean isCloseIn) {
        return FileUtil.writeFromStream(in, FileUtil.touch(fullFilePath), isCloseIn);
    }

    /**
     * 获取BufferedReader
     * @param fullFilePath 文件完整路径
     */
    public static BufferedReader getUtf8Reader(String fullFilePath) {
        return FileUtil.getUtf8Reader(fullFilePath);
    }

    /**
     * 获取BufferedReader
     * @param fullFilePath 文件完整路径
     * @param charset 字符集
     */
    public static BufferedReader getReader(String fullFilePath, Charset charset) {
        return FileUtil.getReader(fullFilePath, charset);
    }

    /**
     * 获取BufferedWriter
     * @param fullFilePath 文件完整路径
     * @param charset 字符集
     * @param isAppend 是否追加内容
     */
    public static BufferedWriter getWriter(String fullFilePath, Charset charset, boolean isAppend) {
        return FileUtil.getWriter(fullFilePath, charset, isAppend);
    }

    /**
     * 获取UUID
     */
    public static String uuid() {
        return removeAny(StrUtil.uuid(), "-");
    }

    /**
     * 转换为仅包含日期的Date
     */
    public static Date dateOnly(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 转换为仅包含日期的Date
     */
    public static Date dateOnly(LocalDate date) {
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 转换为仅包含日期的Date
     */
    public static Date dateOnly(LocalDateTime date) {
        return Date.from(date.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 格式化为月份
     * @return Integer类型的月份：yyyyMM
     */
    public static Integer formatMonthToInt(Date date) {
        return Integer.valueOf(formatDateTime(date, YYYYMM));
    }

    /**
     * 格式化为月份
     * @return Integer类型的月份：yyyyMM
     */
    public static Integer formatMonthToInt(TemporalAccessor date) {
        return Integer.valueOf(formatDateTime(date, YYYYMM));
    }

    /**
     * 格式化为月份
     * @return String类型的月份：yyyyMM
     */
    public static String formatMonthToStr(Date date) {
        return formatDateTime(date, YYYYMM);
    }

    /**
     * 格式化为月份
     * @return String类型的月份：yyyyMM
     */
    public static String formatMonthToStr(TemporalAccessor date) {
        return formatDateTime(date, YYYYMM);
    }

    /**
     * 格式化为月份
     * @return String类型的月份：yyyy-MM
     */
    public static String formatMonth(Date date) {
        return formatDateTime(date, YYYY_MM);
    }

    /**
     * 格式化为月份
     * @return String类型的月份：yyyy-MM
     */
    public static String formatMonth(TemporalAccessor date) {
        return formatDateTime(date, YYYY_MM);
    }

    /**
     * 格式化为日期
     * @return String类型的日期：yyyy-MM-dd
     */
    public static String formatDate(Date date) {
        return formatDateTime(date, YYYY_MM_DD);
    }

    /**
     * 格式化为日期
     * @return String类型的日期：yyyy-MM-dd
     */
    public static String formatDate(TemporalAccessor date) {
        return formatDateTime(date, YYYY_MM_DD);
    }

    /**
     * 格式化为日期
     * @return Integer类型的日期：yyyyMMdd
     */
    public static Integer formatDateToInt(Date date) {
        return Integer.valueOf(formatDateTime(date, YYYYMMDD));
    }

    /**
     * 格式化为日期
     * @return Integer类型的日期：yyyyMMdd
     */
    public static Integer formatDateToInt(TemporalAccessor date) {
        return Integer.valueOf(formatDateTime(date, YYYYMMDD));
    }

    /**
     * 格式化为日期
     * @return String类型的日期：yyyyMMdd
     */
    public static String formatDateToStr(Date date) {
        return formatDateTime(date, YYYYMMDD);
    }

    /**
     * 格式化为日期
     * @return String类型的日期：yyyyMMdd
     */
    public static String formatDateToStr(TemporalAccessor date) {
        return formatDateTime(date, YYYYMMDD);
    }

    /**
     * 格式化为时间
     * @return String类型的时间：HH:mm:ss
     */
    public static String formatTime(Date date) {
        return formatDateTime(date, HH_MM_SS);
    }

    /**
     * 格式化为时间
     * @return String类型的时间：HH:mm:ss
     */
    public static String formatTime(TemporalAccessor date) {
        return formatDateTime(date, HH_MM_SS);
    }

    /**
     * 格式化为时间
     * @return Integer类型的时间：HHmmss
     */
    public static Integer formatTimeToInt(Date date) {
        return Integer.valueOf(formatDateTime(date, HHMMSS));
    }

    /**
     * 格式化为时间
     * @return Integer类型的时间：HHmmss
     */
    public static Integer formatTimeToInt(TemporalAccessor date) {
        return Integer.valueOf(formatDateTime(date, HHMMSS));
    }

    /**
     * 格式化为时间
     * @return String类型的时间：HHmmss
     */
    public static String formatTimeToStr(Date date) {
        return formatDateTime(date, HHMMSS);
    }

    /**
     * 格式化为时间
     * @return String类型的时间：HHmmss
     */
    public static String formatTimeToStr(TemporalAccessor date) {
        return formatDateTime(date, HHMMSS);
    }

    /**
     * 格式化为日期时间
     * @return String类型的日期时间：yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(Date date) {
        return formatDateTime(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 格式化为日期时间
     * @return String类型的日期时间：yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(TemporalAccessor date) {
        return formatDateTime(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 格式化为日期时间
     * @return String类型的日期时间：yyyyMMddHHmmss
     */
    public static String formatDateTimeToStr(Date date) {
        return formatDateTime(date, YYYYMMDDHHMMSS);
    }

    /**
     * 格式化为日期时间
     * @return String类型的日期时间：yyyyMMddHHmmss
     */
    public static String formatDateTimeToStr(TemporalAccessor date) {
        return formatDateTime(date, YYYYMMDDHHMMSS);
    }

    /**
     * 格式化为日期
     * @return String类型的日期时间：yyyy年MM月dd日
     */
    public static String formatDateToDialect(Date date) {
        return formatDateTime(date, YYYYNMMYDDR);
    }

    /**
     * 格式化为日期
     * @return String类型的日期时间：yyyy年MM月dd日
     */
    public static String formatDateToDialect(TemporalAccessor date) {
        return formatDateTime(date, YYYYNMMYDDR);
    }

    /**
     * 日期时间格式化
     * @param date 时间
     * @param pattern 正则
     */
    public static String formatDateTime(Date date, String pattern) {
        if (date == null) {
            throw new RuntimeException("date不可为空！");
        }
        return getDateTimeFormatter(pattern).format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    /**
     * 日期时间格式化（Instant不可格式化）
     * @param date 时间
     * @param pattern 正则
     */
    public static String formatDateTime(TemporalAccessor date, String pattern) {
        if (date == null) {
            throw new RuntimeException("date不可为空！");
        }
        return getDateTimeFormatter(pattern).format(date);
    }

    /**
     * 仿slf4j的Log打印，使用{}占位
     */
    public static String format(CharSequence template, Object... params) {
        return StrUtil.format(template, params);
    }

    /**
     * 限制字符串长度，如果超过指定长度，截取指定长度并在末尾加"..."
     * @param string 指定的字符串
     * @param length 指定的长度
     * @return 注意：拼接...后长度将超过指定的长度length
     */
    public static String maxLength(CharSequence string, int length) {
        return StrUtil.maxLength(string, length);
    }

    /**
     * 去掉指定后缀
     */
    public static String removeSuffix(CharSequence str, CharSequence suffix) {
        return StrUtil.removeSuffix(str, suffix);
    }

    /**
     * 移除字符串中所有给定字符串，当某个字符串出现多次，则全部移除
     */
    public static String removeAny(CharSequence str, CharSequence... strsToRemove) {
        return StrUtil.removeAny(str, strsToRemove);
    }

    /**
     * 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix) {
        return StrUtil.startWith(str, prefix);
    }

    /**
     * 截取字符串
     * @return 从位置0开始到指定长度的字符串，不会异常
     */
    public static String sub(CharSequence str, int length) {
        return StrUtil.sub(str, 0, length);
    }

    /**
     * 安全的截取指定位置的字符串，不会异常，位置为负数代表倒数
     */
    public static String sub(CharSequence str, int fromIndexInclude, int toIndexExclude) {
        return StrUtil.sub(str, fromIndexInclude, toIndexExclude);
    }

    /**
     * 按位置替换字符串
     */
    public static String replace(CharSequence str, int startInclude, int endExclude, CharSequence replacedStr) {
        return StrUtil.replaceByCodePoint(str, startInclude, endExclude, replacedStr);
    }

    /**
     * 替换字符串
     */
    public static String replaceChars(CharSequence str, String chars, CharSequence replacedStr) {
        return StrUtil.replace(str, chars, replacedStr);
    }

    /**
     * 替换字符数组
     */
    public static String replaceChars(CharSequence str, char[] chars, CharSequence replacedStr) {
        return StrUtil.replaceChars(str, chars, replacedStr);
    }

    /**
     * 转换为UTF-8字节数组
     */
    public static byte[] utf8Bytes(CharSequence str) {
        return StrUtil.utf8Bytes(str);
    }

    /**
     * 空白字符串的默认返回值
     * @param str 指定字符串
     * @param defaultStr 默认值
     * @return 如果指定字符串是空白或NULL的则返回默认值
     */
    public static String blankToDefault(CharSequence str, String defaultStr) {
        return StrUtil.blankToDefault(str, defaultStr);
    }

    /**
     * 解析成本地时间
     * @param text 时间文本 HH:mm:ss
     * @return null 解析错误异常
     */
    public static LocalTime parse2LocalTime(String text) {
        try {
            return LocalTime.parse(text, getDateTimeFormatter(HH_MM_SS));
        }
        catch (Exception e) {
            throw new RuntimeException("解析日期时间错误");
        }
    }

    /**
     * 解析成本地时间
     * @param text 时间文本
     * @param pattern 正则
     * @return null 解析错误异常
     */
    public static LocalTime parse2LocalTime(String text, String pattern) {
        try {
            return LocalTime.parse(text, getDateTimeFormatter(pattern));
        }
        catch (Exception e) {
            throw new RuntimeException("解析日期时间错误");
        }
    }

    /**
     * 解析成本地日期
     * @param text 时间文本 yyyy-MM-dd
     * @return 解析错误异常
     */
    public static LocalDate parse2LocalDate(String text) {
        try {
            return LocalDate.parse(text, getDateTimeFormatter(YYYY_MM_DD));
        }
        catch (Exception e) {
            throw new RuntimeException("解析日期时间错误");
        }
    }

    /**
     * 解析成本地日期
     * @param text 时间文本
     * @param pattern 正则
     * @return 解析错误异常
     */
    public static LocalDate parse2LocalDate(String text, String pattern) {
        try {
            if (YYYY_MM.equals(pattern)) {
                text = text + "-01";
                pattern = YYYY_MM_DD;
            }
            if (YYYYMM.equals(pattern)) {
                text = text + "01";
                pattern = YYYYMMDD;
            }
            return LocalDate.parse(text, getDateTimeFormatter(pattern));
        }
        catch (Exception e) {
            throw new RuntimeException("解析日期时间错误");
        }
    }

    /**
     * 解析成本地日期时间
     * @param text 时间文本
     * @param pattern 正则
     * @return 解析错误异常
     */
    public static LocalDateTime parse2LocalDateTime(String text, String pattern) {
        try {
            return LocalDateTime.parse(text, getDateTimeFormatter(pattern));
        }
        catch (Exception e) {
            throw new RuntimeException("解析日期时间错误");
        }
    }

    /**
     * 解析成本地日期时间
     * @param text 时间文本 yyyy-MM-dd HH:mm:ss
     * @return 解析错误异常
     */
    public static LocalDateTime parse2LocalDateTime(String text) {
        try {
            return LocalDateTime.parse(text, getDateTimeFormatter(YYYY_MM_DD_HH_MM_SS));
        }
        catch (Exception e) {
            throw new RuntimeException("解析日期时间错误");
        }
    }

    /**
     * 获取日期时间格式器
     * @param pattern 正则
     */
    public static DateTimeFormatter getDateTimeFormatter(String pattern) {
        DateTimeFormatter dtf = DTF_MAP.get(pattern);
        if (dtf == null) {
            dtf = DateTimeFormatter.ofPattern(pattern);
            DTF_MAP.putIfAbsent(pattern, dtf);
        }
        return dtf;
    }

    /**
     * 转为Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        return BeanUtil.beanToMap(bean);
    }

    /**
     * 浅拷贝-列表
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> tClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream().map(x -> copyProperties(x, tClass)).collect(Collectors.toList());
    }

    /**
     * 浅拷贝
     */
    public static <T> T copyProperties(Object source, Class<T> tClass, String... ignoreProperties) {
        return BeanUtil.copyProperties(source, tClass, ignoreProperties);
    }

    /**
     * 浅拷贝-拷贝至目标对象-忽略同名不同属性的错误，忽略null值的拷贝不做覆盖
     */
    public static void copyPropertiesIgnoreErrorAndNull(Object source, Object target) {
        BeanUtil.copyProperties(source, target, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
    }

    /**
     * 浅拷贝-拷贝至目标对象
     */
    public static void copyProperties(Object source, Object target, CopyOptions copyOptions) {
        BeanUtil.copyProperties(source, target, copyOptions);
    }

    /**
     * 深拷贝 如果对象实现Cloneable接口，调用其clone方法 如果实现Serializable接口，执行深度克隆 否则返回null
     */
    public static <T> T clone(T obj) {
        return ObjectUtil.clone(obj);
    }

    /**
     * 深拷贝 序列化后拷贝流的方式克隆 对象必须实现Serializable接口
     */
    public static <T> T cloneByStream(T obj) {
        return ObjectUtil.cloneByStream(obj);
    }

    /**
     * 地址脱敏
     * @param sensitiveSize 敏感信息长度，比如以下是4
     * @return 北京市海淀区****
     */
    public static String dstAddress(String address, int sensitiveSize) {
        return DesensitizedUtil.address(address, sensitiveSize);
    }

    /**
     * 银行卡号脱敏 由于银行卡号长度不定，所以只展示前4位，后面的位数根据卡号决定展示1-4位
     */
    public static String dstBankCard(String bankCardNo) {
        return DesensitizedUtil.bankCard(bankCardNo);
    }

    /**
     * 中国车牌脱敏
     */
    public static String dstCarLicense(String carLicense) {
        return DesensitizedUtil.carLicense(carLicense);
    }

    /**
     * 中文姓名脱敏 只显示第一个汉字，其他隐藏为2个星号，比如：李**
     */
    public static String dstChineseName(String fullName) {
        return DesensitizedUtil.chineseName(fullName);
    }

    /**
     * 电子邮箱脱敏 邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示，比如：d**@126.com
     */
    public static String dstEmail(String email) {
        return DesensitizedUtil.email(email);
    }

    /**
     * 固定电话脱敏 固定电话 前四位，后两位脱敏
     */
    public static String dstFixedPhone(String num) {
        return DesensitizedUtil.fixedPhone(num);
    }

    /**
     * 手机号脱敏 前三位，后4位，其他隐藏，比如135****2210
     */
    public static String dstMobilePhone(String num) {
        return DesensitizedUtil.mobilePhone(num);
    }

    /**
     * 身份证号脱敏 前三位，后4位，其他隐藏，比如532***********3716
     */
    public static String dstIdCardNum(String idCardNum) {
        return DesensitizedUtil.idCardNum(idCardNum, 3, 4);
    }

    /**
     * 密码脱敏 密码的全部字符都用*代替，比如：******
     */
    public static String dstPassword(String password) {
        return DesensitizedUtil.password(password);
    }

    /**
     * URL编码
     */
    public static String urlEncode(CharSequence source) {
        return new PercentCodec().encode(source, StandardCharsets.UTF_8);
    }

    /**
     * BASE64编码
     */
    public static String base64Encode(CharSequence source) {
        return Base64.encode(source);
    }

    /**
     * BASE64解码
     */
    public static String base64Decode(CharSequence source) {
        return Base64.decodeStr(source);
    }

    /**
     * BASE64编码
     */
    public static String base64Encode(CharSequence source, Charset charset) {
        return Base64.encode(source, charset);
    }

    /**
     * BASE64解码
     */
    public static String base64Decode(CharSequence source, Charset charset) {
        return Base64.decodeStr(source, charset);
    }

    /**
     * DES解码
     * @param secretText 密文
     * @param key 密钥
     * @return 原文
     */
    public static String desDecode(String secretText, String key) {
        return SecureUtil.des(key.getBytes()).decryptStr(Base64.decode(secretText));
    }

    /**
     * DES编码
     * @param planText 原文
     * @param key 密钥
     * @return 密文
     */
    public static String desEncode(String planText, String key) {
        return SecureUtil.des(key.getBytes()).encryptBase64(planText);
    }

    /**
     * md5
     * @param planText 原文
     * @return 密文
     */
    public static String md5(String planText) {
        return SecureUtil.md5(planText);
    }

    /**
     * 生成RSA密钥
     * @param keySize 密钥长度 1024/2048
     */
    public static ObjectNode createRsaKeys(int keySize) {
        KeyPair pair = SecureUtil.generateKeyPair("RSA", keySize);
        ObjectNode keyPairMap = new ObjectMapper().createObjectNode();
        keyPairMap.put("publicKey", Base64.encode(pair.getPublic().getEncoded()));
        keyPairMap.put("privateKey", Base64.encode(pair.getPrivate().getEncoded()));
        return keyPairMap;
    }

    /**
     * 使用公钥加密
     * @param plainText 原文
     * @param publicKey 公钥
     * @return 密文
     */
    public static String rsaEncrypt(String plainText, String publicKey) {
        return SecureUtil.rsa(null, publicKey).encryptBase64(plainText, KeyType.PublicKey);
    }

    /**
     * 使用私钥解密
     * @param data 密文
     * @param privateKey 私钥
     * @return 原文
     */
    public static String rsaDecrypt(String data, String privateKey) {
        return SecureUtil.rsa(privateKey, null).decryptStr(data, KeyType.PrivateKey);
    }

    /**
     * 获取客户端IP
     * @param request 请求
     */
    public static String getIpAddr(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            InetSocketAddress inetSocketAddress = request.getRemoteAddress();
            if (inetSocketAddress == null) {
                return "";
            }
            ip = inetSocketAddress.getAddress().toString();
            ip = ip.substring(ip.indexOf("/") + 1);

            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                return LOCAL_ADDR;
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip.length() > 15) {
            int i;
            if ((i = ip.indexOf(",")) > 0) {
                ip = ip.substring(0, i);
            }
        }
        return ip;
    }

    /**
     * 获取客户端ip
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-Ip");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-Ip");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("entrust-client-ip");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(StrUtil.trim(ip))) {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getLocalHost();
                    ip = inetAddress.getHostAddress();
                }
                catch (UnknownHostException ignored) {
                }
            }
        }
        // 多级方向代理
        if (ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }

    /**
     * 判断是否是ajax请求
     * @param request 请求
     */
    public static boolean isAjax(HttpServletRequest request) {
        String requestHeader = request.getHeader("X-Requested-With");
        return (requestHeader != null && requestHeader.equals("XMLHttpRequest"));
    }

    /**
     * 获取指定长度的随机字符串，范围0-9
     * @param n 长度
     */
    public static String randomNumbers(int n) {
        return RandomUtil.randomNumbers(n);
    }

    /**
     * 获取指定长度的随机字符串，范围0-9/a-z/A-Z
     * @param n 长度
     */
    public static String randomString(int n) {
        return RandomUtil.randomString(n);
    }

    /**
     * 随机生成一个范围内的数字
     * @param minInclude 包含
     * @param maxExclude 不包含
     */
    public static int randomInt(int minInclude, int maxExclude) {
        return RandomUtil.randomInt(minInclude, maxExclude);
    }

    /**
     * 抽奖
     * @param drawList 抽取的概率列表
     * @return int 抽取到的奖品索引
     */
    public static int randomDraw(List<Double> drawList) {
        // 计算概率最小值
        double min = drawList.get(0);
        for (Double probability : drawList) {
            if (probability <= 0 || probability > 1) {
                throw new IllegalArgumentException("drawList内的值必须大于0且小于1");
            }
            if (probability < min) {
                min = probability;
            }
        }
        DecimalFormat dFormat = new DecimalFormat();
        dFormat.setMaximumFractionDigits(11);
        String minStr = dFormat.format(min);
        int nt = minStr.length() - minStr.indexOf(".") - 1;
        // 生成范围值数组
        double m = Math.pow(10, nt);
        int[] scope = new int[drawList.size()];
        int s = 0;
        for (Double probability : drawList) {
            int t = (int) (multiply(m, probability));
            if (s == 0) {
                scope[s] = t;
            }
            else {
                scope[s] = scope[s - 1] + t;
            }
            s++;
        }
        // 随机抽取
        Random random = ThreadLocalRandom.current();
        int drawInt = random.nextInt(scope[scope.length - 1]);
        for (int i = 0; i < scope.length; i++) {
            int start;
            int end = scope[i];
            if (i == 0) {
                start = 0;
            }
            else {
                start = scope[i - 1];
            }
            if (start <= drawInt && drawInt < end) {
                drawInt = i;
                break;
            }
        }
        return drawInt;
    }

    /**
     * 判断是否是移动端
     * @param request 请求
     */
    public static boolean isMobile(HttpServletRequest request) {
        boolean flag = false;
        String ua = request.getHeader("User-Agent");
        // 排除 苹果桌面系统
        if (!ua.contains("Windows NT") && !ua.contains("Macintosh")) {
            for (String item : USER_AGENT_ARR) {
                if (ua.contains(item)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * 精确的加法运算
     * @param v1 被加数
     * @param v2 加数
     * @return (v1 + v2)
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.add(b2).doubleValue();
    }

    /**
     * 精确的减法运算
     * @param v1 被减数
     * @param v2 减数
     * @return (v1 - v2)
     */
    public static double subtract(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 精确的乘法运算
     * @param v1 被乘数
     * @param v2 乘数
     * @return (v1 * v2)
     */
    public static double multiply(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 精确的除法运算
     * @param v1 被除数
     * @param v2 除数
     * @param scale 当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入(可设为10提高精度)
     * @return (v1 / v2)
     */
    public static double divide(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("精度不可小于0");
        }
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 精确的小数位四舍五入
     * @param v 需要四舍五入的数字
     * @param scale 精度
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("精度不可小于0");
        }
        BigDecimal b = BigDecimal.valueOf(v);
        BigDecimal one = BigDecimal.valueOf(1);
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }

}
