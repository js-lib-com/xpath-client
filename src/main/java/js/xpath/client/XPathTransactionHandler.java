package js.xpath.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;

public class XPathTransactionHandler implements InvocationHandler {
	private static final Log log = LogFactory.getLog(XPathTransactionHandler.class);

	private final DocumentBuilder builder;
	private final String implementationURL;

	public XPathTransactionHandler(String implementationURL) {
		this.builder = Classes.loadService(DocumentBuilder.class);
		this.implementationURL = normalizeURL(implementationURL);
	}

	/**
	 * Test constructor.
	 * 
	 * @param builder
	 * @param implementationURL
	 */
	public XPathTransactionHandler(DocumentBuilder builder, String implementationURL) {
		this.builder = builder;
		this.implementationURL = normalizeURL(implementationURL);
	}

	private static String normalizeURL(String implementationURL) {
		if (implementationURL.charAt(implementationURL.length() - 1) != '/') {
			implementationURL += '/';
		}
		return implementationURL.replace(":xpath", "");
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		URL url = new URL(implementationURL + path(method, args));
		log.debug("Load document from |%s|.", url);
		Document document = builder.loadHTML(url);

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
			object = ValueHandler.getInstance(method.getGenericReturnType()).getValue(document, new ResultMethod(method, xpath));
		} else {
			object = resultClass.newInstance();
			for (ResultClass.Field field : resultClass.getFields()) {
				field.setValue(object, ValueHandler.getInstance(field.getType()).getValue(document, field));
			}
		}

		return object;
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