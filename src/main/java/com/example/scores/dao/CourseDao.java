package com.example.scores.dao;

import com.example.scores.db.DBUtil;
import com.example.scores.pojo.Course;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程数据访问对象：course 表的增删改查。
 */
public class CourseDao {

    /** 新增课程，返回主键 */
    public int insert(Course course) throws SQLException {
        String sql = "INSERT INTO course (course_no, name, credit, teacher) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getCourseNo());
            ps.setString(2, course.getName());
            ps.setBigDecimal(3, course.getCredit());
            ps.setString(4, course.getTeacher());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /** 按主键更新课程 */
    public int update(Course course) throws SQLException {
        String sql = "UPDATE course SET name = ?, credit = ?, teacher = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getName());
            ps.setBigDecimal(2, course.getCredit());
            ps.setString(3, course.getTeacher());
            ps.setInt(4, course.getId());
            return ps.executeUpdate();
        }
    }

    /** 按主键删除课程（成绩会级联删除） */
    public int deleteById(int id) throws SQLException {
        String sql = "DELETE FROM course WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** 按主键查询 */
    public Course findById(int id) throws SQLException {
        String sql = "SELECT id, course_no, name, credit, teacher FROM course WHERE id = ?";
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

    /** 按课程编号精确查询 */
    public Course findByCourseNo(String courseNo) throws SQLException {
        String sql = "SELECT id, course_no, name, credit, teacher FROM course WHERE course_no = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /** 按课程编号/名称模糊查询，都为空时返回全部 */
    public List<Course> search(String courseNo, String name) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, course_no, name, credit, teacher FROM course WHERE 1 = 1");
        List<String> params = new ArrayList<>();
        if (courseNo != null && !courseNo.isBlank()) {
            sql.append(" AND course_no LIKE CONCAT('%', ?, '%')");
            params.add(courseNo.trim());
        }
        if (name != null && !name.isBlank()) {
            sql.append(" AND name LIKE CONCAT('%', ?, '%')");
            params.add(name.trim());
        }
        sql.append(" ORDER BY course_no");

        List<Course> list = new ArrayList<>();
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

    /** 查询全部课程 */
    public List<Course> findAll() throws SQLException {
        return search(null, null);
    }

    /** 统计该课程已录入的成绩条数，用于删除前提示 */
    public int countScores(int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM score WHERE course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setCourseNo(rs.getString("course_no"));
        c.setName(rs.getString("name"));
        BigDecimal credit = rs.getBigDecimal("credit");
        c.setCredit(credit);
        c.setTeacher(rs.getString("teacher"));
        return c;
    }
}
