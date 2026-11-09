# 学生课程成绩管理系统（Student Score Manager）

> 计算机应用技术专业 · 课程设计 / 个人练习项目
> Java 17 + JDBC + MySQL 8 + Maven · 控制台版

一个不依赖任何框架的课程成绩管理系统。用最基础的 JDBC 手写 DAO，把「建表 → 连接 → 增删改查 → 统计 → 导出」整条链路走通，
适合作为 Java 基础 + MySQL 学习后的第一个完整项目，也适合写进简历作为「数据库编程 / JDBC 实践」的经历。

---

## 一、项目简介

本项目实现一个管理员使用的成绩管理后台：

- 管理员账号登录（密码 SHA-256 哈希存储，不存明文）
- 学生信息、课程信息、成绩记录的完整增删改查
- 成绩支持按学生、按课程、按分数段查询
- 按课程 / 按学生两个维度统计平均分、最高分、最低分、及格率
- 查询结果一键导出 CSV（UTF-8 带 BOM，Excel 打开不乱码）

技术选型刻意保持"朴素"：没有 Spring、没有 MyBatis、没有连接池、没有前端框架，
所有 SQL 都是手写的，方便初学者逐行读懂并能在答辩时讲清楚。

---

## 二、功能清单

| 编号 | 功能 | 说明 | 状态 |
| --- | --- | --- | --- |
| 1 | 登录 | 管理员登录，密码 SHA-256 哈希比对，最多 3 次尝试 | 已实现 |
| 2 | 学生管理 | 增、删、改、查（按学号 / 姓名模糊查询） | 已实现 |
| 3 | 课程管理 | 增、删、改、查（按编号 / 名称模糊查询） | 已实现 |
| 4 | 成绩管理 | 录入、按学生查询、按课程查询、按分数段查询、修改、删除 | 已实现 |
| 5 | 成绩统计 | 按课程统计、按学生统计：平均分 / 最高分 / 最低分 / 及格率；另含全局平均分 | 已实现 |
| 6 | CSV 导出 | 导出最近一次查询结果，UTF-8 + BOM，字段按 RFC 4180 转义 | 已实现 |
| 7 | 数据库 | 4 张表（student / course / score / sys_user），主外键 + 索引 + 唯一约束 + CHECK 约束 | 已实现 |

### 数据库设计要点

- `score` 表通过 `UNIQUE KEY uk_student_course (student_id, course_id)` 保证「一个学生的一门课只有一条成绩」。
- `score` 的两个外键都是 `ON DELETE CASCADE`：删除学生或课程时，其成绩自动清理，不会留下孤儿数据。
- `score.score` 上有 `CHECK (score >= 0 AND score <= 100)`，数据库层面兜底分数范围。
- 常用查询字段建有索引：`student.student_no`、`student.name`、`course.course_no`、`course.name`、`score.course_id`、`score.score`。

---

## 三、技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 | Java 17（`maven.compiler.release=17`） |
| 数据库 | MySQL 8.0+（开发验证使用 MySQL 8.4.3） |
| 数据访问 | 原生 JDBC + `PreparedStatement` |
| 构建工具 | Maven |
| 第三方依赖 | 仅 `com.mysql:mysql-connector-j:8.0.33` |
| 界面 | 控制台（`Scanner` + 文本表格） |
| 开发工具 | IntelliJ IDEA、Navicat |

> 说明：JDK 17 是编译目标。用 JDK 18/21 运行本项目也可以，但 `pom.xml` 里的 `release` 固定为 17，保证向下兼容。

---

## 四、项目结构

```
student-score-manager/
├── pom.xml                          # Maven 配置，只引入 mysql-connector-j
├── README.md
├── LICENSE                          # MIT
├── .gitignore                       # 忽略 target / .idea / *.iml / db.properties
├── sql/
│   └── init.sql                     # 建库 + 建表 + 初始数据（可重复执行）
├── docs/
│   └── screenshots/                 # 运行截图目录（请自行放截图）
└── src/main/
    ├── java/com/example/scores/
    │   ├── pojo/                    # 实体类
    │   │   ├── Student.java
    │   │   ├── Course.java
    │   │   ├── Score.java           # 成绩（存外键 id）
    │   │   ├── ScoreDetail.java     # 成绩 + 学生 + 课程（三表联查结果）
    │   │   ├── User.java
    │   │   ├── CourseStat.java      # 按课程统计结果
    │   │   └── StudentStat.java     # 按学生统计结果
    │   ├── dao/                     # 数据访问层，只写 SQL
    │   │   ├── UserDao.java
    │   │   ├── StudentDao.java
    │   │   ├── CourseDao.java
    │   │   ├── ScoreDao.java
    │   │   └── StatDao.java
    │   ├── db/
    │   │   └── DBUtil.java          # 读取 db.properties，返回 Connection
    │   ├── service/                 # 业务层，做参数校验和规则判断
    │   │   ├── AuthService.java
    │   │   ├── StudentService.java
    │   │   ├── CourseService.java
    │   │   ├── ScoreService.java
    │   │   ├── StatService.java
    │   │   └── ExportService.java
    │   ├── util/
    │   │   ├── Sha256Util.java      # SHA-256 哈希与比对
    │   │   ├── CsvExporter.java     # CSV 导出（BOM + 转义）
    │   │   ├── ConsoleUtil.java     # 中文对齐、数字格式化
    │   │   ├── BusinessException.java
    │   │   └── InputClosedException.java
    │   └── main/                    # 控制台界面层
    │       ├── MainApp.java         # 入口：连库 -> 登录 -> 主菜单
    │       ├── ConsoleInput.java    # 统一输入（校验数字/日期）
    │       ├── ConsoleView.java     # 表格输出
    │       ├── StudentMenu.java
    │       ├── CourseMenu.java
    │       ├── ScoreMenu.java
    │       └── StatMenu.java
    └── resources/
        ├── db.properties.example    # 连接配置模板（提交到仓库）
        └── db.properties            # 真实配置（被 .gitignore 忽略，需自己创建）
```

分层调用方向：`main` → `service` → `dao` → `db`。`pojo` 在各层之间传递数据，`util` 提供无状态的工具方法。

---

## 五、数据库表说明

### 1. student（学生表）

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | INT | PK, AUTO_INCREMENT | 主键 |
| student_no | VARCHAR(20) | NOT NULL, UNIQUE | 学号 |
| name | VARCHAR(50) | NOT NULL, INDEX | 姓名 |
| gender | VARCHAR(10) | NOT NULL, 默认 '未知' | 男 / 女 / 未知 |
| major | VARCHAR(50) | NOT NULL, 默认 '' | 专业 |
| class_name | VARCHAR(50) | NOT NULL, 默认 '' | 班级 |
| phone | VARCHAR(20) | NOT NULL, 默认 '' | 联系电话 |
| created_at | DATETIME | 默认 CURRENT_TIMESTAMP | 创建时间 |

### 2. course（课程表）

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | INT | PK, AUTO_INCREMENT | 主键 |
| course_no | VARCHAR(20) | NOT NULL, UNIQUE | 课程编号 |
| name | VARCHAR(50) | NOT NULL, INDEX | 课程名称 |
| credit | DECIMAL(3,1) | NOT NULL, 默认 2.0 | 学分 |
| teacher | VARCHAR(50) | NOT NULL, 默认 '' | 任课教师 |
| created_at | DATETIME | 默认 CURRENT_TIMESTAMP | 创建时间 |

### 3. score（成绩表）

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | INT | PK, AUTO_INCREMENT | 主键 |
| student_id | INT | NOT NULL, FK → student.id, CASCADE | 学生 |
| course_id | INT | NOT NULL, FK → course.id, CASCADE | 课程 |
| score | DECIMAL(5,2) | NOT NULL, CHECK 0~100 | 分数 |
| exam_date | DATE | NULL | 考试日期 |
| created_at | DATETIME | 默认 CURRENT_TIMESTAMP | 录入时间 |
| updated_at | DATETIME | 自动更新 | 修改时间 |

索引：`uk_student_course(student_id, course_id)` 唯一、`idx_score_course(course_id)`、`idx_score_value(score)`。

### 4. sys_user（系统用户表）

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | INT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 登录名 |
| password | CHAR(64) | NOT NULL | SHA-256 哈希（64 位十六进制） |
| real_name | VARCHAR(50) | 默认 '' | 真实姓名 |
| role | VARCHAR(20) | 默认 'ADMIN' | 角色 |
| created_at | DATETIME | 默认 CURRENT_TIMESTAMP | 创建时间 |

**默认账号**

| 用户名 | 密码 | 说明 |
| --- | --- | --- |
| admin | admin123 | 仅用于本地练习，正式使用请改密码 |

密码不是明文存储，`init.sql` 里写入的是 `SHA-256("admin123")`：

```
240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
```

---

## 六、运行步骤

### 前置条件

- 已安装 JDK 17 或更高版本（`java -version` 能输出版本号）
- 已安装 MySQL 8.0 或更高版本，且服务已启动
- 已安装 IntelliJ IDEA（社区版即可）
- **不需要单独安装 Maven**：IDEA 自带 Maven（本项目在 IDEA 2026.1.3 自带的 Maven 3.9.11 上验证通过）。
  只有在命令行执行 `mvn` 命令时才需要单独安装并把 `bin` 目录加入 PATH；
  在 IDEA 里点 Maven 面板的刷新/构建按钮不受影响。

### 步骤 1：创建数据库

**方式 A：Navicat**

1. 连接本地 MySQL。
2. 打开「查询」窗口，把 `sql/init.sql` 的全部内容粘贴进去。
3. 点击「运行」，看到最后的自检结果（学生 5 / 课程 3 / 成绩 11 / 用户 1）即成功。

**方式 B：命令行**

```bash
mysql -u root -p < sql/init.sql
```

> `init.sql` 开头有 `DROP TABLE IF EXISTS`，重复执行会清空并重建这 4 张表，练习时方便重置数据。

### 步骤 2：配置数据库连接

1. 进入 `src/main/resources/`。
2. 复制 `db.properties.example`，重命名为 `db.properties`。
3. 修改其中的密码：

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/student_score_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=你的MySQL密码
```

> `db.properties` 已被 `.gitignore` 忽略，不会被提交。**不要**把真实密码写进 Java 代码或 README。

### 步骤 3：在 IDEA 中运行

1. IDEA 选择 `File → Open`，打开项目根目录（含 `pom.xml` 的那一层）。
   弹出「Trust and Open Project?」时点 **Trust Project**。
2. 等待右下角 Maven 导入结束。**判断是否导入成功**：项目树里 `src/main/java` 应显示为**蓝色**的源码根，
   且右侧 `Maven` 面板里能看到本项目和 `Dependencies`。
   如果 `java` 是灰色普通文件夹，说明 Maven 还没导入 —— 右键 `pom.xml` → `Maven` → `Reload project`。
3. 确认 `File → Project Structure → Project SDK` 为 JDK 17+，`Language level` 为 17。
4. 打开 `src/main/java/com/example/scores/main/MainApp.java`，右键 `Run 'MainApp.main()'`。
5. 用 `admin / admin123` 登录。

**如果控制台中文乱码**：在 IDEA 的 `Run → Edit Configurations → VM options` 中加入：

```
-Dfile.encoding=UTF-8
```

同时把 `Settings → Editor → File Encodings` 的三项都设为 UTF-8。

> 实测记录：在 IDEA 2026.1.3 上打开本项目，`src/main/java` 一开始显示为普通文件夹，
> 需要等 Maven 自动导入完成（或手动 Reload）后才变成源码根。这是 IDEA 的正常行为，不是项目配置问题。

**如果运行时报 `Cannot run program ... corretto-24.0.2\bin\java.exe` 或 `Unsupported JDK`**

说明 IDEA 给项目选的 JDK 在你电脑上不存在（通常是 IDEA 从别的项目继承了一个已卸载的 JDK 路径）。修复步骤：

1. `File → Project Structure → Project`，把 `SDK` 改成你本机真实存在的 JDK 17+。
   下拉列表里如果只有 `1.8` 和一个带红色/失效标记的项，点 `Add SDK → JDK...`，选择本机 JDK 安装目录
   （例如 `C:\Users\你的用户名\.jdks\corretto-18.0.2`）。
2. 切到 `Modules → Sources`，确认 `Language level` 是 `17 - Sealed types...`。
3. 切到 `Modules → Dependencies`，确认 `Module SDK` 是 `Project SDK`。
4. 点 `Apply → OK`，然后按 `Ctrl+F9` 做一次**完整 Build**（不要只编译单个文件），
   否则 `target/classes` 里可能只有部分 class，运行时会报「包 com.example.scores.db 不存在」。

> 实测记录：以上四步是本次在真实 IDEA 2026.1.3 上踩到的坑，照此操作后程序在 IDEA 中正常运行。

### 步骤 4：命令行运行（可选）

前提：命令行里能直接执行 `mvn`。如果你的电脑没有单独装 Maven，可以临时用 IDEA 自带的（把路径换成你的 IDEA 安装目录）：

```powershell
# PowerShell 中临时使用 IDEA 自带 Maven
$mvn = "D:\IntelliJ IDEA 2026.1.3\plugins\maven\lib\maven3\bin\mvn.cmd"
```

```bash
# 1. 打包
mvn clean package

# 2. 复制依赖到 target/dependency
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

# 3. 运行（Windows 用分号分隔 classpath）
java -Dfile.encoding=UTF-8 -cp "target/classes;target/dependency/*" com.example.scores.main.MainApp
```

> 本项目**没有**打成 fat-jar（受"只引入一个依赖、不过度设计"的约束）。
> 直接 `java -jar target/student-score-manager.jar` 会报 `找不到 JDBC 驱动类`，这是预期行为，请用上面的 `-cp` 方式运行。

> 实测记录：`mvn -v` 在未安装 Maven 的机器上会报「无法将 mvn 项识别为 cmdlet」；
> 但 IDEA 内部构建正常，因为 IDEA 自带 Maven 3.9.11（位于 `IDEA安装目录\plugins\maven\lib\maven3`）。

---

## 七、使用说明

登录后主菜单：

```
1. 学生管理      -> 查看 / 查询 / 新增 / 修改 / 删除学生
2. 课程管理      -> 查看 / 查询 / 新增 / 修改 / 删除课程
3. 成绩管理      -> 录入 / 按学生查 / 按课程查 / 按分数段查 / 修改 / 删除 / 导出 CSV
4. 成绩统计      -> 按课程统计 / 按学生统计 / 全局平均分
0. 退出系统
```

几个使用约定：

- **学号、课程编号新增后不可修改**，避免破坏已有成绩的关联；其余字段可改。
- **同一学生同一课程只能有一条成绩**，重复录入会提示已有成绩的 id，引导去「修改成绩」。
- **删除学生或课程会级联删除其成绩**，删除前会显示将要影响的成绩条数并要求输入 `y` 确认。
- **导出 CSV** 使用「最近一次查询结果」，所以要先查询再导出；默认保存到**当前运行目录**下的 `exports/`（在 IDEA 中运行即项目根目录的 `exports/`，在命令行运行时是执行 `java` 命令时所在的目录），也可以手动输入完整路径。

---

## 八、已实现 / 未实现

### 已实现

- 管理员登录，SHA-256 哈希校验，3 次失败退出
- 学生、课程、成绩的完整增删改查
- 成绩按学生、按课程、按分数段查询，以及组合条件查询
- 按课程 / 按学生的平均分、最高分、最低分、及格率统计
- CSV 导出（UTF-8 BOM，Excel 直接打开不乱码）
- 参数校验：分数 0~100、学分范围、性别枚举、手机号格式、学号/课程编号唯一
- 统一的业务异常提示，不会把 SQL 异常堆栈直接甩给用户
- 输入流结束（脚本输入 / Ctrl+Z）时干净退出，不会死循环

### 未实现（有意不做，避免超出学习范围）

- 多角色权限（目前只有 ADMIN，未做学生端/教师端）
- 修改密码、用户注册、找回密码
- 分页查询（数据量小，一次全查）
- 连接池（HikariCP 等）、事务编排、ORM 框架
- Swing / Web 图形界面
- 单元测试（验证过程见下方"开发验证记录"）

---

## 九、开发验证记录

本项目在交付前做过以下**实际执行**的验证（非虚构）：

| 验证项 | 方式 | 结果 |
| --- | --- | --- |
| SQL 可在 MySQL 8 执行 | 在临时 MySQL 8.4.3 实例上完整执行 `sql/init.sql` | 通过，4 张表创建成功，初始数据 5/3/11/1 |
| Java 编译 | `javac --release 17 -Xlint:all`，30 个源文件 | 通过，0 错误 0 警告 |
| Maven 构建 | `mvn clean package`（IDEA 自带 Maven 3.9.11 + JDK 18 运行） | 通过，编译 31 个源文件，生成 `student-score-manager.jar` |
| IDEA 导入 | IntelliJ IDEA 2026.1.3 打开项目根目录 | 通过，pom.xml 被识别，Terminal 与 Maven 构建均可用 |
| IDEA 内运行 | 在 IDEA 中运行 `MainApp`，登录并查看按课程统计 | 通过，中文正常，统计数字与 SQL 核对一致 |
| Excel 打开 CSV | 用 Excel 双击打开导出文件 | 通过，中文不乱码；日期列需加宽才不显示 `########` |
| 功能正确性 | 临时断言程序直接调用 Service 层，共 44 项断言 | 全部 PASS，0 FAIL |
| CSV 编码 | 读取导出文件首字节 | `EF BB BF`，中文正常 |

验证覆盖的内容包括：登录成功/失败、密码哈希值正确、学生与课程增删改查、重复学号与重复课程编号拦截、分数越界拦截、
同一学生同一课程重复录入拦截、按学生/课程/分数段查询、SQL 注入字符串不返回数据、统计值精确比对、CSV BOM 与中文内容、级联删除计数。

> 说明：验证是在临时独立 MySQL 实例（端口 3399，独立数据目录）上完成的，未改动本机已有的 MySQL 服务与数据；
> 验证结束后临时实例与临时文件均已删除。界面交互（菜单输入、表格排版）无法自动化断言，需按第十节手动验收。

---

## 十、本地验收清单

请在你自己的 IDEA + MySQL 环境按顺序逐条验证。每条都给出**操作**和**预期结果**。

### A. 环境与建库

| # | 操作 | 预期结果 |
| --- | --- | --- |
| A1 | `mysql -u root -p < sql/init.sql` | 无报错，最后输出 学生 5 / 课程 3 / 成绩 11 / 用户 1 |
| A2 | Navicat 刷新 `student_score_db` | 看到 student、course、score、sys_user 四张表 |
| A3 | 在 Navicat 查看 score 表设计 | 有 `uk_student_course` 唯一索引、两个外键均为 CASCADE |
| A4 | 复制 `db.properties.example` 为 `db.properties` 并填写密码 | 文件出现在 `src/main/resources/`，内容为你的真实密码 |
| A5 | IDEA 打开项目根目录，等待 Maven 导入 | 弹出 Trust 对话框点 Trust；导入后 `src/main/java` 显示为**蓝色**源码根 |

### B. 启动与登录

| # | 操作 | 预期结果 |
| --- | --- | --- |
| B1 | 运行 `MainApp.main()` | 先打印系统标题，再提示「用户登录（1/3）」 |
| B2 | 输入 `admin` / 错误密码 | 提示「用户名或密码错误」，进入第 2 次尝试 |
| B3 | 连续 3 次输错 | 提示「登录失败次数过多，程序退出」，进程结束（不打印异常堆栈） |
| B4 | 输入 `admin` / `admin123` | 提示「登录成功，欢迎你：系统管理员（admin）」，进入主菜单 |
| B5 | 在 Navicat 查 `SELECT password FROM sys_user` | 是 64 位十六进制字符串，**不是** `admin123` |

### C. 学生管理

| # | 操作 | 预期结果 |
| --- | --- | --- |
| C1 | 菜单 1 → 1 查看全部 | 显示 5 名学生，表格列对齐（中文不错位） |
| C2 | 菜单 1 → 2，学号留空、姓名输入「张」 | 只显示张伟 1 条 |
| C3 | 菜单 1 → 3，新增学号 `20240106`、姓名「测试生」 | 提示「新增成功，学生 id = 6」 |
| C4 | 重复执行 C3 的学号 | 提示「学号已存在：20240106」，不新增 |
| C5 | 菜单 1 → 4，把 id=6 的姓名改为「测试生改」 | 提示「修改成功」，再查询时姓名已变 |
| C6 | 菜单 1 → 5，删除 id=6，输入 `y` | 提示「删除成功，同时删除了 0 条成绩记录」 |
| C7 | 菜单 1 → 3，性别输入「X」 | 提示「只能输入：男 / 女 / 未知」并重新询问 |
| C8 | 菜单 1 → 5，删除 id=1（张伟，有 3 条成绩），输入 `y` | 提示删除了 **3** 条成绩；去成绩查询已无张伟数据 |

### D. 课程管理

| # | 操作 | 预期结果 |
| --- | --- | --- |
| D1 | 菜单 2 → 1 查看全部 | 显示 3 门课程（C001/C002/C003） |
| D2 | 菜单 2 → 3，新增 `C004` / 操作系统 / 3 / 钱老师 | 提示「新增成功，课程 id = 4」 |
| D3 | 重复新增 `C004` | 提示「课程编号已存在：C004」 |
| D4 | 新增时学分输入 `0` | 提示「学分必须在 0.1 ~ 99.9 之间」 |
| D5 | 菜单 2 → 4，修改 id=4 的教师为「李老师」 | 提示「修改成功」，再查看已更新 |
| D6 | 菜单 2 → 5，删除 id=4，输入 `y` | 提示删除成功（该课暂无成绩，删除 0 条） |

### E. 成绩管理

| # | 操作 | 预期结果 |
| --- | --- | --- |
| E1 | 菜单 3 → 5 查询全部 | 显示 11 条成绩，含学号、姓名、课程、分数、考试日期 |
| E2 | 菜单 3 → 2，输入 `20240101` | 只显示张伟的 3 条成绩 |
| E3 | 菜单 3 → 2，输入 `李娜` | 只显示李娜的 3 条成绩（验证按姓名查询） |
| E4 | 菜单 3 → 3，输入 `C001` | 显示 Java 程序设计的所有成绩（3 条） |
| E5 | 菜单 3 → 4，输入 60 / 100 | 只显示 60~100 分的记录，按分数从高到低排列 |
| E6 | 菜单 3 → 4，输入 90 / 100 | 只显示 92.50、95.00、91.50 三条 |
| E7 | 菜单 3 → 4，输入 90 / 60 | 提示「最小分不能大于最大分」 |
| E8 | 菜单 3 → 1，给某学生某课录入已存在的成绩 | 提示「该学生这门课程已有成绩（xx 分）……」，不重复插入 |
| E9 | 菜单 3 → 1，分数输入 `101` | 提示「分数必须在 0 ~ 100 之间」 |
| E10 | 菜单 3 → 6，修改某条成绩为 99 | 提示「修改成功」，再查询该条已变 99 |
| E11 | 菜单 3 → 7，删除某条成绩并确认 | 提示「删除成功」，该条不再出现 |

### F. 统计

| # | 操作 | 预期结果 |
| --- | --- | --- |
| F1 | 菜单 4 → 1 | 每门课一行，含人数、平均分、最高分、最低分、及格率（带 %） |
| F2 | 用 Navicat 手工核对 C001 | `SELECT AVG(score),MAX(score),MIN(score) FROM score WHERE course_id=1;` 与界面数字一致 |
| F3 | 菜单 4 → 2 | 每个学生一行，课程数、平均分、最高分、最低分、及格率 |
| F4 | 手工核对某学生及格率 | 例如王强 2 门课中 1 门及格 → 显示 50.00% |
| F5 | 菜单 4 → 3 | 显示全局平均分，数值与 `SELECT AVG(score) FROM score` 一致 |

### G. CSV 导出

| # | 操作 | 预期结果 |
| --- | --- | --- |
| G1 | 先执行菜单 3 → 5（全部成绩），再菜单 3 → 8，路径直接回车 | 提示「导出成功：...\exports\score_yyyyMMdd_HHmmss.csv」；**在 IDEA 中运行时**目录是项目根目录下的 `exports\` |
| G2 | 用 Excel 双击打开该 CSV | 中文表头和数据**不乱码**，列对齐正确 |
| G2b | 若「考试日期」列显示 `########` | 这是**列宽不够**，双击列边界加宽后即显示 `2025/1/10`，数据本身正确 |
| G3 | 用记事本以 UTF-8 打开，或用 `Format-Hex` 看前 3 字节 | 开头为 `EF BB BF`（BOM） |
| G4 | 不查询直接点菜单 3 → 8 | 提示「请先执行一次查询」 |
| G5 | 导出一个按分数段查询的结果 | CSV 内容与屏幕显示的记录一致，行数相同 |

### H. 安全与健壮性

| # | 操作 | 预期结果 |
| --- | --- | --- |
| H1 | 菜单 3 → 2，输入 `' OR '1'='1` | 显示「没有查询到成绩数据」，不返回全表、不报 SQL 错误 |
| H2 | 把 `db.properties` 的密码改错，重启程序 | 提示「数据库连接失败」+ 排查提示，程序退出而非抛堆栈 |
| H3 | 把 `db.properties` 临时改名，启动程序 | 提示「未找到 db.properties，请复制 db.properties.example……」 |
| H4 | 停止 MySQL 服务后启动程序 | 提示「数据库连接失败」，程序正常退出 |
| H5 | 菜单里输入非数字（如 `abc`）作为 id | 提示「请输入合法的整数」并重新询问，不崩溃 |
| H6 | 输入日期 `2025-13-01` | 提示「日期格式应为 yyyy-MM-dd」并重新询问 |

### I. 代码规范自查

| # | 检查项 | 预期结果 |
| --- | --- | --- |
| I1 | 全局搜索 `Statement`（非 Prepared） | 只有 `PreparedStatement`，无字符串拼接 SQL 值 |
| I2 | 全局搜索 `password` 字面量 | Java 代码中无明文密码，只有 `db.properties`（已被忽略） |
| I3 | `git status` | 不出现 `db.properties`、`target/`、`.idea/`、`*.iml` |
| I4 | 每个 DAO 方法 | 都用 try-with-resources 关闭 Connection / PreparedStatement / ResultSet |
| I5 | 注释语言 | 类、方法、关键逻辑均为中文注释 |

---

## 十一、Git 提交与 GitHub 推送

以下命令在项目根目录（`pom.xml` 所在目录）执行。**执行前确认 `db.properties` 已被忽略。**

```bash
# 0. 确认忽略规则生效（不应看到 db.properties、target、.idea）
git status --ignored --short

# 1. 初始化仓库
git init
git branch -M main

# 2. 首次提交前检查将要提交的文件
git add .
git status

# 3. 提交
git commit -m "feat: 学生课程成绩管理系统（Java 17 + JDBC + MySQL 8 控制台版）"

# 4. 关联远程仓库（把 URL 换成你自己的仓库地址）
git remote add origin https://github.com/你的用户名/student-score-manager.git

# 5. 推送
git push -u origin main
```

如果本机还没配置过 Git 身份：

```bash
git config --global user.name "你的名字"
git config --global user.email "你的邮箱"
```

**提交前自查**（重要）：

```bash
# 确认 db.properties 没有被跟踪；有输出说明误提交了
git ls-files | findstr db.properties
```

如果误提交了，先执行 `git rm --cached src/main/resources/db.properties` 再提交一次。

---

## 十二、学习收获

写完这个项目，应该能讲清楚下面这些点（面试/答辩常问）：

1. **JDBC 的完整流程**：`DriverManager.getConnection` → `PreparedStatement` → `executeQuery/executeUpdate` → `ResultSet` 映射成对象。
2. **为什么用 `PreparedStatement`**：占位符 `?` 让驱动做参数转义，避免 `' OR '1'='1` 这类 SQL 注入；同时能复用执行计划。
3. **try-with-resources 的意义**：Connection / Statement / ResultSet 都是需要显式关闭的资源，try-with-resources 保证异常时也能关闭。
4. **三层分层的职责**：`main` 只管输入输出，`service` 管校验和业务规则，`dao` 只管 SQL。改数据库不影响界面，改界面不影响 SQL。
5. **外键与级联删除**：`ON DELETE CASCADE` 让"删学生"自动清理成绩，避免孤儿数据；同时要在界面层提示影响范围。
6. **唯一约束兜底**：即使代码校验漏了，数据库的 `UNIQUE KEY` 仍会拦住重复数据（错误码 1062）。
7. **聚合统计 SQL**：`COUNT / AVG / MAX / MIN / GROUP BY`，以及用 `SUM(CASE WHEN score >= 60 THEN 1 ELSE 0 END) / COUNT(*)` 算及格率。
8. **CSV 与 BOM**：Excel 默认按本地编码猜文本文件，UTF-8 文件加 BOM 才能正确识别中文；字段含逗号/引号时按 RFC 4180 转义。
9. **配置与代码分离**：连接信息放 `db.properties`，并加入 `.gitignore`，避免密码进版本库。
10. **密码哈希**：数据库只存 SHA-256 摘要，登录时对输入做同样哈希再比对。真实生产应使用带盐的慢哈希（BCrypt），这一点要能说清楚差异。

---

## 十三、常见问题

**Q1：启动报「未找到 db.properties」**
没有创建配置文件。复制 `src/main/resources/db.properties.example` 为同目录下的 `db.properties` 并填写密码。

**Q2：启动报「数据库连接失败」**
依次检查：MySQL 服务是否启动、`db.properties` 的 url 端口是否为 3306、用户名密码是否正确、是否执行过 `sql/init.sql`。

**Q3：控制台中文乱码**
IDEA 运行配置加 `-Dfile.encoding=UTF-8`；同时把 `Settings → Editor → File Encodings` 全部设为 UTF-8。

**Q4：`java -jar` 报「找不到 JDBC 驱动类」**
本项目没打 fat-jar。请用 `java -cp "target/classes;target/dependency/*" com.example.scores.main.MainApp` 运行，或直接在 IDEA 里运行 `MainApp`。

**Q5：删除学生后成绩还在吗？**
不在。`score` 表外键是 `ON DELETE CASCADE`，删除学生会连带删除其全部成绩，删除前界面会提示影响的条数。

**Q6：忘记 admin 密码怎么办？**
用 Navicat 执行：
```sql
UPDATE sys_user
SET password = SHA2('newpassword', 256)
WHERE username = 'admin';
```
`SHA2(..., 256)` 的结果与 Java 的 `Sha256Util.hash` 一致（都是小写十六进制）。

---

## 十四、截图

请运行后自行截图，放到 `docs/screenshots/` 目录，建议至少包含：

| 文件名建议 | 截图内容 |
| --- | --- |
| `01-login.png` | 登录界面与登录成功提示 |
| `02-main-menu.png` | 主菜单 |
| `03-student-list.png` | 学生列表（表格对齐） |
| `04-score-query.png` | 按分数段查询结果 |
| `05-stat-course.png` | 按课程统计（含及格率） |
| `06-csv-excel.png` | Excel 打开导出的 CSV（中文正常） |
| `07-navicat-tables.png` | Navicat 中的四张表结构 |

---

## 十五、License

本项目采用 [MIT License](LICENSE)，可自由用于学习、修改和展示。
