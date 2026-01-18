package com.kishultan.persistence.query;

import com.kishultan.persistence.dialect.DatabaseDialect;
import com.kishultan.persistence.query.config.CriterionConfig;
import com.kishultan.persistence.query.config.FieldNamingStrategyChain;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRowMapper<T> implements RowMapper<T> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRowMapper.class);
    
    // BeanMeta缓存：Class -> BeanMeta
    private static final Map<Class<?>, BeanMeta> metaCache = new ConcurrentHashMap<>();
    
    private final Map<String, TableMeta> aliasMapping = new HashMap<>();
    private CriterionConfig criterionConfig;
    private DatabaseDialect dialect;

    @Override
    public RowMapper<T> setConfig(CriterionConfig config) {
        this.criterionConfig = config;
        return this;
    }

    @Override
    public CriterionConfig getConfig() {
        if (criterionConfig == null) {
            return CriterionConfig.GlobalConfig.getConfig();
        }
        return criterionConfig;
    }

    /**
     * 设置数据库方言
     */
    public void setDialect(DatabaseDialect dialect) {
        this.dialect = dialect;
    }
    
    private BeanMeta getBeanMeta(Class<?> clazz) {
        return metaCache.computeIfAbsent(clazz, BeanMeta::new);
    }

    private static String toTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            if (table.name() != null && !table.name().isEmpty()) {
                return table.name();
            }
        }
        return camelToUnderline(clazz.getSimpleName());
    }

    private static String camelToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private static Field getPkField(Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    private Object getEntityId(Object entity) {
        if (entity == null) return null;
        Class<?> clazz = entity.getClass();
        BeanMeta beanMeta = getBeanMeta(clazz);
        Field pk = getPkField(clazz);
        if (pk == null) return null;
        
        PropertyAccessor accessor = beanMeta.getPropertyAccessor(pk);
        return accessor.get(entity);
    }

    public void register(Class<?> clazz) {
        String tableName = toTableName(clazz);
        String alias = tableName.toUpperCase();
        Field pkField = getPkField(clazz);
        if (pkField == null) {
            //throw new IllegalStateException("No @Id field found for class " + clazz.getName());
            return;
        }
        aliasMapping.put(alias, new TableMeta(tableName, pkField.getName(), clazz));
    }

    public void register(Class<?> clazz, String alias) {
        String tableName = toTableName(clazz);
        alias = alias.toUpperCase();
        Field pkField = getPkField(clazz);
        if (pkField == null) {
            //throw new IllegalStateException("No @Id field found for class " + clazz.getName());
            return;
        }
        aliasMapping.put(alias, new TableMeta(tableName, pkField.getName(), clazz));
    }

    public void register(String tableName, String alias, Class<?> clazz) {
        alias = alias.toUpperCase();
        Field pkField = getPkField(clazz);
        if (pkField == null) {
            //throw new IllegalStateException("No @Id field found for class " + clazz.getName());
            return;
        }
        aliasMapping.put(alias, new TableMeta(tableName, pkField.getName(), clazz));
    }

    /**
     * 将当前 ResultSet 所在行映射为对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs, Class<T> resultType) throws Exception {
        if (List.class.isAssignableFrom(resultType)) {
            throw new IllegalArgumentException("resultType cannot be List type.");
        }
        // 1️⃣ 简单类型 如果是基础类型或常见简单类型，直接取第一列
        if (isSimpleType(resultType)) {
            Object val = rs.getObject(1); // 默认取第一列
            return (T) convertValue(val, resultType);
        }
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        // 2️⃣ Map 类型
        if (Map.class.isAssignableFrom(resultType)) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                String colName = meta.getColumnLabel(i);
                Object val = rs.getObject(i);
                rowMap.put(colName, val);
            }
            return (T) rowMap;
        }
        // 3️⃣ 实体类
        Set<String> visited = new HashSet<>(); // 防止循环引用
        return buildEntity(rs, resultType, colCount, meta, visited);
    }

    /**
     * 合并列表，按主键去重
     */
    public <T> List<T> mergeList(List<T> rawList, Class<T> resultType) throws Exception {
        Map<Object, T> mergedMap = new LinkedHashMap<>();
        Field idField = getPkField(resultType);
        if (idField == null) {
            throw new IllegalStateException("No @Id field found for class " + resultType.getName());
        }
        
        BeanMeta beanMeta = getBeanMeta(resultType);
        PropertyAccessor idAccessor = beanMeta.getPropertyAccessor(idField);
        
        for (T obj : rawList) {
            if (obj == null) continue;
            Object idVal = idAccessor.get(obj);
            if (idVal == null) {
                mergedMap.put(UUID.randomUUID(), obj);
                continue;
            }
            if (mergedMap.containsKey(idVal)) {
                merge(mergedMap.get(idVal), obj, new HashSet<>());
            } else {
                mergedMap.put(idVal, obj);
            }
        }
        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 合并两个对象（递归），避免循环引用
     */
    @SuppressWarnings("unchecked")
    public <T> T merge(T existing, T incoming, Set<String> visited) throws Exception {
        if (existing == null) return incoming;
        if (incoming == null) return existing;
        Class<?> clazz = existing.getClass();
        
        BeanMeta beanMeta = getBeanMeta(clazz);
        
        Field pk = getPkField(clazz);
        Object pkVal = null;
        if (pk != null) {
             pkVal = beanMeta.getPropertyAccessor(pk).get(existing);
        }
        
        String visitedKey = clazz.getName() + ":" + pkVal;
        if (visited.contains(visitedKey)) {
            return existing;
        }
        visited.add(visitedKey);
        
        for (Field field : clazz.getDeclaredFields()) {
            // 跳过静态字段
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            
            PropertyAccessor accessor = beanMeta.getPropertyAccessor(field);
            
            Object oldVal = accessor.get(existing);
            Object newVal = accessor.get(incoming);
            
            if (newVal == null) continue;
            // 集合类型（OneToMany）
            if (Collection.class.isAssignableFrom(field.getType())) {
                Collection<Object> oldCol = (Collection<Object>) oldVal;
                Collection<Object> newCol = (Collection<Object>) newVal;
                if (newCol != null && !newCol.isEmpty()) {
                    if (oldCol == null) {
                        oldCol = new ArrayList<>();
                        accessor.set(existing, oldCol);
                    }
                    // 按主键去重（优化：使用 Set 存储已处理的主键，避免 O(n²) 查找）
                    Set<Object> existingIds = new HashSet<>();
                    for (Object o : oldCol) {
                        Object id = getEntityId(o);
                        if (id != null) {
                            existingIds.add(id);
                        }
                    }
                    for (Object item : newCol) {
                        Object itemId = getEntityId(item); // 这里若是能用BeanMeta更好，但item类型不固定
                        if (itemId == null || !existingIds.contains(itemId)) {
                            oldCol.add(item);
                            if (itemId != null) {
                                existingIds.add(itemId);
                            }
                        }
                    }
                }
            }
            // 嵌套实体（OneToOne / ManyToOne）
            else if (isEntity(field.getType())) {
                Object mergedChild = merge(oldVal, newVal, visited);
                accessor.set(existing, mergedChild);
            }
            // 普通字段
            else {
                accessor.set(existing, newVal);
            }
        }
        return existing;
    }

    private boolean isEntity(Class<?> clazz) {
        return aliasMapping.values().stream()
                .anyMatch(meta -> meta.entityClass.equals(clazz));
    }

    @SuppressWarnings("unchecked")
    private <T> T buildEntity(ResultSet rs, Class<T> rootClass,
                              int colCount, ResultSetMetaData meta,
                              Set<String> visited) throws Exception {
        TableMeta rootMeta = findMetaByClass(rootClass);
        if (rootMeta == null) {
            throw new IllegalStateException("Class not registered: " + rootClass.getName());
        }
        Object pkValue = null;
        Map<String, Object> values = new HashMap<>();
        for (int i = 1; i <= colCount; i++) {
            String label = meta.getColumnLabel(i);
            String alias, field;
            if (label.contains("__")) {
                String[] parts = label.split("__", 2);
                alias = parts[0];
                field = parts[1];
            } else {
                alias = rootMeta.tableName;
                field = label;
            }
            TableMeta tm = resolveTableMeta(alias, rootClass);
            if (tm == null || !tm.entityClass.equals(rootClass)) continue;
            Object val = rs.getObject(i);
            values.put(field, val);
            if (field.equalsIgnoreCase(tm.pkField)) pkValue = val;
        }
        if (pkValue == null) return null;
        String visitedKey = rootMeta.tableName + ":" + pkValue;
        if (visited.contains(visitedKey)) {
            return (T) createProxyObject(rootClass, pkValue);
        }
        visited.add(visitedKey);
        
        BeanMeta beanMeta = getBeanMeta(rootClass);
        T instance = (T) beanMeta.newInstance();
        
        populate(instance, values, beanMeta);
        
        for (Field field : rootClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            TableMeta childMeta = findMetaByClass(fieldType);
            if (childMeta != null) {
                Object child = buildEntity(rs, fieldType, colCount, meta, visited);
                if (child != null) {
                    PropertyAccessor childAccessor = beanMeta.getPropertyAccessor(field);
                    childAccessor.set(instance, child);
                }
            } else if (Collection.class.isAssignableFrom(fieldType)) {
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> elemType = (Class<?>) listType.getActualTypeArguments()[0];
                TableMeta elemMeta = findMetaByClass(elemType);
                if (elemMeta != null) {
                    Object child = buildEntity(rs, elemType, colCount, meta, visited);
                    if (child != null) {
                        PropertyAccessor collAccessor = beanMeta.getPropertyAccessor(field);
                        Collection<Object> coll = (Collection<Object>) collAccessor.get(instance);
                        if (coll == null) {
                            coll = new ArrayList<>();
                            collAccessor.set(instance, coll);
                        }
                        Object childId = getEntityId(child);
                        if (childId == null || coll.stream().noneMatch(o -> Objects.equals(getEntityId(o), childId))) {
                            coll.add(child);
                        }
                    }
                }
            }
        }
        visited.remove(visitedKey);
        return instance;
    }

    /**
     * 根据策略链填充字段
     */
    private void populate(Object instance, Map<String, Object> values, BeanMeta beanMeta) throws Exception {
        CriterionConfig config = getConfig();
        FieldNamingStrategyChain strategyChain = config.resolveStrategyChain();
        boolean warnOnMissing = config.resolveWarnOnMissingField();
        boolean strictMode = config.resolveStrictMode();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String columnName = entry.getKey();
            PropertyAccessor accessor = beanMeta.getPropertyAccessor(columnName, strategyChain);
            
            if (accessor != null) {
                Object convertedValue = convertValue(entry.getValue(), accessor.getType());
                accessor.set(instance, convertedValue);
            } else {
                if (strictMode) {
                    throw new NoSuchFieldException("无法在类 " + instance.getClass().getName() + " 中找到对应列 " + columnName + " 的属性");
                }
                if (warnOnMissing) {
                    logger.warn("无法在类 {} 中找到对应列 {} 的属性", instance.getClass().getName(), columnName);
                }
            }
        }
    }

    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz == java.util.Date.class
                || clazz == java.time.LocalDate.class
                || clazz == java.time.LocalDateTime.class
                || clazz == java.time.LocalTime.class;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;
        if (value instanceof java.sql.Date && targetType == java.time.LocalDate.class) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof java.sql.Timestamp && targetType == java.time.LocalDateTime.class) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.sql.Time && targetType == java.time.LocalTime.class) {
            return ((java.sql.Time) value).toLocalTime();
        }
        if (value instanceof String) {
            String str = (String) value;
            if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(str);
            if (targetType == Long.class || targetType == long.class) return Long.parseLong(str);
            if (targetType == Double.class || targetType == double.class) return Double.parseDouble(str);
            if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(str);
        }
        return value;
    }

    private TableMeta findMetaByClass(Class<?> clazz) {
        return aliasMapping.values().stream()
                .filter(m -> m.entityClass.equals(clazz))
                .findFirst().orElse(null);
    }

    private TableMeta resolveTableMeta(String alias, Class<?> targetClass) {
        // 使用方言去掉列名引号
        if (dialect != null) {
            alias = dialect.unquoteIdentifier(alias);
        }
        
        // 将别名转换为大写进行查找
        TableMeta tm = aliasMapping.get(alias.toUpperCase());
        if (tm != null) return tm;

        String tableName = toTableName(targetClass);
        Field pk = getPkField(targetClass);
        if (pk == null) {
            throw new IllegalStateException("No @Id field found for class " + targetClass.getName());
        }
        return new TableMeta(tableName, pk.getName(), targetClass);
    }

    private Object createProxyObject(Class<?> targetClass, Object idValue) {
        try {
            BeanMeta beanMeta = getBeanMeta(targetClass);
            Object proxy = beanMeta.newInstance();
            Field pk = getPkField(targetClass);
            if (pk != null) {
                beanMeta.getPropertyAccessor(pk).set(proxy, idValue);
            }
            return proxy;
        } catch (Exception e) {
            return null;
        }
    }

    static class TableMeta {
        String tableName;
        String pkField;
        Class<?> entityClass;

        TableMeta(String tableName, String pkField, Class<?> entityClass) {
            this.tableName = tableName;
            this.pkField = pkField;
            this.entityClass = entityClass;
        }
    }
}
