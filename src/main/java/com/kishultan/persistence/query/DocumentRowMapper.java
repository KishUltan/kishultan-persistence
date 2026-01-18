package com.kishultan.persistence.query;

import com.kishultan.persistence.query.config.CriterionConfig;
import com.kishultan.persistence.query.config.FieldNamingStrategyChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NoSQL 文档结果映射器
 * 用于将文档数据库（MongoDB、Neo4j、Redis 等）的查询结果映射为 POJO
 * 
 * 支持：
 * - Map/Document 到 POJO 的映射
 * - ResultSet 到 POJO 的映射（通过适配层）
 * - 字段名匹配策略（驼峰、下划线等）
 * - 类型转换
 * - 嵌套对象映射
 * 
 * 实现了 RowMapper 接口，可以与 QueryExecutor 统一使用
 */
public class DocumentRowMapper<T> implements RowMapper<T> {
    private static final Logger logger = LoggerFactory.getLogger(DocumentRowMapper.class);
    
    // BeanMeta 缓存
    private static final Map<Class<?>, BeanMeta> beanMetaCache = new ConcurrentHashMap<>();
    
    // CriterionConfig 配置
    private CriterionConfig criterionConfig;
    
    /**
     * 将 Map/Document 映射为 POJO
     * 
     * @param source 源数据（Map、Document、Node 等）
     * @param targetType 目标类型
     * @return 映射后的 POJO 对象
     */
    @SuppressWarnings("unchecked")
    public T mapDocument(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        
        // 如果源类型就是目标类型，直接返回
        if (targetType.isInstance(source)) {
            return (T) source;
        }
        
        // 简单类型处理
        if (isSimpleType(targetType)) {
            return convertSimpleType(source, targetType);
        }
        
        // Map 类型处理
        if (Map.class.isAssignableFrom(targetType)) {
            if (source instanceof Map) {
                return (T) source;
            }
            // 将其他类型转换为 Map
            return (T) convertToMap(source);
        }
        
        // POJO 类型处理
        return mapToPojo(source, targetType);
    }
    
    /**
     * 将 Map/Document 映射为 POJO
     */
    @SuppressWarnings("unchecked")
    private T mapToPojo(Object source, Class<T> targetType) {
        try {
            // 获取 BeanMeta
            BeanMeta beanMeta = getBeanMeta(targetType);
            
            // 创建目标对象实例
            T target = (T) beanMeta.newInstance();
            
            // 将源数据转换为 Map
            Map<String, Object> sourceMap = convertToMap(source);
            
            // 获取字段命名策略链
            FieldNamingStrategyChain strategyChain = FieldNamingStrategyChain.DEFAULT;
            
            // 遍历源 Map 的每个字段
            for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 查找对应的属性访问器
                PropertyAccessor accessor = beanMeta.getPropertyAccessor(key, strategyChain);
                if (accessor != null) {
                    // 类型转换并设置值
                    Object convertedValue = convertValue(value, accessor.getType());
                    accessor.set(target, convertedValue);
                }
            }
            
            return target;
        } catch (Exception e) {
            logger.error("Failed to map document to POJO: {}", targetType.getName(), e);
            return null;
        }
    }
    
    /**
     * 将各种类型的源数据转换为 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object source) {
        if (source instanceof Map) {
            return (Map<String, Object>) source;
        }
        
        // MongoDB Document
        if (source.getClass().getName().equals("org.bson.Document")) {
            try {
                return (Map<String, Object>) source.getClass().getMethod("asMap").invoke(source);
            } catch (Exception e) {
                logger.warn("Failed to convert MongoDB Document to Map", e);
            }
        }
        
        // Neo4j Node
        if (source.getClass().getName().contains("neo4j") && 
            source.getClass().getSimpleName().equals("Node")) {
            try {
                return (Map<String, Object>) source.getClass().getMethod("asMap").invoke(source);
            } catch (Exception e) {
                logger.warn("Failed to convert Neo4j Node to Map", e);
            }
        }
        
        // 使用反射将对象转换为 Map
        Map<String, Object> map = new HashMap<>();
        try {
            Class<?> clazz = source.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(source);
                map.put(field.getName(), value);
            }
        } catch (Exception e) {
            logger.warn("Failed to convert object to Map using reflection", e);
        }
        
        return map;
    }
    
    /**
     * 类型转换
     */
    @SuppressWarnings("unchecked")
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        // 如果类型匹配，直接返回
        if (targetType.isInstance(value)) {
            return value;
        }
        
        // 简单类型转换
        if (isSimpleType(targetType)) {
            return convertSimpleType(value, targetType);
        }
        
        // 集合类型转换
        if (Collection.class.isAssignableFrom(targetType)) {
            return convertCollection(value, targetType);
        }
        
        // Map 类型转换
        if (Map.class.isAssignableFrom(targetType)) {
            if (value instanceof Map) {
                return value;
            }
            return convertToMap(value);
        }
        
        // POJO 类型转换（递归映射）
        @SuppressWarnings("unchecked")
        DocumentRowMapper<Object> mapper = new DocumentRowMapper<>();
        @SuppressWarnings("unchecked")
        Class<Object> targetTypeObj = (Class<Object>) targetType;
        return (T) mapper.mapDocument(value, targetTypeObj);
    }
    
    /**
     * 简单类型转换
     */
    @SuppressWarnings("unchecked")
    private <R> R convertSimpleType(Object value, Class<R> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType.isInstance(value)) {
            return (R) value;
        }
        
        // 字符串转换
        if (targetType == String.class) {
            return (R) String.valueOf(value);
        }
        
        // 数字类型转换
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return (R) Integer.valueOf(((Number) value).intValue());
            }
            if (value instanceof String) {
                return (R) Integer.valueOf((String) value);
            }
        }
        
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return (R) Long.valueOf(((Number) value).longValue());
            }
            if (value instanceof String) {
                return (R) Long.valueOf((String) value);
            }
        }
        
        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return (R) Double.valueOf(((Number) value).doubleValue());
            }
            if (value instanceof String) {
                return (R) Double.valueOf((String) value);
            }
        }
        
        if (targetType == Float.class || targetType == float.class) {
            if (value instanceof Number) {
                return (R) Float.valueOf(((Number) value).floatValue());
            }
            if (value instanceof String) {
                return (R) Float.valueOf((String) value);
            }
        }
        
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) {
                return (R) value;
            }
            if (value instanceof String) {
                return (R) Boolean.valueOf((String) value);
            }
            if (value instanceof Number) {
                return (R) Boolean.valueOf(((Number) value).intValue() != 0);
            }
        }
        
        logger.warn("Cannot convert value {} to type {}", value, targetType.getName());
        return null;
    }
    
    /**
     * 集合类型转换
     */
    @SuppressWarnings("unchecked")
    private Object convertCollection(Object value, Class<?> targetType) {
        if (!(value instanceof Collection)) {
            return null;
        }
        
        Collection<?> sourceCollection = (Collection<?>) value;
        Collection<Object> targetCollection;
        
        if (List.class.isAssignableFrom(targetType)) {
            targetCollection = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(targetType)) {
            targetCollection = new HashSet<>();
        } else {
            return null;
        }
        
        // 获取集合元素类型（简化实现，假设是 Object）
        for (Object item : sourceCollection) {
            targetCollection.add(item);
        }
        
        return targetCollection;
    }
    
    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
               type == String.class ||
               type == Integer.class ||
               type == Long.class ||
               type == Double.class ||
               type == Float.class ||
               type == Boolean.class ||
               type == Byte.class ||
               type == Short.class ||
               type == Character.class ||
               Number.class.isAssignableFrom(type) ||
               type == Date.class ||
               type == java.sql.Date.class ||
               type == java.sql.Timestamp.class;
    }
    
    /**
     * 获取 BeanMeta（带缓存）
     */
    private BeanMeta getBeanMeta(Class<?> clazz) {
        return beanMetaCache.computeIfAbsent(clazz, BeanMeta::new);
    }
    
    /**
     * 批量映射
     */
    @SuppressWarnings("unchecked")
    public <R> List<R> mapDocuments(List<?> sources, Class<R> targetType) {
        List<R> results = new ArrayList<>();
        DocumentRowMapper<R> mapper = new DocumentRowMapper<>();
        for (Object source : sources) {
            R mapped = mapper.mapDocument(source, targetType);
            if (mapped != null) {
                results.add(mapped);
            }
        }
        return results;
    }
    
    // ==================== RowMapper 接口实现 ====================
    
    /**
     * 实现 RowMapper 接口的 mapRow 方法
     * 将 ResultSet 转换为 Map，然后使用 mapDocument 进行映射
     * 这样可以让 DocumentRowMapper 与 SQL 执行器兼容
     */
    @Override
    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs, Class<T> resultType) throws Exception {
        // 将 ResultSet 转换为 Map
        Map<String, Object> rowMap = convertResultSetToMap(rs);
        
        // 使用 mapDocument 进行映射
        return mapDocument(rowMap, resultType);
    }
    
    /**
     * 将 ResultSet 转换为 Map
     */
    private Map<String, Object> convertResultSetToMap(ResultSet rs) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i);
            map.put(columnName, value);
        }
        
        return map;
    }
    
    @Override
    public CriterionConfig getConfig() {
        return criterionConfig;
    }
    
    @Override
    public RowMapper<T> setConfig(CriterionConfig config) {
        this.criterionConfig = config;
        return this;
    }
}

