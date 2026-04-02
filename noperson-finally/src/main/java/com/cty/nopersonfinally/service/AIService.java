package com.cty.nopersonfinally.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI诊断服务
 */
@Service
public class AIService {

    private static final String API_KEY = "sk-90252ff69e7e43648a996bec57056f83";
    private static final String MODEL = "qwen-vl-max";
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final RestTemplate restTemplate;

    public AIService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 调用百炼API进行病虫害诊断
     */
    public com.cty.nopersonfinally.controller.AIController.DiagnoseResult diagnose(String imageUrl, String cropType) {
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(API_KEY);

            // 打印图片URL，用于调试
            System.out.println("图片URL: " + imageUrl);
            
            // 构建请求体
            String jsonBody = String.format("{\n" +
                    "  \"model\": \"%s\",\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": [\n" +
                    "        {\n" +
                    "          \"type\": \"image_url\",\n" +
                    "          \"image_url\": {\n" +
                    "            \"url\": \"%s\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"type\": \"text\",\n" +
                    "          \"text\": \"请仔细分析这张%s图片，识别具体的病虫害种类。请提供详细的识别结果并输出JSON格式。JSON必须包含以下字段：diseaseName（病虫害名称）、confidence（置信度，0-100的整数）、recommendedPesticide（推荐使用的农药名称及用量）、controlMeasures（详细的防治措施和注意事项）。即使不确定，也要给出最可能的病虫害名称和相关建议。\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"max_tokens\": 1000\n" +
                    "}", MODEL, imageUrl, cropType);

            // 发送请求
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                return parseResponse(responseBody);
            } else {
                throw new RuntimeException("API调用失败，状态码：" + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("调用百炼API失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解析API响应
     */
    private com.cty.nopersonfinally.controller.AIController.DiagnoseResult parseResponse(String responseBody) {
        try {
            System.out.println("百炼API返回内容: " + responseBody);
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseJson = objectMapper.readTree(responseBody);
            
            // 兼容不同的响应格式
            JsonNode choices = null;
            
            // 格式1: { "output": { "choices": [...] } }
            JsonNode output = responseJson.get("output");
            if (output != null) {
                choices = output.get("choices");
            }
            
            // 格式2: { "choices": [...] }
            if (choices == null) {
                choices = responseJson.get("choices");
            }
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode choice = choices.get(0);
                JsonNode message = choice.get("message");
                
                // 兼容不同的message格式
                String text = null;
                
                if (message != null) {
                    // 格式1: message.content 是数组
                    JsonNode content = message.get("content");
                    if (content != null && content.isArray() && content.size() > 0) {
                        for (JsonNode item : content) {
                            if ("text".equals(item.get("type").asText())) {
                                text = item.get("text").asText();
                                break;
                            }
                        }
                    }
                    // 格式2: message.content 是字符串
                    else if (content != null && content.isTextual()) {
                        text = content.asText();
                    }
                }
                
                // 格式3: choice.text 直接是字符串
                if (text == null) {
                    text = choice.get("text").asText();
                }
                
                if (text != null) {
                    // 去除代码块标记
                    text = text.trim();
                    if (text.startsWith("```json") && text.endsWith("```")) {
                        text = text.substring(7, text.length() - 3).trim();
                    }
                    
                    // 尝试解析JSON
                    try {
                        JsonNode resultJson = objectMapper.readTree(text);
                        com.cty.nopersonfinally.controller.AIController.DiagnoseResult result = new com.cty.nopersonfinally.controller.AIController.DiagnoseResult();
                        if (resultJson.has("diseaseName")) {
                            result.setDiseaseName(resultJson.get("diseaseName").asText());
                        }
                        if (resultJson.has("confidence")) {
                            result.setConfidence(resultJson.get("confidence").asInt());
                        }
                        if (resultJson.has("recommendedPesticide")) {
                            result.setRecommendedPesticide(resultJson.get("recommendedPesticide").asText());
                        }
                        if (resultJson.has("controlMeasures")) {
                            // 如果controlMeasures是数组，转换为字符串
                            JsonNode controlMeasuresNode = resultJson.get("controlMeasures");
                            if (controlMeasuresNode.isArray()) {
                                StringBuilder measures = new StringBuilder();
                                for (JsonNode measure : controlMeasuresNode) {
                                    measures.append(measure.asText()).append("；");
                                }
                                if (measures.length() > 0) {
                                    measures.setLength(measures.length() - 1); // 移除最后一个分号
                                }
                                result.setControlMeasures(measures.toString());
                            } else {
                                result.setControlMeasures(controlMeasuresNode.asText());
                            }
                        }
                        return result;
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 如果不是JSON格式，返回默认结果
                        return getDefaultResult();
                    }
                }
            }
            
            return getDefaultResult();
            
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultResult();
        }
    }

    /**
     * 获取默认结果
     */
    private com.cty.nopersonfinally.controller.AIController.DiagnoseResult getDefaultResult() {
        com.cty.nopersonfinally.controller.AIController.DiagnoseResult result = new com.cty.nopersonfinally.controller.AIController.DiagnoseResult();
        result.setDiseaseName("未知病虫害");
        result.setConfidence(50);
        result.setRecommendedPesticide("暂无推荐药剂");
        result.setControlMeasures("请咨询专业农技人员");
        return result;
    }
}