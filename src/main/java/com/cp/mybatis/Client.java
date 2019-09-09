package com.cp.mybatis;

import com.cp.mybatis.bean.LoanSwitchConfig;
import com.cp.mybatis.mapper.LoanSwitchConfigMapper;

/**
 * Description:
 *
 * @author chenpeng
 * @date 2019/9/9 14:56
 */
public class Client {
    public static void main(String[] args) {

        LoanSwitchConfigMapper mapper = SqlSession.getMapper(LoanSwitchConfigMapper.class);
        LoanSwitchConfig byAppName = mapper.getByAppName("41");
        System.out.println(byAppName.toString());
    }
}