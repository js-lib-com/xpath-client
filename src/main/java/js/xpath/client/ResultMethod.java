package js.xpath.client;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import js.format.Format;

public class ResultMethod implements ResultClass.Field {
	private final Method method;
	private final XPath xpath;

	public ResultMethod(Method method, XPath xpath) {
		this.method = method;
		this.xpath = xpath;
	}

	@Override
	public XPathDescriptor getXPath() {
		return new XPathDescriptorImpl(xpath);
	}

	@Override
	public XPathDescriptor getValueXPath() {
		return null;
	}

	@Override
	public Type getType() {
		return method.getGenericReturnType();
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new UnsupportedOperationException();
	}

	private static class XPathDescriptorImpl implements XPathDescriptor {
		private final XPath xpath;
		private Class<?> type;

		public XPathDescriptorImpl(XPath xpath) {
			this.xpath = xpath;
		}

		@Override
		public void setType(Class<?> type) {
			this.type = type;
		}

		@Override
		public Class<?> getType() {
			return type;
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
		public js.xpath.client.XPath.Type getXPathType() {
			return xpath.type();
		}
	}
}
