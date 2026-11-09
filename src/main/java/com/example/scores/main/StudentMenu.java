package com.example.scores.main;

import com.example.scores.pojo.Student;
import com.example.scores.service.StudentService;
import com.example.scores.util.BusinessException;

import java.util.List;

/**
 * 学生管理菜单：增、删、改、查（按学号/姓名）。
 */
public class StudentMenu {

    private final ConsoleInput input;
    private final StudentService studentService;

    public StudentMenu(ConsoleInput input, StudentService studentService) {
        this.input = input;
        this.studentService = studentService;
    }

    /** 显示学生管理菜单，返回时回到主菜单 */
    public void show() {
        while (true) {
            System.out.println();
            System.out.println("=== 学生管理 ===");
            System.out.println("1. 查看全部学生");
            System.out.println("2. 按学号/姓名查询");
            System.out.println("3. 新增学生");
            System.out.println("4. 修改学生");
            System.out.println("5. 删除学生");
            System.out.println("0. 返回主菜单");
            String choice = input.readLine("请选择：");
            try {
                switch (choice) {
                    case "1" -> ConsoleView.printStudents(studentService.search(null, null));
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
        String studentNo = input.readLine("学号（模糊，可空）：");
        String name = input.readLine("姓名（模糊，可空）：");
        if (studentNo.isEmpty() && name.isEmpty()) {
            System.out.println("两个条件都为空，将显示全部学生。");
        }
        ConsoleView.printStudents(studentService.search(studentNo, name));
    }

    private void add() {
        Student s = new Student();
        s.setStudentNo(input.readRequired("学号："));
        s.setName(input.readRequired("姓名："));
        s.setGender(readGender());
        s.setMajor(input.readLine("专业（可空）："));
        s.setClassName(input.readLine("班级（可空）："));
        s.setPhone(input.readLine("联系电话（可空）："));
        int id = studentService.add(s);
        System.out.println("新增成功，学生 id = " + id);
    }

    private void update() {
        List<Student> all = studentService.search(null, null);
        if (all.isEmpty()) {
            System.out.println("当前没有学生，无法修改。");
            return;
        }
        ConsoleView.printStudents(all);
        int id = input.readInt("请输入要修改的学生 id（0 取消）：");
        if (id == 0) {
            System.out.println("已取消。");
            return;
        }
        Student old = studentService.findById(id);
        System.out.println("当前信息：学号 " + old.getStudentNo() + "，姓名 " + old.getName()
                + "，性别 " + old.getGender() + "，专业 " + old.getMajor()
                + "，班级 " + old.getClassName() + "，电话 " + old.getPhone());
        System.out.println("提示：学号不可修改，直接回车表示保留原值。");

        String name = input.readLine("新姓名（原 " + old.getName() + "）：");
        if (!name.isEmpty()) {
            old.setName(name);
        }
        String gender = input.readLine("新性别（男/女/未知，原 " + old.getGender() + "）：");
        if (!gender.isEmpty()) {
            old.setGender(gender);
        }
        String major = input.readLine("新专业（原 " + old.getMajor() + "）：");
        if (!major.isEmpty()) {
            old.setMajor(major);
        }
        String className = input.readLine("新班级（原 " + old.getClassName() + "）：");
        if (!className.isEmpty()) {
            old.setClassName(className);
        }
        String phone = input.readLine("新电话（原 " + old.getPhone() + "）：");
        if (!phone.isEmpty()) {
            old.setPhone(phone);
        }
        studentService.update(old);
        System.out.println("修改成功。");
    }

    private void delete() {
        List<Student> all = studentService.search(null, null);
        if (all.isEmpty()) {
            System.out.println("当前没有学生，无法删除。");
            return;
        }
        ConsoleView.printStudents(all);
        int id = input.readInt("请输入要删除的学生 id（0 取消）：");
        if (id == 0) {
            System.out.println("已取消。");
            return;
        }
        Student old = studentService.findById(id);
        System.out.println("即将删除：" + old.getStudentNo() + " " + old.getName()
                + "，其成绩记录也会一并删除。");
        if (!input.confirm("确认删除？")) {
            System.out.println("已取消。");
            return;
        }
        int deletedScores = studentService.delete(id);
        System.out.println("删除成功，同时删除了 " + deletedScores + " 条成绩记录。");
    }

    /** 读取并校验性别输入 */
    private String readGender() {
        while (true) {
            String value = input.readLine("性别（男/女/未知，直接回车为未知）：");
            if (value.isEmpty()) {
                return "未知";
            }
            if ("男".equals(value) || "女".equals(value) || "未知".equals(value)) {
                return value;
            }
            System.out.println("只能输入：男 / 女 / 未知");
        }
    }
}
