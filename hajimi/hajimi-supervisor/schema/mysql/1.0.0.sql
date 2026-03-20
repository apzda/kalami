-- 监督者任务队列
CREATE TABLE IF NOT EXISTS `sys_supervisor_task`
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
