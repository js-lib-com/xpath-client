package js.xpath.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import js.dom.Document;
import js.dom.Element;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;

public class PrimitiveValueHandler extends ValueHandler {
	private static final Log log = LogFactory.getLog(PrimitiveValueHandler.class);

	@Override
	public Object getValue(Document document, ResultClass.Field field) throws Exception {
		Type type = field.getType();
		if (type instanceof ParameterizedType) {
			throw new BugError("Invalid <XPath> field |%s|. Not supported generic type |%s|.", field, type);
		}

		XPathDescriptor xpath = field.getXPath();
		// type is not parameterized so seems safe to cast to class
		xpath.setType((Class<?>) type);

		Element element = document.getByXPath(xpath.getValue());
		if (element == null) {
			log.warn("Incorrect <XPath> value on field |%s|. No element found.", field);
			return null;
		}

		return getElementValue(element, xpath);
	}
}
