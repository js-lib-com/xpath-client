package js.xpath.client;

import java.lang.reflect.Type;

import org.w3c.dom.Node;

import js.converter.Converter;
import js.converter.ConverterRegistry;
import js.dom.Document;
import js.dom.Element;
import js.format.Format;
import js.util.Classes;
import js.util.Types;

public abstract class ValueHandler {
	private final Converter converter;

	protected ValueHandler() {
		this.converter = ConverterRegistry.getConverter();
	}

	public abstract Object getValue(Document document, ResultClass.Field field) throws Exception;

	protected Object getElementValue(Element element, XPathDescriptor descriptor) throws Exception {
		Class<? extends XPath.Parser> parserClass = descriptor.getParserClass();
		if (parserClass != null) {
			XPath.Parser parser = Classes.newInstance(parserClass);
			return parser.parse(element.getText());
		}

		Class<? extends Format> formatClass = descriptor.getFormatClass();
		if (formatClass != null) {
			Format format = Classes.newInstance(formatClass);
			return format.parse(element.getText());
		}

		String attributeName = descriptor.getAttributeName();
		if (attributeName != null) {
			return converter.asObject(element.getAttr(attributeName), descriptor.getType());
		}

		switch (descriptor.getXPathType()) {
		case TEXT:
			return getRichText(element);

		default:
			break;
		}

		return converter.asObject(element.getText().trim(), descriptor.getType());
	}

	private String getRichText(Element element) {
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(element.getTag().toUpperCase());
		builder.append('>');

		try {
			getRichText((Node) Classes.invoke(element, "getNode"), builder);
		} catch (Exception e) {
		}

		builder.append('<');
		builder.append('/');
		builder.append(element.getTag().toUpperCase());
		builder.append('>');

		return builder.toString().trim();
	}

	private static void getRichText(Node node, StringBuilder builder) {
		Node n = node.getFirstChild();
		while (n != null) {
			if (n.getNodeType() == Node.TEXT_NODE) {
				builder.append(n.getNodeValue());
			} else if (n.getNodeType() == Node.ELEMENT_NODE) {
				builder.append('<');
				builder.append(n.getNodeName());
				
				if(n.getNodeName().equals("A")) {
					builder.append(" href='");
					builder.append(((org.w3c.dom.Element)n).getAttribute("href"));
					builder.append("'");
				}
				else if(n.getNodeName().equals("IMG")) {
					builder.append(" src='");
					builder.append(((org.w3c.dom.Element)n).getAttribute("src"));
					builder.append("'");
				}
				
				builder.append('>');
				getRichText(n, builder);
				builder.append('<');
				builder.append('/');
				builder.append(n.getNodeName());
				builder.append('>');
			}
			n = n.getNextSibling();
		}
	}

	private static final ValueHandler ARRAY = new ArrayValueHandler();
	private static final ValueHandler COLLECTION = new CollectionValueHandler();
	private static final ValueHandler MAP = new MapValueHandler();
	private static final ValueHandler PRIMITIVE = new PrimitiveValueHandler();

	public static ValueHandler getInstance(Type type) {
		if (Types.isArray(type)) {
			return ARRAY;
		}
		if (Types.isCollection(type)) {
			return COLLECTION;
		}
		if (Types.isMap(type)) {
			return MAP;
		}
		return PRIMITIVE;
	}
}
