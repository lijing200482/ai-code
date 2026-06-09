package com.zijing.aicode.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import com.zijing.aicode.ai.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {

    private static final String FILE_SAVE_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存HTML网页代码
     * @param htmlCodeResult
     * @return
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存多文件网页代码
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        if (result.getHtmlCode() != null) {
            writeFile(baseDirPath, "index.html", result.getHtmlCode());
        }
        if (result.getCssCode() != null) {
            writeFile(baseDirPath, "style.css", result.getCssCode());
        }
        if (result.getJsCode() != null) {
            writeFile(baseDirPath, "script.js", result.getJsCode());
        }
        return new File(baseDirPath);
    }

    /**
     * 构建唯一目录路径:tmp/code_output/bizType_雪花id
     * @param bizType
     * @return
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextId());
        String dirPath = FILE_SAVE_PATH + File.separator + uniqueName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存文件
     */
    private static void writeFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}