package com.example.scores.service;

import com.example.scores.dao.StudentDao;
import com.example.scores.pojo.Student;
import com.example.scores.util.BusinessException;

import java.sql.SQLException;
import java.util.List;

/**
 * 学生业务层：负责参数校验和业务规则，再调用 DAO。
 * 校验放在这里，控制台界面只负责收集输入。
 */
public class StudentService {

    private final StudentDao studentDao = new StudentDao();

    /** 新增学生 */
    public int add(Student student) {
        validate(student);
        try {
            if (studentDao.findByStudentNo(student.getStudentNo()) != null) {
                throw new BusinessException("学号已存在：" + student.getStudentNo());
            }
            return studentDao.insert(student);
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw new BusinessException("学号已存在：" + student.getStudentNo());
            }
            throw new BusinessException("新增学生失败：" + e.getMessage(), e);
        }
    }

    /** 修改学生（学号不可改） */
    public void update(Student student) {
        if (student.getId() == null) {
            throw new BusinessException("缺少学生主键，无法修改");
        }
        validate(student);
        try {
            Student old = studentDao.findById(student.getId());
            if (old == null) {
                throw new BusinessException("学生不存在，可能已被删除");
            }
            studentDao.update(student);
        } catch (SQLException e) {
            throw new BusinessException("修改学生失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除学生。
     * 成绩表配置了 ON DELETE CASCADE，删除学生会连带删除其成绩，
     * 所以这里先查出关联成绩条数并返回给界面提示。
     *
     * @return 被级联删除的成绩条数
     */
    public int delete(int id) {
        try {
            Student old = studentDao.findById(id);
            if (old == null) {
                throw new BusinessException("学生不存在，可能已被删除");
            }
            int scoreCount = studentDao.countScores(id);
            studentDao.deleteById(id);
            return scoreCount;
        } catch (SQLException e) {
            throw new BusinessException("删除学生失败：" + e.getMessage(), e);
        }
    }

    /** 按学号/姓名查询，参数为空则查全部 */
    public List<Student> search(String studentNo, String name) {
        try {
            return studentDao.search(studentNo, name);
        } catch (SQLException e) {
            throw new BusinessException("查询学生失败：" + e.getMessage(), e);
        }
    }

    /** 按主键查询 */
    public Student findById(int id) {
        try {
            Student s = studentDao.findById(id);
            if (s == null) {
                throw new BusinessException("未找到 id = " + id + " 的学生");
            }
            return s;
        } catch (SQLException e) {
            throw new BusinessException("查询学生失败：" + e.getMessage(), e);
        }
    }

    /** 基础字段校验 */
    private void validate(Student s) {
        if (s == null) {
            throw new BusinessException("学生信息不能为空");
        }
        if (s.getStudentNo() == null || s.getStudentNo().isBlank()) {
            throw new BusinessException("学号不能为空");
        }
        if (s.getStudentNo().trim().length() > 20) {
            throw new BusinessException("学号长度不能超过 20 个字符");
        }
        if (s.getName() == null || s.getName().isBlank()) {
            throw new BusinessException("姓名不能为空");
        }
        if (s.getName().trim().length() > 50) {
            throw new BusinessException("姓名长度不能超过 50 个字符");
        }
        if (s.getGender() == null || s.getGender().isBlank()) {
            s.setGender("未知");
        }
        if (!"男".equals(s.getGender()) && !"女".equals(s.getGender()) && !"未知".equals(s.getGender())) {
            throw new BusinessException("性别只能是：男 / 女 / 未知");
        }
        if (s.getMajor() == null) {
            s.setMajor("");
        }
        if (s.getClassName() == null) {
            s.setClassName("");
        }
        if (s.getPhone() == null) {
            s.setPhone("");
        }
        if (!s.getPhone().isEmpty() && !s.getPhone().matches("\\d{6,20}")) {
            throw new BusinessException("联系电话只能是 6~20 位数字");
        }
        s.setStudentNo(s.getStudentNo().trim());
        s.setName(s.getName().trim());
    }

    /** 判断是否是唯一键冲突（MySQL 错误码 1062） */
    private boolean isDuplicateKey(SQLException e) {
        return e.getErrorCode() == 1062;
    }
}
