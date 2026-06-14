package com.zijing.aicode.service;

/**
 * 截图服务
 */
public interface ScreenshotService {
    /**
     * 截图并上传到腾讯云cos
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);
}
