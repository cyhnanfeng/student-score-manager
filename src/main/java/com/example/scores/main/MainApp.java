package com.example.scores.main;

import com.example.scores.db.DBUtil;
import com.example.scores.pojo.User;
import com.example.scores.service.AuthService;
import com.example.scores.service.CourseService;
import com.example.scores.service.ExportService;
import com.example.scores.service.ScoreService;
import com.example.scores.service.StatService;
import com.example.scores.service.StudentService;
import com.example.scores.util.BusinessException;
import com.example.scores.util.ConsoleUtil;
import com.example.scores.util.InputClosedException;

import java.util.Scanner;

/**
 * 程序入口：控制台版学生课程成绩管理系统。
 * <p>
 * 启动流程：测试数据库连接 -> 登录（SHA-256 校验） -> 主菜单循环。
 * 运行方式：在 IDEA 中右键运行本类的 main 方法，
 * 或命令行 java -cp "target/classes;target/dependency/*" com.example.scores.main.MainApp
 */
public class MainApp {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public static void main(String[] args) {
        // 控制台中文输出：Windows 终端若乱码，可在 IDEA 的 Run 配置里加 -Dfile.encoding=UTF-8
        printBanner();

        if (!DBUtil.testConnection()) {
            System.out.println("程序退出：数据库不可用。");
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleInput input = new ConsoleInput(scanner);

            User user = login(input);
            if (user == null) {
                System.out.println("登录失败次数过多，程序退出。");
                return;
            }
            System.out.println();
            System.out.println("登录成功，欢迎你：" + user.getRealName() + "（" + user.getUsername() + "）");
            mainMenu(input, user);
        } catch (InputClosedException e) {
            // 标准输入被关闭（脚本输入结束 / Ctrl+Z / 终端断开）：正常退出，不打印堆栈
            System.out.println("检测到输入已结束，程序退出。");
            return;
        }
        System.out.println("已退出系统，再见。");
    }

    /** 登录，最多尝试 3 次 */
    private static User login(ConsoleInput input) {
        AuthService authService = new AuthService();
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            System.out.println();
            System.out.println("=== 用户登录（" + attempt + "/" + MAX_LOGIN_ATTEMPTS + "）===");
            String username = input.readLine("用户名：");
            String password = input.readLine("密码：");
            try {
                return authService.login(username, password);
            } catch (BusinessException e) {
                System.out.println("【登录失败】" + e.getMessage());
            }
        }
        return null;
    }

    /** 主菜单循环 */
    private static void mainMenu(ConsoleInput input, User user) {
        StudentService studentService = new StudentService();
        CourseService courseService = new CourseService();
        ScoreService scoreService = new ScoreService();
        StatService statService = new StatService();
        ExportService exportService = new ExportService();

        StudentMenu studentMenu = new StudentMenu(input, studentService);
        CourseMenu courseMenu = new CourseMenu(input, courseService);
        ScoreMenu scoreMenu = new ScoreMenu(input, scoreService, studentService, courseService, exportService);
        StatMenu statMenu = new StatMenu(input, statService);

        while (true) {
            System.out.println();
            ConsoleUtil.line();
            System.out.println("  学生课程成绩管理系统  |  当前用户：" + user.getRealName());
            ConsoleUtil.line();
            System.out.println("1. 学生管理");
            System.out.println("2. 课程管理");
            System.out.println("3. 成绩管理");
            System.out.println("4. 成绩统计");
            System.out.println("0. 退出系统");
            String choice = input.readLine("请选择：");
            switch (choice) {
                case "1" -> studentMenu.show();
                case "2" -> courseMenu.show();
                case "3" -> scoreMenu.show();
                case "4" -> statMenu.show();
                case "0" -> {
                    return;
                }
                default -> System.out.println("无效选项，请重新选择。");
            }
        }
    }

    private static void printBanner() {
        System.out.println("============================================================");
        System.out.println("            学生课程成绩管理系统 v1.0");
        System.out.println("            Java 17 + JDBC + MySQL 8（控制台版）");
        System.out.println("============================================================");
    }
}
