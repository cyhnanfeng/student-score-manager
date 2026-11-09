package com.example.scores.dao;

import com.example.scores.db.DBUtil;
import com.example.scores.pojo.Score;
import com.example.scores.pojo.ScoreDetail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 成绩数据访问对象：score 表的增删改查，以及成绩明细的联表查询。
 */
public class ScoreDao {

    /** 三表联查的公共 SELECT 片段，避免每处重复书写 */
    private static final String DETAIL_COLUMNS =
            "SELECT s.id AS score_id, st.id AS student_id, st.student_no, st.name AS student_name, "
                    + "st.class_name, c.id AS course_id, c.course_no, c.name AS course_name, c.credit, "
                    + "s.score, s.exam_date "
                    + "FROM score s "
                    + "JOIN student st ON s.student_id = st.id "
                    + "JOIN course c ON s.course_id = c.id ";

    /** 新增成绩，返回主键 */
    public int insert(Score score) throws SQLException {
        String sql = "INSERT INTO score (student_id, course_id, score, exam_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, score.getStudentId());
            ps.setInt(2, score.getCourseId());
            ps.setBigDecimal(3, score.getScore());
            setDate(ps, 4, score.getExamDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /** 按主键更新成绩（学生和课程不可改，只改分数与考试日期） */
    public int update(Score score) throws SQLException {
        String sql = "UPDATE score SET score = ?, exam_date = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, score.getScore());
            setDate(ps, 2, score.getExamDate());
            ps.setInt(3, score.getId());
            return ps.executeUpdate();
        }
    }

    /** 按主键删除成绩 */
    public int deleteById(int id) throws SQLException {
        String sql = "DELETE FROM score WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** 按主键查询成绩（不带学生、课程信息） */
    public Score findById(int id) throws SQLException {
        String sql = "SELECT id, student_id, course_id, score, exam_date FROM score WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Score s = new Score();
                    s.setId(rs.getInt("id"));
                    s.setStudentId(rs.getInt("student_id"));
                    s.setCourseId(rs.getInt("course_id"));
                    s.setScore(rs.getBigDecimal("score"));
                    Date d = rs.getDate("exam_date");
                    s.setExamDate(d == null ? null : d.toLocalDate());
                    return s;
                }
            }
        }
        return null;
    }

    /** 判断某学生某课程是否已有成绩，用于录入前的重复校验 */
    public Score findByStudentAndCourse(int studentId, int courseId) throws SQLException {
        String sql = "SELECT id, student_id, course_id, score, exam_date FROM score "
                + "WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Score s = new Score();
                    s.setId(rs.getInt("id"));
                    s.setStudentId(rs.getInt("student_id"));
                    s.setCourseId(rs.getInt("course_id"));
                    s.setScore(rs.getBigDecimal("score"));
                    Date d = rs.getDate("exam_date");
                    s.setExamDate(d == null ? null : d.toLocalDate());
                    return s;
                }
            }
        }
        return null;
    }

    /** 全部成绩明细 */
    public List<ScoreDetail> findAllDetails() throws SQLException {
        return queryDetails(DETAIL_COLUMNS + "ORDER BY st.student_no, c.course_no", List.of());
    }

    /** 按学生查询成绩（学号或姓名模糊匹配） */
    public List<ScoreDetail> findByStudent(String keyword) throws SQLException {
        String sql = DETAIL_COLUMNS
                + "WHERE st.student_no LIKE CONCAT('%', ?, '%') OR st.name LIKE CONCAT('%', ?, '%') "
                + "ORDER BY st.student_no, c.course_no";
        return queryDetails(sql, List.of(keyword, keyword));
    }

    /** 按课程查询成绩（课程编号或名称模糊匹配） */
    public List<ScoreDetail> findByCourse(String keyword) throws SQLException {
        String sql = DETAIL_COLUMNS
                + "WHERE c.course_no LIKE CONCAT('%', ?, '%') OR c.name LIKE CONCAT('%', ?, '%') "
                + "ORDER BY c.course_no, st.student_no";
        return queryDetails(sql, List.of(keyword, keyword));
    }

    /** 按分数段查询（含边界） */
    public List<ScoreDetail> findByScoreRange(BigDecimal min, BigDecimal max) throws SQLException {
        String sql = DETAIL_COLUMNS
                + "WHERE s.score >= ? AND s.score <= ? "
                + "ORDER BY s.score DESC, st.student_no";
        List<Object> params = List.of(min, max);
        return queryDetails(sql, params);
    }

    /**
     * 组合查询：学号/姓名、课程、分数段都可为空。
     * 用 StringBuilder 动态拼接 SQL 片段，但值始终走 ? 占位符。
     */
    public List<ScoreDetail> search(String studentKeyword, String courseKeyword,
                                    BigDecimal minScore, BigDecimal maxScore) throws SQLException {
        StringBuilder sql = new StringBuilder(DETAIL_COLUMNS).append("WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (studentKeyword != null && !studentKeyword.isBlank()) {
            sql.append(" AND (st.student_no LIKE CONCAT('%', ?, '%') OR st.name LIKE CONCAT('%', ?, '%'))");
            params.add(studentKeyword.trim());
            params.add(studentKeyword.trim());
        }
        if (courseKeyword != null && !courseKeyword.isBlank()) {
            sql.append(" AND (c.course_no LIKE CONCAT('%', ?, '%') OR c.name LIKE CONCAT('%', ?, '%'))");
            params.add(courseKeyword.trim());
            params.add(courseKeyword.trim());
        }
        if (minScore != null) {
            sql.append(" AND s.score >= ?");
            params.add(minScore);
        }
        if (maxScore != null) {
            sql.append(" AND s.score <= ?");
            params.add(maxScore);
        }
        sql.append(" ORDER BY st.student_no, c.course_no");
        return queryDetails(sql.toString(), params);
    }

    /** 通用的明细查询执行方法 */
    private List<ScoreDetail> queryDetails(String sql, List<?> params) throws SQLException {
        List<ScoreDetail> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDetail(rs));
                }
            }
        }
        return list;
    }

    private ScoreDetail mapDetail(ResultSet rs) throws SQLException {
        ScoreDetail d = new ScoreDetail();
        d.setScoreId(rs.getInt("score_id"));
        d.setStudentId(rs.getInt("student_id"));
        d.setStudentNo(rs.getString("student_no"));
        d.setStudentName(rs.getString("student_name"));
        d.setClassName(rs.getString("class_name"));
        d.setCourseId(rs.getInt("course_id"));
        d.setCourseNo(rs.getString("course_no"));
        d.setCourseName(rs.getString("course_name"));
        d.setCredit(rs.getBigDecimal("credit"));
        d.setScore(rs.getBigDecimal("score"));
        Date examDate = rs.getDate("exam_date");
        d.setExamDate(examDate == null ? null : examDate.toLocalDate());
        return d;
    }

    /** LocalDate 写入数据库；null 时写 SQL NULL */
    private void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }
}
