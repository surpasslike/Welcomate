# Welcomate 项目改进学习路线图

## 🎯 学习目标

- **主要目标**：通过改进项目提升 Android 开发水平
- **次要目标**：准备面试和简历项目展示
- **学习方式**：自主实现为主，遇到问题时寻求指导

---

## 📋 改进任务清单（按优先级）

### ⭐ 阶段 1：基础改进（1-2周）- 面试重点

这些改进会让你的项目在面试中脱颖而出，展示你对 Android 核心知识的掌握。

#### 任务 1.1：修复静态引用内存泄漏 ⚠️ 重要

**文件位置**: `app-client/src/main/java/com/surpasslike/welcomate/activity/MainActivity.java:31`

**问题**:
```java
private static IAdminService mAdminService; // 这会导致内存泄漏
```

**为什么这是问题？**
- static 引用会持有 Service 的引用
- 即使 Activity 被销毁，引用仍然存在
- 导致 Activity 无法被 GC 回收

**学习任务**:
1. 理解 Android 内存泄漏的原理
2. 研究 static 关键字在 Java 中的行为
3. 了解 WeakReference 和 ServiceLocator 模式

**实现提示**（不是答案）:
- 方案 A：移除 static，使用实例变量（简单但有局限）
- 方案 B：创建 ServiceManager 单例，使用弱引用（推荐）
- 方案 C：使用 Application 类存储（中等方案）

**验证方法**:
```bash
# 使用 Android Profiler 或 LeakCanary 检测
# 旋转屏幕多次，观察内存是否持续增长
```

**面试问答**:
- Q: 为什么 static 会导致内存泄漏？
- Q: 如何检测和修复内存泄漏？
- Q: WeakReference vs SoftReference 的区别？

**预计时间**: 2-4小时

---

#### 任务 1.2：数据库线程安全 ⚠️ 重要

**文件位置**:
- `app-server/src/main/java/com/surpasslike/welcomateservice/data/db/DatabaseHelper.java`
- `app-server/src/main/java/com/surpasslike/welcomateservice/data/UserRepository.java`

**问题**:
- 数据库操作可能在多个线程同时执行
- DatabaseHelper 的单例实现不是线程安全的
- UserRepository 的方法没有同步机制

**学习任务**:
1. 理解 SQLite 的并发模型
2. 学习 Java 同步机制（synchronized、Lock）
3. 了解 WAL (Write-Ahead Logging) 模式

**实现提示**:
1. 查阅 SQLiteDatabase 的文档，了解如何启用 WAL
2. 研究单例模式的线程安全实现（DCL - Double-Checked Locking）
3. 考虑在 DatabaseHelper 的 getWritableDatabase() 周围加同步

**关键问题思考**:
- DatabaseHelper 应该在哪里初始化？
- 读写操作需要分别同步吗？
- 如果多个线程同时写入会发生什么？

**验证方法**:
```java
// 创建测试：启动多个线程同时写数据库
// 观察是否有 SQLiteDatabaseLockedException
```

**面试问答**:
- Q: SQLite 支持并发吗？
- Q: WAL 模式是什么？有什么好处？
- Q: 如何实现线程安全的单例？

**预计时间**: 4-6小时

---

#### 任务 1.3：完善异常处理

**问题文件**: 几乎所有的 Repository 和 DatabaseHelper 方法

**当前问题**:
```java
} catch (Exception e) {
    e.printStackTrace();
    return null;  // 调用者不知道为什么失败
}
```

**学习任务**:
1. 理解 Java 异常处理最佳实践
2. 学习自定义异常类
3. 了解 Kotlin 的 Result 类型（可选）

**实现提示**:
1. 定义业务异常类（例如 `UserNotFoundException`, `DatabaseException`）
2. 在 Repository 层抛出业务异常
3. 在 ViewModel 层捕获并转换为 UI 状态
4. 在 Activity 层显示友好的错误消息

**关键设计决策**:
- 哪些异常应该被捕获？哪些应该抛出？
- 异常信息应该包含什么？
- 如何向用户展示错误？

**面试问答**:
- Q: checked vs unchecked 异常的区别？
- Q: 什么时候应该捕获异常？什么时候应该抛出？
- Q: 如何设计异常层次结构？

**预计时间**: 4-5小时

---

### ⭐⭐ 阶段 2：架构升级（2-3周）- 进阶展示

这些改进展示你对现代 Android 架构的理解。

#### 任务 2.1：迁移到 Room ORM 🔥 核心

**为什么要做这个？**
- Room 是 Google 官方推荐的数据库方案
- 编译时验证 SQL，减少运行时错误
- 自动处理线程安全
- 支持 LiveData/Flow 响应式编程
- **面试高频考点**

**学习任务**:
1. 学习 Room 的三大组件：Entity、DAO、Database
2. 理解注解的作用：@Entity、@Dao、@Database、@Query
3. 了解数据库迁移策略
4. 学习 LiveData 的使用

**实现步骤建议**（自己查资料实现）:

**第一步：添加依赖**
- 在 `app-server/build.gradle` 中添加 Room 依赖
- 添加 kapt 插件（如果使用 Kotlin）或 annotationProcessor

**第二步：定义 Entity**
- 将 `User.java` 改造为 Room Entity
- 使用 @Entity、@PrimaryKey、@ColumnInfo 注解
- 思考：主键应该是什么？自增 ID 还是 username？

**第三步：创建 DAO**
- 创建 `UserDao` 接口
- 定义 CRUD 方法：insert、update、delete、query
- 使用 @Insert、@Update、@Delete、@Query 注解
- 思考：查询方法的返回值应该是什么？List? LiveData? Flow?

**第四步：创建 Database**
- 创建 `AppDatabase` 抽象类
- 使用 @Database 注解
- 实现单例模式（线程安全）

**第五步：迁移 Repository**
- 将 DatabaseHelper 的调用改为 DAO 调用
- 移除手动的 SQL 语句
- 添加线程管理（Room 默认禁止主线程操作）

**学习资源**:
- 官方文档：https://developer.android.com/training/data-storage/room
- Codelab：搜索 "Android Room Codelab"

**验证方法**:
- 所有现有功能正常工作
- 使用 Database Inspector 查看数据
- 编写单元测试验证 DAO

**常见问题**:
- Room 报错 "Cannot access database on main thread"
  → 需要使用协程、RxJava 或 Executors
- 迁移失败
  → 检查 Entity 定义是否与旧数据库一致

**面试问答**:
- Q: Room vs 原始 SQLite 的优势？
- Q: Room 如何保证线程安全？
- Q: 如何处理数据库版本升级？
- Q: LiveData 和 Flow 的区别？

**预计时间**: 8-12小时

---

#### 任务 2.2：完善客户端 MVVM 架构

**当前问题**:
- `app-client` 的 Activity 中混杂了业务逻辑
- 没有使用 ViewModel
- 数据和 UI 耦合严重

**学习任务**:
1. 理解 MVVM 架构模式
2. 学习 ViewModel 和 LiveData
3. 了解数据绑定（ViewBinding/DataBinding）

**实现步骤**（自己设计）:

**第一步：为每个 Activity 创建 ViewModel**
- LoginActivity → LoginViewModel
- RegisterActivity → RegisterViewModel
- HomeActivity → HomeViewModel

**第二步：定义 UI 状态**
- 创建 sealed class 或 data class 表示状态
- 例如：LoginState.Loading, LoginState.Success, LoginState.Error

**第三步：重构业务逻辑**
- 将 AIDL 调用从 Activity 移到 ViewModel
- 使用 LiveData 暴露状态给 Activity
- Activity 只负责观察状态和更新 UI

**第四步：启用 ViewBinding**
- 在 build.gradle 中启用
- 移除 findViewById
- 使用类型安全的视图引用

**关键设计问题**:
- ViewModel 如何获取 IAdminService？（需要注入）
- 如何处理 AIDL 的异步回调？
- 如何在 ViewModel 中处理 Service 连接状态？

**面试问答**:
- Q: MVVM vs MVC vs MVP 的区别？
- Q: ViewModel 为什么能在配置变更时存活？
- Q: LiveData 的生命周期感知是如何实现的？

**预计时间**: 10-14小时

---

#### 任务 2.3：集成 Hilt 依赖注入 🔥 高级

**为什么需要 DI？**
- 当前的单例模式代码耦合严重
- 难以编写单元测试（无法 mock）
- Hilt 是 Android 官方推荐的 DI 框架

**学习任务**:
1. 理解依赖注入的概念和好处
2. 学习 Hilt 的基础使用
3. 了解 @Inject、@Module、@Provides 注解

**实现挑战**:
- 如何注入 IAdminService？（它是通过 bindService 获取的）
- 如何处理 Service 连接的异步性？
- 如何在不同的 scope 中管理依赖？

**建议方案**:
1. 创建 ServiceModule 提供 IAdminService
2. 使用 @Singleton 或自定义 scope
3. 封装 Service 连接逻辑到 ServiceConnectionManager

**面试问答**:
- Q: 什么是依赖注入？为什么需要它？
- Q: Hilt vs Dagger 的区别？
- Q: @Singleton 和 @ActivityScoped 的区别？

**预计时间**: 12-16小时

---

### ⭐⭐⭐ 阶段 3：质量提升（1-2周）- 专业性展示

#### 任务 3.1：编写单元测试

**为什么重要？**
- 展示你的工程能力
- 保证代码质量
- 支持重构
- **面试加分项**

**学习任务**:
1. 学习 JUnit 5 基础
2. 学习 Mockito 框架
3. 了解 Android 测试框架（AndroidX Test）

**测试重点**:
1. Repository 层测试（核心业务逻辑）
2. ViewModel 层测试（UI 逻辑）
3. DAO 层测试（数据库操作）

**测试覆盖目标**:
- Repository: 80%+
- ViewModel: 70%+
- 整体: 60%+

**面试问答**:
- Q: 单元测试 vs 集成测试的区别？
- Q: 如何测试异步代码？
- Q: 什么是 Test Double？Mock vs Stub？

**预计时间**: 10-15小时

---

#### 任务 3.2：改进 AIDL 安全性

**当前问题**:
- AdminService 虽然有签名权限保护，但没有验证调用者
- 任何拥有权限的应用都可以调用

**学习任务**:
1. 理解 Android 权限系统
2. 学习 Binder 机制
3. 了解包名和签名验证

**实现提示**:
- 在 AdminApiImpl 的每个方法中添加权限检查
- 使用 Binder.getCallingUid() 获取调用者
- 验证调用者的包名和签名

**预计时间**: 6-8小时

---

## 🎯 面试准备：如何展示这个项目

### 项目介绍模板

```
"这是我用来学习 Android 核心技术的一个项目，实现了一个客户端-服务端
架构的用户管理系统。

项目的核心亮点是：
1. 使用 AIDL 实现了跨进程通信
2. 采用了 MVVM 架构 + Repository 模式
3. 使用 Room 进行数据持久化
4. 集成了 Hilt 依赖注入
5. 单元测试覆盖率达到 60%+

最有挑战的部分是 [选择一个任务，比如 AIDL 安全性]，我通过 [你的解决方案]
解决了这个问题。这让我深入理解了 [相关知识点]。"
```

### 每个任务完成后问自己

1. **Why?** 为什么要做这个改进？
2. **What?** 具体做了什么？
3. **How?** 如何实现的？遇到了什么困难？
4. **Result?** 达到了什么效果？学到了什么？

### 技术栈展示（简历用）

**改进前:**
```
- Java
- SQLite
- AIDL
- Service
- 基础 MVVM
```

**改进后:**
```
- Java / Kotlin (可选)
- Room + LiveData
- AIDL + Binder 安全
- Bound Service + MVVM
- Hilt (依赖注入)
- JUnit + Mockito (单元测试)
- Material Design
```

---

## 📚 推荐学习资源

### 官方文档（最权威）
1. Android Developers: https://developer.android.com/
2. Jetpack 组件: https://developer.android.com/jetpack
3. 架构指南: https://developer.android.com/topic/architecture

### 优质教程
1. Google Codelabs（边学边做）
2. 《Android 开发艺术探索》（深入理解原理）
3. Medium Android 专栏（最新趋势）

### 开源项目参考
1. Google Architecture Samples（官方架构示例）
2. Sunflower（Google 示例 App）

---

## ✅ 学习检查点

### 每完成一个任务后

- [ ] 代码是否按预期工作？
- [ ] 是否理解了背后的原理？
- [ ] 能否向别人解释清楚？
- [ ] 代码是否优雅、可维护？
- [ ] 是否编写了测试？

### 每完成一个阶段后

- [ ] 在 GitHub 上创建一个 tag（如 v1.0-stage1-basic）
- [ ] 写一个简短的总结文档
- [ ] 准备这个阶段的面试问答
- [ ] 实际运行测试，确保没有回退

---

## 🆘 遇到问题时

### 推荐的解决流程

1. **先自己查资料**（30分钟 - 1小时）
   - 阅读官方文档
   - 搜索 Stack Overflow
   - 查看开源项目实现

2. **尝试解决**（1-2小时）
   - 写代码尝试
   - 运行和调试
   - 记录遇到的错误

3. **寻求指导**（如果卡住了）
   - 告诉我你尝试了什么
   - 遇到了什么具体问题
   - 我会给你提示而不是直接答案

4. **总结学习**
   - 理解为什么这样解决
   - 记录到笔记中
   - 思考还有没有其他方案

---

## 📊 进度追踪

### 阶段 1：基础改进
- [ ] 任务 1.1：修复内存泄漏
- [ ] 任务 1.2：数据库线程安全
- [ ] 任务 1.3：完善异常处理

### 阶段 2：架构升级
- [ ] 任务 2.1：迁移到 Room
- [ ] 任务 2.2：完善 MVVM
- [ ] 任务 2.3：集成 Hilt

### 阶段 3：质量提升
- [ ] 任务 3.1：编写单元测试
- [ ] 任务 3.2：改进 AIDL 安全性

---

## 🎉 完成后的收获

技术方面：
- ✅ 掌握 Android 核心组件和架构模式
- ✅ 理解现代 Android 开发最佳实践
- ✅ 具备编写高质量代码的能力
- ✅ 能够独立解决复杂技术问题

面试方面：
- ✅ 有一个完整的项目可以讲述
- ✅ 能够展示"before & after"的对比
- ✅ 证明了学习能力和问题解决能力
- ✅ 展示了代码质量意识

---

## 💪 开始行动

**第一步：从任务 1.1 开始**
- 这是最简单的任务，可以快速建立信心
- 涉及的知识点（内存泄漏）是面试高频考点
- 预计 2-4 小时完成

**当你完成任务 1.1 后：**
- 告诉我你的实现方案
- 我会帮你 review
- 然后给你下一步的建议

**记住：**
- 不要追求完美，先完成再优化
- 每个任务都是一个学习机会
- 遇到困难很正常，关键是坚持

祝你学习顺利！💪
