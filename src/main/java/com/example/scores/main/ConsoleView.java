package com.example.scores.main;

import com.example.scores.pojo.Course;
import com.example.scores.pojo.CourseStat;
import com.example.scores.pojo.ScoreDetail;
import com.example.scores.pojo.Student;
import com.example.scores.pojo.StudentStat;
import com.example.scores.util.ConsoleUtil;

import java.util.List;

/**
 * 控制台表格输出：把 POJO 列表打印成对齐的表格。
 * 所有方法只做展示，不访问数据库。
 */
public class ConsoleView {

    private ConsoleView() {
    }

    /** 打印学生列表 */
    public static void printStudents(List<Student> students) {
        if (students == null || students.isEmpty()) {
            System.out.println("（没有查询到学生数据）");
            return;
        }
        System.out.println(ConsoleUtil.padRight("ID", 5) + ConsoleUtil.padRight("学号", 14)
                + ConsoleUtil.padRight("姓名", 10) + ConsoleUtil.padRight("性别", 6)
                + ConsoleUtil.padRight("专业", 20) + ConsoleUtil.padRight("班级", 12)
                + "电话");
        ConsoleUtil.line();
        for (Student s : students) {
            System.out.println(ConsoleUtil.padRight(String.valueOf(s.getId()), 5)
                    + ConsoleUtil.padRight(s.getStudentNo(), 14)
                    + ConsoleUtil.padRight(s.getName(), 10)
                    + ConsoleUtil.padRight(s.getGender(), 6)
                    + ConsoleUtil.padRight(s.getMajor(), 20)
                    + ConsoleUtil.padRight(s.getClassName(), 12)
                    + ConsoleUtil.nvl(s.getPhone()));
        }
        System.out.println("共 " + students.size() + " 条记录");
    }

    /** 打印课程列表 */
    public static void printCourses(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            System.out.println("（没有查询到课程数据）");
            return;
        }
        System.out.println(ConsoleUtil.padRight("ID", 5) + ConsoleUtil.padRight("课程编号", 12)
                + ConsoleUtil.padRight("课程名称", 20) + ConsoleUtil.padRight("学分", 8) + "任课教师");
        ConsoleUtil.line();
        for (Course c : courses) {
            System.out.println(ConsoleUtil.padRight(String.valueOf(c.getId()), 5)
                    + ConsoleUtil.padRight(c.getCourseNo(), 12)
                    + ConsoleUtil.padRight(c.getName(), 20)
                    + ConsoleUtil.padRight(c.getCredit() == null ? "-" : c.getCredit().toPlainString(), 8)
                    + ConsoleUtil.nvl(c.getTeacher()));
        }
        System.out.println("共 " + courses.size() + " 条记录");
    }

    /** 打印成绩明细列表 */
    public static void printScoreDetails(List<ScoreDetail> details) {
        if (details == null || details.isEmpty()) {
            System.out.println("（没有查询到成绩数据）");
            return;
        }
        System.out.println(ConsoleUtil.padRight("成绩ID", 8) + ConsoleUtil.padRight("学号", 14)
                + ConsoleUtil.padRight("姓名", 10) + ConsoleUtil.padRight("班级", 12)
                + ConsoleUtil.padRight("课程编号", 10) + ConsoleUtil.padRight("课程名称", 18)
                + ConsoleUtil.padRight("学分", 6) + ConsoleUtil.padRight("分数", 8) + "考试日期");
        ConsoleUtil.line();
        for (ScoreDetail d : details) {
            System.out.println(ConsoleUtil.padRight(String.valueOf(d.getScoreId()), 8)
                    + ConsoleUtil.padRight(d.getStudentNo(), 14)
                    + ConsoleUtil.padRight(d.getStudentName(), 10)
                    + ConsoleUtil.padRight(d.getClassName(), 12)
                    + ConsoleUtil.padRight(d.getCourseNo(), 10)
                    + ConsoleUtil.padRight(d.getCourseName(), 18)
                    + ConsoleUtil.padRight(d.getCredit() == null ? "-" : d.getCredit().toPlainString(), 6)
                    + ConsoleUtil.padRight(ConsoleUtil.num(d.getScore()), 8)
                    + (d.getExamDate() == null ? "-" : d.getExamDate().toString()));
        }
        System.out.println("共 " + details.size() + " 条记录");
    }

    /** 打印按课程统计结果 */
    public static void printCourseStats(List<CourseStat> stats) {
        if (stats == null || stats.isEmpty()) {
            System.out.println("（暂无统计数据，请先录入成绩）");
            return;
        }
        System.out.println(ConsoleUtil.padRight("课程编号", 10) + ConsoleUtil.padRight("课程名称", 20)
                + ConsoleUtil.padRight("人数", 6) + ConsoleUtil.padRight("平均分", 9)
                + ConsoleUtil.padRight("最高分", 9) + ConsoleUtil.padRight("最低分", 9) + "及格率");
        ConsoleUtil.line();
        for (CourseStat s : stats) {
            System.out.println(ConsoleUtil.padRight(s.getCourseNo(), 10)
                    + ConsoleUtil.padRight(s.getCourseName(), 20)
                    + ConsoleUtil.padRight(String.valueOf(s.getStudentCount()), 6)
                    + ConsoleUtil.padRight(ConsoleUtil.num(s.getAvgScore()), 9)
                    + ConsoleUtil.padRight(ConsoleUtil.num(s.getMaxScore()), 9)
                    + ConsoleUtil.padRight(ConsoleUtil.num(s.getMinScore()), 9)
                    + ConsoleUtil.percent(s.getPassRate()));
        }
    }

    /** 打印按学生统计结果 */
    public static void printStudentStats(List<StudentStat> stats) {
        if (stats == null || stats.isEmpty()) {
            System.out.println("（暂无统计数据，请先录入成绩）");
            return;
        }
        System.out.println(ConsoleUtil.padRight("学号", 14) + ConsoleUtil.padRight("姓名", 10)
                + ConsoleUtil.padRight("班级", 12) + ConsoleUtil.padRight("课程数", 8)
                + ConsoleUtil.padRight("平均分", 9) + ConsoleUtil.padRight("最高分", 9)
                + ConsoleUtil.padRight("最低分", 9) + "及格率");
        ConsoleUtil.line();
        for (StudentStat s : stats) {
            System.out.println(ConsoleUtil.padRight(s.getStudentNo(), 14)
                    + ConsoleUtil.padRight(s.getStudentName(), 10)
                    + ConsoleUtil.padRight(s.getClassName(), 12)
                    + ConsoleUtil.padRight(String.valueOf(s.getCourseCount()), 8)
                    + ConsoleUtil.padRight(ConsoleUtil.num(s.getAvgScore()), 9)
                    + ConsoleUtil.padRight(ConsoleUtil.num(s.getMaxScore()), 9)
                    + ConsoleUtil.padRight(ConsoleUtil.num(s.getMinScore()), 9)
                    + ConsoleUtil.percent(s.getPassRate()));
        }
    }
}
