# Welcomate 项目 - 详细问题清单

## 问题分布统计

总计发现问题：**35 个**
- 高严重程度：9 个（26%）
- 中严重程度：16 个（46%）
- 低严重程度：10 个（28%）

---

## 问题详表（按严重程度排序）

### 高严重程度问题（9 个）

| # | 文件 | 行号 | 问题 | 影响范围 | 修复难度 |
|---|------|------|------|--------|--------|
| 1 | UserRepository.java | 62-74 | 密码哈希算法过时且缺少盐值 | 认证系统 | 中 |
| 2 | AdminApiImpl.java | 全文 | AIDL 接口缺少权限验证 | 跨进程通信 | 高 |
| 3 | IAdminService.aidl | 全文 | AIDL 数据传输未加密 | 数据安全 | 高 |
| 4 | MainActivity.java | 31 | 静态引用导致的内存泄漏 | 内存管理 | 低 |
| 5 | MyApplication.java | 8 | Context 静态引用泄漏 | 内存管理 | 低 |
| 6 | AdminApiImpl.java | 全文 | AIDL 方法线程安全问题 | 并发访问 | 中 |
| 7 | UserRepository.java | 45-54 | 单例线程安全问题 | 并发访问 | 中 |
| 8 | DatabaseHelper.java | 全文 | 数据库存储未加密 | 数据保护 | 中 |
| 9 | 缺失 | N/A | 缺少单元测试 | 代码质量 | 高 |

### 中严重程度问题（16 个）

| # | 文件 | 行号 | 问题 | 影响范围 | 修复难度 |
|---|------|------|------|--------|--------|
| 1 | User.java | 10 | 模型中直接暴露密码字段 | 安全性 | 低 |
| 2 | AppConstants.java | 37-39 | 缺少密码强度验证 | 认证 | 低 |
| 3 | AndroidManifest.xml | 全文 | 签名级权限验证不完整 | 权限管理 | 中 |
| 4 | MainActivity.java | 全文 | Service 连接生命周期管理不完善 | 资源管理 | 中 |
| 5 | HomeActivity.java | 95-135 | Dialog 内存泄漏风险 | 内存管理 | 低 |
| 6 | 多个 Activity | 全文 | ViewBinding 资源释放不完整 | 生命周期 | 低 |
| 7 | UserRepository.java | 66-73 | 异常处理过度宽泛 | 错误处理 | 中 |
| 8 | 多个 | 全文 | RemoteException 处理不一致 | 错误处理 | 中 |
| 9 | UserRepository.java | 全文 | 缺少数据库异常处理 | 稳定性 | 中 |
| 10 | UserRepository.java | 89,135 | 数据库 Cursor 资源泄漏 | 内存管理 | 中 |
| 11 | MainActivity.java | 132-137 | Service 连接泄漏 | 资源管理 | 低 |
| 12 | MainActivity.java | 34-55 | Listener 回调内存泄漏 | 内存管理 | 中 |
| 13 | 多个 Activity | 全文 | 缺少加载状态指示 | UX/性能 | 低 |
| 14 | 多个 Activity | 全文 | 错误消息过于通用 | UX | 低 |
| 15 | ValidationUtils.java | 全文 | 输入验证不完整 | 安全性 | 中 |
| 16 | 整体 | N/A | 缺少 Jetpack 库使用 | 架构 | 高 |

### 低严重程度问题（10 个）

| # | 文件 | 行号 | 问题 | 影响范围 | 修复难度 |
|---|------|------|------|--------|--------|
| 1 | 整体 | N/A | 缺少网络层/远程数据源 | 架构 | 高 |
| 2 | 多个 Activity | 全文 | Activity 中混入业务逻辑 | 架构 | 中 |
| 3 | 整体 | N/A | 缺少依赖注入框架 | 架构 | 中 |
| 4 | UserRepository.java | 45-54 | 单例模式使用不当 | 设计模式 | 中 |
| 5 | AdminDashboardActivity.java | 全文 | 缺少观察者模式 | 架构 | 低 |
| 6 | 多个 | N/A | 缺少 Fragment 使用 | 架构 | 高 |
| 7 | 多个 | N/A | 缺少 Material Design 3 | UI | 低 |
| 8 | UserRepository.java | 91,95,138,139 | 使用已废弃的 API | 兼容性 | 低 |
| 9 | 多个 | N/A | 缺少 Fragment DialogFragment | 架构 | 中 |
| 10 | 整体 | N/A | 缺少文档和 ADR | 文档 | 低 |

---

## 修复优先级矩阵

```
高影响 + 高紧急  │ 高影响 + 低紧急
──────────────────┼──────────────────
密码哈希算法       │ 缺少单元测试
AIDL权限验证      │ 迁移到 Room
静态引用泄漏      │ 集成 Hilt
线程安全问题      │ 缺少文档

低影响 + 高紧急  │ 低影响 + 低紧急
──────────────────┼──────────────────
缺少加载状态      │ Material Design 3
错误消息提示      │ 网络层支持
密码可见性        │ Fragment 迁移
输入验证          │ 离线模式
```

---

## 每日改进计划（时间估算）

### 第 1 周（关键安全修复）
**工时：40 小时**

#### 第 1 天（8h）- 密码安全
- [ ] 集成 PBKDF2 或 bcrypt 库 (2h)
- [ ] 实现密码盐值生成 (2h)
- [ ] 更新密码哈希逻辑 (2h)
- [ ] 迁移现有密码数据 (2h)

#### 第 2-3 天（16h）- AIDL 权限验证
- [ ] 分析 AIDL 安全需求 (2h)
- [ ] 实现 Binder 权限检查 (4h)
- [ ] 添加操作日志记录 (4h)
- [ ] 编写权限验证测试 (6h)

#### 第 4 天（8h）- 内存泄漏修复
- [ ] 移除静态 Service 引用 (3h)
- [ ] 修复 Context 泄漏 (2h)
- [ ] 修复 Dialog 泄漏 (3h)

#### 第 5 天（8h）- 数据库并发安全
- [ ] 实现数据库锁机制 (4h)
- [ ] 启用 WAL 模式 (2h)
- [ ] 测试并发访问 (2h)

### 第 2 周（架构改进）
**工时：40 小时**

#### 第 6-7 天（16h）- Room 迁移
- [ ] 设计 Room 架构 (2h)
- [ ] 创建 Entity 类 (4h)
- [ ] 实现 DAO 接口 (6h)
- [ ] 迁移数据 (4h)

#### 第 8-9 天（16h）- 异常处理和日志
- [ ] 创建自定义异常类 (2h)
- [ ] 统一异常处理策略 (4h)
- [ ] 添加日志框架 (4h)
- [ ] 补充数据库操作异常处理 (6h)

#### 第 10 天（8h）- 输入验证
- [ ] 增强验证工具类 (3h)
- [ ] 添加正则表达式验证 (2h)
- [ ] 实现实时输入反馈 (3h)

### 第 3-4 周（测试和文档）
**工时：60 小时**

#### 第 11-14 天（32h）- 单元测试
- [ ] Repository 层测试 (8h)
- [ ] ViewModel 测试 (8h)
- [ ] AIDL 集成测试 (8h)
- [ ] Utility 类测试 (8h)

#### 第 15-16 天（16h）- UI 改进
- [ ] 添加加载状态 (4h)
- [ ] 改善错误提示 (4h)
- [ ] 密码可见性切换 (4h)
- [ ] 响应式布局调整 (4h)

#### 第 17-18 天（12h）- 文档
- [ ] 完善 README (4h)
- [ ] JavaDoc 注释 (4h)
- [ ] 架构设计文档 (4h)

---

## 代码示例 - 快速修复方案

### 问题 1: 密码哈希升级

**当前代码（不安全）**
```java
private String hashPassword(String password) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
    return Base64.encodeToString(hash, Base64.NO_WRAP);
}
```

**改进代码（安全）**
```java
private String hashPassword(String password) throws Exception {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    
    KeySpec spec = new PBKDFKeySpec(
        password.toCharArray(), 
        salt, 
        100000,  // 迭代次数
        256      // 输出长度
    );
    
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] hash = factory.generateSecret(spec).getEncoded();
    
    // 返回 salt + hash
    byte[] combined = new byte[salt.length + hash.length];
    System.arraycopy(salt, 0, combined, 0, salt.length);
    System.arraycopy(hash, 0, combined, salt.length, hash.length);
    
    return Base64.encodeToString(combined, Base64.NO_WRAP);
}

public boolean verifyPassword(String password, String storedHash) throws Exception {
    byte[] combined = Base64.decode(storedHash, Base64.NO_WRAP);
    byte[] salt = new byte[16];
    System.arraycopy(combined, 0, salt, 0, 16);
    
    KeySpec spec = new PBKDFKeySpec(
        password.toCharArray(), 
        salt, 
        100000, 
        256
    );
    
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] hash = factory.generateSecret(spec).getEncoded();
    
    byte[] storedHashBytes = new byte[256];
    System.arraycopy(combined, 16, storedHashBytes, 0, 256);
    
    return MessageDigest.isEqual(hash, storedHashBytes);
}
```

### 问题 2: AIDL 权限验证

**改进代码**
```java
public class AdminApiImpl extends IAdminService.Stub {
    
    private static final String CALLER_PACKAGE = "com.surpasslike.welcomate";
    
    @Override
    public String loginAdmin(String account, String password) {
        verifyCallerPermission();
        logOperation("loginAdmin", account);
        return userRepository.loginAdmin(account, password);
    }
    
    private void verifyCallerPermission() {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        
        String[] packages = getPackageManager().getPackagesForUid(callingUid);
        if (packages == null || !Arrays.asList(packages).contains(CALLER_PACKAGE)) {
            throw new SecurityException("Caller not allowed");
        }
        
        // 额外的签名验证
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(CALLER_PACKAGE, 
                PackageManager.GET_SIGNATURES);
            // 验证签名...
        } catch (PackageManager.NameNotFoundException e) {
            throw new SecurityException("Caller package not found");
        }
    }
    
    private void logOperation(String operation, String details) {
        int uid = Binder.getCallingUid();
        long timestamp = System.currentTimeMillis();
        // 记录到日志或数据库
        Log.i("AdminAPI", String.format(
            "[%d] UID=%d Operation=%s Details=%s", 
            timestamp, uid, operation, details
        ));
    }
}
```

### 问题 3: 移除静态引用

**当前代码**
```java
public class MainActivity extends AppCompatActivity {
    static IAdminService mAdminService;  // 静态引用，内存泄漏
    
    public static IAdminService getAdminService() {
        return mAdminService;
    }
}
```

**改进代码**
```java
public class ServiceLocator {
    private static final ServiceLocator INSTANCE = new ServiceLocator();
    
    private IAdminService adminService;
    private final List<ServiceStateListener> listeners = new CopyOnWriteArrayList<>();
    
    public static ServiceLocator getInstance() {
        return INSTANCE;
    }
    
    public void setAdminService(IAdminService service) {
        this.adminService = service;
        notifyListeners(service != null);
    }
    
    public IAdminService getAdminService() {
        return adminService;
    }
    
    public void addListener(ServiceStateListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners(boolean connected) {
        for (ServiceStateListener listener : listeners) {
            listener.onServiceStateChanged(connected);
        }
    }
    
    public interface ServiceStateListener {
        void onServiceStateChanged(boolean connected);
    }
}

// 使用方式
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        IAdminService service = ServiceLocator.getInstance().getAdminService();
        if (service != null) {
            // 使用 service
        } else {
            showError("Service not available");
        }
    }
}
```

### 问题 4: 线程安全的 DatabaseHelper

**改进代码**
```java
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;
    
    private static volatile DatabaseHelper instance;
    private final Object writeLock = new Object();
    private final Object readLock = new Object();
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context);
                    instance.enableWriteAheadLogging();
                }
            }
        }
        return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        synchronized (writeLock) {
            db.execSQL(CREATE_TABLE_USERS);
        }
    }
    
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }
    
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }
}
```

### 问题 5: 异常处理统一方案

**创建 Result 包装类**
```java
public abstract class Result<T> {
    public static class Success<T> extends Result<T> {
        public final T data;
        public Success(T data) { this.data = data; }
    }
    
    public static class Error<T> extends Result<T> {
        public final Throwable error;
        public Error(Throwable error) { this.error = error; }
    }
    
    public <U> Result<U> flatMap(Function<T, Result<U>> f) {
        if (this instanceof Success) {
            return f.apply(((Success<T>) this).data);
        }
        return (Result<U>) this;
    }
}

// 使用方式
public Result<String> loginAdmin(String account, String password) {
    try {
        String result = userRepository.loginAdmin(account, password);
        return result != null 
            ? new Result.Success<>(result)
            : new Result.Error<>(new Exception("Invalid credentials"));
    } catch (Exception e) {
        return new Result.Error<>(e);
    }
}

// Activity 中使用
Result<String> result = viewModel.loginAdmin(account, password);
if (result instanceof Result.Success) {
    navigateToHome(((Result.Success<String>) result).data);
} else {
    showError(((Result.Error<String>) result).error.getMessage());
}
```

---

## 关键文件修改清单

### 必须修改的文件（按优先级）

**P0 - 立即修改**
1. `/home/user/Welcomate/app-server/src/main/java/com/surpasslike/welcomateservice/data/UserRepository.java`
   - 升级密码哈希算法
   - 添加异常处理
   - 补充输入验证

2. `/home/user/Welcomate/app-server/src/main/java/com/surpasslike/welcomateservice/aidl/AdminApiImpl.java`
   - 添加权限验证
   - 添加操作日志
   - 处理线程安全

3. `/home/user/Welcomate/app-client/src/main/java/com/surpasslike/welcomate/activity/MainActivity.java`
   - 移除静态 Service 引用
   - 改进生命周期管理
   - 添加连接状态检查

4. `/home/user/Welcomate/app-server/src/main/java/com/surpasslike/welcomateservice/data/db/DatabaseHelper.java`
   - 实现线程安全
   - 启用 WAL 模式

**P1 - 短期修改**
5. `/home/user/Welcomate/app-server/src/main/java/com/surpasslike/welcomateservice/MyApplication.java`
   - 改进 Context 管理

6. `/home/user/Welcomate/app-client/src/main/java/com/surpasslike/welcomate/utils/ValidationUtils.java`
   - 增强输入验证

7. `/home/user/Welcomate/app-client/src/main/java/com/surpasslike/welcomate/constants/AppConstants.java`
   - 提高密码最小长度
   - 添加密码复杂性要求

8. 所有 Activity 文件
   - 改进异常处理
   - 改进生命周期管理

---

## 预期改进效果

### 安全性提升
- 从 5/10 → 8/10（+60%）
- 密码安全：从 3/10 → 9/10
- 权限管理：从 4/10 → 8/10
- 数据保护：从 5/10 → 7/10

### 代码质量提升
- 从 6/10 → 8/10（+33%）
- 异常处理：从 5/10 → 8/10
- 资源管理：从 5/10 → 8/10
- 线程安全：从 4/10 → 9/10

### 测试覆盖率
- 从 0% → 70%（目标）
- 关键代码：100%
- 业务逻辑：80%+

### 整体评分
- 从 5.6/10 → 7.5/10（+34%）

---

## 完成时间表

| 阶段 | 时间线 | 工时 | 核心目标 |
|------|-------|------|--------|
| P0 - 安全修复 | 第 1-2 周 | 40h | 关键安全问题 |
| P1 - 架构改进 | 第 3-4 周 | 60h | 代码质量 |
| P2 - 测试补充 | 第 5-6 周 | 40h | 测试覆盖 |
| P3 - 优化 | 第 7-8 周 | 30h | 性能和 UX |
| **总计** | **2 个月** | **170h** | **全面改进** |

