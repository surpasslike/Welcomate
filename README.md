# Welcomate - 一款基于客户端-服务端架构的安卓应用

Welcomate 是一个功能完备的 Android 演示项目，旨在展示一个清晰、健壮且可扩展的客户端-服务端应用架构。项目被构建为一个多模块的 Android 应用，包含一个客户端 (`app-client`) 和一个服务端 (`app-server`)。

该项目不仅实现了用户管理的核心功能，还深入应用了现代 Android 开发中的多种高级概念，包括**多模块架构、MVVM 设计模式、仓库模式 (Repository Pattern)、后台服务 (Service) 以及通过 AIDL 实现的跨进程通信 (IPC)**。

## ✨ 核心功能

### 客户端 (`app-client`)
- **用户认证**: 提供用户注册和登录界面。
- **访客模式**: 允许未登录用户以访客身份访问应用。
- **服务绑定**: 在启动时安全地绑定到服务端，并与之进行通信。
- **简洁的 UI**: 专注于核心功能的用户界面。

### 服务端 (`app-server`)
- **用户数据管理**:
  - **添加用户**: 支持创建新的用户账户。
  - **删除用户**: 提供删除现有用户的功能。
  - **修改密码**: 允许更改指定用户的密码。
  - **查询用户**: 可以查看所有已注册用户的列表。
- **安全的密码存储**: 所有用户密码均通过 **SHA-256 哈希算法**进行处理，确保数据库中不存储明文密码。
- **管理仪表盘**: 提供一个完整的管理后台 UI，让管理员可以直观地进行用户管理操作。
- **安全的跨进程接口**: 通过一个受签名级权限保护的 AIDL 接口，安全地向客户端暴露服务。

## 🛠️ 技术栈与架构

### 技术栈
- **语言**: Java
- **核心组件**: Android Service, Android Activity, RecyclerView
- **架构组件**: ViewModel
- **跨进程通信**: AIDL (Android Interface Definition Language)
- **数据库**: SQLite
- **构建系统**: Gradle

### 架构
本项目采用现代化的、分层的软件架构，以实现关注点分离 (Separation of Concerns) 和高可维护性。

- **多模块架构**:
  - `app-client`: 负责所有面向用户的交互和 UI。
  - `app-server`: 负责所有后台逻辑、数据处理和管理功能。

- **MVVM + Repository 设计模式 (`app-server`)**:
  - **View (UI Layer)**: 由 `Activity` 和 `Adapter` 组成，负责显示数据和捕获用户输入。
  - **ViewModel**: 作为 UI 和数据层之间的桥梁，持有并管理 UI 状态，处理用户交互逻辑。
  - **Repository (Data Layer)**: 作为应用数据的唯一真实来源 (Single Source of Truth)。它封装了所有数据操作（数据库访问、密码哈希等），为上层提供清晰的数据 API。
  - **Model**: 简单的 POJO 类 (`User.java`)，用于定义数据结构。

- **安全的跨进程通信 (IPC)**:
  - **Service**: `AdminService` 是一个纯粹的**绑定服务 (Bound Service)**，作为 AIDL 接口的宿主。
  - **AIDL**: `IAdminService.aidl` 定义了客户端和服务端之间的通信契约。
  - **权限保护**: 服务被一个 `signature` 级别的自定义权限所保护，确保只有使用相同密钥签名的客户端 (`app-client`) 才能绑定和调用服务，防止了来自其他应用的未授权访问。

## 🚀 如何构建和运行

这是一个标准的 Android Studio 项目。你可以按照以下步骤来构建和运行它：

1.  **克隆仓库**:
    ```bash
    git clone git@github.com:surpasslike/Welcomate.git
    ```

2.  **在 Android Studio 中打开**:
    - 打开 Android Studio。
    - 选择 "File" -> "Open"，然后导航到你克隆的 `Welcomate` 项目根目录。
    - 等待 Gradle 完成项目同步和构建。

3.  **运行应用**:
    由于这是一个客户端-服务端架构的应用，你需要在一台设备或模拟器上**安装两个 APK**。

    - **第一步：安装并运行服务端 (`app-server`)**
      - 在 Android Studio 的配置下拉菜单中，选择 `app-server`。
      - 点击 "Run" (▶️) 按钮。这将会编译、安装并启动服务端的管理应用。你可以选择性地使用其 UI 注册第一个管理员账户。

    - **第二步：安装并运行客户端 (`app-client`)**
      - 再次点击配置下拉菜单，选择 `app-client`。
      - 点击 "Run" (▶️) 按钮。这将会编译并安装客户端应用。
      - 客户端启动后，它会自动在后台绑定到已安装的 `app-server`。现在你可以在客户端上进行注册和登录操作了。

    **注意**: 建议先安装 `app-server`，因为 `app-client` 依赖于它所提供的后台服务。若只安装`app-client`，也可以使用访客模式进入应用。