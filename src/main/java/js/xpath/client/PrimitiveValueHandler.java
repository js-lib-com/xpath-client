package js.xpath.client;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import js.dom.Document;
import js.dom.Element;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;
import js.util.Strings;

public class PrimitiveValueHandler extends ValueHandler {
	private static final Log log = LogFactory.getLog(PrimitiveValueHandler.class);

	@Override
	public Object getValue(Document document, ResultClass.Field field) throws Exception {
		Type type = field.getType();
		if (type instanceof ParameterizedType) {
			throw new BugError("Invalid <XPath> field |%s|. Not supported generic type |%s|.", field, type);
		}

		XPathDescriptor xpathDescriptor = field.getXPath();
		// type is not parameterized so seems safe to cast to class
		xpathDescriptor.setType((Class<?>) type);

		javax.xml.xpath.XPath xpath = XPathFactory.newInstance().newXPath();
		Object result = xpath.evaluate(xpathDescriptor.getValue(), getW3cDocument(document), XPathConstants.NODE);
		if (result == null) {
			return null;
		}
		Node node = (Node) result;
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			return getElementValue(getElement(document, node), xpathDescriptor);

		case Node.TEXT_NODE:
			return Strings.trim(node.getNodeValue());
		}

		log.debug("XPath expression |%s| yields a node that is not element. Force to null.", xpath);
		return null;
	}

	private static org.w3c.dom.Document getW3cDocument(Document document) throws Exception {
		return Classes.invoke(document, "getDocument");
	}

	private static Element getElement(Document document, org.w3c.dom.Node node) throws Exception {
		Method method = Classes.getMethod(document.getClass(), "getElement", org.w3c.dom.Node.class);
		return (Element) method.invoke(document, node);
	}
}
