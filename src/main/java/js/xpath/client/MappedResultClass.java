package js.xpath.client;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.Element;
import js.format.Format;
import js.lang.BugError;
import js.util.Classes;
import js.xpath.client.XPath.Parser;

public class MappedResultClass implements ResultClass {
	private final Object instance;
	private final List<ResultClass.Field> fields;

	public MappedResultClass(Type resultType, String mappingsResource) throws NoSuchFieldException, SecurityException {
		this.instance = Classes.newInstance(resultType);
		this.fields = new ArrayList<>();

		DocumentBuilder builder = Classes.loadService(DocumentBuilder.class);
		Document document = builder.loadXML(Classes.getResourceAsStream(mappingsResource));

		Class<?> resultClass = this.instance.getClass();
		for (Element element : document.getRoot().getChildren()) {
			fields.add(new FieldElement(resultClass.getDeclaredField(element.getAttr("name")), element));
		}
	}

	@Override
	public Object newInstance() {
		return instance;
	}

	@Override
	public Iterable<ResultClass.Field> getFields() {
		return fields;
	}

	private static class FieldElement implements ResultClass.Field {
		private final java.lang.reflect.Field field;
		private final XPathDescriptor descriptor;

		public FieldElement(java.lang.reflect.Field field, Element element) {
			this.field = field;
			this.field.setAccessible(true);
			this.descriptor = new XPathDescriptorImpl(element);
		}

		@Override
		public XPathDescriptor getXPath() {
			return descriptor;
		}

		@Override
		public XPathDescriptor getValueXPath() {
			throw new UnsupportedOperationException();
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

	private static class XPathDescriptorImpl implements XPathDescriptor {
		private final Element element;
		private Class<?> type;

		public XPathDescriptorImpl(Element element) {
			this.element = element;
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
			return element.getAttr("xpath");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Parser> getParserClass() throws ClassNotFoundException, ClassCastException {
			String parser = element.getAttr("parser");
			return (Class<? extends Parser>) (parser != null ? Class.forName(parser) : null);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<? extends Format> getFormatClass() throws ClassNotFoundException, ClassCastException {
			String format = element.getAttr("format");
			return (Class<? extends Format>) (format != null ? Class.forName(format) : null);
		}

		@Override
		public String getAttributeName() {
			return element.getAttr("attribute");
		}

		@Override
		public XPath.Type getXPathType() throws IllegalArgumentException {
			String type = element.getAttr("type");
			if (type != null) {
				return XPath.Type.valueOf(type);
			}
			return XPath.Type.STRING;
		}
	}
}
