package js.xpath.client.unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.Element;
import js.xpath.client.ConnectionFactory;
import js.xpath.client.Mappings;
import js.xpath.client.XPathTransactionHandler;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class MappedResultTest {
	@Mock
	private DocumentBuilder builder;

	@Mock
	private ConnectionFactory connectionFactory;

	@Mock
	private HttpURLConnection connection;

	private XPathTransactionHandler transaction;

	@Before
	public void beforeTest() throws IOException {
		when(connectionFactory.openConnection(any(URL.class))).thenReturn(connection);
		transaction = new XPathTransactionHandler(builder, connectionFactory, Client.class, "http://server.com/");
	}

	@Test
	public void invoke() throws Throwable {
		Element textElement = mock(Element.class);
		when(textElement.getText()).thenReturn("text");

		Element attributeElement = mock(Element.class);
		when(attributeElement.getAttr("href")).thenReturn("url");

		Document document = mock(Document.class);
		when(document.getByXPath("//*[class='name']")).thenReturn(textElement);
		when(document.getByXPath("//li[class='link']/a")).thenReturn(attributeElement);

		when(connection.getResponseCode()).thenReturn(200);
		when(builder.loadHTML((InputStream)any())).thenReturn(document);

		Object proxy = new Object();
		Method method = Client.class.getMethod("getDataObject", String.class);
		DataObject object = (DataObject) transaction.invoke(proxy, method, new Object[] { "test" });

		assertThat(object, notNullValue());
		assertThat(object.name, notNullValue());
		assertThat(object.name, equalTo("text"));
		assertThat(object.link, notNullValue());
		assertThat(object.link, equalTo("url"));
	}

	private interface Client {
		@Path("api/{name}")
		@Mappings("data-object.xml")
		DataObject getDataObject(@PathParam("name") String name);
	}

	private static class DataObject {
		private String name;
		private String link;
	}
}
