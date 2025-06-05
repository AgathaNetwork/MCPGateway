package cn.org.agatha.mcpgateway.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class DatabaseConnectionManager {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;

    private Connection connection;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> healthCheckTask;

    @PostConstruct
    public void init() {
        try {
            Class.forName(driverClass);
            connect();
            startHealthCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() {
        if (healthCheckTask != null && !healthCheckTask.isCancelled()) {
            healthCheckTask.cancel(true);
        }
        closeConnection();
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(dbUrl, username, password);
                System.out.println("✅ 数据库连接已建立");
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ 数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("❌ 关闭数据库连接失败: " + e.getMessage());
        }
    }

    private void startHealthCheck() {
        healthCheckTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (connection == null || connection.isClosed()) {
                    System.out.println("⚠️ 数据库连接已断开，尝试重连...");
                    connect();
                }
            } catch (SQLException e) {
                System.err.println("❌ 健康检查失败: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS); // 每5秒检查一次
    }

    /**
     * 查询 authme 表中 realname 列与传入玩家名相符的条目（忽略大小写），
     * 并返回 realname、lastlogin、regdate 和 isLogged。
     *
     * @param playerName 玩家名
     * @return 包含 realname、lastlogin、regdate 和 isLogged 的 Map，若未找到则返回空 Map
     */
    public Map<String, Object> queryAuthMeByPlayerName(String playerName) {
        Map<String, Object> result = new HashMap<>();
        String querySql = "SELECT realname, lastlogin, regdate, isLogged FROM authme WHERE LOWER(realname) = LOWER(?)";

        try (PreparedStatement statement = connection.prepareStatement(querySql)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result.put("realname", resultSet.getString("realname"));
                    // 将时间戳转换为 long 类型
                    result.put("lastlogin", resultSet.getLong("lastlogin"));
                    result.put("regdate", resultSet.getLong("regdate"));
                    result.put("isLogged", resultSet.getInt("isLogged"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 查询 authme 表失败: " + e.getMessage());
        }

        return result;
    }

    public Connection getConnection() {
        return connection;
    }
}