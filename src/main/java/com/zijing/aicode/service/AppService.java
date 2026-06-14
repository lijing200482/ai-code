package com.zijing.aicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zijing.aicode.entity.dto.app.AppAddRequest;
import com.zijing.aicode.entity.dto.app.AppQueryRequest;
import com.zijing.aicode.entity.po.App;
import com.zijing.aicode.entity.po.User;
import com.zijing.aicode.entity.vo.AppVO;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author zijing
 */
public interface AppService extends IService<App> {


    /**
     * 部署应用
     * @param appId
     * @param loginUser
     * @return
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 通过对话生成应用代码
     * @param appId 应用Id
     * @param message 提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId 应用ID
     * @param appUrl 应用访问URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 创建app
     * @param appAddRequest
     * @param loginUser
     * @return
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 构造 AppVO 对象
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 构造查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用列表
     *
     * @param appList 应用列表
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

}
