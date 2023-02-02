package com.jslib.xpath.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jslib.xpath.client.XPath.NULL_Format;
import com.jslib.xpath.client.XPath.NULL_Parser;
import com.jslib.xpath.client.XPath.Parser;
import com.jslib.xpath.client.XPath.Type;

import js.format.Format;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueXPath {
	String value();

	Type type() default Type.STRING;

	String attribute() default "";

	Class<? extends Parser> parser() default NULL_Parser.class;

	Class<? extends Format> format() default NULL_Format.class;
}
