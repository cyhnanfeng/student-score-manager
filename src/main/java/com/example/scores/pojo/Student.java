package com.example.scores.pojo;

/**
 * 学生实体类，对应数据库表 student。
 */
public class Student {

    /** 主键 */
    private Integer id;
    /** 学号（唯一） */
    private String studentNo;
    /** 姓名 */
    private String name;
    /** 性别：男 / 女 / 未知 */
    private String gender;
    /** 专业 */
    private String major;
    /** 班级 */
    private String className;
    /** 联系电话 */
    private String phone;

    public Student() {
    }

    public Student(Integer id, String studentNo, String name, String gender,
                   String major, String className, String phone) {
        this.id = id;
        this.studentNo = studentNo;
        this.name = name;
        this.gender = gender;
        this.major = major;
        this.className = className;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", studentNo='" + studentNo + "', name='" + name + "'}";
    }
}
