package cn.org.agatha.mcpgateway.server;

import cn.org.agatha.mcpgateway.server.DatabaseConnectionManager;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
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

    @Tool(description = "获取玩家信息，包括注册时间、上次登录时间、在线状态等，玩家名不区分大小写")
    public List<String> fetchPlayerInfo(String name) {
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

    @Tool(description = "获取近期活跃的玩家，如果在调用其他接口前不确定准确玩家名，可以先使用这个工具。")
    public List<String> getRecentPlayers() {
        return databaseConnectionManager.getRecentPlayers();
    }

    @Tool(description = "列出 openid.supplies 中的所有条目，包括 id 和 content，并返回一个友好的回复")
    public List<String> listSupplies() throws UnsupportedEncodingException {
        List<Map<String, String>> supplies = databaseConnectionManager.querySupplies();
        if (supplies.isEmpty()) {
            return Collections.singletonList("没有找到任何物资条目");
        }

        StringBuilder response = new StringBuilder("物资清单如下：\n");
        for (Map<String, String> supply : supplies) {
            String id = supply.get("id");
            String content = supply.get("content");
            // 确保内容以 UTF-8 编码处理
            response.append(String.format("- ID: %s, 内容: %s\n", id, new String(content.getBytes(), "UTF-8")));
        }

        return Collections.singletonList(response.toString());
    }

    @Tool(description = "根据资源 ID 获取详细信息，包括世界、坐标、效率、状态和备注")
    public List<String> getSupplyDetails(String id) throws UnsupportedEncodingException {
        Map<String, String> supplyDetails = databaseConnectionManager.querySupplyDetailsById(id);
        if (supplyDetails.isEmpty()) {
            return Collections.singletonList("未找到该资源 ID 的详细信息");
        }

        // 格式化状态信息
        String statusText = "未知";
        if ("0".equals(supplyDetails.get("status"))) {
            statusText = "不可用";
        } else if ("1".equals(supplyDetails.get("status"))) {
            statusText = "当前可用";
        }

        // 构造返回信息
        StringBuilder response = new StringBuilder();
        response.append(String.format("资源 ID: %s\n", id));
        response.append(String.format("世界: %s\n", supplyDetails.get("world")));
        response.append(String.format("坐标: X=%s, Y=%s, Z=%s\n", 
            supplyDetails.get("x"), supplyDetails.get("y"), supplyDetails.get("z")));
        response.append(String.format("效率: %s\n", supplyDetails.get("efficiency")));
        response.append(String.format("状态: %s\n", statusText));
        response.append(String.format("备注: %s\n", 
            new String(supplyDetails.get("message").getBytes(), "UTF-8")));

        return Collections.singletonList(response.toString());
    }

}