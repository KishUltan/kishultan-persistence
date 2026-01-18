package com.kishultan.persistence.query.config;

/**
 * QueryBuilder配置类
 * 统一管理全局配置和实例配置，通过作用范围区分
 */
public class CriterionConfig {
    private FieldNamingStrategyChain strategyChain = FieldNamingStrategyChain.DEFAULT;
    private Boolean warnOnMissingField = null;
    private Boolean strictMode = null;
    
    /**
     * 获取字段命名策略链
     */
    public FieldNamingStrategyChain getStrategyChain() {
        return strategyChain;
    }
    
    /**
     * 设置字段命名策略链
     */
    public CriterionConfig setStrategyChain(FieldNamingStrategyChain chain) {
        if (chain == null) {
            throw new IllegalArgumentException("策略链不能为null");
        }
        this.strategyChain = chain;
        return this;
    }
    
    /**
     * 是否在找不到字段时输出警告日志（null表示使用全局配置）
     */
    public Boolean getWarnOnMissingField() {
        return warnOnMissingField;
    }
    
    /**
     * 设置是否在找不到字段时输出警告日志
     */
    public CriterionConfig setWarnOnMissingField(Boolean warn) {
        this.warnOnMissingField = warn;
        return this;
    }
    
    /**
     * 严格模式：找不到字段时抛出异常而非跳过（null表示使用全局配置）
     */
    public Boolean getStrictMode() {
        return strictMode;
    }
    
    /**
     * 设置严格模式
     */
    public CriterionConfig setStrictMode(Boolean strict) {
        this.strictMode = strict;
        return this;
    }
    
    /**
     * 解析字段命名策略链值（考虑null值）
     */
    public FieldNamingStrategyChain resolveStrategyChain() {
        return strategyChain != null ? strategyChain : GlobalConfig.strategyChain;
    }
    
    /**
     * 解析警告字段值（考虑null值）
     */
    public boolean resolveWarnOnMissingField() {
        return warnOnMissingField != null ? warnOnMissingField : GlobalConfig.warnOnMissingField;
    }
    
    /**
     * 解析严格模式值（考虑null值）
     */
    public boolean resolveStrictMode() {
        return strictMode != null ? strictMode : GlobalConfig.strictMode;
    }
    
    /**
     * 创建一个新配置，基于当前配置
     */
    public CriterionConfig copy() {
        CriterionConfig copy = new CriterionConfig();
        copy.strategyChain = this.strategyChain;
        copy.warnOnMissingField = this.warnOnMissingField;
        copy.strictMode = this.strictMode;
        return copy;
    }
    
    /**
     * 获取默认配置
     */
    public static CriterionConfig getDefault() {
        return new CriterionConfig();
    }
    
    /**
     * 全局配置（静态内部类，管理全局默认值）
     */
    public static class GlobalConfig {
        private static volatile FieldNamingStrategyChain strategyChain = FieldNamingStrategyChain.DEFAULT;
        private static volatile boolean warnOnMissingField = true;
        private static volatile boolean strictMode = false;
        
        /**
         * 获取全局字段命名策略链
         */
        public static FieldNamingStrategyChain getStrategyChain() {
            return strategyChain;
        }
        
        /**
         * 设置全局字段命名策略链
         */
        public static void setStrategyChain(FieldNamingStrategyChain chain) {
            if (chain == null) {
                throw new IllegalArgumentException("策略链不能为null");
            }
            strategyChain = chain;
        }
        
        /**
         * 获取全局警告配置
         */
        public static boolean isWarnOnMissingField() {
            return warnOnMissingField;
        }
        
        /**
         * 设置全局警告配置
         */
        public static void setWarnOnMissingField(boolean warn) {
            warnOnMissingField = warn;
        }
        
        /**
         * 获取全局严格模式配置
         */
        public static boolean isStrictMode() {
            return strictMode;
        }
        
        /**
         * 设置全局严格模式配置
         */
        public static void setStrictMode(boolean strict) {
            strictMode = strict;
        }
        
        /**
         * 通过系统属性初始化全局配置
         */
        public static void init() {
            String strategyConfig = System.getProperty("querybuilder.field.naming.strategy");
            if (strategyConfig != null && !strategyConfig.trim().isEmpty()) {
                strategyChain = FieldNamingStrategyChain.fromString(strategyConfig);
            }
            
            warnOnMissingField = Boolean.parseBoolean(
                System.getProperty("querybuilder.warn.on.missing.field", "true")
            );
            
            strictMode = Boolean.parseBoolean(
                System.getProperty("querybuilder.strict.mode", "false")
            );
        }
        
        /**
         * 重置全局配置为默认值
         */
        public static void reset() {
            strategyChain = FieldNamingStrategyChain.DEFAULT;
            warnOnMissingField = true;
            strictMode = false;
        }
        
        /**
         * 获取全局配置对象（所有属性为null，表示使用全局默认值）
         */
        public static CriterionConfig getConfig() {
            return new CriterionConfig();
        }
    }
}
