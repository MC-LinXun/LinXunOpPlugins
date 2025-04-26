# 🧹 凌寻扫地姬 (Linxun AutoClean) - 自动清理插件

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.12.2+-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

## ⚙️ 配置信息

### 核心参数
| 参数名          | 说明                     | 单位 |
|----------------|--------------------------|------|
| `InstantlyTime` | 首次执行时间             | 秒   |
| `DelayTime`     | 间隔执行时间             | 秒   |
| `CountDownTime` | 倒计时提示时长           | 秒   |

⚠️ **注意**  
使用指令修改配置会覆盖手动修改的**config.yml**文件内容 每次修改配置文件后 必须执行**relode**来重载文件，否则不生效


---

## 🎮 指令列表

| 指令                | 功能描述                |权限  |
|---------------------|--------------------------|---|
| `/linxun_cleanItem` | 显示帮助菜单             |OP|
| `start`            | 开启自动清理              |OP|
| `stop`             | 停止自动清理              |OP|
| `clean`            | 立即清理掉落物            |OP|
| `revise <类型> <值>`| 修改配置参数             | OP|
| `reload`           | 重载配置文件              |OP|
| `status`           | 查看插件状态              |OP|
| `print`            | 打印完整配置              |OP|

---

## 🛠️ 使用示例
```bash
# 修改倒计时为10秒
/linxun_cleanItem revise CountDownTime 10

# 重载配置文件
/linxun_cleanItem reload
