package com.jslib.xpath.client;

import java.lang.reflect.Type;

public interface ResultClass {
	Object newInstance();

	Iterable<Field> getFields();

	interface Field {
		XPathDescriptor getXPath();

		XPathDescriptor getValueXPath();

		Type getType();

		void setValue(Object object, Object value);
	}
}
