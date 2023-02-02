package com.jslib.xpath.client;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;
import js.util.Strings;

public class XPathTransactionHandler implements InvocationHandler {
	private static final Log log = LogFactory.getLog(XPathTransactionHandler.class);

	private final DocumentBuilder builder;
	private final ConnectionFactory connectionFactory;
	private final Class<?> interfaceClass;
	private final String implementationURL;
	private final String defaultPathFormat;

	public XPathTransactionHandler(Class<?> interfaceClass, String implementationURL) {
		this(Classes.loadService(DocumentBuilder.class), new ConnectionFactory(), interfaceClass,
				normalizeURL(implementationURL));
	}

	public XPathTransactionHandler(DocumentBuilder builder, ConnectionFactory connectionFactory,
			Class<?> interfaceClass, String implementationURL) {
		this.builder = builder;
		this.connectionFactory = connectionFactory;
		this.interfaceClass = interfaceClass;
		this.implementationURL = normalizeURL(implementationURL);

		Path pathAnnotation = interfaceClass.getAnnotation(Path.class);
		this.defaultPathFormat = pathAnnotation != null ? pathAnnotation.value() : null;
	}

	private static String normalizeURL(String implementationURL) {
		if (implementationURL.charAt(implementationURL.length() - 1) != '/') {
			implementationURL += '/';
		}
		return implementationURL.replace(":xpath", "");
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		URL url = new URL(implementationURL + path(defaultPathFormat, method, args));
		log.debug("Load document from |%s|.", url);

		HttpURLConnection connection = connectionFactory.openConnection(url);
		for (Map.Entry<String, String> entry : headers(interfaceClass).entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}

		if (connection.getResponseCode() != 200) {
			// there is an inconsistency on HttpURLConnection API:
			// - 301 uses input stream, in which case error stream is null
			// - 404 uses error stream
			InputStream inputStream = connection.getErrorStream();
			if (inputStream == null) {
				inputStream = connection.getInputStream();
			}
			builder.loadHTML(inputStream).dump();
			return null;
		}

		Document document = null;
		try {
			document = builder.loadHTML(connection.getInputStream());
		} finally {
			connection.disconnect();
		}

		ResultClass resultClass = null;
		Mappings mappings = method.getAnnotation(Mappings.class);
		if (mappings != null) {
			resultClass = new MappedResultClass(method.getGenericReturnType(), mappings.value());
		} else {
			resultClass = new AnnotatedResultClass(method.getGenericReturnType());
		}

		Object object = null;
		XPath xpath = method.getAnnotation(XPath.class);
		if (xpath != null) {
			object = ValueHandler.getInstance(method.getGenericReturnType()).getValue(document,
					new ResultMethod(method, xpath));
		} else {
			object = resultClass.newInstance();
			for (ResultClass.Field field : resultClass.getFields()) {
				field.setValue(object, ValueHandler.getInstance(field.getType()).getValue(document, field));
			}
		}

		return object;
	}

	private static Map<String, String> headers(Class<?> interfaceClass) {
		Map<String, String> headers = new HashMap<>();
		Header headerAnnotation = interfaceClass.getAnnotation(Header.class);
		if (headerAnnotation == null) {
			return headers;
		}

		for (String value : headerAnnotation.value()) {
			// Strings.split takes care to trim returned items
			List<String> pair = Strings.split(value, ':');
			// first pair item is the header name and the second is the header value
			headers.put(pair.get(0), pair.get(1));
		}
		return headers;
	}

	private static String path(String defaultPathFormat, Method method, Object[] args) {
		String pathFormat = null;
		Path pathAnnotation = method.getAnnotation(Path.class);
		if (pathAnnotation != null) {
			pathFormat = pathAnnotation.value();
		}
		if (pathFormat == null) {
			pathFormat = defaultPathFormat;
		}
		if (pathFormat == null) {
			throw new BugError("Missing <Path> annotation from method |%s| or from parent class.", method);
		}

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