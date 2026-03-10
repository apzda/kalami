TRUNCATE TABLE ucenter_user_account;
TRUNCATE TABLE ucenter_user_account_transaction;

DELETE
FROM `ucenter_user`
WHERE id = 100;

INSERT INTO `ucenter_user` (id, created_at, created_by, username, nickname, passwd, status)
VALUES (100, 1748398166000, '1', 'test', 'Administrator',
        '{bcrypt}$2a$10$lda8JKIdmgV8mXLFZVTiVOgHaiQuRJXtyL55RbECrs0HtkHf4ZHy.', 'ACTIVATED');

INSERT INTO ucenter_user_account (id, created_at, created_by, updated_at, updated_by, tenant_id, uid, asset, balance,
                                  frozen, transition, version)
VALUES (1970733012012662786, 1758694339172, '0', 1758694339172, '0', 0, 1, 'CASH', '100', '0', '0', 0);

INSERT INTO ucenter_user_account_transaction (id, created_at, created_by, updated_at, updated_by, tenant_id, uid, asset,
                                              amount, pre_balance, balance, pre_frozen, frozen, biz_type, biz_subject,
                                              biz_order_no, trans_hash, remark)
VALUES (1970733012083965954, 1758694339186, '0', 1758694339186, '0', 0, 1, 'CASH', '100', '0', '100.08', '0', '0',
        'test', 'test', 'T0001', '4e44e95d3125d5b4c64a524ee9e08d3d', '测试');
