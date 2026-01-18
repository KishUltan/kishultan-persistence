package com.kishultan.persistence.query.monitor;

import java.util.List;

/**
 * 查询性能指标接口
 * 提供查询执行的各种性能指标
 */
public interface QueryMetrics {
    /**
     * 获取总查询次数
     *
     * @return 总查询次数
     */
    long getTotalQueryCount();

    /**
     * 获取成功查询次数
     *
     * @return 成功查询次数
     */
    long getSuccessQueryCount();

    /**
     * 获取失败查询次数
     *
     * @return 失败查询次数
     */
    long getFailedQueryCount();

    /**
     * 获取平均执行时间（毫秒）
     *
     * @return 平均执行时间
     */
    double getAverageExecutionTime();

    /**
     * 获取最大执行时间（毫秒）
     *
     * @return 最大执行时间
     */
    long getMaxExecutionTime();

    /**
     * 获取最小执行时间（毫秒）
     *
     * @return 最小执行时间
     */
    long getMinExecutionTime();

    /**
     * 获取最后执行时间（毫秒）
     *
     * @return 最后执行时间
     */
    long getLastExecutionTime();

    /**
     * 获取总执行时间（毫秒）
     *
     * @return 总执行时间
     */
    long getTotalExecutionTime();

    /**
     * 获取查询成功率
     *
     * @return 查询成功率（0-1）
     */
    double getSuccessRate();

    /**
     * 获取查询失败率
     *
     * @return 查询失败率（0-1）
     */
    double getFailureRate();

    /**
     * 获取每秒查询数（QPS）
     *
     * @return QPS
     */
    double getQueriesPerSecond();

    /**
     * 获取平均结果集大小
     *
     * @return 平均结果集大小
     */
    double getAverageResultSetSize();

    /**
     * 获取总结果集大小
     *
     * @return 总结果集大小
     */
    long getTotalResultSetSize();

    /**
     * 获取慢查询数量
     *
     * @param threshold 慢查询阈值（毫秒）
     * @return 慢查询数量
     */
    long getSlowQueryCount(long threshold);

    /**
     * 获取慢查询比例
     *
     * @param threshold 慢查询阈值（毫秒）
     * @return 慢查询比例（0-1）
     */
    double getSlowQueryRate(long threshold);

    /**
     * 获取性能趋势数据
     *
     * @param timeWindow 时间窗口（分钟）
     * @return 性能趋势数据
     */
    List<PerformanceTrend> getPerformanceTrend(int timeWindow);

    /**
     * 重置所有指标
     */
    void reset();
}
