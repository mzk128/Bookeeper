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

## 当前项目基线

- Git 仓库已经初始化并完成首次 GitHub 推送。
- 远程仓库：`https://github.com/mzk128/Bookeeper.git`
- 默认分支：`main`
- 本地分支跟踪：`main` → `origin/main`
- 当前应用为 Jetpack Compose 模板，`Hello Android!` 已在模拟器中运行成功。
- Gradle 依赖已经整理，Navigation、Lifecycle ViewModel、Room 和 KSP 已接入并通过构建。
- 首页、账单、统计、设置页面、应用级 NavHost、Scaffold 和底部导航骨架已经建立。
- `MainActivity` 目前只负责设置主题并承载 `BookeeperApp`。
- 下一任务：用户完成模拟器导航验收后，建立 Room 账单数据层。

当前构建基线：

- Android Gradle Plugin 9.3.1 / Gradle 9.5.0
- KSP 2.3.10
- Navigation Compose 2.9.8
- Lifecycle 2.11.0
- Room 2.8.4
- Java 源码和目标兼容级别 17
- `testDebugUnitTest`、`lintDebug`、`assembleDebug` 已于 2026-08-04 通过
- 导航骨架的 `testDebugUnitTest`、`lintDebug`、`assembleDebug` 已于 2026-08-04 通过

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

项目已经初始化并关联远程仓库，可以使用以下命令检查状态：

```powershell
git status --short --branch
git remote -v
git diff
git diff --staged
```

查看当前分支与最近提交：

```powershell
git branch --show-current
git log -5 --oneline --decorate
```

提交前应先检查 `git diff`，提交后按需执行 `git push`。Agent 不得因为远程仓库已经配置就自动提交或推送。

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
10. 引入依赖时统一在 `gradle/libs.versions.toml` 管理版本，避免在多个构建文件中重复硬编码版本号。
11. 当前使用 AGP 9 的内置 Kotlin 支持，不额外添加旧的 `org.jetbrains.kotlin.android` 插件。

## 下一阶段实施顺序

1. 由用户在模拟器中确认应用启动、当前导航高亮和四个顶级页面切换正常。
2. 检查 Git 工作区，读取现有 Room/KSP 配置并确定版本 1 schema 目录。
3. 使用 `Long` 分值定义账单金额，并创建 Transaction、Category、Account 实体。
4. 创建 DAO、`BookeeperDatabase` 和初始 Repository，不使用破坏性迁移。
5. 为 DAO 查询、收支汇总和数据持久化增加内存数据库测试。
6. 执行 `testDebugUnitTest`、`lintDebug`、`assembleDebug`；Room 仪器测试需要在线模拟器。

## 推荐验证顺序

普通代码改动完成后依次执行：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

涉及 Room、Android 框架或 Compose 交互的功能，还应在模拟器或真机上执行仪器测试和人工检查。
