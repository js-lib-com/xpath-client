package js.xpath.client.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import js.xpath.client.XPath;
import js.xpath.client.XPathClientFactory;

public class SoftSchoolsTest {
	private static XPathClientFactory factory;

	@BeforeClass
	public static void beforeClass() {
		factory = new XPathClientFactory();
	}

	private SoftSchools softSchools;

	@Before
	public void beforeTest() {
		softSchools = factory.getRemoteInstance("https://www.softschools.com/", SoftSchools.class);
	}

	@Test
	public void getDescription() {
		String description = softSchools.getDescription("animals/lion_facts/2");
		assertThat(description, notNullValue());
		assertThat(description, startsWith("Lions are one of the largest cats in the world."));
	}

	@Test
	public void getFacts() {
		SoftSchoolsFacts facts = softSchools.getFacts("animals/lion_facts/2");
		assertThat(facts, notNullValue());
		assertThat(facts.getTitle(), notNullValue());
		assertThat(facts.getTitle(), equalTo("Lion Facts"));
		assertThat(facts.getDescription(), notNullValue());
		assertThat(facts.getDescription(), startsWith("Lions are one of the largest cats in the world."));
		assertThat(facts.getFacts(), notNullValue());
		assertThat(facts.getFacts(), hasSize(15));
		assertThat(facts.getFacts().get(0), startsWith("Lions are carnivores and they hunt mostly antelopes"));
	}

	@Test
	public void getFactLinks() {
		List<String> links = softSchools.getFactLinks("animals");
		assertThat(links, notNullValue());
		assertThat(links, hasSize(758));

		for (String link : links) {
			System.out.println(link);
		}
	}

	@Test
	public void getFactsArray() {
		String[] facts = softSchools.getFactsArray("animals/lion_facts/2");
		assertThat(facts, notNullValue());
		assertThat(facts, arrayWithSize(15));
		assertThat(facts[0], startsWith("Lions are carnivores and they hunt mostly antelopes"));
	}

	// --------------------------------------------------------------------------------------------

	private interface SoftSchools {
		@Path("facts/{path}/")
		@XPath("//*[@class='factsmaintbl']//TD")
		String getDescription(@PathParam("path") String path);

		@Path("facts/{path}/")
		SoftSchoolsFacts getFacts(@PathParam("path") String path);

		@Path("facts/{category}")
		@XPath(value = "//*[@id='sortable_list']/A", attribute = "href")
		List<String> getFactLinks(@PathParam("category") String category);

		@Path("facts/{path}/")
		@XPath("//*[@class='fact_topbar']/*/*/TD")
		String[] getFactsArray(@PathParam("path") String path);
	}

	private static class SoftSchoolsFacts {
		@XPath("//*[@id='hdrcnt']/H1")
		private String title;

		@XPath("//*[@class='factsmaintbl']//TD")
		private String description;

		@XPath("//*[@class='fact_topbar']/*/*/TD")
		private List<String> facts;

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}

		public List<String> getFacts() {
			return facts;
		}
	}
}
