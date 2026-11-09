package com.example.scores.dao;

import com.example.scores.db.DBUtil;
import com.example.scores.pojo.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户数据访问对象：只负责 sys_user 表的 SQL 操作。
 */
public class UserDao {

    /**
     * 按用户名查询用户（含密码哈希）。
     *
     * @param username 用户名
     * @return 找到返回 User，找不到返回 null
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, real_name, role FROM sys_user WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("real_name"),
                            rs.getString("role"));
                }
            }
        }
        return null;
    }
}
