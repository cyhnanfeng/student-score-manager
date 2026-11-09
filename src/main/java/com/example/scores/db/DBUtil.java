package com.example.scores.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类。
 * <p>
 * 连接参数从 classpath 下的 db.properties 读取，Java 代码中不出现账号密码。
 * 只提供“取连接”的静态方法，连接由调用方用 try-with-resources 关闭。
 */
public final class DBUtil {

    private static final String CONFIG_FILE = "db.properties";

    /** 配置只加载一次 */
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "未找到 " + CONFIG_FILE + "，请复制 src/main/resources/db.properties.example "
                                + "并重命名为 db.properties，然后填写数据库密码。");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("读取 " + CONFIG_FILE + " 失败：" + e.getMessage(), e);
        }
        // JDBC 4.0 之后驱动可自动注册，这里显式加载一次，便于出错时定位问题
        String driver = PROPS.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("找不到 JDBC 驱动类：" + driver, e);
        }
    }

    private DBUtil() {
    }

    /**
     * 获取一个新的数据库连接。
     *
     * @return Connection，使用完必须关闭（推荐 try-with-resources）
     * @throws SQLException 连接失败时抛出
     */
    public static Connection getConnection() throws SQLException {
        String url = required("jdbc.url");
        String username = required("jdbc.username");
        String password = PROPS.getProperty("jdbc.password", "");
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 测试数据库是否可连通，供程序启动时给出友好提示。
     *
     * @return 连接成功返回 true
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("【数据库连接失败】" + e.getMessage());
            System.err.println("请检查：1) MySQL 服务是否启动；2) db.properties 中的 url/用户名/密码；"
                    + "3) 是否已执行 sql/init.sql 建库。");
            return false;
        }
    }

    private static String required(String key) {
        String value = PROPS.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(CONFIG_FILE + " 缺少配置项：" + key);
        }
        return value.trim();
    }
}
