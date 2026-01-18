package com.kishultan.persistence.query;

import com.kishultan.persistence.query.config.CriterionConfig;
import java.sql.ResultSet;

/**
 * 行映射器接口
 * 负责将ResultSet的一行数据映射为指定类型的对象
 */
public interface RowMapper<T> {
    /**
     * 将ResultSet的一行数据映射为指定类型的对象
     *
     * @param rs         ResultSet
     * @param resultType 结果类型
     * @return 映射后的对象
     * @throws Exception 异常
     */
    T mapRow(ResultSet rs, Class<T> resultType) throws Exception;
    
    /**
     * 获取当前RowMapper的配置
     * 默认实现返回null，表示使用全局配置
     * 子类可以覆盖此方法以提供实例级配置
     * @return 配置对象，可能为null（表示使用全局配置）
     */
    default CriterionConfig getConfig() {
        return null;
    }
    
    /**
     * 设置当前RowMapper的配置
     * 默认实现抛出UnsupportedOperationException，表示不支持配置
     * 子类可以覆盖此方法以支持配置设置
     * @param config 配置对象，null表示使用全局配置
     * @return 当前RowMapper实例（支持链式调用）
     * @throws UnsupportedOperationException 如果子类不支持配置
     */
    default RowMapper<T> setConfig(CriterionConfig config) {
        throw new UnsupportedOperationException("This RowMapper implementation does not support configuration");
    }
}
