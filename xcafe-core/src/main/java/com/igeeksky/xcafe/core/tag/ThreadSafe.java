package com.igeeksky.xcafe.core.tag;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 线程安全标记
 * @author Tony.Lau
 * @createTime 2016-10-07 23:26:52
 * @since 0.9.0
 * @email coffeelifelau@163.com
 * @blog<a href="http://blog.csdn.net/coffeelifelau">刘涛的编程笔记</a>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ThreadSafe {

}
