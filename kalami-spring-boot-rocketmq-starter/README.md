# RocketMQ 启动器

## 测试说明

启动测试之前需要创建以下`Topic`：

1. **demo_tx**: `mqadmin updateTopic -n nameserver:9876 -b broker:10911 -a +message.type=TRANSACTION -t demo_tx`
2. **demo_delay**: `mqadmin updateTopic -n nameserver:9876 -b broker:10911 -a +message.type=DELAY -t demo_delay`
3. **demo**: `mqadmin updateTopic -n nameserver:9876 -b broker:10911 -t demo`

> 需要替换`nameserver:9876`与`broker:10911`.
