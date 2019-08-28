package js.xpath.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom HTTP header(s) sent on every request to the remote site.
 * 
 * @author Iulian Rotaru
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
	/**
	 * HTTP header(s) as name / value pairs separated by colon. Spaces around colon are ignored.
	 * 
	 * @return custom HTTP headers.
	 */
	String[] value();
}
