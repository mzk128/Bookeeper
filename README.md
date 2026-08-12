# Bookeeper

Bookeeper 是一款面向 Android 手机的本地记账应用，用于记录每天的收入与支出，并提供账单查询、收支汇总和分类统计功能。

## 当前状态

截至 2026-08-12，项目已完成记账核心闭环：新增、查看、编辑、删除确认、多条件筛选、首页月度汇总和最近账单均已连接 Room 的响应式 Flow。用户已人工确认收入/支出保存返回、金额显示和账单列表实时刷新正常。

本阶段开发起点：

- 本地分支：`main`
- 远程跟踪分支：`origin/main`
- 起点提交：`04832d5 feat: add app navigation skeleton`

- [x] 创建 Android 项目
- [x] 配置 Android SDK
- [x] 创建并运行 Jetpack Compose `Hello Android!` 页面
- [x] 确定第一版产品范围和技术架构
- [x] 初始化 Git 版本控制并配置 `.gitignore`
- [x] 关联 GitHub 远程仓库并完成首次推送
- [x] 整理 Gradle 和版本目录依赖
- [x] 引入 Navigation、Room、ViewModel 等基础组件
- [x] 建立首页、账单、统计、设置页面和底部导航骨架
- [x] 在模拟器中人工验证应用启动、导航高亮、顶部标题和四个页面切换
- [x] 添加 `.gitattributes` 并统一仓库换行符规则
- [x] 定义账单、金额、分类、账户和时间领域模型
- [x] 创建 Room `Transaction`、`Category`、`Account` Entity 与 DAO
- [x] 建立版本 1 Room 本地数据库并导出 schema
- [x] 添加第一版内置分类和默认现金账户
- [x] 创建 Repository 并提供 Flow 数据流
- [x] 完成 DAO、外键、筛选、汇总和初始化仪器测试
- [x] 实现收入、支出的新增与表单校验
- [x] 实现账单详情、修改和带确认步骤的删除
- [x] 实现真实账单列表
- [x] 实现收入/支出、分类、账户和日期范围筛选
- [x] 实现首页月度收支汇总和最近账单
- [ ] 实现分类统计
- [ ] 实现设置、分类管理和数据导入导出
- [ ] 补充自动化测试并发布第一版

当前入口文件：

```text
app/src/main/java/com/example/bookeeper/MainActivity.kt
```

当前已实现的应用行为：

- `MainActivity` 设置 Material 3 主题并承载 `BookeeperApp`。
- 应用级 `Scaffold` 提供顶部标题和底部导航栏。
- 应用级 `NavHost` 管理首页、账单、统计和设置四个顶级目的地。
- 四个页面均为明确的占位界面，尚未连接真实账单数据。
- 底部导航使用保存/恢复状态和 `launchSingleTop`，避免重复创建顶级页面。

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

金额使用 `Long` 保存最小货币单位“分”，避免使用浮点数产生精度误差。例如，`25.68` 元在数据库中保存为 `2568` 分。单笔账单金额始终为正数，由 `INCOME` 或 `EXPENSE` 决定收支方向；账户初始余额和汇总差额允许为负数。

当前数据设计采用以下第一版约定：

- 收支类型使用稳定字符串 `income`、`expense` 持久化，避免枚举名称重构直接破坏旧数据。
- 分类区分收入与支出类型；账户支持现金、银行卡、支付宝、微信和其他类型。
- 一笔账单关联一个分类和一个账户，时间以 Unix epoch 毫秒保存。
- 已被账单引用的分类和账户不能直接删除，日常管理使用“归档”，以保留历史记录。
- 时间区间查询统一使用 `[开始时间, 结束时间)`，避免相邻日期范围重复计算边界账单。

版本 1 本地数据层已经完成：

- `BookeeperDatabase` 包含账单、分类和账户三张表，不启用破坏性迁移。
- Room schema 已导出到 `app/schemas/com.example.bookeeper.data.local.BookeeperDatabase/1.json` 并供迁移测试复用。
- 首次建库同步写入 8 个支出分类、5 个收入分类和默认“现金”账户；固定 ID 与 `INSERT OR IGNORE` 保证初始化稳定且幂等。
- `BookeeperRepository` 与 `OfflineBookeeperRepository` 向后续 ViewModel 提供领域对象、CRUD 和响应式 `Flow`。
- 模拟器内存数据库测试已覆盖 CRUD、外键约束、组合筛选、半开时间区间和期间收支汇总。

实际 `bookeeper.db` 不需要开发者手工创建；后续应用入口首次获取 `BookeeperDatabase` 实例时，会在手机或模拟器的应用私有目录中自动生成。

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

当前基础依赖版本：

- Android Gradle Plugin：9.3.1
- Gradle Wrapper：9.5.0
- KSP：2.3.10
- Navigation Compose：2.9.8
- Lifecycle：2.11.0
- Room：2.8.4
- Java 源码和目标兼容级别：17

## 当前代码结构

```text
com.example.bookeeper
├─ MainActivity.kt
├─ navigation
│  ├─ BookeeperNavHost.kt
│  └─ TopLevelDestination.kt
├─ domain/model
│  ├─ Money.kt、TransactionType.kt、TransactionRecord.kt
│  └─ Category.kt、Account.kt、AccountType.kt
├─ data
│  ├─ local/BookeeperDatabase.kt、DefaultDataCallback.kt
│  ├─ local/converter/BookeeperTypeConverters.kt
│  ├─ local/entity/TransactionEntity.kt、CategoryEntity.kt、AccountEntity.kt
│  ├─ local/dao/TransactionDao.kt、CategoryDao.kt、AccountDao.kt
│  ├─ local/model/PeriodSummary.kt
│  ├─ mapper/EntityMappers.kt
│  └─ repository/BookeeperRepository.kt、OfflineBookeeperRepository.kt
├─ ui
│  ├─ BookeeperApp.kt
│  ├─ home/HomeScreen.kt
│  ├─ ledger/LedgerScreen.kt
│  ├─ statistics/StatisticsScreen.kt
│  ├─ settings/SettingsScreen.kt
│  ├─ components/FeaturePlaceholderScreen.kt
│  ├─ transaction（待创建）
│  └─ theme
└─ util（待创建）
```

`navigation`、Room 本地数据层、依赖容器、账单新增/详情/编辑/删除、筛选和首页汇总已经建立；统计页面、分类/账户管理与数据导入导出尚待实现。

## 开发环境

当前本机环境：

```text
项目目录：D:\A_Mycodes\Android_Studio_code\Bookeeper
Android 工具目录：D:\MisItems\Android
Android SDK：D:\MisItems\Android\Android_SDK
JDK：17
```

`local.properties` 是本机配置文件，不应提交到版本控制。

## 版本控制

- 远程仓库：[mzk128/Bookeeper](https://github.com/mzk128/Bookeeper)
- 默认分支：`main`
- 远程名称：`origin`
- 远程协议：HTTPS
- 首次推送：已完成
- 本阶段开发起点：`04832d5 feat: add app navigation skeleton`

开始开发前使用 `git status --short --branch` 检查工作区；功能完成并验证后再提交。除非明确执行发布步骤，否则本地提交不会自动推送到远程仓库。

仓库级 `.gitattributes` 已规定 Kotlin、Gradle、XML、JSON、Markdown 等文本文件使用 LF，Windows 命令脚本使用 CRLF，图片和 JAR 等资源按二进制处理。首次应用规则时应使用 `git add --renormalize .`，并在提交前检查暂存差异。

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

最近一次基础依赖验证（2026-08-04）：

- `testDebugUnitTest`：通过
- `lintDebug`：通过
- `assembleDebug`：通过
- KSP 任务：执行成功
- 调试 APK：生成成功

最近一次导航骨架验证（2026-08-12）：

- 顶级路由唯一性单元测试：通过
- `testDebugUnitTest`：通过
- `lintDebug`：通过
- `assembleDebug`：通过
- 模拟器人工交互：用户已确认应用启动、当前导航高亮、顶部标题和四个页面切换正常

最近一次领域模型与 Room Entity/DAO 验证（2026-08-12）：

- 金额、账单约束和类型转换器单元测试：通过
- Room KSP 编译及 DAO SQL 校验：通过
- `testDebugUnitTest`：通过
- `lintDebug`：通过
- `assembleDebug`：通过
- `BookeeperDatabase` 版本 1 schema：生成成功
- 默认数据初始化、CRUD、外键、筛选和汇总仪器测试：6 项全部通过
- `connectedDebugAndroidTest`：通过（Pixel 7 Pro API 36 模拟器）

最近一次新增账单闭环验证（2026-08-12）：

- ViewModel 与领域模型 JVM 单元测试：14 项全部通过
- Room 与新增账单 Compose 仪器测试：8 项全部通过
- `lintDebug`：通过
- `assembleDebug`：通过
- `installDebug`：成功安装到 Pixel 7 Pro API 36 模拟器
- 实际应用冷启动：成功，进程存活且崩溃日志为空
- 导航烟雾检查：首页 → 账单 → 新增账单正常
- 真实默认数据加载：新增页已显示“餐饮”和“现金”

最近一次账单管理、筛选与首页验证（2026-08-12）：

- 用户人工新增收入/支出：保存返回、金额显示、列表实时刷新均通过
- JVM 单元测试：编辑、删除、筛选、日期范围、首页汇总等全部通过
- Android 仪器测试：9 项全部通过，包含删除确认 Compose 测试
- `lintDebug`、`assembleDebug`、`installDebug`：通过
- 实际首页：月度收入、支出、结余与最近账单区域正常渲染
- 实际账单页：筛选入口与类型、分类、账户、起止日期控件正常渲染
- 实际应用冷启动与导航烟雾检查：通过，崩溃日志为空
- Room schema：未变化，仍为版本 1

## 下一步

下一开发阶段为“统计与基础资料管理”，建议按以下顺序推进：

1. 由用户人工验收详情、编辑、删除确认、筛选和首页月度汇总。
2. 实现统计页的月份切换、每日收支趋势和分类占比。
3. 实现分类管理，内置分类允许归档但保留历史账单。
4. 实现账户管理和账户余额计算。
5. 为统计、分类和账户功能补充测试。

本阶段的验收标准：

- 金额以 `Long` 保存最小货币单位“分”。
- Room schema 版本从 1 开始并纳入版本控制。
- DAO 支持新增、修改、删除、按时间查询和收支汇总。
- 数据库与 Repository 测试通过。
- 不启用 `fallbackToDestructiveMigration()`，避免升级时静默清空账单。
- `testDebugUnitTest`、`lintDebug` 和 `assembleDebug` 保持通过。
