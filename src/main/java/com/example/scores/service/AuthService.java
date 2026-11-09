package com.example.scores.service;

import com.example.scores.dao.UserDao;
import com.example.scores.pojo.User;
import com.example.scores.util.BusinessException;
import com.example.scores.util.Sha256Util;

import java.sql.SQLException;

/**
 * 登录服务：把用户输入的明文密码做 SHA-256 后与数据库哈希比对。
 */
public class AuthService {

    private final UserDao userDao = new UserDao();

    /**
     * 登录校验。
     *
     * @param username 用户名
     * @param rawPassword 明文密码（不会存库）
     * @return 登录成功的用户对象
     * @throws BusinessException 用户名或密码错误
     */
    public User login(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        try {
            User user = userDao.findByUsername(username.trim());
            // 用户不存在和密码错误返回同一句提示，避免泄露账号是否存在
            if (user == null || !Sha256Util.matches(rawPassword, user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }
            return user;
        } catch (SQLException e) {
            throw new BusinessException("登录查询数据库失败：" + e.getMessage(), e);
        }
    }
}
