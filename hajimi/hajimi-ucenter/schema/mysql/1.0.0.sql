CREATE TABLE IF NOT EXISTS `ucenter_user`
(
    `id`                BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'User Identifier',
    `created_at`        BIGINT UNSIGNED NULL                              DEFAULT NULL,
    `created_by`        VARCHAR(32)     NULL                              DEFAULT NULL COMMENT 'Create User Id',
    `updated_at`        BIGINT UNSIGNED NULL                              DEFAULT NULL COMMENT 'Last modified time',
    `updated_by`        VARCHAR(32)     NULL                              DEFAULT NULL COMMENT 'Last modified by who',
    `deleted`           BIT             NOT NULL                          DEFAULT FALSE COMMENT 'Soft deleted Flag',
    `username`          VARCHAR(32)     NOT NULL COMMENT 'User Name',
    `nickname`          VARCHAR(64)     NULL                              DEFAULT NULL COMMENT 'nickname',
    `passwd`            VARCHAR(512)    NOT NULL                          DEFAULT '' COMMENT 'password',
    `avatar`            VARCHAR(1024)   NULL                              DEFAULT NULL COMMENT 'avatar',
    `phone`             VARCHAR(24)     NULL                              DEFAULT NULL COMMENT 'Phone Number',
    `email`             VARCHAR(128)    NULL                              DEFAULT NULL COMMENT 'Email',
    `landing`           VARCHAR(128)    NULL                              DEFAULT NULL COMMENT 'Landing Page',
    `status`            ENUM ('PENDING','ACTIVATED', 'LOCKED','DISABLED') DEFAULT 'PENDING' COMMENT 'status of user',
    `expired_at`        BIGINT UNSIGNED NULL                              DEFAULT NULL COMMENT 'account expired time',
    `passwd_expired_at` BIGINT UNSIGNED NULL                              DEFAULT NULL COMMENT 'password expired time',
    `remark`            VARCHAR(255)    NULL                              DEFAULT NULL COMMENT 'remark',
    UNIQUE KEY `UDX_USERNAME` (`username`),
    INDEX `IDX_CREATE_AT` (created_at ASC)
) COMMENT ='Users';

CREATE TABLE IF NOT EXISTS `ucenter_user_meta`
(
    `id`         BIGINT UNSIGNED NOT NULL COMMENT 'id',
    `created_at` BIGINT UNSIGNED NULL       DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL       DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL   DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)     NOT NULL   DEFAULT '0' COMMENT 'tenant id',
    `type`       ENUM ('S','I','L','D','F') DEFAULT 'S' COMMENT 'value type: S-string;I-int;L-long;D-double;F-float',
    `uid`        BIGINT UNSIGNED NOT NULL COMMENT 'user id',
    `name`       VARCHAR(32)     NOT NULL COMMENT 'meta name',
    `value`      LONGTEXT                   DEFAULT null COMMENT 'value',
    `remark`     TEXT                       DEFAULT null COMMENT 'remark',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UDX_USER_META` (`uid`, `name`)
) COMMENT ='用户元数据表';

CREATE TABLE IF NOT EXISTS `ucenter_oauth`
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `uid`        BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'User ID',
    `provider`   VARCHAR(24)     NOT NULL COMMENT 'OpenID Provider',
    `app_id`     VARCHAR(32)     NOT NULL COMMENT 'appId',
    `open_id`    VARCHAR(256)    NOT NULL COMMENT 'OpenID',
    `union_id`   VARCHAR(256)    NOT NULL COMMENT 'UnionID',
    `login_time` BIGINT UNSIGNED NULL     DEFAULT NULL COMMENT 'first login time',
    `rec_code`   VARCHAR(24)     NULL     DEFAULT NULL COMMENT 'Recommend Code',
    `device`     VARCHAR(24)     NULL     DEFAULT NULL COMMENT 'first login device',
    `ip`         VARCHAR(256)    NULL     DEFAULT NULL COMMENT 'the ip address(v4 or v6) from which the user login',
    `user_agent` VARCHAR(512)    NULL     DEFAULT NULL COMMENT 'user agent',
    `spm`        VARCHAR(256)    NULL     DEFAULT NULL COMMENT 'spm',
    `remark`     VARCHAR(255)    NULL     DEFAULT NULL COMMENT 'remark',
    UNIQUE KEY `UDX_TYPE_ID` (`provider`, `app_id`, `open_id`),
    INDEX `IDX_UNION_ID` (`union_id`),
    INDEX `FK_USER_ID` (`uid` asc)
) COMMENT ='Oauth2.0 grants';

CREATE TABLE IF NOT EXISTS `ucenter_oauth_meta`
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `oauth_id`   BIGINT UNSIGNED NOT NULL,
    `name`       VARCHAR(32)     NOT NULL COMMENT 'name',
    `value`      TEXT COMMENT 'value',
    `remark`     VARCHAR(255)    NULL     DEFAULT NULL COMMENT 'remark',
    UNIQUE KEY `UDX_ID_NAME` (`oauth_id`, `name`)
) COMMENT ='oauth meta';

CREATE TABLE IF NOT EXISTS `ucenter_oauth_session`
(
    `id`            BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `created_at`    BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by`    VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at`    BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by`    VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`       BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `uid`           BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'user id',
    `oauth_id`      BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'oauth id',
    `provider`      VARCHAR(24)     NOT NULL COMMENT 'OpenID Provider',
    `app_id`        VARCHAR(32)     NOT NULL COMMENT 'appId',
    `grant_code`    VARCHAR(256)    NULL     DEFAULT NULL COMMENT 'grant code',
    `access_token`  VARCHAR(256)    NULL     DEFAULT NULL COMMENT 'access token',
    `refresh_token` VARCHAR(1024)   NULL     DEFAULT NULL COMMENT 'refresh token',
    `login_time`    BIGINT UNSIGNED NOT NULL COMMENT 'first login time',
    `expired_at`    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'expire time',
    `open_id`       VARCHAR(256)    NOT NULL COMMENT 'OpenID',
    `union_id`      VARCHAR(256)    NOT NULL COMMENT 'UnionID',
    `simulator`     BIT             NOT NULL DEFAULT FALSE COMMENT 'if the device is a simulator',
    `device`        VARCHAR(24)     NOT NULL COMMENT 'the device from which the user login',
    `ip`            VARCHAR(256)    NOT NULL COMMENT 'the ip from which the user login',
    `user_agent`    VARCHAR(512)    NULL     DEFAULT NULL COMMENT 'user agent',
    `spm`           VARCHAR(256)    NULL     DEFAULT NULL COMMENT 'spm',
    `extra`         LONGTEXT        NULL     DEFAULT NULL COMMENT 'extra data(prefer json format)',
    INDEX (`oauth_id`),
    INDEX `IDX_CTIME` (`created_at`),
    INDEX `IDX_EXPIRE` (`expired_at`),
    INDEX `IDX_UID` (`uid`),
    INDEX `IDX_TYPE_ID` (`provider`, `app_id`, `open_id`)
) COMMENT ='oauth login sessions';

CREATE TABLE IF NOT EXISTS `ucenter_user_mfa`
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `enabled`    BIT             NOT NULL DEFAULT FALSE NULL COMMENT 'enabled or not',
    `uid`        BIGINT UNSIGNED NOT NULL COMMENT 'user id',
    `auth_type`  varchar(128)    NOT NULL COMMENT 'auth type',
    `phone`      varchar(64)              DEFAULT NULL COMMENT 'phone number',
    `email`      varchar(256)             DEFAULT NULL COMMENT 'email address',
    `secret_key` varchar(256)             DEFAULT NULL COMMENT 'secret key of mfa',
    UNIQUE KEY `uniq_uid_auth_type` (`uid`, `auth_type`)
) comment = 'User multiple factor authenticate configuration';

CREATE TABLE IF NOT EXISTS `ucenter_role`
(
    `id`          BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'id',
    `created_at`  BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by`  VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at`  BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by`  VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`     BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`   VARCHAR(32)     NOT NULL DEFAULT '0' COMMENT 'tenant id',
    `role`        VARCHAR(32)     NOT NULL COMMENT 'role',
    `name`        VARCHAR(128)    NOT NULL COMMENT 'role name',
    `landing`     VARCHAR(128)    NULL     DEFAULT NULL COMMENT 'Landing Page',
    `builtin`     BIT             NOT NULL DEFAULT FALSE COMMENT 'builtin role, cannot be deleted',
    `leasable`    BIT             NOT NULL DEFAULT FALSE COMMENT 'true for tenant',
    `provider`    VARCHAR(24)     NOT NULL COMMENT 'provider(db, ldap or ad)',
    `description` TEXT            NULL COMMENT 'Description',
    UNIQUE KEY UDX_ROLE (`tenant_id`, `role`)
) COMMENT = 'roles';

CREATE TABLE IF NOT EXISTS `ucenter_role_children`
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'id',
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)     NOT NULL COMMENT 'tenant id',
    `role_id`    BIGINT UNSIGNED NOT NULL COMMENT 'Role Id',
    `child_id`   BIGINT UNSIGNED NOT NULL COMMENT 'Child Role Id',
    INDEX IDX_ROLE (`role_id`),
    INDEX IDX_CHILD (`child_id`)
) COMMENT = 'role children';

CREATE TABLE IF NOT EXISTS `ucenter_user_role`
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'id',
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)     NOT NULL DEFAULT '0' COMMENT 'Tenant id',
    `org_id`     VARCHAR(32)     NULL     DEFAULT null COMMENT 'Organization id',
    `uid`        BIGINT UNSIGNED NOT NULL COMMENT 'user id',
    `role_id`    BIGINT UNSIGNED NOT NULL COMMENT 'role id',
    INDEX IDX_TENANT_UID (`tenant_id`, `uid`),
    INDEX IDX_TENANT_ROLE (`tenant_id`, `role_id`)
) COMMENT = 'user roles';

CREATE TABLE IF NOT EXISTS `ucenter_rbac_privilege`
(
    `id`          BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'id',
    `created_at`  BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by`  VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at`  BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by`  VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`     BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`   VARCHAR(32)     NOT NULL DEFAULT '0' COMMENT 'tenant id',
    `name`        VARCHAR(256)    NOT NULL COMMENT 'the name of this privilege',
    `type`        VARCHAR(24)     NOT NULL COMMENT 'the type of this privilege',
    `builtin`     BIT             NOT NULL DEFAULT FALSE COMMENT 'builtin privilege, cannot be deleted',
    `service`     VARCHAR(32)     NULL     DEFAULT NULL COMMENT 'the service id',
    `permission`  VARCHAR(64)     NOT NULL COMMENT 'the permission of this privilege',
    `extra`       TEXT            NULL COMMENT 'extra data of this privilege',
    `description` TEXT            NULL COMMENT 'the description',
    `remark`      TEXT            NULL COMMENT 'the remark',
    INDEX IDX_TENANT_ID (`tenant_id`),
    UNIQUE UDX_P_T (`permission`, `tenant_id`)
) COMMENT = 'privileges';

CREATE TABLE IF NOT EXISTS `ucenter_role_privilege`
(
    `id`           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'id',
    `created_at`   BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by`   VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at`   BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by`   VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`      BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`    VARCHAR(32)     NOT NULL COMMENT 'tenant id',
    `role_id`      BIGINT UNSIGNED NOT NULL COMMENT 'ROLE ID',
    `privilege_id` BIGINT UNSIGNED NOT NULL COMMENT 'privilege id',
    INDEX IDX_PRIVILEGE_ID (`privilege_id`),
    INDEX IDX_TENANT_ID (`tenant_id`, `role_id`)
) COMMENT = 'role privileges';

CREATE TABLE IF NOT EXISTS `ucenter_rbac_resource`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `created_at`  BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by`  VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at`  BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by`  VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`     BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `pid`         BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the parent id of this resource',
    `service`     VARCHAR(32)     NULL     DEFAULT NULL COMMENT 'the service id',
    `rid`         VARCHAR(32)     NOT NULL COMMENT 'the resource id',
    `name`        VARCHAR(64)     NOT NULL COMMENT 'the resource name',
    `actions`     VARCHAR(512)    NOT NULL DEFAULT 'c,r,u,d' COMMENT 'actions can be performed on resource',
    `builtin`     BIT             NOT NULL DEFAULT FALSE COMMENT 'builtin resource, cannot be deleted',
    `explorer`    VARCHAR(256)             DEFAULT NULL COMMENT 'a FQDN class name for exploring the ids of this resource ',
    `description` VARCHAR(1024)            DEFAULT NULL COMMENT 'the description of this resource',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UDX_NAME` (`pid`, `rid`)
) COMMENT ='Security Resource';

CREATE TABLE ucenter_tenant
(
    `id`         BIGINT UNSIGNED  NOT NULL PRIMARY KEY,
    `created_at` BIGINT UNSIGNED  NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)      NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED  NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)      NULL COMMENT 'Last updated by who',
    `deleted`    BIT              NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `parent_id`  BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '上级租户ID',
    `agent_id`   BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '代理ID',
    `code`       VARCHAR(12)      NOT NULL COMMENT '租户代码',
    `name`       VARCHAR(128)     NOT NULL COMMENT '租户名称',
    `short_name` VARCHAR(24)      NULL     DEFAULT NULL COMMENT '租户简称',
    `phone`      VARCHAR(24)      NOT NULL COMMENT '联系电话',
    `contact`    VARCHAR(64)      NULL COMMENT '联系人姓名',
    `address`    VARCHAR(256)     NULL COMMENT '联系地址',
    `logo`       VARCHAR(1024)    NULL     DEFAULT NULL COMMENT '租户头像',
    `uid`        BIGINT UNSIGNED  NULL     DEFAULT NULL COMMENT '租户超级管理员ID',
    `status`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '租户状态: 0 - 正常，1 - 冻结',
    `remark`     VARCHAR(256)     NULL     DEFAULT NULL COMMENT '备注',
    UNIQUE KEY `UDX_CODE` (`code`),
    UNIQUE KEY `UDX_PHONE` (`phone`),
    INDEX IDX_UID (`uid`)
) COMMENT '租户';

CREATE TABLE IF NOT EXISTS `ucenter_tenant_user`
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)     NOT NULL COMMENT 'tenant id',
    `uid`        BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `current`    BIT             NULL     DEFAULT FALSE COMMENT '是否是用户默认选择的租户',
    INDEX IDX_UT_ID (`uid`),
    UNIQUE UDX_TU_ID (`tenant_id`, `uid`)
) COMMENT ='tenant user';

CREATE TABLE IF NOT EXISTS `ucenter_tenant_subscription`
(
    `id`           BIGINT UNSIGNED  NOT NULL PRIMARY KEY,
    `created_at`   BIGINT UNSIGNED  NULL     DEFAULT NULL,
    `created_by`   VARCHAR(32)      NULL COMMENT 'Create User Id',
    `updated_at`   BIGINT UNSIGNED  NULL     DEFAULT NULL,
    `updated_by`   VARCHAR(32)      NULL COMMENT 'Last updated by who',
    `deleted`      BIT              NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`    VARCHAR(32)      NOT NULL COMMENT 'tenant id',
    `service`      VARCHAR(32)      NULL     DEFAULT NULL COMMENT 'the service id',
    `expired_time` DATETIME         NULL     DEFAULT NULL COMMENT 'expired time',
    `expired`      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'expired',
    UNIQUE UDX_TU_SERVICE (`tenant_id`, `service`, `expired_time`),
    INDEX IDX_EXPIRED (`expired`, `tenant_id`),
    INDEX IDX_SERVICE (`service`, `expired`)
) COMMENT ='租户订购的服务';

CREATE TABLE ucenter_organization
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)     NOT NULL COMMENT 'Tenant id',
    `parent_id`  BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级组织ID',
    `code`       VARCHAR(12)     NOT NULL COMMENT '组织部门代码',
    `name`       VARCHAR(128)    NOT NULL COMMENT '组织部门名称',
    `short_name` VARCHAR(24)     NULL     DEFAULT NULL COMMENT '组织部门简称',
    `logo`       VARCHAR(1024)   NULL     DEFAULT NULL COMMENT '组织部门头像',
    `remark`     VARCHAR(256)    NULL     DEFAULT NULL COMMENT '备注',
    UNIQUE KEY `UDX_CODE` (`tenant_id`, `code`),
    INDEX `IDX_PID` (`parent_id`)
) COMMENT '组织';

CREATE TABLE ucenter_organization_user
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `created_by` VARCHAR(32)     NULL COMMENT 'Create User Id',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL,
    `updated_by` VARCHAR(32)     NULL COMMENT 'Last updated by who',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT 'Soft Deleted Flag',
    `tenant_id`  VARCHAR(32)     NOT NULL COMMENT 'tenant id',
    `org_id`     VARCHAR(32)     NOT NULL COMMENT 'organization id',
    `uid`        BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
    `current`    BIT             NULL     DEFAULT FALSE COMMENT '是否是用户默认选择的租户',
    INDEX IDX_UT_ID (`uid`),
    UNIQUE UDX_TU_ID (`tenant_id`, `org_id`, `uid`)
) COMMENT '组织用户';

CREATE TABLE `ucenter_agent`
(
    `id`         BIGINT UNSIGNED  NOT NULL PRIMARY KEY COMMENT 'ID',
    `created_at` BIGINT UNSIGNED  NULL     DEFAULT NULL COMMENT '创建时间',
    `created_by` VARCHAR(32)      NULL     DEFAULT NULL COMMENT '创建人ID',
    `updated_at` BIGINT UNSIGNED  NULL     DEFAULT NULL COMMENT '最后更新时间',
    `updated_by` VARCHAR(32)      NULL     DEFAULT NULL COMMENT '最后更新人ID',
    `deleted`    BIT              NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `tenant_id`  VARCHAR(32)      NOT NULL COMMENT '租户ID',
    `parent_id`  BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '上级代理商ID',
    `uid`        BIGINT UNSIGNED  NULL     DEFAULT NULL COMMENT '系统用户ID',
    `name`       VARCHAR(128)     NOT NULL COMMENT '代理商名称',
    `short_name` VARCHAR(24)      NULL     DEFAULT NULL COMMENT '代理商简称',
    `phone`      VARCHAR(24)      NOT NULL COMMENT '联系电话',
    `contact`    VARCHAR(64)      NULL COMMENT '联系人姓名',
    `address`    VARCHAR(256)     NULL COMMENT '联系地址',
    `mer_no`     JSON             NULL COMMENT '商户号',
    `ratio`      JSON             NULL COMMENT '费率',
    `status`     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态: 0 - 正常，1 - 冻结',
    `remark`     VARCHAR(256)     NULL     DEFAULT NULL COMMENT '备注',
    UNIQUE KEY `UDX_PHONE` (`phone`),
    INDEX IDX_UID (`uid`)
) COMMENT '代理商';

-- 用户资产
CREATE TABLE `ucenter_user_account`
(
    `id`         BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'ID',
    `created_at` BIGINT UNSIGNED NULL     DEFAULT NULL COMMENT '创建时间',
    `created_by` VARCHAR(32)     NULL     DEFAULT NULL COMMENT '创建人ID',
    `updated_at` BIGINT UNSIGNED NULL     DEFAULT NULL COMMENT '最后更新时间',
    `updated_by` VARCHAR(32)     NULL     DEFAULT NULL COMMENT '最后更新人ID',
    `deleted`    BIT             NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `tenant_id`  VARCHAR(32)     NOT NULL DEFAULT '0' COMMENT '租户ID',
    `uid`        BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `asset`      VARCHAR(24)     NOT NULL COMMENT '账户资产类型',
    `balance`    DECIMAL(18, 3)  NOT NULL DEFAULT 0 COMMENT '可用余额',
    `frozen`     DECIMAL(18, 3)  NOT NULL DEFAULT 0 COMMENT '冻结金额',
    `transition` DECIMAL(18, 3)  NOT NULL DEFAULT 0 COMMENT '在途金额',
    `version`    SMALLINT        NOT NULL DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY UDX_ASSET (tenant_id asc, uid asc, asset)
) COMMENT '用户资产表';

CREATE TABLE IF NOT EXISTS ucenter_user_account_transaction
(
    `id`           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'ID',
    `created_at`   BIGINT UNSIGNED NULL     DEFAULT NULL COMMENT '创建时间',
    `created_by`   VARCHAR(32)     NULL     DEFAULT NULL COMMENT '创建人ID',
    `updated_at`   BIGINT UNSIGNED NULL     DEFAULT NULL COMMENT '最后更新时间',
    `updated_by`   VARCHAR(32)     NULL     DEFAULT NULL COMMENT '最后更新人ID',
    `deleted`      BIT             NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `tenant_id`    VARCHAR(32)     NOT NULL DEFAULT '0' COMMENT '租户ID',
    `uid`          BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `asset`        VARCHAR(24)     NOT NULL COMMENT '账户资产类型',
    `amount`       DECIMAL(18, 3)  NOT NULL COMMENT '交易发生额',
    `pre_balance`  DECIMAL(18, 3)  NOT NULL COMMENT '交易前余额',
    `balance`      DECIMAL(18, 3)  NOT NULL COMMENT '交易后余额',
    `pre_frozen`   DECIMAL(18, 3)  NOT NULL COMMENT '交易前冻结金额',
    `frozen`       DECIMAL(18, 3)  NOT NULL COMMENT '交易后冻结金额',
    `biz_type`     VARCHAR(24)     NOT NULL COMMENT '交易业务类型(线)',
    `biz_subject`  VARCHAR(64)     NOT NULL COMMENT '业务描述',
    `biz_order_no` VARCHAR(32)     NOT NULL COMMENT '交易业务订单编号',
    `transit_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '预交易ID',
    `trans_hash`   CHAR(32)        NOT NULL COMMENT '交易摘要md5(tenant_id+uid+asset+biz_type+biz_order_no)',
    `remark`       VARCHAR(256)    NULL     DEFAULT NULL COMMENT '交易备注',
    INDEX IDX_USER_ID (tenant_id, uid, asset, biz_type),
    UNIQUE KEY UDX_HASH (trans_hash)
) COMMENT '用户资产交易流水记录表';

CREATE TABLE IF NOT EXISTS ucenter_user_account_transition
(
    `id`            BIGINT UNSIGNED                               NOT NULL PRIMARY KEY COMMENT 'ID',
    `created_at`    BIGINT UNSIGNED                               NULL     DEFAULT NULL COMMENT '创建时间',
    `created_by`    VARCHAR(32)                                   NULL     DEFAULT NULL COMMENT '创建人ID',
    `updated_at`    BIGINT UNSIGNED                               NULL     DEFAULT NULL COMMENT '最后更新时间',
    `updated_by`    VARCHAR(32)                                   NULL     DEFAULT NULL COMMENT '最后更新人ID',
    `deleted`       BIT                                           NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `tenant_id`     VARCHAR(32)                                   NOT NULL DEFAULT '0' COMMENT '租户ID',
    `uid`           BIGINT UNSIGNED                               NOT NULL COMMENT '用户ID',
    `asset`         VARCHAR(24)                                   NOT NULL COMMENT '资产类型',
    `amount`        DECIMAL(18, 3)                                NOT NULL COMMENT '意向金额',
    `biz_type`      VARCHAR(24)                                   NOT NULL COMMENT '交易业务类型(线)',
    `biz_subject`   VARCHAR(64)                                   NOT NULL COMMENT '业务描述',
    `biz_order_no`  VARCHAR(32)                                   NOT NULL COMMENT '交易业务订单编号',
    `last_tid`      BIGINT UNSIGNED                               NOT NULL COMMENT '预交易发生时，最近一笔交易ID',
    `trans_hash`    CHAR(32)                                      NOT NULL COMMENT '交易摘要md5(tenant_id+uid+asset+biz_type+biz_order_no)',
    `remark`        VARCHAR(256)                                  NULL     DEFAULT NULL COMMENT '交易备注',
    `status`        ENUM ('PENDING','CANCELED','DONE','REFUNDED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `cancel_reason` VARCHAR(512)                                  NULL     DEFAULT NULL COMMENT '取消原因',
    INDEX IDX_CREATE_TIME (created_at asc) using BTREE,
    INDEX IDX_USER_ID (tenant_id, uid, asset, biz_type),
    UNIQUE KEY UDX_HASH (trans_hash)
) COMMENT '账户在途资产记录';

CREATE TABLE IF NOT EXISTS ucenter_user_account_refund
(
    `id`           BIGINT UNSIGNED NOT NULL PRIMARY KEY COMMENT 'ID',
    `created_at`   BIGINT UNSIGNED NULL     DEFAULT NULL COMMENT '创建时间',
    `created_by`   VARCHAR(32)     NULL     DEFAULT NULL COMMENT '创建人ID',
    `updated_at`   BIGINT UNSIGNED NULL     DEFAULT NULL COMMENT '最后更新时间',
    `updated_by`   VARCHAR(32)     NULL     DEFAULT NULL COMMENT '最后更新人ID',
    `deleted`      BIT             NOT NULL DEFAULT FALSE COMMENT '删除标记',
    `tenant_id`    VARCHAR(32)     NOT NULL DEFAULT '0' COMMENT '租户ID',
    `uid`          BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `asset`        VARCHAR(24)     NOT NULL COMMENT '账户资产类型',
    `trans_id`     BIGINT UNSIGNED NOT NULL COMMENT '交易ID',
    `amount`       DECIMAL(18, 3)  NOT NULL COMMENT '退款金额',
    `biz_type`     VARCHAR(24)     NOT NULL COMMENT '交易业务类型(线)',
    `biz_subject`  VARCHAR(64)     NOT NULL COMMENT '业务描述',
    `biz_order_no` VARCHAR(32)     NOT NULL COMMENT '交易业务订单编号',
    `remark`       VARCHAR(256)    NULL     DEFAULT NULL COMMENT '交易备注',
    `reason`       VARCHAR(512)    NULL     DEFAULT NULL COMMENT '退款原因',
    INDEX IDX_CREATE_TIME (created_at asc),
    INDEX IDX_USER_ID (tenant_id, uid, asset, biz_type),
    INDEX IDX_TRANS_ID (tenant_id, trans_id)
) COMMENT '账户交易退款';

-- username: admin
INSERT INTO `ucenter_user` (id, created_at, created_by, username, nickname, passwd, status)
VALUES (1, 1748398166000, '1', 'admin', 'Administrator',
        '{bcrypt}$2a$10$lda8JKIdmgV8mXLFZVTiVOgHaiQuRJXtyL55RbECrs0HtkHf4ZHy.', 'ACTIVATED');
-- tenant: 0
INSERT INTO `ucenter_tenant` (id, created_at, created_by, code, name, short_name, logo, uid, status, phone)
    VALUE (0, 1748398166000, '1', '0', '', '', '', '1', 0, '');
-- oauth
INSERT INTO `ucenter_oauth` (id, created_at, created_by, uid, provider, app_id, open_id, union_id)
VALUES (1, 1748398166000, '1', 1, 'db', 'db', 'admin', 'admin');
-- tenant user
INSERT INTO `ucenter_tenant_user`(id, created_at, created_by, tenant_id, uid, current)
VALUES (1, 1748398166000, '1', '0', 1, true);
-- roles
INSERT INTO `ucenter_role` (id, created_at, created_by, role, name, builtin, provider)
VALUES (1, 1748398166000, '1', 'sa', 'Super Administrator', true, 'db');

-- privileges
INSERT INTO `ucenter_rbac_privilege` (id, created_at, created_by, name, type, builtin, permission, service)
VALUES (1, 1748398166000, '1', 'All Privileges', 'resource', true, '*:*', 'sys');

-- privileges of role
INSERT INTO `ucenter_role_privilege` (id, created_at, created_by, tenant_id, role_id, privilege_id)
VALUES (1, 1748398166000, '1', '0', 1, 1);

-- role of user
INSERT INTO `ucenter_user_role` (id, created_at, created_by, uid, role_id)
VALUES (1, 1748398166000, '1', 1, 1);
