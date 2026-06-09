package com.zijing.aicode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import com.zijing.aicode.ai.model.enums.CodeGenTypeEnum;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;

/**
 * 多文件代码文件保存器模板
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        // 保存 CSS 文件
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        // 保存 JavaScript 文件
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected void verifyInput(MultiFileCodeResult result) {
        super.verifyInput(result);
        // 至少要有 HTML 代码，CSS 和 JS 可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }
}
