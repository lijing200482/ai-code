package com.zijing.aicode.core;

import com.zijing.aicode.ai.AiCodeGeneratorService;
import com.zijing.aicode.ai.AiCodeGeneratorServiceFactory;
import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import com.zijing.aicode.entity.enums.CodeGenTypeEnum;
import com.zijing.aicode.core.parser.CodeParserExecutor;
import com.zijing.aicode.core.saver.CodeFileSaverExecutor;
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
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 生成代码统一接口(响应式)
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        //校验
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请选择代码生成模式");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            }

            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成代码统一接口（流式）
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        //校验
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请选择代码生成模式");
        }

        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream
     * @param appId
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(chunk -> {
                    //实时收集代码片段
                    codeBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    //流式返回完成保存代码
                    try {
                        String completeHtmlCode = codeBuilder.toString();
                        //使用解析器解析代码
                        Object parserResult = CodeParserExecutor.executeParser(completeHtmlCode, codeGenType);
                        //使用执行器保存代码
                        File savedDir = CodeFileSaverExecutor.executeSaver(parserResult, codeGenType, appId);
                        log.info("保存成功，路径为{}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败", e);
                    }
                });
    }
}
