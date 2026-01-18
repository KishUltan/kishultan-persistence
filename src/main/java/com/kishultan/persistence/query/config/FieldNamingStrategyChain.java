package com.kishultan.persistence.query.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 字段命名策略链
 * 支持配置多个策略，按优先级依次尝试
 * 
 * 注意：ANNOTATION策略必须始终是第一个策略，不允许禁用或移除
 */
public class FieldNamingStrategyChain {
    private final List<FieldNamingStrategy> strategies;
    
    /**
     * 默认策略链：注解优先 -> 原样匹配 -> 驼峰转换 -> 忽略大小写
     */
    public static final FieldNamingStrategyChain DEFAULT = new FieldNamingStrategyChain(
        FieldNamingStrategy.ANNOTATION,
        FieldNamingStrategy.EXACT_MATCH,
        FieldNamingStrategy.CAMEL_CASE,
        FieldNamingStrategy.CASE_INSENSITIVE
    );
    
    /**
     * 注解优先策略链：只使用注解和原样匹配
     */
    public static final FieldNamingStrategyChain ANNOTATION_ONLY = new FieldNamingStrategyChain(
        FieldNamingStrategy.ANNOTATION,
        FieldNamingStrategy.EXACT_MATCH
    );
    
    /**
     * 宽松策略链：注解优先 -> 原样匹配 -> 驼峰转换 -> 忽略大小写
     */
    public static final FieldNamingStrategyChain LENIENT = new FieldNamingStrategyChain(
        FieldNamingStrategy.ANNOTATION,
        FieldNamingStrategy.EXACT_MATCH,
        FieldNamingStrategy.CAMEL_CASE,
        FieldNamingStrategy.CASE_INSENSITIVE
    );
    
    public FieldNamingStrategyChain(FieldNamingStrategy... strategies) {
        this.strategies = new ArrayList<>(Arrays.asList(strategies));
        ensureAnnotationFirst();
    }
    
    public FieldNamingStrategyChain(List<FieldNamingStrategy> strategies) {
        this.strategies = new ArrayList<>(strategies);
        ensureAnnotationFirst();
    }
    
    /**
     * 获取策略列表
     */
    public List<FieldNamingStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }
    
    /**
     * 添加策略到链末尾
     */
    public FieldNamingStrategyChain addStrategy(FieldNamingStrategy strategy) {
        this.strategies.add(strategy);
        return this;
    }
    
    /**
     * 在指定位置插入策略
     * 注意：不能在第一位插入非ANNOTATION策略
     */
    public FieldNamingStrategyChain addStrategy(int index, FieldNamingStrategy strategy) {
        if (index == 0 && strategy != FieldNamingStrategy.ANNOTATION) {
            throw new IllegalArgumentException("ANNOTATION策略必须始终是第一个策略，不能在第一位插入其他策略");
        }
        this.strategies.add(index, strategy);
        return this;
    }
    
    /**
     * 移除策略
     * 注意：ANNOTATION策略不允许移除
     */
    public FieldNamingStrategyChain removeStrategy(FieldNamingStrategy strategy) {
        if (strategy == FieldNamingStrategy.ANNOTATION) {
            throw new UnsupportedOperationException("ANNOTATION策略不允许移除，必须始终启用");
        }
        this.strategies.remove(strategy);
        return this;
    }
    
    /**
     * 清空所有策略
     * 注意：此方法会恢复为仅包含ANNOTATION的策略链
     */
    public FieldNamingStrategyChain clear() {
        this.strategies.clear();
        this.strategies.add(FieldNamingStrategy.ANNOTATION);
        return this;
    }
    
    /**
     * 从配置字符串创建策略链
     * 格式：ANNOTATION,EXACT_MATCH,CAMEL_CASE
     * 
     * 注意：如果配置中没有ANNOTATION，会自动添加到第一位
     */
    public static FieldNamingStrategyChain fromString(String config) {
        if (config == null || config.trim().isEmpty()) {
            return DEFAULT;
        }
        
        String[] parts = config.split(",");
        List<FieldNamingStrategy> strategies = new ArrayList<>();
        boolean hasAnnotation = false;
        
        for (String part : parts) {
            try {
                FieldNamingStrategy strategy = FieldNamingStrategy.valueOf(part.trim());
                if (strategy == FieldNamingStrategy.ANNOTATION) {
                    hasAnnotation = true;
                }
                strategies.add(strategy);
            } catch (IllegalArgumentException e) {
                // 忽略无效的策略
            }
        }
        
        // 如果没有ANNOTATION，自动添加到第一位
        if (!hasAnnotation) {
            strategies.add(0, FieldNamingStrategy.ANNOTATION);
        }
        
        return strategies.isEmpty() ? DEFAULT : new FieldNamingStrategyChain(strategies);
    }
    
    /**
     * 确保ANNOTATION策略在第一位
     */
    private void ensureAnnotationFirst() {
        if (strategies.isEmpty()) {
            strategies.add(FieldNamingStrategy.ANNOTATION);
            return;
        }
        
        if (strategies.get(0) != FieldNamingStrategy.ANNOTATION) {
            // 找到ANNOTATION的位置
            int annotationIndex = strategies.indexOf(FieldNamingStrategy.ANNOTATION);
            if (annotationIndex > 0) {
                // 移动到第一位
                strategies.remove(annotationIndex);
                strategies.add(0, FieldNamingStrategy.ANNOTATION);
            } else {
                // 没有ANNOTATION，添加到第一位
                strategies.add(0, FieldNamingStrategy.ANNOTATION);
            }
        }
    }
    
    /**
     * 转换为配置字符串
     */
    @Override
    public String toString() {
        return String.join(",", strategies.stream()
            .map(Enum::name)
            .toArray(String[]::new));
    }
}
