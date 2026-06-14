package com.zijing.aicode.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScreenshotUtilsTest {


    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://www.codefather.cn";
        String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        assertNotNull(webPageScreenshot);
    }
}