const http = require('http');
const fs = require('fs');
const yaml = require('js-yaml');

// 读取并解析 config.yml 文件
const configPath = './config.yml';
let config;
try {
    const configFile = fs.readFileSync(configPath, 'utf8');
    config = yaml.load(configFile);
} catch (error) {
    console.error('Failed to load config.yml:', error);
    process.exit(1);
}

const server = http.createServer((req, res) => {
    if (req.url === '/events') {
        res.writeHead(200, {
            'Content-Type': 'text/event-stream',
            'Cache-Control': 'no-cache',
            'Connection': 'keep-alive'
        });

        // 模拟事件推送
        setInterval(() => {
            const data = JSON.stringify({ message: `Event at ${new Date().toISOString()}` });
            res.write(`data: ${data}\n\n`);
        }, 2000);

        // 关闭连接时清理资源
        req.on('close', () => {
            console.log('Client disconnected');
        });
    } else {
        res.writeHead(404);
        res.end();
    }
});

// 使用 config.yml 中的端口号启动服务器
server.listen(config.server.port, () => {
    console.log(`MCP SSE Server is running on http://localhost:${config.server.port}`);
});