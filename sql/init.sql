-- ============================================================
-- 学生课程成绩管理系统 —— 数据库初始化脚本
-- 适用环境：MySQL 8.0 及以上
-- 使用方式（二选一）：
--   1) Navicat：新建连接 -> 打开查询窗口 -> 粘贴本文件全部内容 -> 运行
--   2) 命令行：mysql -u root -p < sql/init.sql
-- 注意：脚本会先删除同名表再重建，重复执行会清空原有数据。
-- ============================================================

-- 1. 创建数据库（utf8mb4 才能正确保存中文与 emoji）
CREATE DATABASE IF NOT EXISTS student_score_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE student_score_db;

-- 2. 删除旧表（先删有外键的子表，再删父表）
DROP TABLE IF EXISTS score;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS sys_user;

-- ============================================================
-- 3. 学生表
-- ============================================================
CREATE TABLE student (
    id         INT          NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    student_no VARCHAR(20)  NOT NULL                COMMENT '学号，唯一',
    name       VARCHAR(50)  NOT NULL                COMMENT '姓名',
    gender     VARCHAR(10)  NOT NULL DEFAULT '未知'  COMMENT '性别：男/女/未知',
    major      VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '专业',
    class_name VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '班级',
    phone      VARCHAR(20)  NOT NULL DEFAULT ''     COMMENT '联系电话',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_student_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='学生信息表';

-- ============================================================
-- 4. 课程表
-- ============================================================
CREATE TABLE course (
    id         INT           NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    course_no  VARCHAR(20)   NOT NULL                COMMENT '课程编号，唯一',
    name       VARCHAR(50)   NOT NULL                COMMENT '课程名称',
    credit     DECIMAL(3, 1) NOT NULL DEFAULT 2.0    COMMENT '学分',
    teacher    VARCHAR(50)   NOT NULL DEFAULT ''     COMMENT '任课教师',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_no (course_no),
    KEY idx_course_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='课程信息表';

-- ============================================================
-- 5. 成绩表
--    一个学生的一门课只允许有一条成绩（唯一索引保证）
-- ============================================================
CREATE TABLE score (
    id         INT           NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    student_id INT           NOT NULL                COMMENT '学生主键，外键 -> student.id',
    course_id  INT           NOT NULL                COMMENT '课程主键，外键 -> course.id',
    score      DECIMAL(5, 2) NOT NULL                COMMENT '分数，0 ~ 100',
    exam_date  DATE          NULL                    COMMENT '考试日期',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course (student_id, course_id),
    KEY idx_score_course (course_id),
    KEY idx_score_value (score),
    CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES student (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_score_course FOREIGN KEY (course_id) REFERENCES course (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_score_range CHECK (score >= 0 AND score <= 100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='学生成绩表';

-- ============================================================
-- 6. 系统用户表（登录用，密码为 SHA-256 哈希，禁止明文）
--    默认账号：admin    默认密码：admin123
--    哈希值 = SHA-256("admin123") 的 64 位小写十六进制
-- ============================================================
CREATE TABLE sys_user (
    id         INT          NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
    username   VARCHAR(50)  NOT NULL                COMMENT '登录用户名，唯一',
    password   CHAR(64)     NOT NULL                COMMENT 'SHA-256 密码哈希（64 位十六进制）',
    real_name  VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '真实姓名',
    role       VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT '角色：ADMIN',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='系统用户表';

-- ============================================================
-- 7. 初始化数据
-- ============================================================

-- 管理员账号：admin / admin123
INSERT INTO sys_user (username, password, real_name, role)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', 'ADMIN');

-- 学生
INSERT INTO student (student_no, name, gender, major, class_name, phone) VALUES
('20240101', '张伟', '男', '计算机应用技术', '计应2401', '13800000001'),
('20240102', '李娜', '女', '计算机应用技术', '计应2401', '13800000002'),
('20240103', '王强', '男', '计算机应用技术', '计应2401', '13800000003'),
('20240104', '赵敏', '女', '计算机应用技术', '计应2402', '13800000004'),
('20240105', '陈杰', '男', '计算机应用技术', '计应2402', '13800000005');

-- 课程
INSERT INTO course (course_no, name, credit, teacher) VALUES
('C001', 'Java 程序设计', 4.0, '刘老师'),
('C002', '数据库原理', 3.0, '孙老师'),
('C003', '计算机网络', 2.0, '周老师');

-- 成绩
INSERT INTO score (student_id, course_id, score, exam_date) VALUES
(1, 1, 92.50, '2025-01-10'),
(1, 2, 88.00, '2025-01-12'),
(1, 3, 76.00, '2025-01-14'),
(2, 1, 95.00, '2025-01-10'),
(2, 2, 91.50, '2025-01-12'),
(2, 3, 83.00, '2025-01-14'),
(3, 1, 58.00, '2025-01-10'),
(3, 2, 62.00, '2025-01-12'),
(4, 1, 71.00, '2025-01-10'),
(4, 3, 66.50, '2025-01-14'),
(5, 2, 49.00, '2025-01-12');

-- ============================================================
-- 8. 自检：确认表结构与初始数据
-- ============================================================
SELECT '学生' AS 表名, COUNT(*) AS 行数 FROM student
UNION ALL
SELECT '课程', COUNT(*) FROM course
UNION ALL
SELECT '成绩', COUNT(*) FROM score
UNION ALL
SELECT '用户', COUNT(*) FROM sys_user;
