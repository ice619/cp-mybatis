package com.cp.mybatis.annotation;

import java.lang.annotation.*;

/**
 * Description:
 *
 * @author chenpeng
 * @date 2019/9/5 20:13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ExtParam {
    String value();
}
