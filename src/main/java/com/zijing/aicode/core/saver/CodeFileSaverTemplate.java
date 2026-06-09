package com.zijing.aicode.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.zijing.aicode.ai.model.enums.CodeGenTypeEnum;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 代码文件保存器模板
 * 定义了保存代码文件的模板方法，具体的保存逻辑由子类
 */
public abstract class CodeFileSaverTemplate<T> {

    private static final String FILE_SAVE_PATH = System.getProperty("user.dir") + "/tmp/code_output";


    public final File saveCode(T result) {
        //1.验证输入
        verifyInput(result);
        //2.构建唯一目录路径
        String dirPath = buildUniqueDir();
        //3.保存文件
        saveFiles(result, dirPath);
        //4.返回保存的目录对象
        return new File(dirPath);
    }

    /**
     * 验证输入结果的合法性，子类可根据需要重写此方法进行具体的验证逻辑
     * @param result
     */
    protected void verifyInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }

    /**
     * 构建唯一目录路径，子类可根据需要重写此方法进行具体的目录构建逻辑
     */
    protected final String buildUniqueDir() {
        String codeType = getCodeType().getValue();
        String uniqueName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextId());
        String dirPath = FILE_SAVE_PATH + File.separator + uniqueName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件的工具方法
     *
     * @param dirPath  目录路径
     * @param filename 文件名
     * @param content  文件内容
     */
    public final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 保存文件（具体实现交给子类）
     *
     * @param result      代码结果对象
     * @param baseDirPath 基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);

    /**
     * 获取代码生成类型
     *
     * @return 代码生成类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeType();

}


