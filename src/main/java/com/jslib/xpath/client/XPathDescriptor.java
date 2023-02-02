package com.jslib.xpath.client;

import js.format.Format;

public interface XPathDescriptor {
	void setType(Class<?> type);

	Class<?> getType();

	String getValue();

	/**
	 * 
	 * @return
	 * @throws ClassNotFoundException if configured class cannot be found.
	 * @throws ClassCastException if configured class is found but does not implements parser interface.
	 */
	Class<? extends XPath.Parser> getParserClass() throws ClassNotFoundException, ClassCastException;

	Class<? extends Format> getFormatClass() throws ClassNotFoundException, ClassCastException;

	String getAttributeName();

	/**
	 * 
	 * @return
	 * @throws IllegalArgumentException if configured XPath type is not a constant of {@link XPath.Type}.
	 */
	XPath.Type getXPathType() throws IllegalArgumentException;
}