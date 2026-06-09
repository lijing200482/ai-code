package com.zijing.aicode.core;

import com.zijing.aicode.ai.AiCodeGeneratorService;
import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import com.zijing.aicode.ai.model.enums.CodeGenTypeEnum;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;


@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 生成代码统一接口
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        //校验
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请选择代码生成模式");
        }
        switch (codeGenTypeEnum) {
            case HTML:
                return generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE:
                return generateAndSaveMultiFileCode(userMessage);
            default:
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
        }
    }

    /**
     * 生成代码统一接口（流式）
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        //校验
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请选择代码生成模式");
        }
        switch (codeGenTypeEnum) {
            case HTML:
                return generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE:
                return generateAndSaveMultiFileCodeStream(userMessage);
            default:
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
        }
    }

    /**
     * 生成并保存 HTML 代码(流式)
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        //当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(chunk -> {
                    //实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    //流式返回完成保存代码
                    try {
                        String completeHtmlCode = codeBuilder.toString();
                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                        //保存代码
                        File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                        log.info("保存html代码完成，文件路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败", e.getMessage());
                    }
                });
    }

    /**
     * 生成并保存多文件代码(流式)
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(chunk -> {
                    //实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    //流式返回完成保存代码
                    try {
                        String completeHtmlCode = codeBuilder.toString();
                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeHtmlCode);
                        //保存代码
                        File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                        log.info("保存html代码完成，文件路径为：{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败", e.getMessage());
                    }
                });
    }

    /**
     * 生成并保存html代码
     * @param userMessage
     * @return
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成并保存多文件代码
     * @param userMessage
     * @return
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }

}
