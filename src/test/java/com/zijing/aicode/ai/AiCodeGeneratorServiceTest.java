package com.zijing.aicode.ai;

import com.zijing.aicode.ai.model.HtmlCodeResult;
import com.zijing.aicode.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Slf4j
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode("请生成一个登录页面 20行");
        Assertions.assertNotNull(htmlCodeResult);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode("请生成一个登录页面 50行");
        Assertions.assertNotNull(multiFileCodeResult);
    }

    @Test
    void generateHtmlCodeStream() {
        aiCodeGeneratorService.generateHtmlCodeStream("请生成一个登录页面 20行").subscribe(System.out::println);

    }
}