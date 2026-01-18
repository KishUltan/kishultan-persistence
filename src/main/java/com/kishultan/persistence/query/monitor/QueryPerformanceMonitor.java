package com.kishultan.persistence.query.monitor;

import java.util.List;
import java.util.Map;

/**
 * 查询性能监控接口
 * 提供查询执行性能监控和统计功能
 */
public interface QueryPerformanceMonitor {
    /**
     * 开始监控查询执行
     *
     * @param sql        SQL语句
     * @param parameters 查询参数
     * @return 监控上下文ID
     */
    String startMonitoring(String sql, Object[] parameters);

    /**
     * 结束监控查询执行
     *
     * @param contextId   监控上下文ID
     * @param success     是否执行成功
     * @param resultCount 结果数量
     */
    void endMonitoring(String contextId, boolean success, int resultCount);

    /**
     * 记录查询错误
     *
     * @param contextId 监控上下文ID
     * @param error     错误信息
     */
    void recordError(String contextId, Throwable error);

    /**
     * 获取性能指标
     *
     * @return 性能指标对象
     */
    QueryMetrics getMetrics();

    /**
     * 获取慢查询列表
     *
     * @param threshold 慢查询阈值（毫秒）
     * @return 慢查询列表
     */
    List<SlowQueryInfo> getSlowQueries(long threshold);

    /**
     * 获取查询统计信息
     *
     * @return 查询统计信息
     */
    Map<String, QueryStatistics> getQueryStatistics();

    /**
     * 清除所有监控数据
     */
    void clearMetrics();

    /**
     * 是否启用监控
     *
     * @return 是否启用
     */
    boolean isEnabled();

    /**
     * 设置监控开关
     *
     * @param enabled 是否启用
     */
    void setEnabled(boolean enabled);
}
