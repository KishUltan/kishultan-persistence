package com.kishultan.persistence.query;

/**
 * 属性访问器接口
 * 抽象属性的读写操作，屏蔽底层实现（反射、MethodHandle、LambdaMetafactory等）
 */
public interface PropertyAccessor {
    
    /**
     * 获取属性值
     * @param bean Bean实例
     * @return 属性值
     */
    Object get(Object bean);
    
    /**
     * 设置属性值
     * @param bean Bean实例
     * @param value 属性值
     */
    void set(Object bean, Object value);
    
    /**
     * 获取属性类型
     * @return 属性类型
     */
    Class<?> getType();
    
    /**
     * 获取属性名
     * @return 属性名
     */
    String getName();
}
