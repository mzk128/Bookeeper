# Bookeeper

Bookeeper 是一款面向 Android 手机的本地记账应用，用于记录每天的收入与支出，并提供账单查询、收支汇总和分类统计功能。

## 当前状态

项目目前处于基础工程阶段。

- [x] 创建 Android 项目
- [x] 配置 Android SDK
- [x] 创建并运行 Jetpack Compose `Hello Android!` 页面
- [x] 确定第一版产品范围和技术架构
- [x] 初始化 Git 版本控制并配置 `.gitignore`
- [ ] 建立页面导航和应用主界面
- [ ] 建立 Room 本地数据库
- [ ] 实现收入、支出的新增、修改和删除
- [ ] 实现账单列表及筛选
- [ ] 实现首页收支汇总
- [ ] 实现分类统计
- [ ] 实现设置、分类管理和数据导入导出
- [ ] 补充自动化测试并发布第一版

当前入口文件：

```text
app/src/main/java/com/example/bookeeper/MainActivity.kt
```

## 第一版目标

第一版采用本地优先的单机记账方式，先完成完整、可靠的日常记账流程，不依赖账号、网络或云服务。

核心流程：

```text
新增收入/支出
    → 保存到本地数据库
    → 显示到账单列表
    → 自动更新余额和统计
    → 支持修改或删除
```

第一版计划包含以下页面：

1. 首页：显示总余额、本月收入、本月支出、本月结余和最近账单。
2. 新增记录：填写类型、金额、分类、账户、日期和备注。
3. 账单：按日期显示记录，并支持查看、筛选、修改和删除。
4. 统计：显示月份汇总、每日趋势和分类占比。
5. 设置：管理分类、货币、主题以及数据导入导出。

## 数据设计

主要数据包括：

- `Transaction`：收入或支出记录。
- `Category`：餐饮、交通、工资等收支分类。
- `Account`：现金、银行卡、支付宝、微信等账户。

金额计划使用 `Long` 保存最小货币单位“分”，避免使用浮点数产生精度误差。例如，`25.68` 元在数据库中保存为 `2568` 分。

## 技术方案

- 开发语言：Kotlin
- UI：Jetpack Compose + Material 3
- 页面导航：Navigation Compose
- 架构：UI + ViewModel + Repository + 本地数据源
- 状态管理：ViewModel + StateFlow
- 数据库：Room
- 设置存储：DataStore
- 后台任务：WorkManager（后续用于提醒和备份）
- 最低 Android 版本：Android 7.0（API 24）
- JDK：17

当前模板使用 `compileSdk 37` 和 `targetSdk 37`。正式开发前计划确认本机 SDK 安装情况；如果不需要测试 Android 17 预览功能，则优先切换到稳定 SDK。

## 计划中的代码结构

```text
com.example.bookeeper
├─ MainActivity.kt
├─ navigation
├─ data
│  ├─ local
│  ├─ model
│  └─ repository
├─ ui
│  ├─ home
│  ├─ ledger
│  ├─ transaction
│  ├─ statistics
│  ├─ settings
│  ├─ components
│  └─ theme
└─ util
```

目录将在相应功能开始实现时创建，当前未实现的目录不提前生成。

## 开发环境

当前本机环境：

```text
项目目录：D:\A_Mycodes\Android_Studio_code\Bookeeper
Android 工具目录：D:\MisItems\Android
Android SDK：D:\MisItems\Android\Android_SDK
JDK：17
```

`local.properties` 是本机配置文件，不应提交到版本控制。

## 构建与验证

在 PowerShell 中进入项目根目录后，可以运行：

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

生成的调试 APK 通常位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

连接真机或启动模拟器后，可以安装调试版本：

```powershell
.\gradlew.bat installDebug
```

## 下一步

下一开发阶段将完成：

1. 整理 Gradle 和版本目录依赖。
2. 引入 Navigation、Room、ViewModel 等组件。
3. 创建应用主导航和首页、账单、统计、设置页面骨架。
4. 保证项目可以构建并在模拟器中启动。
