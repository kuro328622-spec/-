package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AIController {

    private final String API_KEY = "sk-f974792c82654737969c794721128ef0"; // 请替换为您真实的 DeepSeek API Key
    private final String API_URL = "https://api.deepseek.com/chat/completions";

    /**
     * 通用 AI 聊天接口
     */
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        return callDeepSeek("你是一个专业的规模化羊场管理助手。", userMessage);
    }

    /**
     * ✨ 新增：AI 深度报告生成接口
     */
    @PostMapping("/report")
    public String generateReport(@RequestBody Map<String, String> payload) {
        String dataContext = payload.get("dataContext");
        String reportType = payload.get("reportType");

        String systemPrompt = "你是一位资深的规模化羊场养殖管理专家和财务审计师。"
                + "请根据用户提供的实时养殖数据，生成一份专业的【" + reportType + "】。"
                + "报告必须包含：1. 数据概览；2. 异常发现（如成本激增、库存告急、单价波动）；3. 经营建议（如优化采购策略、调整饲喂方案）；4. 经济效益评价。"
                + "请使用 Markdown 格式输出，语言要专业且具有指导意义。";

        return callDeepSeek(systemPrompt, dataContext);
    }

    private String callDeepSeek(String systemPrompt, String userContent) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                    "{\"model\": \"deepseek-chat\", \"messages\": ["
                    + "{\"role\": \"system\", \"content\": \"%s\"},"
                    + "{\"role\": \"user\", \"content\": \"%s\"}"
                    + "]}", systemPrompt.replace("\"", "\\\""), userContent.replace("\"", "\\\"").replace("\n", "\\n")
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return response.toString();
        } catch (Exception e) {
            return "{\"error\": \"AI 服务连接失败: " + e.getMessage() + "\"}";
        }
    }
}
