package com.zijing.aicode.core.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MultiFileCodeParser
 */
@Slf4j
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult>{

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```json\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        // 如果被 ```json 包裹，先提取内部 JSON
        String jsonContent = codeContent;
        Matcher jsonMatcher = JSON_BLOCK_PATTERN.matcher(codeContent);
        if (jsonMatcher.find()) {
            jsonContent = jsonMatcher.group(1).trim();
        }
        // 优先尝试 JSON 解析
        MultiFileCodeResult jsonResult = tryParseFromJson(jsonContent);
        if (jsonResult != null) {
            return jsonResult;
        }
        // 回退到 Markdown 代码块提取
        return parseFromMarkdown(codeContent);
    }

    /**
     * 尝试从 JSON 字符串解析多文件代码
     */
    private MultiFileCodeResult tryParseFromJson(String codeContent) {
        try {
            JsonNode root = MAPPER.readTree(codeContent);
            String htmlCode = root.has("htmlCode") ? root.get("htmlCode").asText() : null;
            String cssCode = root.has("cssCode") ? root.get("cssCode").asText() : null;
            String jsCode = root.has("jsCode") ? root.get("jsCode").asText() : null;
            String description = root.has("description") ? root.get("description").asText() : null;
            if (htmlCode != null || cssCode != null || jsCode != null) {
                MultiFileCodeResult result = new MultiFileCodeResult();
                result.setHtmlCode(htmlCode);
                result.setCssCode(cssCode);
                result.setJsCode(jsCode);
                result.setDescription(description);
                return result;
            }
        } catch (Exception e) {
            log.debug("JSON 解析失败，尝试 Markdown 提取: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从 Markdown 代码块提取代码
     */
    private MultiFileCodeResult parseFromMarkdown(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        String htmlCode = extractCode(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCode(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCode(codeContent, JS_CODE_PATTERN);
        if (htmlCode != null) {
            result.setHtmlCode(htmlCode.trim());
        }
        if (cssCode != null) {
            result.setCssCode(cssCode.trim());
        }
        if (jsCode != null) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    /**
     * 根据正则表达式提取代码内容
     */
    private String extractCode(String codeContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(codeContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
