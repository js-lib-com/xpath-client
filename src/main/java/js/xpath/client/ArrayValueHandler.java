package js.xpath.client;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import js.dom.Document;
import js.dom.EList;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;

public class ArrayValueHandler extends ValueHandler {
	private static final Log log = LogFactory.getLog(ArrayValueHandler.class);

	@Override
	public Object getValue(Document document, ResultClass.Field field) throws Exception {
		Type type = field.getType();
		Type componentType = type instanceof Class? ((Class<?>) type).getComponentType(): ((GenericArrayType) type).getGenericComponentType();
		if (componentType instanceof ParameterizedType) {
			throw new BugError("Invalid <XPath> field |%s|. Not supported generic type |%s|.", field, type);
		}
		Class<?> componentClass = (Class<?>) componentType;

		XPathDescriptor xpath = field.getXPath();
		xpath.setType(componentClass);
		
		EList items = document.findByXPath(xpath.getValue());
		Object array = Array.newInstance(componentClass, items.size());

		if (items.isEmpty()) {
			log.warn("Suspect <XPath> value on field |%s|. No document elements found.", field);
			return array;
		}

		for(int i = 0; i < items.size();++i) {
			Array.set(array, i, getElementValue(items.item(i), xpath));
		}
		return array;
	}
}
