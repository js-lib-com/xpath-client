package com.jslib.xpath.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

import js.format.Format;
import js.lang.BugError;
import js.util.Classes;

public class AnnotatedResultClass implements ResultClass {
	private final Type resultType;
	private final Class<?> resultClass;

	public AnnotatedResultClass(Type resultType) {
		this.resultType = resultType;

		if (resultType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) resultType).getRawType();
			if (rawType instanceof ParameterizedType) {
				throw new BugError("Not supported inner generic type |%s|.", resultType);
			}
			resultClass = (Class<?>) rawType;
		} else {
			resultClass = (Class<?>) resultType;
		}
	}

	@Override
	public Object newInstance() {
		return Classes.newInstance(resultType);
	}

	@Override
	public Iterable<ResultClass.Field> getFields() {
		class ResultIterable implements Iterable<ResultClass.Field> {
			private java.lang.reflect.Field[] fields;
			private int index;

			public ResultIterable(Class<?> resultClass) {
				this.fields = resultClass.getDeclaredFields();
				this.index = -1;
			}

			@Override
			public Iterator<Field> iterator() {
				return new Iterator<ResultClass.Field>() {
					@Override
					public boolean hasNext() {
						return ++index < fields.length;
					}

					@Override
					public Field next() {
						return new FieldImpl(fields[index]);
					}
				};
			}
		}

		return new ResultIterable(resultClass);
	}

	private static class FieldImpl implements ResultClass.Field {
		private java.lang.reflect.Field field;

		public FieldImpl(java.lang.reflect.Field field) {
			this.field = field;
			this.field.setAccessible(true);
		}

		@Override
		public XPathDescriptor getXPath() {
			XPath xpath = field.getAnnotation(XPath.class);
			if (xpath == null) {
				throw new BugError("Missing <XPath> annotation from field |%s|.", field);
			}
			String value = xpath.value();
			if (value.isEmpty()) {
				throw new BugError("Empty <XPath> annotation on field |%s|.", field);
			}
			return new XPathDescriptorImpl(xpath);
		}

		@Override
		public XPathDescriptor getValueXPath() {
			ValueXPath xpath = field.getAnnotation(ValueXPath.class);
			if (xpath == null) {
				throw new BugError("Missing <ValueXPath> annotation from field |%s|.", field);
			}
			String value = xpath.value();
			if (value.isEmpty()) {
				throw new BugError("Empty <ValueXPath> annotation on field |%s|.", field);
			}
			return new ValueXPathDescriptorImpl(xpath);
		}

		@Override
		public Type getType() {
			return field.getGenericType();
		}

		@Override
		public void setValue(Object object, Object value) {
			try {
				field.set(object, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new BugError(e);
			}
		}

		@Override
		public String toString() {
			return field.toString();
		}
	}

	private static abstract class BaseXPathDescriptor implements XPathDescriptor {
		private Class<?> type;

		@Override
		public void setType(Class<?> type) {
			this.type = type;
		}

		@Override
		public Class<?> getType() {
			return type;
		}
	}

	private static class XPathDescriptorImpl extends BaseXPathDescriptor {
		private final XPath xpath;

		public XPathDescriptorImpl(XPath xpath) {
			this.xpath = xpath;
		}

		@Override
		public String getValue() {
			return xpath.value();
		}

		@Override
		public Class<? extends XPath.Parser> getParserClass() {
			Class<? extends XPath.Parser> parserClass = xpath.parser();
			return parserClass == XPath.NULL_Parser.class ? null : parserClass;
		}

		@Override
		public Class<? extends Format> getFormatClass() {
			Class<? extends Format> formatClass = xpath.format();
			return formatClass == XPath.NULL_Format.class ? null : formatClass;
		}

		@Override
		public String getAttributeName() {
			String attributeName = xpath.attribute();
			return attributeName.isEmpty() ? null : attributeName;
		}

		@Override
		public com.jslib.xpath.client.XPath.Type getXPathType() {
			return xpath.type();
		}
	}

	private static class ValueXPathDescriptorImpl extends BaseXPathDescriptor {
		private final ValueXPath xpath;

		public ValueXPathDescriptorImpl(ValueXPath xpath) {
			this.xpath = xpath;
		}

		@Override
		public String getValue() {
			return xpath.value();
		}

		@Override
		public Class<? extends XPath.Parser> getParserClass() {
			Class<? extends XPath.Parser> parserClass = xpath.parser();
			return parserClass == XPath.NULL_Parser.class ? null : parserClass;
		}

		@Override
		public Class<? extends Format> getFormatClass() {
			Class<? extends Format> formatClass = xpath.format();
			return formatClass == XPath.NULL_Format.class ? null : formatClass;
		}

		@Override
		public String getAttributeName() {
			String attributeName = xpath.attribute();
			return attributeName.isEmpty() ? null : attributeName;
		}

		@Override
		public com.jslib.xpath.client.XPath.Type getXPathType() {
			return xpath.type();
		}
	}
}
