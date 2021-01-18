package com.dn.utils;

import com.dn.exception.ReturnTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 方法相关的工具类, 简化方法的解析
 */
@Slf4j
public class MethodUtils {
    /**
     * 方法返回值泛型的缓存
     */
    public static final Map<String, List<Class>> genericTypeCache = new HashMap(64);

    /**
     * 方法返回值类型的缓存
     */
    public static final Map<String, Class> returnTypeCache = new HashMap(64);

    /**
     * 获取方法的唯一表示, 类路径::方法名(参数名:参数类型=运行时传入值)
     *
     * @param method 方法对象
     * @param args   运行时传参
     * @return
     */
    public static String getMethodId(Method method, Object... args) {

        // 1. 获取方法所在的实体类的名称
        String className = method.getDeclaringClass().getName();

        // 2. 获取方法的所有参数类型
        Parameter[] parameters = method.getParameters();

        // 4. 拼接内容 类路径::方法名(
        StringBuilder idBuilder = new StringBuilder(className).append("::").append(method.getName()).append("(");

        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            String parameterTypeName = parameters[i].getType().getTypeName();

            idBuilder.append(paramName).append(":").append(parameterTypeName);
            if (args.length == parameters.length) {
                idBuilder.append("=").append(args[i].toString());
            }
            if (i != parameters.length - 1) {
                idBuilder.append(",");
            }
        }

        // 5. 拼接内容 )
        return idBuilder.append(")").toString();
    }


    /**
     * 获取方法括号中的参数名称, 这里我们是基于 Spring 的参数解析完成的
     *
     * @param method 方法对象
     * @return
     */
    public static String[] getMethodParamNames(Method method) {
        ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = discoverer.getParameterNames(method);
        return parameterNames;
    }


    /**
     * 获取方法返回值中的泛型
     *
     * @param method
     * @return
     */
    public static List<Class> getMethodGenericTypes(Method method) {
        // 1. 获取方法的唯一标识
        String methodId = getMethodId(method);

        // 2. 尝试从缓存中获取结果
        List<Class> cache = genericTypeCache.get(methodId);

        // 3. 拿不到结果时
        if (cache == null) {
            // 基于唯一标识进行上锁, 将锁细粒度化. 避免线程冲突
            synchronized (methodId) {
                // 4. 继续尝试从缓存中获取, 稳一点
                cache = genericTypeCache.get(methodId);
                if (cache != null) {
                    return cache;
                }

                cache = new ArrayList<>();

                // 5. 通过代码获取
                Type genericReturnType = method.getGenericReturnType();

                if (genericReturnType instanceof ParameterizedType) {
                    Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                    for (Type type : actualTypeArguments) {
                        try {
                            cache.add(Class.forName(type.getTypeName()));
                        } catch (ClassNotFoundException e) {
                            log.error("类无法加载", e);
                        }
                    }
                }

                // 6. 填充结果到缓存
                genericTypeCache.put(methodId, cache);
            }
        }

        return cache;
    }


    /**
     * 获取方法返回值的类型, 如果是接口, 则获取它唯一的实现类. 如果存在多个实现类, 则抛出异常
     *
     * @param method
     * @return
     */
    public static Class getMethodReturnType(Method method) {
        // 1. 获取方法的唯一标识
        String methodId = getMethodId(method);
        Class<?> returnType = returnTypeCache.get(methodId);

        if(returnType == null) {
            // 基于唯一标识进行上锁, 将锁细粒度化. 避免线程冲突
            synchronized (methodId) {
                returnType = method.getReturnType();

                // 如果它是一个接口 或者 它是一个抽象类. 那么我们在进行 JSON 转换的时候是会抛出异常的. 为了解决这个问题. 我们需要拿到它的实现类
                if (returnType.isInterface() || Modifier.isAbstract(returnType.getModifiers())) {
                    List<Class> implementationClass = ClassUtils.getImplementationClass(returnType);
                    if (implementationClass.size() > 1) {
                        throw new ReturnTypeException("返回值类型为 [" + returnType.getName() + "] 的方法" + method.getName() + " 无法完成序列化操作, 因为它的实现类太多了. 如果要反序列化, 必须保证返回值类型为具体对象, 或者是只有一个子类的抽象类和接口");
                    }
                    if (implementationClass.size() == 0) {
                        throw new ReturnTypeException("返回值类型为 [" + returnType.getName() + "] 的方法 " + method.getName() + " 无法完成序列化操作, 因为返回类型没有实现类");
                    }
                    returnType = implementationClass.get(0);
                }

                returnTypeCache.put(methodId, returnType);
            }
        }

        // 如果它不是接口 或者 抽象类, 那么直接返回就 ok 了
        return returnType;
    }


}
