package com.example.scores.main;

import com.example.scores.pojo.Course;
import com.example.scores.service.CourseService;
import com.example.scores.util.BusinessException;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程管理菜单：增、删、改、查。
 */
public class CourseMenu {

    private final ConsoleInput input;
    private final CourseService courseService;

    public CourseMenu(ConsoleInput input, CourseService courseService) {
        this.input = input;
        this.courseService = courseService;
    }

    /** 显示课程管理菜单 */
    public void show() {
        while (true) {
            System.out.println();
            System.out.println("=== 课程管理 ===");
            System.out.println("1. 查看全部课程");
            System.out.println("2. 按编号/名称查询");
            System.out.println("3. 新增课程");
            System.out.println("4. 修改课程");
            System.out.println("5. 删除课程");
            System.out.println("0. 返回主菜单");
            String choice = input.readLine("请选择：");
            try {
                switch (choice) {
                    case "1" -> ConsoleView.printCourses(courseService.findAll());
                    case "2" -> search();
                    case "3" -> add();
                    case "4" -> update();
                    case "5" -> delete();
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

    private void search() {
        String courseNo = input.readLine("课程编号（模糊，可空）：");
        String name = input.readLine("课程名称（模糊，可空）：");
        ConsoleView.printCourses(courseService.search(courseNo, name));
    }

    private void add() {
        Course c = new Course();
        c.setCourseNo(input.readRequired("课程编号（如 C004）："));
        c.setName(input.readRequired("课程名称："));
        c.setCredit(input.readRequiredDecimal("学分（如 3 或 2.5）："));
        c.setTeacher(input.readLine("任课教师（可空）："));
        int id = courseService.add(c);
        System.out.println("新增成功，课程 id = " + id);
    }

    private void update() {
        List<Course> all = courseService.findAll();
        if (all.isEmpty()) {
            System.out.println("当前没有课程，无法修改。");
            return;
        }
        ConsoleView.printCourses(all);
        int id = input.readInt("请输入要修改的课程 id（0 取消）：");
        if (id == 0) {
            System.out.println("已取消。");
            return;
        }
        Course old = courseService.findById(id);
        System.out.println("当前信息：编号 " + old.getCourseNo() + "，名称 " + old.getName()
                + "，学分 " + old.getCredit() + "，教师 " + old.getTeacher());
        System.out.println("提示：课程编号不可修改，直接回车表示保留原值。");

        String name = input.readLine("新课程名称（原 " + old.getName() + "）：");
        if (!name.isEmpty()) {
            old.setName(name);
        }
        BigDecimal credit = input.readOptionalDecimal("新学分（原 " + old.getCredit() + "）：");
        if (credit != null) {
            old.setCredit(credit);
        }
        String teacher = input.readLine("新任课教师（原 " + old.getTeacher() + "）：");
        if (!teacher.isEmpty()) {
            old.setTeacher(teacher);
        }
        courseService.update(old);
        System.out.println("修改成功。");
    }

    private void delete() {
        List<Course> all = courseService.findAll();
        if (all.isEmpty()) {
            System.out.println("当前没有课程，无法删除。");
            return;
        }
        ConsoleView.printCourses(all);
        int id = input.readInt("请输入要删除的课程 id（0 取消）：");
        if (id == 0) {
            System.out.println("已取消。");
            return;
        }
        Course old = courseService.findById(id);
        System.out.println("即将删除：" + old.getCourseNo() + " " + old.getName()
                + "，该课程下的成绩也会一并删除。");
        if (!input.confirm("确认删除？")) {
            System.out.println("已取消。");
            return;
        }
        int deletedScores = courseService.delete(id);
        System.out.println("删除成功，同时删除了 " + deletedScores + " 条成绩记录。");
    }
}
