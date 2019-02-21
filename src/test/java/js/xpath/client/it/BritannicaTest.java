package js.xpath.client.it;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import js.xpath.client.XPath;
import js.xpath.client.XPath.Type;
import js.xpath.client.XPathClientFactory;

public class BritannicaTest {
	private static XPathClientFactory factory;

	@BeforeClass
	public static void beforeClass() {
		factory = new XPathClientFactory();
	}

	private Britannica britannica;

	@Before
	public void beforeTest() {
		britannica = factory.getRemoteInstance("https://www.britannica.com/", Britannica.class);
	}

	@Test
	public void getArticle() throws IOException {
		String article = britannica.getArticle("animal/lion");
		assertThat(article, notNullValue());
		assertThat(article, startsWith("<ARTICLE>"));
	}

	// --------------------------------------------------------------------------------------------

	private interface Britannica {
		@Path("{path}")
		@XPath(value = "//*[@id='article-content']", type = Type.TEXT)
		String getArticle(@PathParam("path") String path);
	}
}
