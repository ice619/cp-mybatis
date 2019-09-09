package com.cp.mybatis;

import java.lang.reflect.Proxy;

/**
 * Description:
 *
 * @author chenpeng
 * @date 2019/9/9 14:51
 */
public class SqlSession {

    //获取mapper代理类
    public static <T> T getMapper(Class<T> tClass){
        return (T)Proxy.newProxyInstance(tClass.getClassLoader(),new Class [] {tClass}, new MyInvocationHandlerMbatis(tClass));

    }
}