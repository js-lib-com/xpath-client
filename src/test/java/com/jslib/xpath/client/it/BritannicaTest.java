package com.jslib.xpath.client.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jslib.xpath.client.XPath;
import com.jslib.xpath.client.XPathClientFactory;
import com.jslib.xpath.client.XPath.Type;

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
		@XPath(value = "//*[contains(concat(' ', normalize-space(@class), ' '),' article-content ')]", type = Type.TEXT)
		String getArticle(@PathParam("path") String path);
	}
}
