package com.example.scores.dao;

import com.example.scores.db.DBUtil;
import com.example.scores.pojo.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生数据访问对象：student 表的增删改查。
 * 所有 SQL 都使用 PreparedStatement 占位符，防止 SQL 注入。
 */
public class StudentDao {

    /**
     * 新增学生。
     *
     * @return 数据库生成的主键
     */
    public int insert(Student student) throws SQLException {
        String sql = "INSERT INTO student (student_no, name, gender, major, class_name, phone) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getStudentNo());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            ps.setString(4, student.getMajor());
            ps.setString(5, student.getClassName());
            ps.setString(6, student.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * 按主键更新学生信息（学号不允许修改，避免影响已有成绩关联）。
     *
     * @return 影响行数
     */
    public int update(Student student) throws SQLException {
        String sql = "UPDATE student SET name = ?, gender = ?, major = ?, class_name = ?, phone = ? "
                + "WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getGender());
            ps.setString(3, student.getMajor());
            ps.setString(4, student.getClassName());
            ps.setString(5, student.getPhone());
            ps.setInt(6, student.getId());
            return ps.executeUpdate();
        }
    }

    /**
     * 按主键删除学生。成绩表设置了 ON DELETE CASCADE，会一并删除该生成绩。
     *
     * @return 影响行数
     */
    public int deleteById(int id) throws SQLException {
        String sql = "DELETE FROM student WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** 按主键查询 */
    public Student findById(int id) throws SQLException {
        String sql = "SELECT id, student_no, name, gender, major, class_name, phone FROM student WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /** 按学号精确查询 */
    public Student findByStudentNo(String studentNo) throws SQLException {
        String sql = "SELECT id, student_no, name, gender, major, class_name, phone FROM student "
                + "WHERE student_no = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * 按学号或姓名模糊查询；两个参数都为空时返回全部学生。
     * 使用 LIKE + CONCAT，占位符仍然是 ?，不会拼接用户输入。
     */
    public List<Student> search(String studentNo, String name) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, student_no, name, gender, major, class_name, phone FROM student WHERE 1 = 1");
        List<String> params = new ArrayList<>();
        if (studentNo != null && !studentNo.isBlank()) {
            sql.append(" AND student_no LIKE CONCAT('%', ?, '%')");
            params.add(studentNo.trim());
        }
        if (name != null && !name.isBlank()) {
            sql.append(" AND name LIKE CONCAT('%', ?, '%')");
            params.add(name.trim());
        }
        sql.append(" ORDER BY student_no");

        List<Student> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** 查询全部学生 */
    public List<Student> findAll() throws SQLException {
        return search(null, null);
    }

    /** 统计该学生已录入的成绩条数，用于删除前提示 */
    public int countScores(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM score WHERE student_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setStudentNo(rs.getString("student_no"));
        s.setName(rs.getString("name"));
        s.setGender(rs.getString("gender"));
        s.setMajor(rs.getString("major"));
        s.setClassName(rs.getString("class_name"));
        s.setPhone(rs.getString("phone"));
        return s;
    }
}
