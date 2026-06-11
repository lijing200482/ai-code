package com.zijing.aicode.core.parser;

import com.zijing.aicode.entity.enums.CodeGenTypeEnum;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;

/**
 * 代码解析执行器
 * 根据代码生成的类型执行相应的解析逻辑代码
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析器
     * @param codeConetnt
     * @param codeGenType
     * @return
     */
    public static Object executeParser(String codeConetnt, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeConetnt);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeConetnt);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持生成改代码类型" + codeGenType);
        };
    }
}
