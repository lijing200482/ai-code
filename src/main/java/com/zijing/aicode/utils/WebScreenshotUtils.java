package com.zijing.aicode.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import com.zijing.aicode.exception.BusinessException;
import com.zijing.aicode.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class WebScreenshotUtils {

    private static final WebDriver webDriver;

    // 全局静态初始化，避免重复初始化驱动程序：
    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存图片到文件
     * @param imageBytes
     * @param imagePath
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (IORuntimeException e) {
            log.error("保存截图失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存截图失败");
        }
    }

    /**
     * 压缩图片
     * @param originalImagePath
     * @param compressedImagePath
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {

        //图片处理的质量
        final float QUALITY = 0.3f;

        try {
            //压缩图片
            ImgUtil.compress(FileUtil.file(originalImagePath), FileUtil.file(compressedImagePath), QUALITY);
        } catch (IORuntimeException e) {
            log.error("压缩图片失败{}->{}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    private static void waitForPageLoad(WebDriver driver) {
        try {
            //创建等待界面加载对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            //等待页面加载完成document.readyState == complete
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete"));
            //额外再等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出了异常，继续执行截图", e);
        }
    }


    /**
     * 生成网页截图
     * @param webUrl 网页地址
     * @return 压缩后的路径 ,失败返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {

        if (StringUtil.isEmpty(webUrl)) {
            log.error("网页地址为空");
            return null;
        }
        try {
            //创建临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenShots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            //定义图片后缀
            final String SUFFIX = ".png";
            //原始图片文件路径
            String originalImagePath = rootPath + File.separator + RandomUtil.randomString(5) + SUFFIX;
            //访问地址
            webDriver.get(webUrl);
            //等待页面加载完成
            waitForPageLoad(webDriver);
            //截取当前页面，并保存为图片文件
            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            //保存原始图片
            saveImage(screenshotBytes, originalImagePath);
            log.info("截图成功，原始图片路径为：{}", originalImagePath);
            //压缩图片
            final String COMPRESS_SUFFIX = "_compress.jpg";
            String compressImagePath = rootPath + File.separator + RandomUtil.randomString(5) + COMPRESS_SUFFIX;
            compressImage(originalImagePath, compressImagePath);
            log.info("压缩图片成功，压缩图片路径为：{}", compressImagePath);
            //删除原始图片，只保留压缩图片
            FileUtil.del(originalImagePath);
            return compressImagePath;
        } catch (Exception e) {
            log.error("截图失败", e);
            return null;
        }
    }
}
