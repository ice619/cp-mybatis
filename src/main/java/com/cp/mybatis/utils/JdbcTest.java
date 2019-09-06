package com.cp.mybatis.utils;

import com.cp.mybatis.utils.JDBCUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author chenpeng
 * @date 2019/9/5 17:12
 */
public class JdbcTest {
    public static void main(String[] args) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        ResultSet res = JDBCUtils.query("select * from loan_application", params);
        while (res.next()) {
            String id = res.getString("loan_id");
            System.out.println("id:" + id );
        }
    }
}