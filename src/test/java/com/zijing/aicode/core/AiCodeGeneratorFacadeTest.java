package com.zijing.aicode.core;

import com.zijing.aicode.entity.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
@Slf4j
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("请生成一个登录页面 20行", CodeGenTypeEnum.MULTI_FILE, 1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream("请生成一个登录页面 50行", CodeGenTypeEnum.MULTI_FILE, 1L);

        List<String> result = stringFlux.collectList().block();
        Assertions.assertNotNull(result);
        String resultStr = String.join("", result);
        Assertions.assertNotNull(resultStr);

    }
}