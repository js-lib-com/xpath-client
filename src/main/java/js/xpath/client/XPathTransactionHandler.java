package js.xpath.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import js.converter.Converter;
import js.converter.ConverterRegistry;
import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.EList;
import js.dom.Element;
import js.lang.BugError;
import js.util.Classes;

public class XPathTransactionHandler implements InvocationHandler {
	private final DocumentBuilder builder;
	private final Converter converter;
	private final String implementationURL;

	public XPathTransactionHandler(String implementationURL) {
		this.builder = Classes.loadService(DocumentBuilder.class);
		this.converter = ConverterRegistry.getConverter();
		if (implementationURL.charAt(implementationURL.length() - 1) != '/') {
			implementationURL += '/';
		}
		this.implementationURL = implementationURL.replace(":xpath", "");
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		URL url = new URL(implementationURL + path(method, args));
		Document document = builder.loadHTML(url);

		Class<?> valueType = method.getReturnType();
		Object value = valueType.newInstance();

		for (Field field : valueType.getDeclaredFields()) {
			field.setAccessible(true);

			ValueXPath valueXPath = field.getAnnotation(ValueXPath.class);
			if (valueXPath != null) {
				Element element = document.getByXPath(valueXPath.value());
				if (element == null) {
					throw new BugError("Incorrect <ValuXPath> value on field |%s|.", field);
				}

				field.set(value, converter.asObject(element.getText().trim(), field.getType()));
				continue;
			}

			TextXPath textXPath = field.getAnnotation(TextXPath.class);
			if (textXPath != null) {
				Element element = document.getByXPath(textXPath.value());
				if (element == null) {
					throw new BugError("Incorrect <TextXPath> value on field |%s|.", field);
				}

				field.set(value, element.getRichText().trim());
				continue;
			}

			ListXPath listXPath = field.getAnnotation(ListXPath.class);
			if (listXPath != null) {
				EList items = document.findByXPath(listXPath.value());
				if (items.isEmpty()) {
					throw new BugError("Incorrect <ListXPath> value on field |%s|.", field);
				}

				// TODO user field parameterized type to create proper list type and use converter
				List<String> list = new ArrayList<>();
				for (Element element : items) {
					list.add(element.getText().trim());
				}

				field.set(value, list);
				continue;
			}

			MapXPath mapXPath = field.getAnnotation(MapXPath.class);
			if (mapXPath != null) {
				EList keys = document.findByXPath(mapXPath.key());
				if (keys.isEmpty()) {
					throw new BugError("Incorrect <MapXPath> value on field |%s|. No keys found.", field);
				}

				EList values = document.findByXPath(mapXPath.value());
				if (values.isEmpty()) {
					throw new BugError("Incorrect <MapXPath> value on field |%s|. No values found.", field);
				}

				if (keys.size() != values.size()) {
					throw new BugError("Incorrect <MapXPath> value on field |%s|. Keys and values count does not match.", field);
				}

				// TODO user field parameterized type to create proper map key and value types and use converter
				Map<String, String> map = new HashMap<>();
				for (int i = 0; i < keys.size(); ++i) {
					map.put(keys.item(i).getText().trim(), values.item(i).getText().trim());
				}

				field.set(value, map);
				continue;
			}

			throw new BugError("Missing XPath annotation from field |%s|.", field);
		}

		return value;
	}

	private static String path(Method method, Object[] args) {
		Path pathAnnotation = method.getAnnotation(Path.class);
		if (pathAnnotation == null) {
			throw new BugError("Missing <Path> annotation from method |%s|.", method);
		}

		String pathFormat = pathAnnotation.value();

		Map<String, Object> variables = new HashMap<>();

		Annotation[][] annotations = method.getParameterAnnotations();
		for (int i = 0; i < args.length; ++i) {
			if (annotations[i].length == 0) {
				throw new BugError("Missing annotation on parameter |%d| from method |%s|.", i, method);
			}
			for (int j = 0; j < annotations[i].length; ++j) {
				Annotation annotation = annotations[i][j];
				if (!(annotation instanceof PathParam)) {
					throw new BugError("Not recognized parameter annotation |%s|.", annotation);
				}
				PathParam pathParam = (PathParam) annotation;
				variables.put(pathParam.value(), args[i]);
			}
		}

		return format(pathFormat, variables);
	}

	public static String format(String pathFormat, Map<String, Object> variables) {
		// 0: NONE
		// 1: TEXT
		// 2: VARIABLE
		int state = 1;

		StringBuilder pathBuilder = new StringBuilder();
		StringBuilder variableNameBuilder = new StringBuilder();

		for (int charIndex = 0; charIndex < pathFormat.length(); ++charIndex) {
			char c = pathFormat.charAt(charIndex);
			switch (state) {
			case 1:
				if (c != '{') {
					pathBuilder.append(c);
					break;
				}
				state = 2;
				variableNameBuilder.setLength(0);
				break;

			case 2:
				if (c != '}') {
					variableNameBuilder.append(c);
					break;
				}
				state = 1;
				pathBuilder.append(variables.get(variableNameBuilder.toString()));
				break;
			}
		}
		return pathBuilder.toString();
	}
}