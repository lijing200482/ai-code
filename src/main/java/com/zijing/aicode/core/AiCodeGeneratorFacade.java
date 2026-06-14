package com.zijing.aicode.core;

import cn.hutool.json.JSONUtil;
import com.zijing.aicode.ai.AiCodeGeneratorService;
import com.zijing.aicode.ai.AiCodeGeneratorServiceFactory;
import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import com.zijing.aicode.ai.model.message.AiResponseMessage;
import com.zijing.aicode.ai.model.message.ToolExecutedMessage;
import com.zijing.aicode.ai.model.message.ToolRequestMessage;
import com.zijing.aicode.entity.enums.CodeGenTypeEnum;
import com.zijing.aicode.core.parser.CodeParserExecutor;
import com.zijing.aicode.core.saver.CodeFileSaverExecutor;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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

        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

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
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
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

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream
                    .onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolCall(partialToolCall -> {
                        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                                .id(partialToolCall.id())
                                .name(partialToolCall.name())
                                .arguments(partialToolCall.partialArguments())
                                .build();
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }
}



