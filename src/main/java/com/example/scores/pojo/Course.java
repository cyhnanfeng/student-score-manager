package com.example.scores.pojo;

import java.math.BigDecimal;

/**
 * 课程实体类，对应数据库表 course。
 */
public class Course {

    /** 主键 */
    private Integer id;
    /** 课程编号（唯一） */
    private String courseNo;
    /** 课程名称 */
    private String name;
    /** 学分 */
    private BigDecimal credit;
    /** 任课教师 */
    private String teacher;

    public Course() {
    }

    public Course(Integer id, String courseNo, String name, BigDecimal credit, String teacher) {
        this.id = id;
        this.courseNo = courseNo;
        this.name = name;
        this.credit = credit;
        this.teacher = teacher;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        return "Course{id=" + id + ", courseNo='" + courseNo + "', name='" + name + "'}";
    }
}
