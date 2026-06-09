package com.zijing.aicode.core.parser;

/**
 * 代码解析器
 * @param <T>
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     * @param codeContent 代码内容
     * @return 解析后的结果对象 (HtmlHtmlCodeResult,MultiFileCodeResult)
     */
    T parseCode(String codeContent);
}
