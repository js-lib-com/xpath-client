package js.xpath.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import js.dom.Document;
import js.dom.EList;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;

public class MapValueHandler extends ValueHandler {
	private static final Log log = LogFactory.getLog(MapValueHandler.class);

	@Override
	public Object getValue(Document document, ResultClass.Field field) throws Exception {
		Type type = field.getType();
		if (!(type instanceof ParameterizedType)) {
			throw new BugError("Invalid <XPath> field |%s|. Expected generic map but got |%s|.", field, type);
		}
		ParameterizedType parameterizedType = (ParameterizedType) type;

		Type[] typeArguments = parameterizedType.getActualTypeArguments();
		if (typeArguments.length != 2) {
			throw new BugError("Invalid <XPath> field |%s|. Map needs two type arguments but got |%d|.", field, typeArguments.length);
		}
		if (typeArguments[0] instanceof ParameterizedType) {
			throw new BugError("Invalid <XPath> field |%s|. Not supported generic type |%s| for collection item.", field, typeArguments[0]);
		}
		if (typeArguments[1] instanceof ParameterizedType) {
			throw new BugError("Invalid <XPath> field |%s|. Not supported generic type |%s| for collection item.", field, typeArguments[1]);
		}

		final Class<?> keyClass = (Class<?>) typeArguments[0];
		final Class<?> valueClass = (Class<?>) typeArguments[1];
		final Class<?> mapClass = (Class<?>) parameterizedType.getRawType();

		Map<Object, Object> map = Classes.newMap(mapClass);

		XPathDescriptor xpath = field.getXPath();
		xpath.setType(keyClass);
		EList keys = document.findByXPath(xpath.getValue());
		if (keys.isEmpty()) {
			log.warn("Suspect <XPath> value on field |%s|. No document elements found.", field);
			return map;
		}

		XPathDescriptor valueXPath = field.getValueXPath();
		valueXPath.setType(valueClass);
		EList values = document.findByXPath(valueXPath.getValue());
		if (keys.size() != values.size()) {
			throw new BugError("Incorrect <XPath> value on field |%s|. Key and value elements count does not match.", field);
		}

		for (int i = 0; i < keys.size(); ++i) {
			map.put(getElementValue(keys.item(i), xpath), getElementValue(values.item(i), valueXPath));
		}

		return map;
	}
}
