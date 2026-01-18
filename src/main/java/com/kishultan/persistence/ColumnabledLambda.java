package com.kishultan.persistence;

import jakarta.persistence.*;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda表达式反射工具类
 * 专门用于解析Columnable（Lambda表达式）的字段信息和JPA注解
 * <p>
 * 从 persistence.orm.query.utils 移到此包，以打破 orm ↔ query ↔ query.utils 的循环依赖
 */
public class ColumnabledLambda {
    // 缓存机制，避免重复反射
    private static final Map<String, FieldInfo> FIELD_INFO_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> FIELD_TYPE_CACHE = new ConcurrentHashMap<>();

    // 私有构造函数，防止实例化
    private ColumnabledLambda() {
    }

    /**
     * 从Columnable中提取字段信息
     * 统一入口方法，返回完整的字段信息
     */
    public static <T, R> FieldInfo getFieldInfo(Columnable<T, R> function) {
        try {
            // 生成缓存key
            String cacheKey = generateCacheKey(function);
            // 检查缓存
            FieldInfo cached = FIELD_INFO_CACHE.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            // 解析字段信息
            FieldInfo fieldInfo = parseFieldInfo(function);
            // 缓存结果
            if (fieldInfo != null) {
                FIELD_INFO_CACHE.put(cacheKey, fieldInfo);
            }
            return fieldInfo;
        } catch (Exception e) {
            return null;
        }
    }
    // ==================== Lambda表达式解析 ====================

    /**
     * 从Columnable中提取字段名
     */
    public static <T, R> String getFieldName(Columnable<T, R> function) {
        FieldInfo info = getFieldInfo(function);
        return info != null ? info.getFieldName() : "unknown_field";
    }

    /**
     * 从Columnable中提取数据库列名
     */
    public static <T, R> String getColumnName(Columnable<T, R> function) {
        FieldInfo info = getFieldInfo(function);
        return info != null ? info.getColumnName() : "unknown_column";
    }

    /**
     * 从Columnable中提取字段类型
     */
    public static <T, R> Class<?> getFieldType(Columnable<T, R> function) {
        FieldInfo info = getFieldInfo(function);
        return info != null ? info.getFieldType() : Object.class;
    }

    /**
     * 从Columnable中提取实体类
     */
    public static <T, R> Class<?> getEntityClass(Columnable<T, R> function) {
        FieldInfo info = getFieldInfo(function);
        return info != null ? info.getEntityClass() : null;
    }

    /**
     * 解析字段信息的核心方法
     */
    private static <T, R> FieldInfo parseFieldInfo(Columnable<T, R> function) {
        try {
            // 1. 获取SerializedLambda
            SerializedLambda lambda = getSerializedLambda(function);
            if (lambda == null) {
                return null;
            }
            // 2. 获取目标类和字段名
            // 使用lambda表达式本身的ClassLoader（由portlet ClassLoader加载）
            ClassLoader lambdaClassLoader = function.getClass().getClassLoader();
            Class<?> targetClass = getTargetClass(lambda, lambdaClassLoader);
            String methodName = lambda.getImplMethodName();
            String fieldName = extractFieldNameFromMethod(methodName);
            // 3. 获取字段对象
            Field field = getField(targetClass, fieldName);
            if (field == null) {
                return null;
            }
            // 4. 解析字段信息
            String columnName = resolveColumnName(field, fieldName);
            Class<?> fieldType = field.getType();
            boolean isAssociation = isAssociationField(field);
            return new FieldInfo(fieldName, columnName, fieldType, targetClass, isAssociation);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // ==================== 核心解析方法 ====================

    /**
     * 获取SerializedLambda对象
     */
    private static <T, R> SerializedLambda getSerializedLambda(Columnable<T, R> function) {
        try {
            Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object result = writeReplace.invoke(function);
            return result instanceof SerializedLambda ? (SerializedLambda) result : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取目标类（使用指定的ClassLoader）
     */
    private static Class<?> getTargetClass(SerializedLambda lambda, ClassLoader classLoader) throws ClassNotFoundException {
        String className = lambda.getImplClass().replace('/', '.');
        
        // 首先尝试使用传入的ClassLoader（通常是lambda表达式本身的ClassLoader）
        if (classLoader != null) {
            try {
                return Class.forName(className, true, classLoader);
            } catch (ClassNotFoundException e) {
                // 如果指定的ClassLoader找不到，继续尝试其他方式
            }
        }

        // 尝试使用调用者的类加载器
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // 最后尝试使用ColumnabledLambda自己的类加载器
            return Class.forName(className, true, ColumnabledLambda.class.getClassLoader());
        }
    }

    /**
     * 获取目标类（向后兼容）
     */
    private static Class<?> getTargetClass(SerializedLambda lambda) throws ClassNotFoundException {
        return getTargetClass(lambda, null);
    }

    /**
     * 从方法名提取字段名
     */
    private static String extractFieldNameFromMethod(String methodName) {
        if (methodName.startsWith("get")) {
            String fieldName = methodName.substring(3);
            return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        } else if (methodName.startsWith("is")) {
            String fieldName = methodName.substring(2);
            return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        }
        return methodName;
    }

    /**
     * 获取字段对象（支持继承）
     */
    private static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 尝试从父类获取
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getField(superClass, fieldName);
            }
            return null;
        }
    }

    /**
     * 判断是否是关联字段
     */
    private static boolean isAssociationField(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) ||
                field.isAnnotationPresent(OneToOne.class) ||
                field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToMany.class);
    }
    // ==================== JPA注解解析 ====================

    /**
     * 解析字段的数据库列名（静态方法，供外部调用）
     * 用于支持现有的 getColumnName(Class, String) 方法
     */
    public static String resolveColumnName(Field field, String fieldName) {
        // 1. 优先检查 @JoinColumn 注解（关联字段）
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn != null && !joinColumn.name().isEmpty()) {
            return joinColumn.name();
        }
        // 2. 检查 @Column 注解
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        }
        // 3. 检查是否是关联字段
        if (isAssociationField(field)) {
            // 关联字段默认使用 fieldName + "_id" 规则
            return fieldName + "_id";
        }
        // 4. 默认返回字段名
        return fieldName;
    }

    /**
     * 生成缓存key
     */
    private static <T, R> String generateCacheKey(Columnable<T, R> function) {
        return function.getClass().getName() + "@" + System.identityHashCode(function);
    }
    // ==================== 缓存管理 ====================

    /**
     * 清除缓存
     */
    public static void clearCache() {
        FIELD_INFO_CACHE.clear();
        FIELD_TYPE_CACHE.clear();
    }

    /**
     * 获取缓存统计信息
     */
    public static String getCacheStats() {
        return String.format("FieldInfo缓存: %d, FieldType缓存: %d",
                FIELD_INFO_CACHE.size(), FIELD_TYPE_CACHE.size());
    }

    /**
     * 字段信息类
     */
    public static class FieldInfo {
        private final String fieldName;           // 字段名（如：clinic）
        private final String columnName;          // 数据库列名（如：clinic_id）
        private final Class<?> fieldType;         // 字段类型
        private final Class<?> entityClass;       // 实体类
        private final boolean isAssociation;      // 是否是关联字段

        public FieldInfo(String fieldName, String columnName, Class<?> fieldType,
                         Class<?> entityClass, boolean isAssociation) {
            this.fieldName = fieldName;
            this.columnName = columnName;
            this.fieldType = fieldType;
            this.entityClass = entityClass;
            this.isAssociation = isAssociation;
        }

        // Getters
        public String getFieldName() {
            return fieldName;
        }

        public String getColumnName() {
            return columnName;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }

        public boolean isAssociation() {
            return isAssociation;
        }
    }
}
