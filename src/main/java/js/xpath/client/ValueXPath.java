package js.xpath.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import js.format.Format;
import js.xpath.client.XPath.NULL_Format;
import js.xpath.client.XPath.NULL_Parser;
import js.xpath.client.XPath.Parser;
import js.xpath.client.XPath.Type;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueXPath {
	String value();

	Type type() default Type.STRING;

	String attribute() default "";

	Class<? extends Parser> parser() default NULL_Parser.class;

	Class<? extends Format> format() default NULL_Format.class;
}
