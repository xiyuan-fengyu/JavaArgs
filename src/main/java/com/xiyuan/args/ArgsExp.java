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
     * commandName [optionName&lt;defaultValue&gt;&lt;optionValueRegex&gt;] [optionName] (optionName&lt;optionValueRegex&gt;) &lt;optionName&lt;optionValueRegex&gt;&gt;
     * 其中
     * [optionName&lt;defaultValue&gt;&lt;optionValueRegex&gt;] 为可选参数，不提供这个参数时，使用defaultValue作为值，其他为必选参数
     * [optionName] 为可选参数，如果未提供该参数，值为false，提供了该参数，值为true
     * &lt;optionName&lt;optionValueRegex&gt;&gt; 这种参数是[optionName&lt;defaultValue&gt;&lt;optionValueRegex&gt;] [optionName] (optionName&lt;optionValueRegex&gt;) 没有匹配上的参数列表按顺序匹配
     * 当optionValueRegex为空字符串的时候，匹配所有的值
     * 实际命令的每一项默认使用正则" +"来分割，可以通过重载Args的getItemDivider方法来设置自定义的分割符
     */
    String exp();

    String usage() default "";

}
