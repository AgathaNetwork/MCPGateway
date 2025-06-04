package cn.org.agatha.mcpgateway.config;
import cn.org.agatha.mcpgateway.server.ToolServer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@EnableWebMvc
public class McpServerConfig implements WebMvcConfigurer {
    @Bean
    public ToolCallbackProvider openLibraryTools(ToolServer toolServer) {
        return MethodToolCallbackProvider.builder().toolObjects(toolServer).build();
    }
}