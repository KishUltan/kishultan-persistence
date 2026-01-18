package com.kishultan.persistence.query.monitor;

import java.time.LocalDateTime;

/**
 * 慢查询信息类
 * 记录慢查询的详细信息
 */
public class SlowQueryInfo {
    private final String sql;
    private final Object[] parameters;
    private final long executionTime;
    private final LocalDateTime timestamp;
    private final int resultCount;
    private final String errorMessage;

    /**
     * 构造函数
     *
     * @param sql           SQL语句
     * @param parameters    查询参数
     * @param executionTime 执行时间（毫秒）
     * @param resultCount   结果数量
     * @param errorMessage  错误信息（如果有）
     */
    public SlowQueryInfo(String sql, Object[] parameters, long executionTime,
                         int resultCount, String errorMessage) {
        this.sql = sql;
        this.parameters = parameters;
        this.executionTime = executionTime;
        this.timestamp = LocalDateTime.now();
        this.resultCount = resultCount;
        this.errorMessage = errorMessage;
    }

    // Getter方法
    public String getSql() {
        return sql;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getResultCount() {
        return resultCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 是否有错误
     *
     * @return 是否有错误
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("SlowQueryInfo{sql='%s', executionTime=%dms, resultCount=%d, timestamp=%s, hasError=%s}",
                sql, executionTime, resultCount, timestamp, hasError());
    }
}
