package com.cp.mybatis;
import com.cp.mybatis.annotation.ExtInsert;
import com.cp.mybatis.annotation.ExtParam;
import com.cp.mybatis.annotation.ExtSelect;
import com.cp.mybatis.utils.JDBCUtils;
import com.cp.mybatis.utils.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import	java.util.concurrent.ConcurrentHashMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Description:
 * 1.使用动态代理技术,获取接口方法上的sql语句
 * 2.根据不同的SQL语句
 * @author chenpeng
 * @date 2019/9/5 17:00
 */
public class MyInvocationHandlerMbatis implements InvocationHandler {
    /**
     * 这个就是我们要代理的真实对象
     */
    private Object subject;

    public MyInvocationHandlerMbatis(Object subject) {
        this.subject = subject;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 判断方法上是否有ExtInsert注解
        ExtInsert extInsert = method.getAnnotation(ExtInsert.class);
        if(extInsert != null){
            return insertSQL(extInsert, method, args);
        }
        // 判断方法上注解类型
        ExtSelect extSelect = method.getAnnotation(ExtSelect.class);
        if (extSelect != null) {
            return selectMybatis(extSelect, method, args);
        }

        return null;
    }

    private Object selectMybatis(ExtSelect extSelect, Method method, Object[] args) {

        try {
            // 获取查询SQL语句
            String selectSQL = extSelect.value();
            // 将方法上的参数存放在Map集合中
            Parameter[] parameters = method.getParameters();
            // 获取方法上参数集合
            ConcurrentHashMap<Object, Object> parameterMap = getExtParams(parameters, args);
            // 获取SQL传递参数
            List<String> sqlSelectParameter = SQLUtils.sqlSelectParameter(selectSQL);
            // 排序参数
            List<Object> parameValues = new ArrayList<>();
            for (int i = 0; i < sqlSelectParameter.size(); i++) {
                String parameterName = sqlSelectParameter.get(i);
                Object object = parameterMap.get(parameterName);
                parameValues.add(object.toString());
            }
            // 变为?号
            String newSql = SQLUtils.parameQuestion(selectSQL, sqlSelectParameter);
            System.out.println("执行SQL:" + newSql + "参数信息:" + parameValues.toString());
            // 调用JDBC代码查询
            ResultSet rs = JDBCUtils.query(newSql, parameValues);
            // 获取返回类型
            Class<?> returnType = method.getReturnType();
            if(!rs.next()){
                // 没有查找数据
                return null;
            }
            // 向上移动
            rs.previous();

            // 实例化对象
            Object newInstance = returnType.newInstance();



        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    private Object insertSQL(ExtInsert extInsert, Method method, Object[] args) {
        return null;
    }

    /**
     * 获取方法上参数集合
     * @param parameters
     * @param args
     * @return
     */
    private ConcurrentHashMap<Object, Object> getExtParams(Parameter [] parameters, Object [] args){
        ConcurrentHashMap<Object, Object> paramMap = new ConcurrentHashMap<Object, Object> ();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ExtParam extParam  = parameter.getDeclaredAnnotation(ExtParam.class);
            // 参数名称
            String paramValue  = extParam.value();
            // 参数值
            Object arg = args[i];
            paramMap.put(paramValue, arg);
        }
    }

}