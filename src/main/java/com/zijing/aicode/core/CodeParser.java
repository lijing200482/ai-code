package com.zijing.aicode.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器
 * 提供静态方法解析不同类型的代码内容
 *
 * @author yupi
 */
@Slf4j
@Deprecated
public class CodeParser {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 解析 HTML 单文件代码
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 先尝试 JSON 解析（AI 可能返回 JSON 格式）
        HtmlCodeResult jsonResult = tryParseHtmlFromJson(codeContent);
        if (jsonResult != null) {
            return jsonResult;
        }
        // 再尝试从 Markdown 代码块提取
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 解析多文件代码（HTML + CSS + JS）
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        // 先尝试 JSON 解析（AI 可能返回 JSON 格式）
        MultiFileCodeResult jsonResult = tryParseMultiFileFromJson(codeContent);
        if (jsonResult != null) {
            return jsonResult;
        }
        // 再尝试从 Markdown 代码块提取
        MultiFileCodeResult result = new MultiFileCodeResult();
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    /**
     * 尝试从 JSON 字符串解析出单文件 HTML 代码
     */
    private static HtmlCodeResult tryParseHtmlFromJson(String content) {
        try {
            JsonNode root = MAPPER.readTree(content);
            String htmlCode = root.has("htmlCode") ? root.get("htmlCode").asText() : null;
            String description = root.has("description") ? root.get("description").asText() : null;
            if (htmlCode != null) {
                HtmlCodeResult result = new HtmlCodeResult();
                result.setHtmlCode(htmlCode);
                result.setDescription(description);
                return result;
            }
        } catch (Exception e) {
            // 不是合法 JSON，忽略，继续走原有逻辑
            log.debug("JSON 解析失败，尝试 Markdown 提取: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 尝试从 JSON 字符串解析出多文件代码
     */
    private static MultiFileCodeResult tryParseMultiFileFromJson(String content) {
        try {
            JsonNode root = MAPPER.readTree(content);
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
     * 提取HTML代码内容
     */
    private static String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 根据正则模式提取代码
     */
    private static String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}