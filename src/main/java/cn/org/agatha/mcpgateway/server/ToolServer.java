package cn.org.agatha.mcpgateway.server;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
@Service
public class ToolServer {
    @Tool(description = "批量生成标题")
    public String BatchGenerateTitles(String title) {
        return "1";
    }
    @Tool(description = "文章润色")
    public String polishTheArticle(String content) {
        return "1";
    }
}