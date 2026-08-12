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

- 基线检查日期：2026-08-12。
- Git 仓库已经初始化并完成首次 GitHub 推送。
- 远程仓库：`https://github.com/mzk128/Bookeeper.git`
- 默认分支：`main`
- 本地分支跟踪：`main` → `origin/main`
- 当前应用为 Jetpack Compose 模板，`Hello Android!` 已在模拟器中运行成功。
- Gradle 依赖已经整理，Navigation、Lifecycle ViewModel、Room 和 KSP 已接入并通过构建。
- 首页、账单、统计、设置页面、应用级 NavHost、Scaffold 和底部导航骨架已经建立。
- 用户已在模拟器中确认应用启动、导航高亮、顶部标题和四个页面切换正常。
- `MainActivity` 目前只负责设置主题并承载 `BookeeperApp`。
- 四个顶级页面目前是占位界面，尚未读取或写入真实账单数据。
- `.gitattributes` 已统一文本文件与 Windows 脚本的换行规则。
- 金额、收支类型、分类、账户和账单领域模型已经创建；金额使用 `Long` 分值。
- Room Entity、类型转换器、映射器和三个 DAO 已创建，并已通过 KSP 的 SQL 编译校验。
- 版本 1 `BookeeperDatabase`、默认数据初始化、Repository 和导出的 schema 已创建。
- 首次建库包含 8 个支出分类、5 个收入分类和默认“现金”账户。
- DAO、外键、筛选、汇总和初始化共 6 个模拟器仪器测试已通过。
- `BookeeperApplication` 与 `DefaultAppContainer` 在应用生命周期内提供数据库和 Repository 单例。
- 新增账单 ViewModel、表单校验和 Compose 页面已实现，可保存收入/支出到 Room。
- 账单页已连接 Repository Flow，并显示真实账单、分类和账户名称。
- 新增页是非顶级导航目的地，隐藏底部导航，保存成功后返回账单页。
- 用户已人工确认新增收入/支出保存返回、金额显示和账单列表实时刷新正常。
- 账单详情、编辑和删除已实现；删除必须经过确认对话框。
- 账单页支持收入/支出、分类、账户和包含结束当天的日期范围筛选。
- 首页已连接当前自然月收支汇总和最近 5 笔账单 Flow。
- 用户不需要手工创建 SQLite 文件；Room 将在应用运行时自动创建应用私有数据库。
- 下一任务：人工验收本阶段交互，然后实现统计页与分类/账户管理。

本阶段开发起点：

- 起点提交：`04832d5 feat: add app navigation skeleton`
- 首次暂存 `.gitattributes` 规则后需要执行 `git add --renormalize .` 并检查换行差异

当前构建基线：

- Android Gradle Plugin 9.3.1 / Gradle 9.5.0
- KSP 2.3.10
- Navigation Compose 2.9.8
- Lifecycle 2.11.0
- Room 2.8.4
- Java 源码和目标兼容级别 17
- 导航骨架已于 2026-08-12 完成人工模拟器验收
- 领域模型和类型转换器单元测试已于 2026-08-12 通过
- Room KSP 编译和 DAO SQL 校验已于 2026-08-12 通过
- `testDebugUnitTest`、`lintDebug`、`assembleDebug` 已于 2026-08-12 通过
- `connectedDebugAndroidTest` 已于 2026-08-12 在 Pixel 7 Pro API 36 模拟器通过（6 项测试）
- 新增账单阶段的 `testDebugUnitTest` 已于 2026-08-12 通过（14 项测试）
- 新增账单阶段的 `connectedDebugAndroidTest` 已于 2026-08-12 通过（8 项测试）
- `lintDebug`、`assembleDebug`、`installDebug` 与实际应用启动烟雾检查已于 2026-08-12 通过
- 账单管理阶段的 JVM 单元测试已于 2026-08-12 通过
- 账单管理阶段的 `connectedDebugAndroidTest` 已于 2026-08-12 通过（9 项测试）
- 账单管理阶段的 `lintDebug`、`assembleDebug`、`installDebug` 和实际 UI 烟雾检查已通过
- 本阶段未改变 Room schema，数据库版本仍为 1

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
12. Room 实际数据库文件属于运行时数据，不加入仓库；需要纳入版本控制的是导出的 `app/schemas/*.json` schema 文件。
13. 版本 1 数据库不得启用 `fallbackToDestructiveMigration()`；后续每次 schema 变更都必须增加版本号和可验证的 migration。
14. 出现 `LF will be replaced by CRLF` 时先检查 `.gitattributes`、`core.autocrlf` 和 `git ls-files --eol`，不要仅为消除警告而批量改写源文件。

## 下一阶段实施顺序

1. 由用户人工验收账单详情、编辑、删除确认、筛选及首页汇总。
2. 实现统计页月份切换、每日收支趋势与分类占比。
3. 实现分类列表、新增、编辑和归档；已被账单引用的分类不得直接删除。
4. 实现账户列表、新增、编辑、归档和基于账单的余额计算。
5. 增加统计查询、分类/账户管理和 UI 测试。
6. 执行完整验证序列并确认 schema 变化时提供 migration。

Room 数据层完成后的人工检查：

```text
启动 API 26+ 模拟器
→ 运行 Bookeeper
→ View > Tool Windows > App Inspection
→ Database Inspector
→ 选择 Bookeeper 进程并检查 bookeeper.db
```

## 推荐验证顺序

普通代码改动完成后依次执行：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

涉及 Room、Android 框架或 Compose 交互的功能，还应在模拟器或真机上执行仪器测试和人工检查。
