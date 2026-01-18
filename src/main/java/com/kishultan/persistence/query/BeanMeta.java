package com.kishultan.persistence.query;

import com.kishultan.persistence.query.config.FieldNamingStrategy;
import com.kishultan.persistence.query.config.FieldNamingStrategyChain;
import jakarta.persistence.Column;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Bean元数据
 * 缓存Bean的属性访问器，处理字段匹配策略
 */
public class BeanMeta {
    private final Class<?> beanClass;
    private final Map<String, PropertyAccessor> columnToAccessorMap = new ConcurrentHashMap<>();
    private final Map<Field, PropertyAccessor> fieldToAccessorMap = new ConcurrentHashMap<>();
    private final Supplier<Object> constructor;
    
    public BeanMeta(Class<?> beanClass) {
        this.beanClass = beanClass;
        this.constructor = createConstructor(beanClass);
    }
    
    public PropertyAccessor getPropertyAccessor(Field field) {
        return fieldToAccessorMap.computeIfAbsent(field, LambdaPropertyAccessor::new);
    }
    
    public Object newInstance() {
        return constructor.get();
    }
    
    @SuppressWarnings("unchecked")
    private Supplier<Object> createConstructor(Class<?> clazz) {
        try {
            // caller lookup（定义 lambda 的地方）
            MethodHandles.Lookup callerLookup = MethodHandles.lookup();

            // target lookup（目标类的 lookup）
            MethodHandles.Lookup targetLookup =
                    MethodHandles.privateLookupIn(clazz, callerLookup);

            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true); // JDK 8 / fallback 友好

            MethodHandle ctorHandle =
                    targetLookup.unreflectConstructor(ctor);

            CallSite site = LambdaMetafactory.metafactory(
                    targetLookup, // ⚠️ 注意这里
                    "get",
                    MethodType.methodType(Supplier.class),
                    MethodType.methodType(Object.class),
                    ctorHandle,
                    MethodType.methodType(Object.class)
            );

            return (Supplier<Object>) site.getTarget().invokeExact();

        } catch (Throwable e) {
            // fallback
            return () -> {
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    return ctor.newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(
                            "无法实例化类: " + clazz.getName(), ex
                    );
                }
            };
        }
    }


    public PropertyAccessor getPropertyAccessor(String columnName, FieldNamingStrategyChain strategyChain) {
        return columnToAccessorMap.computeIfAbsent(columnName, key -> findPropertyAccessor(key, strategyChain));
    }
    
    private PropertyAccessor findPropertyAccessor(String columnName, FieldNamingStrategyChain chain) {
        for (FieldNamingStrategy strategy : chain.getStrategies()) {
            Field field = matchField(columnName, strategy);
            if (field != null) {
                return new LambdaPropertyAccessor(field);
            }
        }
        return null;
    }
    
    private Field matchField(String columnName, FieldNamingStrategy strategy) {
        try {
            switch (strategy) {
                case ANNOTATION:
                    for (Field field : beanClass.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Column.class)) {
                            Column column = field.getAnnotation(Column.class);
                            if (columnName.equalsIgnoreCase(column.name())) {
                                return field;
                            }
                        }
                    }
                    return null;
                case EXACT_MATCH:
                    return beanClass.getDeclaredField(columnName);
                case CAMEL_CASE:
                    return beanClass.getDeclaredField(camelCase(columnName));
                case UNDERLINE:
                    return beanClass.getDeclaredField(camelToUnderline(columnName));
                case CASE_INSENSITIVE:
                    for (Field field : beanClass.getDeclaredFields()) {
                        if (field.getName().equalsIgnoreCase(columnName)) {
                            return field;
                        }
                    }
                    return null;
                case UPPER_CASE:
                    return beanClass.getDeclaredField(columnName.toUpperCase());
                case LOWER_CASE:
                    return beanClass.getDeclaredField(columnName.toLowerCase());
                default:
                    return null;
            }
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private String camelCase(String name) {
        if (name == null || name.isEmpty()) return name;
        boolean allUpper = true;
        for (char c : name.toCharArray()) {
            if (Character.isLetter(c) && !Character.isUpperCase(c)) {
                allUpper = false;
                break;
            }
        }
        if (allUpper) {
            name = name.toLowerCase();
        }

        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                up = true;
            } else if (up) {
                sb.append(Character.toUpperCase(c));
                up = false;
            } else {
                sb.append(c);
            }
        }
        if (sb.toString().endsWith("ID")) {
            return sb.substring(0, sb.length() - 2) + "Id";
        }
        return sb.toString();
    }
    
    private String camelToUnderline(String str) {
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
}
