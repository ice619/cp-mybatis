package com.cp.mybatis;
import com.cp.mybatis.annotation.ExtInsert;
import com.cp.mybatis.annotation.ExtParam;
import com.cp.mybatis.annotation.ExtSelect;
import com.cp.mybatis.utils.JDBCUtils;
import com.cp.mybatis.utils.SQLUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import	java.util.concurrent.ConcurrentHashMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 判断方法上是否有ExtInsert注解
        ExtInsert extInsert = method.getAnnotation(ExtInsert.class);
        if (extInsert != null) {
            return insertSQL(extInsert, method, args);
        }
        // 判断方法上注解类型
        ExtSelect extSelect = method.getAnnotation(ExtSelect.class);
        if (extSelect != null) {
            return selectMybatis(extSelect, method, args);
        }

        return null;
    }

    private Object selectMybatis(ExtSelect extSelect, Method method, Object[] args) throws SQLException {

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
            if (!rs.next()) {
                // 没有查找数据
                return null;
            }
            // 向上移动
            rs.previous();

            // 实例化对象
            Object newInstance = returnType.newInstance();
            while (rs.next()) {
                Field[] declaredFields = returnType.getDeclaredFields();
                for(Field field : declaredFields){
                    String fileName = field.getName();
                    String fileNameWithLine = humpToLine(fileName);
                    // 获取集合中数据
                    Object value = rs.getObject(fileNameWithLine);
                    // 设置允许私有访问
                    field.setAccessible(true);
                    // 赋值参数
                    field.set(newInstance, value);
                }


//                for (String parameterName : sqlSelectParameter) {
//                    // 获取集合中数据
//                    Object value = rs.getObject(parameterName);
//                    // 查找对应属性
//                    String s = lineToHump(parameterName);
//                    Field field = returnType.getDeclaredField(s);
//                    // 设置允许私有访问
//                    field.setAccessible(true);
//                    // 赋值参数
//                    field.set(newInstance, value);
//                }

            }

            return newInstance;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    private Object insertSQL(ExtInsert extInsert, Method method, Object[] args) {
        // 获取注解上的sql
        String insertSql = extInsert.value();
        System.out.println("sql:" + insertSql);
        // 获取方法上的参数
        Parameter[] parameters = method.getParameters();
        // 将方法上的参数存放在Map集合中
        ConcurrentHashMap<Object, Object> parameterMap = getExtParams(parameters, args);
        // 获取SQL语句上需要传递的参数
        String[] sqlParameter = SQLUtils.sqlInsertParameter(insertSql);
        List<Object> parameValues = new ArrayList<>();
        for (int i = 0; i < sqlParameter.length; i++) {
            String str = sqlParameter[i];
            Object object = parameterMap.get(str);
            parameValues.add(object);
        }
        // 将SQL语句替换为？号
        String newSql = SQLUtils.parameQuestion(insertSql, sqlParameter);
        System.out.println("newSql:" + newSql);
        // 调用jdbc代码执行
        int insertResult = JDBCUtils.insert(newSql, false, parameValues);
        return insertResult;

    }

    /**
     * 获取方法上参数集合
     *
     * @param parameters
     * @param args
     * @return
     */
    private ConcurrentHashMap<Object, Object> getExtParams(Parameter[] parameters, Object[] args) {
        ConcurrentHashMap<Object, Object> paramMap = new ConcurrentHashMap<Object, Object>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ExtParam extParam = parameter.getDeclaredAnnotation(ExtParam.class);
            // 参数名称
            String paramValue = extParam.value();
            // 参数值
            Object arg = args[i];
            paramMap.put(paramValue, arg);
        }
        return paramMap;
    }

    public static Pattern linePattern = Pattern.compile("_(\\w)");

    /**下划线转驼峰*/
    public static String lineToHump(String str){
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Pattern humpPattern = Pattern.compile("[A-Z]");
    /**驼峰转下划线*/
    public static String humpToLine(String str){
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            matcher.appendReplacement(sb, "_"+matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


}