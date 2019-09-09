package com.cp.mybatis.mapper;

import com.cp.mybatis.annotation.ExtInsert;
import com.cp.mybatis.annotation.ExtParam;
import com.cp.mybatis.annotation.ExtSelect;
import com.cp.mybatis.bean.LoanSwitchConfig;

/**
 * Description:
 *
 * @author chenpeng
 * @date 2019/9/9 15:02
 */
public interface LoanSwitchConfigMapper {
    @ExtInsert("insert into user(userName,userAge) values(#{userName},#{userAge})")
    public int insertUser(@ExtParam("userName") String userName, @ExtParam("userAge") Integer userAge);

    @ExtSelect("select * from loan_switch_config where app_name=#{app_name} ")
    LoanSwitchConfig getByAppName(@ExtParam("app_name") String app);
}
