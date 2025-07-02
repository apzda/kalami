# 用户模型微服务

主包名: `com.apzda.kalami.user`

**工程结构说明**

| 工程              | 所属分层                          | 说明       |
|:----------------|:------------------------------|:---------|
| user-client     |                               | 定义外部调用接口 |
| user-common     |                               | 公共类库     |
| user-domain     | domain                        | 业务领域     |
| user-service    | ui,application,infrastructure | 微服务      |
| user-web-server |                               | 应用启动器    |
