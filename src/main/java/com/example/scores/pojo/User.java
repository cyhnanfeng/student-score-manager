package com.example.scores.pojo;

/**
 * 系统用户实体类，对应数据库表 sys_user。
 * 注意：password 字段保存的是 SHA-256 哈希值，不是明文密码。
 */
public class User {

    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String role;

    public User() {
    }

    public User(Integer id, String username, String password, String realName, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', realName='" + realName + "', role='" + role + "'}";
    }
}
