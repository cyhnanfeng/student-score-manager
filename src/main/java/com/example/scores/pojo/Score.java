package com.example.scores.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成绩实体类，对应数据库表 score。
 * 保存的是外键 id，展示时使用 {@link ScoreDetail}。
 */
public class Score {

    /** 主键 */
    private Integer id;
    /** 学生主键 */
    private Integer studentId;
    /** 课程主键 */
    private Integer courseId;
    /** 分数，0 ~ 100 */
    private BigDecimal score;
    /** 考试日期，可为空 */
    private LocalDate examDate;

    public Score() {
    }

    public Score(Integer id, Integer studentId, Integer courseId, BigDecimal score, LocalDate examDate) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.score = score;
        this.examDate = examDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    @Override
    public String toString() {
        return "Score{id=" + id + ", studentId=" + studentId + ", courseId=" + courseId
                + ", score=" + score + '}';
    }
}
