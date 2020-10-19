package js.xpath.client.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import js.xpath.client.XPath;
import js.xpath.client.XPath.Type;
import js.xpath.client.XPathClientFactory;

public class WikipediaTest {
	private static XPathClientFactory factory;

	@BeforeClass
	public static void beforeClass() {
		factory = new XPathClientFactory();
	}

	private Wikipedia wikipedia;

	@Before
	public void beforeTest() {
		wikipedia = factory.getRemoteInstance("https://en.wikipedia.org/", Wikipedia.class);
	}

	@Test
	public void getArticle() throws IOException {
		String article = wikipedia.getArticle("Bear");
		assertThat(article, notNullValue());
		assertThat(article, startsWith("<DIV>"));
	}

	@Test
	public void getTaxonomy() {
		Taxonomy taxonomy = wikipedia.getTaxonomy("Bactrian_camel");
		assertThat(taxonomy, notNullValue());
		assertThat(taxonomy.domain, nullValue());
		assertThat(taxonomy.kingdom, equalTo("Animalia"));
		assertThat(taxonomy.phylum, equalTo("Chordata"));
		assertThat(taxonomy.clazz, equalTo("Mammalia"));
		assertThat(taxonomy.order, equalTo("Artiodactyla"));
		assertThat(taxonomy.family, equalTo("Camelidae"));
		assertThat(taxonomy.genus, equalTo("Camelus"));
		assertThat(taxonomy.species, equalTo("C. bactrianus"));
	}

	// --------------------------------------------------------------------------------------------

	private interface Wikipedia {
		@Path("wiki/{title}")
		@XPath(value = "//*[@class='mw-parser-output']", type = Type.TEXT)
		String getArticle(@PathParam("title") String title);

		@Path("wiki/{title}")
		Taxonomy getTaxonomy(@PathParam("title") String title);
	}

	private static class Taxonomy {
		@XPath("//TD[contains(text(),'Domain')]/following-sibling::TD/A")
		private String domain;

		@XPath("//TD[contains(text(),'Kingdom')]/following-sibling::TD/A")
		private String kingdom;

		@XPath("//TD[contains(text(),'Phylum')]/following-sibling::TD/A")
		private String phylum;

		@XPath("//TD[contains(text(),'Class')]/following-sibling::TD/A")
		private String clazz;

		@XPath("//TD[contains(text(),'Order')]/following-sibling::TD/A")
		private String order;

		@XPath("//TD[contains(text(),'Family')]/following-sibling::TD/A")
		private String family;

		@XPath("//TD[contains(text(),'Genus')]/following-sibling::TD/A")
		private String genus;

		@XPath("//TD[contains(text(),'Species')]/following-sibling::TD")
		private String species;
	}
}
