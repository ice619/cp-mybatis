package com.cp.mybatis.annotation;

import java.lang.annotation.*;

/**
 * Description:
 *
 * @author chenpeng
 * @date 2019/9/5 17:28
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExtSelect {

    String value();

}
