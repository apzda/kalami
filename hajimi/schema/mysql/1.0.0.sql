-- 短信发送记录
CREATE TABLE IF NOT EXISTS `sys_sms_log`
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
CREATE TABLE IF NOT EXISTS `sys_audit_log`
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
CREATE TABLE IF NOT EXISTS `sys_setting`
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

CREATE TABLE IF NOT EXISTS `sys_setting_revision`
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

-- OPEN API 客户端
CREATE TABLE sys_api_client
(
    id             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at     BIGINT UNSIGNED  NULL     DEFAULT NULL,
    created_by     VARCHAR(32),
    updated_at     BIGINT UNSIGNED  NULL     DEFAULT NULL,
    updated_by     VARCHAR(32),
    deleted        BIT              NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    client_id      VARCHAR(32)      NOT NULL COMMENT '客户端ID',
    client_name    VARCHAR(128)     NOT NULL COMMENT '客户名称',
    client_secret  VARCHAR(32)      NOT NULL COMMENT '客户端密钥',
    client_pri_key TEXT             NOT NULL COMMENT '客户端私钥',
    client_pub_key TEXT             NOT NULL COMMENT '客户端公钥',
    disabled       TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否禁用: 0 - 启用; 1 - 禁用',
    remark         VARCHAR(500)     NULL COMMENT '备注',
    CONSTRAINT UDX_CLIENT_ID UNIQUE (client_id)
) ENGINE = InnoDB COMMENT '开放平台应用客户端';

create table sys_api_client_perm
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at BIGINT UNSIGNED NULL     DEFAULT NULL,
    created_by VARCHAR(32),
    updated_at BIGINT UNSIGNED NULL     DEFAULT NULL,
    updated_by VARCHAR(32),
    deleted    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    client_id  VARCHAR(32)     NOT NULL COMMENT '客户端ID',
    perm       JSON            NULL COMMENT '授权',
    CONSTRAINT UDX_CLIENT_ID UNIQUE (client_id)
) ENGINE = InnoDB COMMENT '开放平台应用客户端授权';

