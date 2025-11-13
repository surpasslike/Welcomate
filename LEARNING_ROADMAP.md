# Welcomate 项目改进学习路线图（2025-11-13 更新版）

**基于最新代码分析 - 适合面试准备和自主学习**

---

## 📊 当前项目状态

根据最新的代码分析，你的项目现状：

| 维度 | 评分 | 说明 |
|------|------|------|
| 安全性 | 5/10 | SHA-256 无盐值，AIDL 无权限验证 |
| 架构 | 6/10 | 部分使用 MVVM，缺少 DI |
| 代码质量 | 6/10 | 异常处理不完整，有资源泄漏 |
| 线程安全 | 4/10 | 数据库无同步，AIDL 并发问题 |
| 测试覆盖 | 0/10 | 完全没有测试 |
| **整体** | **5.0/10** | **中等偏低** |

**技术栈现状**：
- ✅ 已有：ViewBinding、DataBinding、部分 ViewModel、SQLite、AIDL、EventBus
- ❌ 缺失：Room、LiveData、Hilt、Navigation、Coroutines、测试框架

---

## 🎯 学习目标和时间规划

**总时间**: 6-8 周（每周投入 15-20 小时）

**学习成果**：
1. 掌握 Android 核心架构模式（MVVM + Repository + Room）
2. 理解线程安全和内存管理
3. 学会编写可测试的代码
4. 准备好一个可以在面试中展示的项目

---

## 📅 三阶段学习计划

### ⭐ 阶段 1：修复关键问题（第 1-2 周）

**目标**: 修复高风险问题，提升安全性和稳定性
**预计时间**: 15-20 小时
**学习重点**: 安全、线程安全、内存管理

---

#### 任务 1.1：修复密码哈希安全问题 🔥 最重要

**优先级**: P0 - 立即修复
**预计时间**: 3-4 小时

**当前问题**：
📍 位置: `app-server/src/main/java/com/surpasslike/welcomateservice/data/UserRepository.java:62-74`

```java
private String hashPassword(String password) {
    // ❌ 问题：SHA-256 直接哈希，无盐值
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
    return Base64.encodeToString(hash, Base64.NO_WRAP);
}
```

**为什么这是严重问题？**
1. **相同密码 → 相同哈希值**
   - 用户 A 和用户 B 如果密码都是 "123456"，哈希值完全相同
   - 攻击者只需破解一个，就能知道所有相同密码

2. **易受彩虹表攻击**
   - 彩虹表预先计算了常见密码的 SHA-256 值
   - 查表即可破解，无需暴力破解

3. **SHA-256 计算速度太快**
   - 现代显卡每秒可以计算数十亿次 SHA-256
   - 暴力破解很容易

**学习任务**：
1. 研究**盐值（Salt）**的概念和作用
2. 了解 **PBKDF2**（Password-Based Key Derivation Function 2）
3. 理解为什么需要**迭代次数**（iterations）
4. 学习如何**安全存储**盐值和哈希值

**实现方向**（不是完整答案，需要你自己查资料）：

**方案选择**：
- 推荐：PBKDF2（Java 内置，无需额外库）
- 备选：bcrypt（需要引入第三方库，更安全但复杂）

**关键点思考**：
1. 盐值应该存储在哪里？（提示：可以和哈希值一起存在数据库）
2. 每个用户的盐值应该一样还是不一样？（提示：每个用户独立）
3. 迭代次数设置多少？（提示：2024 年推荐 100,000+ 次）
4. 如何验证密码？（提示：用相同盐值和迭代次数重新哈希，比较结果）

**搜索关键词**：
- "Java PBKDF2 password hashing example"
- "SecretKeyFactory PBKDF2WithHmacSHA256"
- "Android password hashing best practices 2024"

**需要修改的方法**：
```java
// UserRepository.java
private String hashPassword(String password)  // 改进此方法
private boolean verifyPassword(String inputPassword, String storedHash)  // 新增此方法
```

**验证方法**：
1. 注册两个用户，使用相同密码
2. 查看数据库，哈希值应该不同
3. 登录应该能正常工作
4. 尝试用错误密码登录，应该失败

**面试问答准备**：
- Q: 为什么需要盐值？
- Q: SHA-256 和 PBKDF2 的区别？
- Q: 如何选择合适的迭代次数？
- Q: bcrypt vs PBKDF2 vs Argon2？

**注意事项**：
- ⚠️ 修改哈希算法后，旧密码将无法验证（需要数据迁移策略）
- 💡 可以先实现新算法，然后清空数据库测试
- 📝 记录你的设计决策，面试时可以讲

---

#### 任务 1.2：移除静态 Service 引用（内存泄漏）

**优先级**: P0 - 立即修复
**预计时间**: 3-4 小时

**当前问题**：
📍 位置: `app-client/src/main/java/com/surpasslike/welcomate/activity/MainActivity.java:31`

```java
public class MainActivity extends AppCompatActivity {
    private static IAdminService mAdminService; // ❌ 静态引用

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAdminService = IAdminService.Stub.asInterface(service);
            // ...
        }
    };
}
```

**为什么会泄漏？**
1. `static` 变量的生命周期 = 应用进程生命周期
2. `mAdminService` 持有 Binder 引用
3. Binder 可能间接持有 Service、Context 等对象
4. 即使 MainActivity 销毁，引用仍然存在
5. 导致 Activity 无法被 GC 回收

**如何验证确实泄漏？**（不需要实现，但可以了解）
```bash
# 方法 1：使用 Android Studio Profiler
1. 打开 Profiler → Memory
2. 旋转屏幕多次（触发 Activity 重建）
3. 手动触发 GC
4. 查看 MainActivity 实例数量（应该只有 1 个，如果有多个就是泄漏）

# 方法 2：集成 LeakCanary（可选）
dependencies {
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}
```

**学习任务**：
1. 理解 Java 的 **static 关键字**和内存模型
2. 学习 Android 的 **Activity 生命周期**
3. 了解 **内存泄漏**的常见模式
4. 研究 **ServiceLocator 模式**或 **Application 单例**

**实现方向**（三种方案，选一种）：

**方案 A：移除 static，使用实例变量**
```java
// 最简单，但其他 Activity 无法共享 Service
private IAdminService mAdminService; // 移除 static
```
- 优点：简单直接
- 缺点：每个 Activity 需要单独绑定 Service
- 适合：只有一个 Activity 使用 Service

**方案 B：使用 Application 单例**
```java
// 创建 MyApplication.java（如果还没有）
public class MyApplication extends Application {
    private IAdminService adminService;

    public IAdminService getAdminService() { return adminService; }
    public void setAdminService(IAdminService service) { this.adminService = service; }
}

// MainActivity.java
MyApplication app = (MyApplication) getApplication();
IAdminService service = app.getAdminService();
```
- 优点：所有 Activity 可以共享
- 缺点：Service 生命周期和 Application 绑定
- 适合：多个 Activity 使用同一个 Service

**方案 C：ServiceLocator 模式（推荐）**
```java
// 创建 ServiceLocator 单例
public class ServiceLocator {
    private static ServiceLocator instance;
    private IAdminService adminService;

    // 提示：这里需要考虑线程安全
    public static ServiceLocator getInstance() { /* ... */ }

    public IAdminService getAdminService() { return adminService; }
    public void setAdminService(IAdminService service) { /* ... */ }
    public void clearService() { this.adminService = null; }
}
```
- 优点：集中管理，可以添加状态监听
- 缺点：比方案 A/B 复杂一些
- 适合：需要灵活管理 Service 生命周期

**关键设计问题**（需要你思考）：
1. Service 应该在什么时候绑定？什么时候解绑？
2. 如果 Service 断开连接，如何通知所有使用者？
3. 如何避免 ServiceLocator 本身也泄漏？
4. 需要线程同步吗？

**搜索关键词**：
- "Android static field memory leak"
- "Android ServiceConnection memory leak"
- "ServiceLocator pattern Android"

**验证方法**：
1. 旋转屏幕多次
2. 使用 Profiler 检查内存
3. 确保只有一个 MainActivity 实例

**面试问答准备**：
- Q: static 为什么会导致内存泄漏？
- Q: 如何检测和修复内存泄漏？
- Q: Activity、Service、Application 的生命周期关系？
- Q: WeakReference 能解决这个问题吗？

---

#### 任务 1.3：数据库线程安全

**优先级**: P0 - 立即修复
**预计时间**: 3-4 小时

**当前问题**：
📍 位置: `app-server/src/main/java/com/surpasslike/welcomateservice/data/db/DatabaseHelper.java`

**问题描述**：
1. **AIDL 方法在 Binder 线程执行**
   - AdminApiImpl 的方法不在主线程
   - 多个客户端可能同时调用

2. **UserRepository 不是线程安全的**
   - 多个线程可能同时访问数据库
   - SQLiteDatabase 默认不支持多线程写入

3. **DatabaseHelper 单例实现有问题**
   ```java
   private static DatabaseHelper instance;

   public static synchronized DatabaseHelper getInstance(Context context) {
       if (instance == null) {
           instance = new DatabaseHelper(context.getApplicationContext());
       }
       return instance;
   }
   ```
   - `synchronized` 在方法级别，但 `instance == null` 检查在锁外（虽然这里问题不大）
   - 每次调用都要获取锁，性能较差

**为什么会出问题？**
```
客户端 A 调用 registerUser()  →  Binder 线程 1  →  SQLite 写
                                                      ↓
客户端 B 同时调用 deleteUser() →  Binder 线程 2  →  SQLite 写  → 冲突！
```

**学习任务**：
1. 理解 **SQLite 的并发模型**
2. 学习 **WAL（Write-Ahead Logging）模式**
3. 掌握 **双重检查锁定（DCL）**单例模式
4. 了解 **synchronized 关键字**的使用

**实现方向**：

**步骤 1：启用 WAL 模式**
```java
// DatabaseHelper.java
@Override
public void onConfigure(SQLiteDatabase db) {
    super.onConfigure(db);
    // 提示：查阅 SQLiteDatabase 文档，寻找启用 WAL 的方法
    // 关键词：setWriteAheadLoggingEnabled
}
```

**WAL 的好处**：
- 读写可以并发进行
- 提高并发性能
- 减少 database locked 错误

**步骤 2：改进 DatabaseHelper 单例**
```java
// 双重检查锁定（DCL）模式
private static volatile DatabaseHelper instance; // 注意 volatile

public static DatabaseHelper getInstance(Context context) {
    if (instance == null) {                      // 第一次检查（无锁）
        synchronized (DatabaseHelper.class) {    // 加锁
            if (instance == null) {               // 第二次检查（有锁）
                instance = new DatabaseHelper(context.getApplicationContext());
            }
        }
    }
    return instance;
}
```

**为什么需要 volatile？**
- 防止指令重排序
- 保证可见性
- 这是面试高频考点！

**步骤 3：保护关键的数据库操作**
```java
// UserRepository.java
public synchronized boolean createUser(User user) {
    // 整个方法加锁，保证原子性
}

// 或者使用更细粒度的锁
private final Object dbLock = new Object();

public boolean createUser(User user) {
    synchronized (dbLock) {
        // 只锁住数据库操作
    }
}
```

**关键设计问题**：
1. 是否需要对所有方法都加锁？
2. 读操作需要加锁吗？
3. 锁的粒度应该多大？（方法级 vs 语句块级）
4. 使用 `synchronized` 还是 `ReentrantLock`？

**搜索关键词**：
- "SQLite WAL mode Android"
- "Java double-checked locking volatile"
- "SQLiteDatabase thread safety"
- "Android database concurrency"

**验证方法**：
```java
// 创建测试：模拟并发写入
for (int i = 0; i < 10; i++) {
    new Thread(() -> {
        // 同时创建用户
        repository.createUser(new User("user" + i, "pass"));
    }).start();
}
// 检查：所有用户都应该成功创建，没有 database locked 异常
```

**面试问答准备**：
- Q: SQLite 支持并发吗？
- Q: WAL 模式的原理和好处？
- Q: 双重检查锁定为什么需要 volatile？
- Q: synchronized 和 Lock 的区别？

---

#### 任务 1.4：添加 AIDL 权限验证

**优先级**: P0 - 立即修复
**预计时间**: 4-5 小时

**当前问题**：
📍 位置: `app-server/src/main/java/com/surpasslike/welcomateservice/aidl/AdminApiImpl.java`

```java
public class AdminApiImpl extends IAdminService.Stub {
    @Override
    public boolean loginAdmin(String username, String password) {
        // ❌ 直接执行，没有检查调用者身份
        return userRepository.loginAdmin(username, password);
    }
}
```

**虽然你定义了签名权限**：
```xml
<!-- AndroidManifest.xml -->
<permission android:name="com.surpasslike.welcomateservice.permission.ADMIN_SERVICE"
            android:protectionLevel="signature" />
```

**但这还不够！**
- 签名权限只保证了"相同签名的应用可以获取权限"
- 但没有验证"谁在调用这个方法"
- 恶意应用可以尝试：
  1. 反编译你的 APK 获取签名
  2. 使用相同签名发布恶意应用
  3. 调用你的 Service 删除所有用户

**学习任务**：
1. 理解 **Android 权限系统**
2. 学习 **Binder 机制**和 IPC
3. 了解 **PackageManager** 的使用
4. 研究如何验证**调用者身份**

**实现方向**：

**步骤 1：获取调用者信息**
```java
public boolean deleteUser(String username) {
    // 获取调用者的 UID
    int callingUid = Binder.getCallingUid();

    // 获取调用者的包名
    String callerPackage = getPackageNameFromUid(callingUid);

    // 验证调用者
    if (!isAuthorizedCaller(callerPackage)) {
        Log.w(TAG, "Unauthorized call from: " + callerPackage);
        return false;
    }

    // 继续执行...
}
```

**步骤 2：实现包名白名单**
```java
private static final String[] AUTHORIZED_PACKAGES = {
    "com.surpasslike.welcomate",  // 你的客户端应用
    // 可以添加更多授权应用
};

private boolean isAuthorizedCaller(String packageName) {
    for (String authorized : AUTHORIZED_PACKAGES) {
        if (authorized.equals(packageName)) {
            return true;
        }
    }
    return false;
}
```

**步骤 3：验证签名（可选，更安全）**
```java
private boolean verifySignature(String packageName) {
    try {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(packageName,
            PackageManager.GET_SIGNATURES);

        // 比较签名
        // 提示：你需要预先存储你的应用签名
        // ...
    } catch (PackageManager.NameNotFoundException e) {
        return false;
    }
}
```

**步骤 4：添加操作日志**
```java
private void logOperation(String operation, String caller, boolean success) {
    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(new Date());

    Log.i(TAG, String.format("[%s] %s by %s: %s",
        timestamp, operation, caller, success ? "SUCCESS" : "FAILED"));

    // 可选：存储到数据库，用于审计
}
```

**关键设计问题**：
1. 是否需要对所有 AIDL 方法都验证？
2. 验证失败应该抛出异常还是返回 false？
3. 是否需要限制调用频率（防止暴力破解）？
4. 日志应该包含哪些信息？

**搜索关键词**：
- "Android Binder.getCallingUid example"
- "AIDL permission check"
- "Android signature verification"
- "PackageManager get calling package"

**辅助工具方法**：
```java
private String getPackageNameFromUid(int uid) {
    PackageManager pm = context.getPackageManager();
    String[] packages = pm.getPackagesForUid(uid);
    return (packages != null && packages.length > 0) ? packages[0] : "unknown";
}
```

**验证方法**：
1. 从你的客户端调用 → 应该成功
2. 修改白名单，移除客户端包名 → 应该失败
3. 查看 Logcat，确认日志记录正确

**面试问答准备**：
- Q: Android 权限的保护级别有哪些？
- Q: Binder IPC 的原理是什么？
- Q: 如何防止 AIDL 接口被恶意调用？
- Q: signature 权限和 normal 权限的区别？

---

### 阶段 1 总结

完成这 4 个任务后，你将：
- ✅ 修复了所有高风险安全问题
- ✅ 理解了密码学基础（哈希、盐值、PBKDF2）
- ✅ 掌握了内存管理（static 引用、内存泄漏）
- ✅ 学会了线程安全（并发、同步、DCL）
- ✅ 了解了 Android 权限和 Binder 机制

**面试亮点**：
- "我发现项目有内存泄漏问题，通过 XXX 方法修复了"
- "原来的密码存储不安全，我升级到了 PBKDF2"
- "为了支持并发访问，我实现了数据库的线程安全"

---

## ⭐⭐ 阶段 2：架构现代化（第 3-5 周）

**目标**: 迁移到现代 Android 架构
**预计时间**: 40-50 小时
**学习重点**: Room、MVVM、LiveData

---

#### 任务 2.1：迁移到 Room ORM 🔥 核心任务

**优先级**: P1 - 高优先级
**预计时间**: 12-16 小时

**为什么要做这个？**
1. **Room 是 Google 官方推荐的数据库方案**（面试必问）
2. **编译时 SQL 验证** - 减少运行时错误
3. **自动线程管理** - 不用手动处理线程安全
4. **更简洁的代码** - 不用写大量的 Cursor 处理代码
5. **支持 LiveData/Flow** - 自动更新 UI

**当前 vs 目标**：

**当前**（原始 SQLite）：
```java
// 查询所有用户 - 需要 30+ 行代码
public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = null;
    try {
        cursor = db.query(TABLE_USERS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            // ... 更多列

            do {
                User user = new User();
                user.setId(cursor.getInt(idIndex));
                user.setUsername(cursor.getString(usernameIndex));
                // ... 更多字段
                users.add(user);
            } while (cursor.moveToNext());
        }
    } finally {
        if (cursor != null) cursor.close();
    }
    return users;
}
```

**目标**（Room）：
```java
// 只需要 2 行注解！
@Query("SELECT * FROM users")
List<User> getAllUsers();

// 或者响应式：
@Query("SELECT * FROM users")
LiveData<List<User>> getAllUsers();
```

**学习任务**：
1. 理解 **ORM（对象关系映射）**的概念
2. 学习 **Room 的三大组件**：Entity、DAO、Database
3. 掌握常用的 **Room 注解**
4. 了解 **数据库迁移**策略

**实现步骤**：

**第一步：添加依赖**

📍 文件: `app-server/build.gradle`

```gradle
dependencies {
    // Room 组件
    def room_version = "2.6.1"
    implementation "androidx.room:room-runtime:$room_version"
    annotationProcessor "androidx.room:room-compiler:$room_version"

    // Room 测试
    testImplementation "androidx.room:room-testing:$room_version"

    // 可选：Room + Kotlin Coroutines（如果你想用协程）
    // implementation "androidx.room:room-ktx:$room_version"
}
```

**第二步：将 User.java 改造为 Room Entity**

📍 文件: `app-server/src/main/java/com/surpasslike/welcomateservice/data/model/User.java`

当前代码：
```java
public class User {
    private int id;
    private String username;
    private String password;
    // getters and setters
}
```

需要添加注解（具体怎么加，你需要查资料）：
```java
// 提示：需要 @Entity 注解
// 提示：需要定义主键 @PrimaryKey
// 提示：可以指定表名 @Entity(tableName = "users")
```

**关键问题思考**：
1. 主键应该是 `id` 还是 `username`？
2. 需要 `autoGenerate = true` 吗？
3. 密码字段需要 `@ColumnInfo` 注解吗？
4. 需要定义索引吗？（提示：username 应该建索引）

**第三步：创建 UserDao 接口**

📍 新建文件: `app-server/src/main/java/com/surpasslike/welcomateservice/data/db/UserDao.java`

```java
// 提示：需要 @Dao 注解

public interface UserDao {
    // 插入用户
    // 提示：@Insert 注解，考虑冲突策略

    // 更新用户
    // 提示：@Update 注解

    // 删除用户
    // 提示：@Delete 注解，还是 @Query 删除？

    // 查询所有用户
    // 提示：@Query("SELECT * FROM users")

    // 根据用户名查询
    // 提示：@Query("SELECT * FROM users WHERE username = :username")

    // 检查用户名是否存在
    // 提示：返回 int 或 boolean？
}
```

**CRUD 操作映射**：

| 原 DatabaseHelper 方法 | 对应的 Room DAO 方法 |
|------------------------|---------------------|
| `insertUser(User)` | `@Insert long insert(User user)` |
| `updateUser(User)` | `@Update int update(User user)` |
| `deleteUserByUsername(String)` | `@Query("DELETE FROM users WHERE username = :username")` |
| `getUserByUsername(String)` | `@Query("SELECT * FROM users WHERE username = :username") User getUser(String username)` |
| `getAllUsers()` | `@Query("SELECT * FROM users") List<User> getAllUsers()` |
| `getUserCount()` | `@Query("SELECT COUNT(*) FROM users") int getUserCount()` |

**第四步：创建 AppDatabase**

📍 新建文件: `app-server/src/main/java/com/surpasslike/welcomateservice/data/db/AppDatabase.java`

```java
// 提示：@Database 注解
// 提示：需要继承 RoomDatabase
// 提示：定义 entities 和 version

public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    // 单例实现（线程安全）
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        // 提示：使用双重检查锁定
        // 提示：使用 Room.databaseBuilder()
        // 提示：考虑是否需要 .fallbackToDestructiveMigration()
    }
}
```

**第五步：重构 UserRepository**

📍 文件: `app-server/src/main/java/com/surpasslike/welcomateservice/data/UserRepository.java`

当前代码使用 `DatabaseHelper`，需要改为使用 `UserDao`：

```java
public class UserRepository {
    private final UserDao userDao;

    private UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
    }

    // 重构所有方法使用 userDao 而不是 DatabaseHelper
    public boolean createUser(User user) {
        // 原来：dbHelper.insertUser(...)
        // 现在：userDao.insert(...)

        // 注意：Room 不允许在主线程操作数据库
        // 需要使用 Executors 或 Thread
    }
}
```

**关键挑战 - 线程管理**：

Room 默认禁止主线程数据库操作，你需要：

**方案 A：使用 Executor**（推荐）
```java
private final ExecutorService executorService = Executors.newSingleThreadExecutor();

public void createUser(User user, Callback callback) {
    executorService.execute(() -> {
        try {
            long id = userDao.insert(user);
            callback.onSuccess(id);
        } catch (Exception e) {
            callback.onError(e);
        }
    });
}

interface Callback {
    void onSuccess(long id);
    void onError(Exception e);
}
```

**方案 B：同步方法 + 调用者处理线程**
```java
public boolean createUser(User user) {
    // AIDL 方法本身就在 Binder 线程，可以直接调用
    try {
        userDao.insert(user);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

**哪种方案更好？**
- 如果只在 AIDL 中使用 → 方案 B 更简单
- 如果在 Activity 中也使用 → 方案 A 更安全

**第六步：数据迁移**

由于你改变了数据库结构，需要考虑：

**选项 1：清空数据库**（简单，适合学习项目）
```java
.fallbackToDestructiveMigration()  // 版本变化时清空数据
```

**选项 2：编写迁移脚本**（生产环境）
```java
static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // SQL 迁移语句
    }
};
```

**搜索关键词**：
- "Android Room tutorial"
- "Room DAO query examples"
- "Room database migration"
- "Room thread safety"

**验证清单**：
- [ ] 用户注册功能正常
- [ ] 用户登录功能正常
- [ ] 管理员可以查看用户列表
- [ ] 管理员可以删除用户
- [ ] 管理员可以修改密码
- [ ] 所有操作没有 "Cannot access database on main thread" 错误

**常见错误排查**：
1. **编译错误：找不到生成的类**
   - 确保已经添加 `annotationProcessor` 依赖
   - Clean Project → Rebuild Project

2. **运行时错误：IllegalStateException: Cannot access database on the main thread**
   - 检查调用栈，确认是在哪里调用的
   - 确保 AIDL 方法确实在 Binder 线程（可以打印 `Thread.currentThread().getName()`）

3. **数据库为空**
   - 检查数据库文件位置是否改变
   - 使用 Device File Explorer 查看 `/data/data/com.surpasslike.welcomateservice/databases/`

**面试问答准备**：
- Q: Room 的三大组件是什么？
- Q: Room 如何保证线程安全？
- Q: @Insert 的冲突策略有哪些？
- Q: Room 和原始 SQLite 的对比？
- Q: 如何进行数据库版本迁移？

**完成后的收获**：
- ✅ 掌握了 Room ORM 的使用
- ✅ 理解了 ORM 的优势
- ✅ 学会了注解的使用
- ✅ 代码量减少约 40%

---

#### 任务 2.2：完善 MVVM - 添加 LiveData

**优先级**: P1 - 高优先级
**预计时间**: 8-10 小时

**当前状态分析**：

你的项目**部分使用了 MVVM**：
- ✅ `AdminDashboardActivity` + `AdminViewModel` ✓ 已实现
- ❌ `app-client` 的所有 Activity 都没有 ViewModel
- ❌ 没有使用 LiveData，数据变化无法自动更新 UI

**为什么需要 LiveData？**

**当前的问题**：
```java
// AdminDashboardActivity.java
private void loadUsers() {
    List<User> users = viewModel.getAllUsers();  // 直接获取数据
    adapter.setUserList(users);  // 手动更新
}

// 如果数据变化了？需要手动再次调用 loadUsers()
// 如果 Activity 在后台？可能导致错误
// 如果数据加载失败？没有统一的错误处理
```

**使用 LiveData 后**：
```java
// ViewModel
private MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();

public LiveData<List<User>> getUsers() {
    return usersLiveData;
}

// Activity
viewModel.getUsers().observe(this, users -> {
    // 数据变化时自动调用
    adapter.setUserList(users);
});
```

**LiveData 的好处**：
1. **生命周期感知** - Activity 在后台时不会更新（避免崩溃）
2. **自动更新** - 数据变化自动通知 UI
3. **避免内存泄漏** - 自动清理观察者
4. **配置变更安全** - 屏幕旋转后自动恢复数据

**学习任务**：
1. 理解 **LiveData** 的原理和生命周期
2. 学习 **MutableLiveData** 的使用
3. 掌握 **Observer 模式**
4. 了解 **ViewModel + LiveData** 的最佳实践

**实现步骤**：

**第一步：为 app-client 创建 ViewModels**

📍 新建文件: `app-client/src/main/java/com/surpasslike/welcomate/viewmodel/LoginViewModel.java`

```java
public class LoginViewModel extends ViewModel {
    // 登录状态
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    // 暴露只读的 LiveData
    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    // 执行登录
    public void login(String username, String password) {
        // 提示：这里需要调用 AIDL
        // 提示：需要在后台线程执行
        // 提示：结果通过 loginState.postValue() 通知 UI
    }
}

// 定义登录状态
class LoginState {
    public static class Loading extends LoginState {}
    public static class Success extends LoginState {
        public final String username;
        public Success(String username) { this.username = username; }
    }
    public static class Error extends LoginState {
        public final String message;
        public Error(String message) { this.message = message; }
    }
}
```

**第二步：在 LoginActivity 中使用**

📍 文件: `app-client/src/main/java/com/surpasslike/welcomate/activity/LoginActivity.java`

当前代码（需要重构）：
```java
// 当前：直接在 Activity 中调用 AIDL
private void performLogin(String username, String password) {
    try {
        boolean success = mAdminService.loginAdmin(username, password);
        if (success) {
            // 跳转
        } else {
            ToastUtils.showShort(this, "登录失败");
        }
    } catch (RemoteException e) {
        // ...
    }
}
```

重构后：
```java
private LoginViewModel viewModel;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // 获取 ViewModel
    viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

    // 观察登录状态
    viewModel.getLoginState().observe(this, state -> {
        if (state instanceof LoginState.Loading) {
            showLoading();
        } else if (state instanceof LoginState.Success) {
            hideLoading();
            navigateToHome(((LoginState.Success) state).username);
        } else if (state instanceof LoginState.Error) {
            hideLoading();
            showError(((LoginState.Error) state).message);
        }
    });
}

private void performLogin(String username, String password) {
    // 只需要调用 ViewModel，不用关心细节
    viewModel.login(username, password);
}
```

**第三步：改进 AdminViewModel**

📍 文件: `app-server/src/main/java/com/surpasslike/welcomateservice/ui/admin/AdminViewModel.java`

当前代码：
```java
public List<User> getAllUsers() {
    return userRepository.getAllUsers();  // 同步返回
}
```

改进为：
```java
private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();

public LiveData<List<User>> getUsers() {
    return usersLiveData;
}

public void loadUsers() {
    // 在后台线程加载
    executorService.execute(() -> {
        List<User> users = userRepository.getAllUsers();
        usersLiveData.postValue(users);  // 线程安全的方式更新
    });
}
```

**第四步：处理 AIDL 的异步性**

**挑战**：ViewModel 需要调用 IAdminService，但它是异步绑定的

**方案 A：传入 Service 引用**
```java
public class LoginViewModel extends ViewModel {
    private IAdminService adminService;

    public void setAdminService(IAdminService service) {
        this.adminService = service;
    }

    public void login(String username, String password) {
        if (adminService == null) {
            loginState.setValue(new LoginState.Error("Service not ready"));
            return;
        }
        // ...
    }
}

// Activity
viewModel.setAdminService(mAdminService);
```

**方案 B：使用 Repository 封装**（更好）
```java
// 创建 AuthRepository 封装 AIDL 调用
public class AuthRepository {
    private IAdminService adminService;

    public void login(String username, String password, Callback callback) {
        executorService.execute(() -> {
            try {
                boolean success = adminService.loginAdmin(username, password);
                callback.onSuccess(success);
            } catch (RemoteException e) {
                callback.onError(e);
            }
        });
    }
}

// ViewModel 使用 Repository
public class LoginViewModel extends ViewModel {
    private final AuthRepository repository;

    public void login(String username, String password) {
        loginState.setValue(new LoginState.Loading());
        repository.login(username, password, new Callback() {
            @Override
            public void onSuccess(boolean success) {
                if (success) {
                    loginState.postValue(new LoginState.Success(username));
                } else {
                    loginState.postValue(new LoginState.Error("Invalid credentials"));
                }
            }

            @Override
            public void onError(Exception e) {
                loginState.postValue(new LoginState.Error(e.getMessage()));
            }
        });
    }
}
```

**关键设计问题**：
1. 每个 Activity 都需要 ViewModel 吗？
2. ViewModel 应该持有 Activity 的引用吗？（提示：不应该！）
3. LiveData 和 MutableLiveData 的区别？
4. 什么时候用 `setValue()` 什么时候用 `postValue()`？

**搜索关键词**：
- "Android LiveData tutorial"
- "ViewModel LiveData best practices"
- "Android MVVM architecture"
- "LiveData vs MutableLiveData"

**验证清单**：
- [ ] 登录时显示加载状态
- [ ] 登录成功/失败有明确反馈
- [ ] 旋转屏幕，数据不丢失
- [ ] Activity 在后台时，LiveData 不更新

**面试问答准备**：
- Q: LiveData 如何实现生命周期感知？
- Q: ViewModel 为什么不能持有 Activity 引用？
- Q: setValue 和 postValue 的区别？
- Q: MutableLiveData 和 LiveData 的关系？

---

#### 任务 2.3：统一异常处理

**优先级**: P1 - 中高优先级
**预计时间**: 4-6 小时

**当前问题**：

项目中的异常处理非常不统一：

```java
// 方式 1：返回 null
public User getUserByUsername(String username) {
    try {
        // ...
    } catch (Exception e) {
        e.printStackTrace();
        return null;  // 调用者无法知道失败原因
    }
}

// 方式 2：返回 boolean
public boolean createUser(User user) {
    try {
        // ...
        return true;
    } catch (Exception e) {
        return false;  // 也无法知道原因
    }
}

// 方式 3：RemoteException
public boolean loginAdmin(String username, String password) throws RemoteException {
    // 调用者需要捕获 RemoteException
}
```

**问题**：
- 无法区分失败原因（网络？数据库？业务逻辑？）
- 错误信息丢失
- 调用者不知道如何处理
- 用户看到的只是"操作失败"

**学习任务**：
1. 学习 **Result 模式**（函数式错误处理）
2. 理解 **Sealed Class**（Kotlin，可选）
3. 掌握**自定义异常**的设计
4. 了解**错误传播**策略

**实现方向**：

**方案 A：Result 包装类**（推荐）

创建通用的 Result 类：
```java
public class Result<T> {
    private final T data;
    private final String error;
    private final boolean success;

    private Result(T data, String error, boolean success) {
        this.data = data;
        this.error = error;
        this.success = success;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, true);
    }

    public static <T> Result<T> error(String error) {
        return new Result<>(null, error, false);
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getError() { return error; }
}
```

使用示例：
```java
// UserRepository
public Result<User> getUserByUsername(String username) {
    try {
        User user = userDao.getUser(username);
        if (user == null) {
            return Result.error("User not found");
        }
        return Result.success(user);
    } catch (Exception e) {
        return Result.error("Database error: " + e.getMessage());
    }
}

// ViewModel
public void login(String username, String password) {
    Result<User> result = repository.getUserByUsername(username);
    if (result.isSuccess()) {
        User user = result.getData();
        // 验证密码...
    } else {
        loginState.postValue(new LoginState.Error(result.getError()));
    }
}
```

**方案 B：自定义异常层次**

```java
// 基础异常
public class AppException extends Exception {
    public AppException(String message) {
        super(message);
    }
}

// 具体异常
public class UserNotFoundException extends AppException {
    public UserNotFoundException(String username) {
        super("User not found: " + username);
    }
}

public class DatabaseException extends AppException {
    public DatabaseException(Throwable cause) {
        super("Database error", cause);
    }
}

// 使用
public User getUserByUsername(String username) throws AppException {
    try {
        User user = userDao.getUser(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return user;
    } catch (SQLException e) {
        throw new DatabaseException(e);
    }
}
```

**哪种方案更好？**
- Result 模式：函数式风格，调用者必须处理错误
- 异常：Java 传统方式，可以向上传播

建议：**结合使用**
- Repository → ViewModel：使用 Result
- ViewModel → Activity：使用 LiveData<State>

**搜索关键词**：
- "Java Result pattern"
- "Android error handling best practices"
- "Custom exception hierarchy"

**面试问答准备**：
- Q: checked 和 unchecked 异常的区别？
- Q: 什么时候应该创建自定义异常？
- Q: Result 模式的优缺点？

---

### 阶段 2 总结

完成后你将掌握：
- ✅ Room ORM 的完整使用
- ✅ MVVM 架构的实战应用
- ✅ LiveData 响应式编程
- ✅ 统一的错误处理机制

**代码质量提升**：
- 代码量减少 30%
- 可维护性提升 50%
- 架构清晰度 +++

---

## ⭐⭐⭐ 阶段 3：质量保证（第 6-8 周）

**目标**: 测试、文档、最后的打磨
**预计时间**: 30-40 小时
**学习重点**: 单元测试、集成测试、依赖注入

---

#### 任务 3.1：编写单元测试

**优先级**: P2 - 中优先级
**预计时间**: 15-20 小时

**为什么需要测试？**
- 保证代码质量
- 支持重构
- 捕获回归错误
- **面试加分项**（很多公司看重测试能力）

**学习任务**：
1. 学习 **JUnit 5** 基础
2. 学习 **Mockito** 框架
3. 了解 **测试金字塔**
4. 掌握 **Android 测试框架**

**测试目标覆盖率**：
- UserRepository: 80%+
- ViewModel: 70%+
- DAO: 100%（Room 提供测试支持）
- 整体: 60%+

**实现步骤**：

**第一步：添加测试依赖**

```gradle
dependencies {
    // JUnit 5
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.3.1'

    // Android 测试
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'

    // Room 测试
    androidTestImplementation 'androidx.room:room-testing:2.6.1'

    // ViewModel 测试
    testImplementation 'androidx.arch.core:core-testing:2.2.0'
}
```

**第二步：测试 UserRepository**

```java
public class UserRepositoryTest {
    private UserRepository repository;
    private UserDao mockDao;

    @Before
    public void setup() {
        mockDao = mock(UserDao.class);
        // 如何注入 mockDao 到 repository？
        // 提示：需要修改 UserRepository，支持依赖注入
    }

    @Test
    public void createUser_success() {
        // 准备测试数据
        User user = new User("test", "password");
        when(mockDao.insert(any(User.class))).thenReturn(1L);

        // 执行
        boolean result = repository.createUser(user);

        // 验证
        assertTrue(result);
        verify(mockDao).insert(user);
    }

    @Test
    public void createUser_duplicateUsername_returnsFalse() {
        // 测试重复用户名的情况
        // 你来实现...
    }
}
```

**第三步：测试 ViewModel**

```java
public class LoginViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private LoginViewModel viewModel;
    private AuthRepository mockRepository;

    @Before
    public void setup() {
        mockRepository = mock(AuthRepository.class);
        viewModel = new LoginViewModel(mockRepository);
    }

    @Test
    public void login_success_updatesStateToSuccess() {
        // 模拟成功登录
        // ...
    }

    @Test
    public void login_invalidCredentials_updatesStateToError() {
        // 模拟登录失败
        // ...
    }
}
```

**关键挑战**：
1. 如何 mock IAdminService？
2. 如何测试异步操作？
3. 如何测试 LiveData？

**搜索关键词**：
- "JUnit Mockito tutorial"
- "Android ViewModel testing"
- "Room DAO testing"

**面试问答准备**：
- Q: 单元测试和集成测试的区别？
- Q: Mock、Stub、Spy 的区别？
- Q: 如何测试异步代码？

---

#### 任务 3.2：集成 Hilt 依赖注入（可选高级任务）

**优先级**: P2 - 低优先级（时间充裕再做）
**预计时间**: 12-15 小时

这是高级任务，如果时间不够可以跳过，但学会后对面试很有帮助。

**为什么需要 DI？**
- 当前所有依赖都是手动创建的（`new`、`getInstance()`）
- 难以编写单元测试（无法 mock）
- 组件之间紧耦合

**学习任务**：
1. 理解**依赖注入**的概念
2. 学习 **Hilt** 基础
3. 了解 **@Inject**、**@Module**、**@Provides**

**搜索关键词**：
- "Android Hilt tutorial"
- "Dependency injection explained"

---

## 📊 学习进度追踪

### 阶段 1：基础修复（第 1-2 周）
- [ ] 任务 1.1：密码哈希安全（3-4h）
- [ ] 任务 1.2：移除静态引用（3-4h）
- [ ] 任务 1.3：数据库线程安全（3-4h）
- [ ] 任务 1.4：AIDL 权限验证（4-5h）
- [ ] **阶段总结和 Review**（2h）

### 阶段 2：架构升级（第 3-5 周）
- [ ] 任务 2.1：迁移到 Room（12-16h）
- [ ] 任务 2.2：完善 MVVM + LiveData（8-10h）
- [ ] 任务 2.3：统一异常处理（4-6h）
- [ ] **阶段总结和 Review**（2h）

### 阶段 3：质量提升（第 6-8 周）
- [ ] 任务 3.1：编写单元测试（15-20h）
- [ ] 任务 3.2：集成 Hilt（可选）（12-15h）
- [ ] **最终总结和面试准备**（4h）

---

## 🎯 面试准备：项目展示话术

### 项目介绍（2 分钟）

```
"这是我用来学习和实践 Android 核心技术的一个项目，实现了一个
客户端-服务端架构的用户管理系统。

项目最初使用了基础的 SQLite + AIDL，我通过系统的重构和优化，
将它升级到了现代 Android 架构标准。

核心改进包括：

1. 安全性：将密码哈希从 SHA-256 升级到 PBKDF2，添加了 AIDL
   权限验证，修复了内存泄漏问题

2. 架构：迁移到 Room + MVVM + LiveData，实现了数据层和 UI 层
   的完全分离

3. 质量：添加了单元测试，覆盖率达到 60%+，实现了统一的异常
   处理机制

4. 并发：解决了数据库的线程安全问题，启用了 WAL 模式

这个项目让我深入理解了 Android 的...（选择你最擅长的部分）"
```

### 技术亮点展示

**如果面试官问："你在这个项目中最大的挑战是什么？"**

选择一个你最有把握的任务，比如：

```
"最大的挑战是迁移到 Room ORM。

当时的问题是：原有的 SQLite 代码有线程安全问题，AIDL 方法在
Binder 线程执行，可能导致数据库并发访问冲突。

我的解决方案是：
1. 首先启用了 WAL 模式，允许读写并发
2. 然后设计了 Entity 和 DAO，将所有 SQL 移到 DAO 中
3. 在 Repository 层处理线程切换
4. 最后编写了测试验证并发安全性

通过这次重构，代码量减少了 30%，同时保证了线程安全。
这让我深入理解了 Room 的内部机制和 Android 的多线程编程。"
```

### 技术栈清单（简历用）

**改进前（基础版）**：
- Java, SQLite, AIDL, ViewBinding, 基础 MVVM

**改进后（现代版）**：
- Java, Room ORM, MVVM + LiveData, AIDL + 权限验证
- 密码学（PBKDF2）, 多线程（Executors, 线程安全）
- 单元测试（JUnit, Mockito）, 覆盖率 60%+
- 依赖注入（Hilt，可选）

---

## 📚 推荐学习资源

### 官方文档（必读）
1. **Android Developers**: https://developer.android.com/
2. **Jetpack 文档**: https://developer.android.com/jetpack
3. **Room 指南**: https://developer.android.com/training/data-storage/room
4. **MVVM 架构**: https://developer.android.com/topic/architecture

### 优质教程
1. **Google Codelabs** - 边学边做
   - "Android Room with a View" Codelab
   - "Architecture Components" Codelab

2. **YouTube 频道**
   - Philipp Lackner（Android 开发）
   - Coding in Flow（MVVM + Room）

3. **书籍**
   - 《Android 开发艺术探索》- 深入理解原理
   - 《Effective Java》- Java 最佳实践

### 开源项目参考
1. **Android Architecture Samples** - Google 官方示例
2. **Sunflower** - Google 的完整示例 App
3. **NotyKT** - MVVM + Room + Hilt 示例

---

## ✅ 每完成一个任务后的检查清单

- [ ] **代码工作正常**？所有功能都能正常使用
- [ ] **理解原理**？能解释为什么这样做
- [ ] **能讲给别人**？能用自己的话描述
- [ ] **代码优雅**？遵循最佳实践
- [ ] **提交代码**？commit message 清晰
- [ ] **更新文档**？记录关键决策

---

## 🆘 遇到问题时的解决流程

### 1. 自己查资料（30-60 分钟）
- 阅读官方文档
- 搜索 Stack Overflow
- 查看类似的开源项目

### 2. 尝试解决（1-2 小时）
- 写代码尝试不同方案
- 使用 debugger 调试
- 记录尝试过程和错误信息

### 3. 寻求帮助（如果真的卡住了）
告诉我：
- 你想实现什么功能？
- 你尝试了哪些方案？
- 遇到了什么具体错误？
- 你的理解是什么？

我会：
- 给你方向性的提示
- 解释相关概念
- 推荐学习资源
- **不会直接给你完整答案**（除非你已经尝试很久）

### 4. 总结学习
- 理解为什么这个方案有效
- 记录到笔记中
- 思考有没有更好的方案

---

## 💪 开始行动

### 🎯 第一步：从任务 1.1 开始

**任务**: 修复密码哈希安全问题
**预计时间**: 3-4 小时
**学习目标**: 理解密码学基础和 PBKDF2

**行动计划**：
1. 阅读本文档中的任务 1.1 部分（15 分钟）
2. 搜索和学习 PBKDF2（30 分钟）
3. 设计你的实现方案（30 分钟）
4. 开始编码实现（1.5-2 小时）
5. 测试验证（30 分钟）

### ✅ 完成后告诉我

当你完成任务 1.1 后，告诉我：
- "我完成了任务 1.1"
- "我使用的方案是：XXX"
- "我的实现在 XXX 文件"
- "我学到了..."

我会：
- Review 你的代码
- 指出可以改进的地方
- 回答你的疑问
- 给你下一步建议

---

## 🎉 完成整个路线图后

你将获得：

### 技术能力
- ✅ 掌握 Android 核心架构（MVVM + Repository + Room）
- ✅ 理解线程安全和并发编程
- ✅ 学会密码学和安全最佳实践
- ✅ 具备编写可测试代码的能力

### 面试竞争力
- ✅ 有一个完整的、可展示的项目
- ✅ 能讲述"改进老项目"的故事
- ✅ 证明了学习能力和问题解决能力
- ✅ 展示了代码质量意识

### 项目质量提升
- **安全性**: 5/10 → 8/10 (+60%)
- **代码质量**: 6/10 → 8/10 (+33%)
- **测试覆盖**: 0% → 60%+
- **整体评分**: 5.0/10 → 7.5/10 (+50%)

---

## 📞 记住

- 不要追求完美，先完成再优化
- 每个任务都是一个学习机会
- 遇到困难很正常，关键是坚持
- 自己动手实现比看答案重要 100 倍

**准备好了就开始吧！从任务 1.1 开始，我会在这里随时帮助你。** 🚀

---

**版本**: 2.0（基于 2025-11-13 最新代码分析）
**更新日期**: 2025-11-13
**分析师**: Claude Code
