package com.zijing.aicode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.entity.enums.CodeGenTypeEnum;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;

/**
 * HTML 代码文件保存器模板
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        //保存HTML代码到 index.html 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void verifyInput(HtmlCodeResult result) {
        super.verifyInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML 代码不能为空");
        }
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

}
