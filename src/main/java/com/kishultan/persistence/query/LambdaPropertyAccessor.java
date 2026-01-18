package com.kishultan.persistence.query;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 基于LambdaMetafactory的属性访问器实现
 * 提供接近直接调用的性能
 */
public class LambdaPropertyAccessor implements PropertyAccessor {
    private final String name;
    private final Class<?> type;
    private final Function<Object, Object> getter;
    private final BiConsumer<Object, Object> setter;

    @SuppressWarnings("unchecked")
    public LambdaPropertyAccessor(Field field) {
        this.name = field.getName();
        this.type = field.getType();
        
        field.setAccessible(true);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        
        this.getter = createGetter(lookup, field);
        this.setter = createSetter(lookup, field);
    }

    @Override
    public Object get(Object bean) {
        return getter.apply(bean);
    }

    @Override
    public void set(Object bean, Object value) {
        setter.accept(bean, value);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    private Function<Object, Object> createGetter(MethodHandles.Lookup lookup, Field field) {
        try {
            MethodHandle handle = lookup.unreflectGetter(field);
            CallSite site = LambdaMetafactory.metafactory(
                lookup,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class),
                handle,
                MethodType.methodType(Object.class, field.getDeclaringClass()) // 这里的返回值如果是基本类型，LambdaMetafactory会自动处理装箱吗？
                // 如果是基本类型，implMethodType 应该是 (Bean)int，而 samMethodType 是 (Object)Object。
                // 自动装箱需要 verify.
                // 实际上可能需要明确指定 implMethodType 为 (Bean)int
                // MethodType.methodType(field.getType(), field.getDeclaringClass()) // 这样是对的
            );
            // 修正：implMethodType 必须与 handle 的类型一致（或兼容）
            // handle type: (DeclaringClass)FieldType
            // samMethodType: (Object)Object
            // instantiatedMethodType: (DeclaringClass)FieldType (带具体类型)
            // 但是 Function.apply 返回 Object。如果 FieldType 是 int，这里需要装箱。
            // LambdaMetafactory 可以自动处理装箱。
            // 但是 instantiatedMethodType 的返回值应该是 FieldType (例如 int)，而不是 Object (除非我们显式要求装箱后的类型)。
            // 如果我们传入 (DeclaringClass)int，LambdaMetafactory 会生成返回 int 的方法。
            // 但是 Function 接口要求返回 Object。
            // 这会导致编译错误或运行时错误，因为生成的类实现 Function，其 apply 方法返回 Object。
            // 必须让 instantiatedMethodType 返回 Object (或引用类型)。
            
            // 重新尝试：
            // handle: (Bean)int
            // samMethodType: (Object)Object
            // instantiatedMethodType: (Bean)Integer (装箱后)
            // 这样 LambdaMetafactory 会自动插入装箱代码。
            
            MethodType instantiatedType = MethodType.methodType(wrap(field.getType()), field.getDeclaringClass());
            
            site = LambdaMetafactory.metafactory(
                lookup,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class),
                handle,
                instantiatedType
            );
            
            return (Function<Object, Object>) site.getTarget().invokeExact();
        } catch (Throwable e) {
            // 降级为 MethodHandle
            try {
                MethodHandle handle = lookup.unreflectGetter(field);
                return bean -> {
                    try {
                        return handle.invoke(bean);
                    } catch (Throwable ex) {
                        throw new RuntimeException(ex);
                    }
                };
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private BiConsumer<Object, Object> createSetter(MethodHandles.Lookup lookup, Field field) {
        try {
            MethodHandle handle = lookup.unreflectSetter(field);
            
            // handle: (Bean, FieldType)void
            // sam: (Object, Object)void
            // instantiated: (Bean, FieldType)void (Wait, second arg must be Object or reference?)
            // 如果 FieldType 是 int，我们需要 (Bean, Integer)void -> (Bean, int)void (自动拆箱)
            
            MethodType instantiatedType = MethodType.methodType(void.class, field.getDeclaringClass(), wrap(field.getType()));
            
            CallSite site = LambdaMetafactory.metafactory(
                lookup,
                "accept",
                MethodType.methodType(BiConsumer.class),
                MethodType.methodType(void.class, Object.class, Object.class),
                handle,
                instantiatedType
            );
            
            return (BiConsumer<Object, Object>) site.getTarget().invokeExact();
        } catch (Throwable e) {
             try {
                MethodHandle handle = lookup.unreflectSetter(field);
                return (bean, val) -> {
                    try {
                        handle.invoke(bean, val);
                    } catch (Throwable ex) {
                        throw new RuntimeException(ex);
                    }
                };
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == boolean.class) return Boolean.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
