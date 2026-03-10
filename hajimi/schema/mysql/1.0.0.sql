-- 短信发送记录
CREATE TABLE IF NOT EXISTS `mashup_sms_log`
(
    `id`         BIGINT UNSIGNED                            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `created_at` BIGINT UNSIGNED                            NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)                                NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED                            NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)                                NULL COMMENT 'Last updated by who',
    `deleted`    BIT                                        NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)                                NULL COMMENT 'the id of the tenant to which this setting belongs',
    `tid`        VARCHAR(64)                                NOT NULL COMMENT 'Template ID',
    `phone`      VARCHAR(11)                                NOT NULL COMMENT 'phone number',
    `vendor`     VARCHAR(16)                                NOT NULL COMMENT 'Sms Vendor Id',
    `status`     enum ('PENDING','SENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING' COMMENT 'Status of sending',
    `sent_time`  BIGINT UNSIGNED                            NULL COMMENT 'SENT TIME',
    `retried`    INTEGER UNSIGNED                           NOT NULL DEFAULT 0 COMMENT 'retry counts',
    `intervals`  SMALLINT UNSIGNED                          NOT NULL COMMENT 'Sending intervals',
    `content`    VARCHAR(256)                               NULL COMMENT 'Sms Text',
    `params`     LONGTEXT                                   NULL COMMENT 'Variables used by sms template',
    `error`      TEXT                                       NULL COMMENT 'Error message',
    INDEX idx_tid (`tid`),
    INDEX idx_phone (`phone`),
    INDEX idx_vendor (`vendor`),
    INDEX idx_sent_time (`sent_time` ASC),
    INDEX idx_status (`status`)
) COMMENT 'Sms Logs';

-- 审计日志
CREATE TABLE IF NOT EXISTS `mashup_audit_log`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)     NULL COMMENT 'the id of the tenant to which this setting belongs',
    `user_id`    VARCHAR(32)     NOT NULL COMMENT 'User Id',
    `target`     VARCHAR(32)     NOT NULL COMMENT 'Target',
    `log_time`   BIGINT UNSIGNED NOT NULL COMMENT 'log timestamp',
    `template`   BIT             NOT NULL DEFAULT false COMMENT 'the message is template',
    `activity`   varchar(64)     NOT NULL COMMENT 'activity',
    `runas`      varchar(32)     null comment 'the userid of the user who performed this activity',
    `level`      varchar(12)     NULL     DEFAULT 'info' COMMENT 'log level',
    `ip`         varchar(256)    NULL     DEFAULT NULL COMMENT 'ip',
    `device`     varchar(64)     null comment 'the device on which the activity performed',
    `message`    TEXT            NULL     DEFAULT NULL COMMENT 'message',
    `args`       LONGTEXT        NULL     DEFAULT NULL COMMENT 'args of message',
    `old_value`  LONGTEXT        NULL     DEFAULT NULL COMMENT 'old json value',
    `new_value`  LONGTEXT        NULL     DEFAULT NULL COMMENT 'new value',
    index idx_log_time (log_time asc),
    index idx_user_id (user_id, log_time asc),
    index idx_tenant_id (tenant_id),
    index idx_activity_user_id (activity, user_id),
    index idx_activity_log_time (activity, log_time),
    index idx_runas (runas, activity)
) COMMENT 'Audit Logs';

-- 配置服务
CREATE TABLE IF NOT EXISTS `mashup_setting`
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at  BIGINT UNSIGNED NULL     DEFAULT NULL,
    created_by  VARCHAR(32),
    updated_at  BIGINT UNSIGNED NULL     DEFAULT NULL,
    updated_by  VARCHAR(32),
    deleted     BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    tenant_id   VARCHAR(32) COMMENT 'the id of the tenant to which this setting belongs',
    version     smallint        NOT NULL COMMENT 'version no.',
    setting_key CHAR(32)        NOT NULL COMMENT 'The MD5 value of Setting Key',
    setting_cls VARCHAR(512)    NOT NULL COMMENT 'Setting class name',
    setting     LONGTEXT COMMENT 'Base64 encoded the JSON value of a Setting',
    UNIQUE INDEX `udx_setting_key` (setting_key)
) COMMENT 'Settings';

CREATE TABLE IF NOT EXISTS `mashup_setting_revision`
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at  BIGINT UNSIGNED NULL     DEFAULT NULL,
    created_by  VARCHAR(32),
    updated_at  BIGINT UNSIGNED NULL     DEFAULT NULL,
    updated_by  VARCHAR(32),
    deleted     BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    tenant_id   VARCHAR(32) COMMENT 'the id of the tenant to which this setting belongs',
    revision    INT UNSIGNED    NOT NULL COMMENT 'Revision Number',
    setting_key CHAR(32)        NOT NULL COMMENT 'The MD5 value of Setting Key',
    setting_cls VARCHAR(512)    NOT NULL COMMENT 'Setting class name',
    setting     LONGTEXT COMMENT 'Base64 encoded the JSON value of a Setting',
    UNIQUE INDEX `udx_setting_key` (setting_key, revision)
) COMMENT 'Setting Revisions';

-- 监督者任务队列
CREATE TABLE IF NOT EXISTS `mashup_supervisor_task`
(
    id          BIGINT UNSIGNED                          NOT NULL PRIMARY KEY,
    create_time datetime                                 NULL     DEFAULT NULL,
    update_time datetime                                 NULL     DEFAULT NULL,
    run_at      BIGINT UNSIGNED                          NOT NULL DEFAULT 0,
    tenant_id   VARCHAR(32)                              NULL     DEFAULT '0' COMMENT 'Tenant ID',
    uid         VARCHAR(32)                              NULL     DEFAULT '0' COMMENT 'User ID',
    sharding    INT                                      NOT NULL DEFAULT 0 COMMENT '分片',
    name        VARCHAR(128)                             NOT NULL COMMENT '任务名称',
    supervisor  VARCHAR(128)                             NOT NULL COMMENT '监督者',
    status      ENUM ('PENDING','RUNNING','DONE','FAIL') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    content     Longtext                                 NOT NULL COMMENT '任务内容',
    retries     INT UNSIGNED                             NOT NULL DEFAULT 0 COMMENT '重试次数',
    remark      text                                     NULL COMMENT '说明',
    INDEX IDX_STATUS (run_at asc, status)
) ENGINE = InnoDB COMMENT '监督任务列表';
