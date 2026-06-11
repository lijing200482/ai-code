package com.zijing.aicode.core.saver;

import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import com.zijing.aicode.entity.enums.CodeGenTypeEnum;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;

import java.io.File;

public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate HTML_SAVER = new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeFileSaverTemplate MULTI_FILE_SAVER = new MultiFileCodeFileSaverTemplate();


    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @param appId
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
        return switch (codeGenType) {
            case HTML -> HTML_SAVER.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> MULTI_FILE_SAVER.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
