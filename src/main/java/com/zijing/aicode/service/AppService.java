package com.zijing.aicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zijing.aicode.entity.dto.app.AppQueryRequest;
import com.zijing.aicode.entity.po.App;
import com.zijing.aicode.entity.vo.AppVO;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author zijing
 */
public interface AppService extends IService<App> {


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

    List<AppVO> getAppVOList(List<App> appList);
}
