package com.example.scores.dao;

import com.example.scores.db.DBUtil;
import com.example.scores.pojo.CourseStat;
import com.example.scores.pojo.StudentStat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计查询 DAO：只做 SELECT + 聚合函数，不修改数据。
 * <p>
 * 及格率 = 分数 >= 60 的条数 / 总条数 * 100，用 SUM(CASE WHEN ...) 实现，
 * 保留两位小数，避免除零错误。
 */
public class StatDao {

    /** 按课程统计：平均分、最高分、最低分、及格率 */
    public List<CourseStat> statByCourse() throws SQLException {
        String sql = "SELECT c.course_no, c.name AS course_name, "
                + "COUNT(s.id) AS student_count, "
                + "ROUND(AVG(s.score), 2) AS avg_score, "
                + "MAX(s.score) AS max_score, "
                + "MIN(s.score) AS min_score, "
                + "ROUND(SUM(CASE WHEN s.score >= 60 THEN 1 ELSE 0 END) / COUNT(s.id) * 100, 2) AS pass_rate "
                + "FROM course c "
                + "JOIN score s ON s.course_id = c.id "
                + "GROUP BY c.id, c.course_no, c.name "
                + "ORDER BY c.course_no";
        List<CourseStat> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CourseStat stat = new CourseStat();
                stat.setCourseNo(rs.getString("course_no"));
                stat.setCourseName(rs.getString("course_name"));
                stat.setStudentCount(rs.getInt("student_count"));
                stat.setAvgScore(rs.getBigDecimal("avg_score"));
                stat.setMaxScore(rs.getBigDecimal("max_score"));
                stat.setMinScore(rs.getBigDecimal("min_score"));
                stat.setPassRate(rs.getBigDecimal("pass_rate"));
                list.add(stat);
            }
        }
        return list;
    }

    /** 按学生统计：平均分、最高分、最低分、及格率 */
    public List<StudentStat> statByStudent() throws SQLException {
        String sql = "SELECT st.student_no, st.name AS student_name, st.class_name, "
                + "COUNT(s.id) AS course_count, "
                + "ROUND(AVG(s.score), 2) AS avg_score, "
                + "MAX(s.score) AS max_score, "
                + "MIN(s.score) AS min_score, "
                + "ROUND(SUM(CASE WHEN s.score >= 60 THEN 1 ELSE 0 END) / COUNT(s.id) * 100, 2) AS pass_rate "
                + "FROM student st "
                + "JOIN score s ON s.student_id = st.id "
                + "GROUP BY st.id, st.student_no, st.name, st.class_name "
                + "ORDER BY st.student_no";
        List<StudentStat> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StudentStat stat = new StudentStat();
                stat.setStudentNo(rs.getString("student_no"));
                stat.setStudentName(rs.getString("student_name"));
                stat.setClassName(rs.getString("class_name"));
                stat.setCourseCount(rs.getInt("course_count"));
                stat.setAvgScore(rs.getBigDecimal("avg_score"));
                stat.setMaxScore(rs.getBigDecimal("max_score"));
                stat.setMinScore(rs.getBigDecimal("min_score"));
                stat.setPassRate(rs.getBigDecimal("pass_rate"));
                list.add(stat);
            }
        }
        return list;
    }

    /** 全局统计：总成绩条数、总平均分 */
    public BigDecimal[] overall() throws SQLException {
        String sql = "SELECT COUNT(*) AS total, ROUND(AVG(score), 2) AS avg_score FROM score";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal total = BigDecimal.valueOf(rs.getLong("total"));
                BigDecimal avg = rs.getBigDecimal("avg_score");
                return new BigDecimal[]{total, avg};
            }
        }
        return new BigDecimal[]{BigDecimal.ZERO, null};
    }
}
