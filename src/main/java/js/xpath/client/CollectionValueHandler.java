package js.xpath.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import js.dom.Document;
import js.dom.EList;
import js.dom.Element;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;

public class CollectionValueHandler extends ValueHandler {
	private static final Log log = LogFactory.getLog(CollectionValueHandler.class);

	@Override
	public Object getValue(Document document, ResultClass.Field field) throws Exception {
		Type type = field.getType();
		if (!(type instanceof ParameterizedType)) {
			throw new BugError("Invalid <XPath> field |%s|. Expected generic collection but got |%s|.", field, type);
		}
		ParameterizedType parameterizedType = (ParameterizedType) type;

		Type[] typeArguments = parameterizedType.getActualTypeArguments();
		if (typeArguments.length != 1) {
			throw new BugError("Invalid <XPath> field |%s|. Too many type arguments |%d|.", field, typeArguments.length);
		}
		if (typeArguments[0] instanceof ParameterizedType) {
			throw new BugError("Invalid <XPath> field |%s|. Not supported generic type |%s| for collection item.", field, typeArguments[0]);
		}

		final Class<?> itemClass = (Class<?>) typeArguments[0];
		final Class<?> listClass = (Class<?>) parameterizedType.getRawType();

		Collection<Object> collection = Classes.newCollection(listClass);

		XPathDescriptor xpath = field.getXPath();
		xpath.setType(itemClass);
		EList items = document.findByXPath(xpath.getValue());
		if (items.isEmpty()) {
			log.warn("Suspect <XPath> value on field |%s|. No document elements found.", field);
			return collection;
		}

		for (Element item : items) {
			collection.add(getElementValue(item, xpath));
		}

		return collection;
	}
}
