package com.jslib.xpath.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.ParseException;

import js.format.Format;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XPath {
	String value();

	Type type() default Type.STRING;

	String attribute() default "";

	Class<? extends Parser> parser() default NULL_Parser.class;

	Class<? extends Format> format() default NULL_Format.class;

	enum Type {
		STRING, TEXT, NUMBER, BOOLEAN
	}

	interface Parser {
		Object parse(String value);
	}

	static class NULL_Parser implements Parser {
		@Override
		public Object parse(String value) {
			return null;
		}
	}

	static class NULL_Format implements Format {
		@Override
		public String format(Object object) {
			return null;
		}

		@Override
		public Object parse(String value) throws ParseException {
			return null;
		}
	}
}
