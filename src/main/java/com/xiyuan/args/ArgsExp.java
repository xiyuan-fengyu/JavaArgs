package com.xiyuan.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ArgsExp {

    /**
     * 格式：
     * commandName [optionName<defaultValue><optionValueRegex>] [optionName] (optionName<optionValueRegex>) <optionName<optionValueRegex>>
     * 其中
     * [optionName<defaultValue><optionValueRegex>] 为可选参数，不提供这个参数时，使用defaultValue作为值，其他为必选参数
     * [optionName] 为可选参数，如果未提供该参数，值为false，提供了该参数，值为true
     * <optionName<optionValueRegex>> 这种参数是[optionName<defaultValue><optionValueRegex>] [optionName] (optionName<optionValueRegex>) 没有匹配上的参数列表按顺序匹配
     * 当optionValueRegex为空字符串的时候，匹配所有的值
     * 实际命令的每一项默认使用正则" +"来分割，可以通过重载Args的getItemDivider方法来设置自定义的分割符
     */
    String exp();

    String usage() default "";

}
