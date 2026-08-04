# Bookeeper Agent 工作说明

本文档记录 Agent 在 Bookeeper 项目中使用的环境、常用命令和操作约定。所有命令默认在 PowerShell 中执行。

## 工作目录

```text
D:\A_Mycodes\Android_Studio_code\Bookeeper
```

Android 开发环境：

```text
Android Studio：D:\MisItems\Android\Android_Studio
Android SDK：D:\MisItems\Android\Android_SDK
JDK：17
```

项目本机的实际 SDK 路径以根目录 `local.properties` 中的 `sdk.dir` 为准。

## 文件查看与搜索

列出根目录文件：

```powershell
Get-ChildItem -Force
```

递归列出工程文件，忽略构建产物：

```powershell
rg --files -g "!build" -g "!.gradle"
```

搜索代码内容：

```powershell
rg "需要查找的文本" app
```

查看文件内容：

```powershell
Get-Content app\build.gradle.kts
Get-Content app\src\main\java\com\example\bookeeper\MainActivity.kt
```

项目已经初始化为 Git 仓库，可以使用以下命令查看工作区状态：

```powershell
git status --short
git diff -- .gitignore README.md AGENTS.md
```

## Gradle 命令

查看可用任务：

```powershell
.\gradlew.bat tasks
```

构建调试版本：

```powershell
.\gradlew.bat assembleDebug
```

运行 JVM 单元测试：

```powershell
.\gradlew.bat testDebugUnitTest
```

运行 Android Lint：

```powershell
.\gradlew.bat lintDebug
```

连接设备后安装调试版本：

```powershell
.\gradlew.bat installDebug
```

连接设备后运行仪器测试：

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

仅在确有必要排查缓存问题时清理构建产物：

```powershell
.\gradlew.bat clean
```

`clean` 会删除可重新生成的构建产物，不应把它作为每次构建前的默认步骤。

## ADB 与模拟器命令

列出已连接的真机和模拟器：

```powershell
& "D:\MisItems\Android\Android_SDK\platform-tools\adb.exe" devices
```

查看已经创建的 AVD：

```powershell
& "D:\MisItems\Android\Android_SDK\emulator\emulator.exe" -list-avds
```

启动指定 AVD，其中名称应替换为上一条命令返回的实际名称：

```powershell
& "D:\MisItems\Android\Android_SDK\emulator\emulator.exe" -avd "Pixel_7_Pro_API_36"
```

查看 Bookeeper 的运行日志：

```powershell
& "D:\MisItems\Android\Android_SDK\platform-tools\adb.exe" logcat | Select-String "bookeeper"
```

## Agent 操作约定

1. 修改代码前先检查现有文件；如果项目已经初始化 Git，再检查 `git status --short`，并保留用户已有的未提交修改。
2. 优先修改源文件和构建配置，不手动编辑 `build/`、`.gradle/`、`.idea/` 等生成目录。
3. 不提交 `local.properties`，也不在文档或代码中保存密码、令牌和签名密钥。
4. 金额使用最小货币单位“分”的整数值存储，不使用 `Double` 作为账单金额的数据类型。
5. 功能实现后至少执行与改动匹配的构建或测试；修改 Gradle 或应用入口时至少运行 `assembleDebug`。
6. 数据库结构发生变化时，必须同步考虑 Room migration 和旧数据兼容，不能通过静默清空用户账单解决升级问题。
7. 删除账单、数据库或用户文件属于高风险操作，必须明确目标，并优先提供确认步骤或可恢复方案。
8. README 中的进度只在功能实际完成并验证后更新为已完成。
9. 除非用户明确要求，不执行发布、上传、推送、签名或对外发送操作。

## 推荐验证顺序

普通代码改动完成后依次执行：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

涉及 Room、Android 框架或 Compose 交互的功能，还应在模拟器或真机上执行仪器测试和人工检查。
