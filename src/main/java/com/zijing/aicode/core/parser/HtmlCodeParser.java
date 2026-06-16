package com.zijing.aicode.core.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zijing.aicode.ai.model.HtmlCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HtmlCodeParser
 */
@Slf4j
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```json\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        // 如果被 ```json 包裹，先提取内部 JSON
        String jsonContent = codeContent;
        Matcher jsonMatcher = JSON_BLOCK_PATTERN.matcher(codeContent);
        if (jsonMatcher.find()) {
            jsonContent = jsonMatcher.group(1).trim();
        }
        // 优先尝试 JSON 解析
        HtmlCodeResult jsonResult = tryParseFromJson(jsonContent);
        if (jsonResult != null) {
            return jsonResult;
        }
        // 回退到 Markdown 代码块提取
        return parseFromMarkdown(codeContent);
    }

    /**
     * 尝试从 JSON 字符串解析 HTML 代码
     */
    private HtmlCodeResult tryParseFromJson(String codeContent) {
        try {
            JsonNode root = MAPPER.readTree(codeContent);
            String htmlCode = root.has("htmlCode") ? root.get("htmlCode").asText() : null;
            String description = root.has("description") ? root.get("description").asText() : null;
            if (htmlCode != null) {
                HtmlCodeResult result = new HtmlCodeResult();
                result.setHtmlCode(htmlCode);
                result.setDescription(description);
                return result;
            }
        } catch (Exception e) {
            log.debug("JSON 解析失败，尝试 Markdown 提取: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从 Markdown 代码块提取 HTML 代码
     */
    private HtmlCodeResult parseFromMarkdown(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML返回
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 根据正则表达式提取html代码内容
     */
    private String extractHtmlCode(String codeContent) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(codeContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
