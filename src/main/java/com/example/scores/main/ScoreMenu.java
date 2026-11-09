package com.example.scores.main;

import com.example.scores.pojo.Course;
import com.example.scores.pojo.Score;
import com.example.scores.pojo.ScoreDetail;
import com.example.scores.pojo.Student;
import com.example.scores.service.CourseService;
import com.example.scores.service.ExportService;
import com.example.scores.service.ScoreService;
import com.example.scores.service.StudentService;
import com.example.scores.util.BusinessException;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/**
 * 成绩管理菜单：录入、按学生查询、按课程查询、按分数段查询、修改、删除、导出 CSV。
 */
public class ScoreMenu {

    private final ConsoleInput input;
    private final ScoreService scoreService;
    private final StudentService studentService;
    private final CourseService courseService;
    private final ExportService exportService;

    /** 保存最近一次查询结果，导出时直接使用 */
    private List<ScoreDetail> lastResult;

    public ScoreMenu(ConsoleInput input, ScoreService scoreService, StudentService studentService,
                     CourseService courseService, ExportService exportService) {
        this.input = input;
        this.scoreService = scoreService;
        this.studentService = studentService;
        this.courseService = courseService;
        this.exportService = exportService;
    }

    /** 显示成绩管理菜单 */
    public void show() {
        while (true) {
            System.out.println();
            System.out.println("=== 成绩管理 ===");
            System.out.println("1. 录入成绩");
            System.out.println("2. 按学生查询");
            System.out.println("3. 按课程查询");
            System.out.println("4. 按分数段查询");
            System.out.println("5. 查询全部成绩");
            System.out.println("6. 修改成绩");
            System.out.println("7. 删除成绩");
            System.out.println("8. 导出上次查询结果为 CSV");
            System.out.println("0. 返回主菜单");
            String choice = input.readLine("请选择：");
            try {
                switch (choice) {
                    case "1" -> add();
                    case "2" -> byStudent();
                    case "3" -> byCourse();
                    case "4" -> byRange();
                    case "5" -> {
                        lastResult = scoreService.findAll();
                        ConsoleView.printScoreDetails(lastResult);
                    }
                    case "6" -> update();
                    case "7" -> delete();
                    case "8" -> exportLast();
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("无效选项，请重新选择。");
                }
            } catch (BusinessException e) {
                System.out.println("【操作失败】" + e.getMessage());
            }
            input.pause();
        }
    }

    private void add() {
        List<Student> students = studentService.search(null, null);
        List<Course> courses = courseService.findAll();
        if (students.isEmpty()) {
            System.out.println("还没有学生，请先到“学生管理”新增学生。");
            return;
        }
        if (courses.isEmpty()) {
            System.out.println("还没有课程，请先到“课程管理”新增课程。");
            return;
        }
        ConsoleView.printStudents(students);
        int studentId = input.readInt("请输入学生 id（0 取消）：");
        if (studentId == 0) {
            System.out.println("已取消。");
            return;
        }
        ConsoleView.printCourses(courses);
        int courseId = input.readInt("请输入课程 id（0 取消）：");
        if (courseId == 0) {
            System.out.println("已取消。");
            return;
        }
        BigDecimal scoreValue = input.readRequiredDecimal("分数（0~100）：");
        Score score = new Score();
        score.setStudentId(studentId);
        score.setCourseId(courseId);
        score.setScore(scoreValue);
        score.setExamDate(input.readOptionalDate("考试日期（yyyy-MM-dd，可空）："));
        int id = scoreService.add(score);
        System.out.println("录入成功，成绩 id = " + id);
    }

    private void byStudent() {
        String keyword = input.readRequired("请输入学号或姓名（支持模糊）：");
        lastResult = scoreService.findByStudent(keyword);
        ConsoleView.printScoreDetails(lastResult);
    }

    private void byCourse() {
        String keyword = input.readRequired("请输入课程编号或名称（支持模糊）：");
        lastResult = scoreService.findByCourse(keyword);
        ConsoleView.printScoreDetails(lastResult);
    }

    private void byRange() {
        BigDecimal min = input.readRequiredDecimal("最低分（含）：");
        BigDecimal max = input.readRequiredDecimal("最高分（含）：");
        lastResult = scoreService.findByScoreRange(min, max);
        ConsoleView.printScoreDetails(lastResult);
    }

    private void update() {
        String keyword = input.readLine("请输入学号或姓名定位成绩（可空表示查看全部）：");
        lastResult = keyword.isEmpty() ? scoreService.findAll() : scoreService.findByStudent(keyword);
        ConsoleView.printScoreDetails(lastResult);
        if (lastResult.isEmpty()) {
            return;
        }
        int id = input.readInt("请输入要修改的成绩 id（0 取消）：");
        if (id == 0) {
            System.out.println("已取消。");
            return;
        }
        Score score = new Score();
        score.setId(id);
        score.setScore(input.readRequiredDecimal("新分数（0~100）："));
        score.setExamDate(input.readOptionalDate("新考试日期（yyyy-MM-dd，可空）："));
        scoreService.update(score);
        System.out.println("修改成功。");
    }

    private void delete() {
        String keyword = input.readLine("请输入学号或姓名定位成绩（可空表示查看全部）：");
        lastResult = keyword.isEmpty() ? scoreService.findAll() : scoreService.findByStudent(keyword);
        ConsoleView.printScoreDetails(lastResult);
        if (lastResult.isEmpty()) {
            return;
        }
        int id = input.readInt("请输入要删除的成绩 id（0 取消）：");
        if (id == 0) {
            System.out.println("已取消。");
            return;
        }
        if (!input.confirm("确认删除该条成绩？")) {
            System.out.println("已取消。");
            return;
        }
        scoreService.delete(id);
        System.out.println("删除成功。");
    }

    private void exportLast() {
        if (lastResult == null || lastResult.isEmpty()) {
            System.out.println("请先执行一次查询（第 2~5 项），再导出结果。");
            return;
        }
        Path defaultFile = exportService.defaultExportFile();
        String custom = input.readLine("导出路径（直接回车使用 " + defaultFile + "）：");
        Path file = custom.isEmpty() ? defaultFile : Path.of(custom);
        Path saved = exportService.exportScores(file, lastResult);
        System.out.println("导出成功：" + saved);
        System.out.println("提示：CSV 使用 UTF-8 带 BOM 编码，可直接用 Excel 打开。");
    }
}
