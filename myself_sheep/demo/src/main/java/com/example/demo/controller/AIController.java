package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AIController {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        // 如果前端传参不对，这里会打印 null，如果是 null 就要检查前端
        System.out.println("==== [DEBUG] 前端发送的消息内容: " + userMessage);

        try {
            URL url = new URL("https://api.deepseek.com/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            // 构造 JSON
            String safeMessage = userMessage == null ? "" : userMessage.replace("\"", "\\\"").replace("\n", "\\n");
            String jsonInputString = "{"
                    + "\"model\": \"deepseek-chat\","
                    + "\"messages\": ["
                    + "{\"role\": \"system\", \"content\": \"你是一个专业的养殖场助手。\"},"
                    + "{\"role\": \"user\", \"content\": \"" + safeMessage + "\"}"
                    + "]"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            System.out.println("==== [DEBUG] DeepSeek 状态码: " + status);

            InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();

            if (is == null) {
                return "{\"error\": \"无法从 API 获取响应流\"}";
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            System.out.println("==== [DEBUG] DeepSeek 原始响应: " + response.toString());
            return response.toString();

        } catch (Exception e) {
            System.err.println("==== [ERROR] 发生异常: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"AI连接失败: " + e.getMessage() + "\"}";
        }
    }
}
