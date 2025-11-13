# Welcomate 项目 - 安全问题快速总结

## 🔴 高优先级问题 (关键)

### 1. 密码安全 - SHA-256 无盐值哈希
**位置**: `app-server/src/main/java/.../data/UserRepository.java`
**代码行**: 62-74 行
**问题**: 使用 SHA-256 直接哈希密码，无盐值，容易被彩虹表攻击

```java
// 现有不安全的实现
private String hashPassword(String password) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
    return Base64.encodeToString(hash, Base64.NO_WRAP);
}
```

**修复方案**: 使用 BCrypt 或 Argon2
```java
// 推荐的安全实现 (使用 BCrypt)
private String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(10));
}

public User login(String account, String password) {
    // ... 获取存储的哈希值
    if (BCrypt.checkpw(password, storedPasswordHash)) {
        // 登录成功
    }
}
```

**影响**: 🔴 **严重** - 可被离线破解

---

### 2. AIDL 服务缺少参数验证
**位置**: `app-server/src/main/java/.../aidl/AdminApiImpl.java`
**问题**: 没有验证 account 和 password 参数的有效性

```java
// 现有的不安全代码
@Override
public String loginAdmin(String account, String password) {
    User user = userRepository.login(account, password);
    if (user != null) {
        return user.getUsername();
    }
    return null;  // 无任何验证
}
```

**修复方案**:
```java
@Override
public String loginAdmin(String account, String password) {
    // 参数验证
    if (account == null || account.isEmpty() || account.length() > 50) {
        Log.w(TAG, "Invalid account parameter");
        return null;
    }
    
    if (password == null || password.isEmpty() || password.length() > 100) {
        Log.w(TAG, "Invalid password parameter");
        return null;
    }
    
    // 业务逻辑
    User user = userRepository.login(account, password);
    return (user != null) ? user.getUsername() : null;
}
```

**影响**: 🔴 **严重** - 可能导致注入攻击

---

### 3. AIDL 服务缺少调用者验证
**位置**: `app-server/src/main/java/.../aidl/AdminApiImpl.java`
**问题**: 没有验证调用者身份和权限

```java
// 修复方案 - 添加调用者验证
@Override
public String loginAdmin(String account, String password) {
    // 验证调用者
    int callingPid = Binder.getCallingPid();
    int callingUid = Binder.getCallingUid();
    
    Log.d(TAG, "loginAdmin called from UID: " + callingUid);
    
    // 参数验证
    if (account == null || account.isEmpty()) {
        Log.w(TAG, "Invalid account from " + callingUid);
        return null;
    }
    
    // 业务逻辑...
}
```

**影响**: 🔴 **严重** - IPC 权限绕过风险

---

## 🟡 中优先级问题 (应该修复)

### 4. 密码长度限制过宽松
**位置**: `app-client/src/main/java/.../constants/AppConstants.java`
**现有**: `PASSWORD_MIN_LENGTH = 1, PASSWORD_MAX_LENGTH = 20`
**建议**: `PASSWORD_MIN_LENGTH = 8, PASSWORD_MAX_LENGTH = 100`

```java
public static class TextLimit {
    public static final int USERNAME_MIN_LENGTH = 3;  // 改为3而非1
    public static final int USERNAME_MAX_LENGTH = 20;
    public static final int PASSWORD_MIN_LENGTH = 8;  // 改为8而非1
    public static final int PASSWORD_MAX_LENGTH = 100; // 改为100而非20
}
```

**影响**: 🟡 **中** - 弱密码容易被破解

---

### 5. 数据库未启用 WAL 模式
**位置**: `app-server/src/main/java/.../data/db/DatabaseHelper.java`
**问题**: 没有启用 WAL (Write-Ahead Logging)

```java
// 修复方案 - 在 onCreate 中添加
@Override
public void onCreate(SQLiteDatabase db) {
    Log.d(TAG, "Creating database and users table...");
    
    // 启用 WAL 模式
    db.enableWriteAheadLogging();
    
    db.execSQL(CREATE_TABLE_USERS);
    Log.d(TAG, "Database created successfully.");
}
```

**影响**: 🟡 **中** - 并发性能差

---

## 🟢 低优先级问题 (建议修复)

### 6. 代码质量警告 - @SuppressLint 过多
**位置**: `app-server/src/main/java/.../data/UserRepository.java`
**问题**: 使用了过时的 getColumnIndex() API

```java
// 当前代码
@SuppressLint("Range")
String storedPasswordHash = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));

// 建议改进 - 使用 Room ORM
@Query("SELECT password FROM users WHERE account = :account")
String getPasswordByAccount(String account);
```

**影响**: 🟢 **低** - 代码质量

---

### 7. 缺少 AIDL 调用日志
**位置**: `app-server/src/main/java/.../aidl/AdminApiImpl.java`
**问题**: 没有记录 AIDL 调用用于审计

**修复方案**:
```java
private static final String TAG = "AdminApiImpl";

@Override
public String loginAdmin(String account, String password) {
    int callingUid = Binder.getCallingUid();
    Log.d(TAG, "loginAdmin called: account=" + account + ", callingUid=" + callingUid);
    
    // ... 业务逻辑
}

@Override
public boolean registerUser(String username, String account, String password) {
    int callingUid = Binder.getCallingUid();
    Log.d(TAG, "registerUser called: username=" + username + ", callingUid=" + callingUid);
    
    // ... 业务逻辑
}
```

**影响**: 🟢 **低** - 审计追踪

---

### 8. 未使用 LiveData/Flow
**位置**: `app-server/src/main/java/.../ui/admin/AdminViewModel.java`
**问题**: ViewModel 中的数据不是响应式的

```java
// 当前代码
public List<User> getAllUsers() {
    return userRepository.getAllUsers();
}

// 建议改进
private MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();

public LiveData<List<User>> getUsers() {
    loadUsers();
    return usersLiveData;
}

private void loadUsers() {
    List<User> users = userRepository.getAllUsers();
    usersLiveData.setValue(users);
}
```

**影响**: 🟢 **低** - 架构质量

---

## 修复优先级时间表

### 第一周 (立即)
- [ ] 实现 BCrypt 密码哈希
- [ ] 添加 AIDL 参数验证
- [ ] 添加调用者权限检查

### 第二周
- [ ] 更新密码长度限制
- [ ] 启用 WAL 模式
- [ ] 添加 AIDL 调用日志

### 第三周及以后
- [ ] 迁移到 Room ORM
- [ ] 实现 LiveData 响应式
- [ ] 添加单元测试

---

## 验收标准

修复完成后应满足:
✅ 所有 AIDL 调用都有参数验证
✅ 密码使用 BCrypt 或 Argon2 哈希
✅ 至少记录关键 AIDL 调用
✅ 密码最小长度 >= 8
✅ 数据库启用 WAL 模式

