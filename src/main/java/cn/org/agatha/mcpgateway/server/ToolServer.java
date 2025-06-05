package cn.org.agatha.mcpgateway.server;

import cn.org.agatha.mcpgateway.server.DatabaseConnectionManager;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Service
public class ToolServer {

    @Autowired
    private DatabaseConnectionManager databaseConnectionManager;

    @Tool(description = "获取玩家信息")
    public List<String> FetchPlayerInfo(String name) {
        Map<String, Object> playerInfo = databaseConnectionManager.queryAuthMeByPlayerName(name);
        if (!playerInfo.isEmpty()) {
            String realname = (String) playerInfo.get("realname");
            long regdate = (long) playerInfo.get("regdate");
            long lastlogin = (long) playerInfo.get("lastlogin");
            int isLogged = (int) playerInfo.get("isLogged");

            // 时间戳转换为北京时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            String formattedRegdate = sdf.format(new Timestamp(regdate));
            String formattedLastlogin = sdf.format(new Timestamp(lastlogin));

            // 在线状态转换
            String onlineStatus = isLogged == 1 ? "在线" : "不在线";

            // 构造返回信息
            String info = String.format(
                "该用户注册于 %s，上次登录是在 %s，当前 %s",
                formattedRegdate,
                formattedLastlogin,
                onlineStatus
            );

            return Collections.singletonList(info);
        }
        return Collections.singletonList("找不到这个玩家");
    }

    @Tool(description = "文章润色")
    public List<String> polishTheArticle(String content) {
        return Collections.singletonList("1");
    }
}