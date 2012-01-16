package mustache;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import mustache.core.Processor;
import mustache.parser.ParseException;
import mustache.parser.Parser;
import mustache.parser.PartialLoader;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SpecTest implements PartialLoader {
	
	private static final String[] TEST_SUITE_PATHS = {
		"mustache/comments.json",
		"mustache/delimiters.json",
		"mustache/interpolation.json",
		"mustache/inverted.json",
		"mustache/partials.json",
		"mustache/sections.json"
	};
	
	private SpecTestCase testCase;
	
	public SpecTest(SpecTestCase testCase) {
		this.testCase = testCase;
	}
	
	@Test
	public void shouldRenderAsExpected() throws ParseException, IOException {
		Processor processor = Parser.parseString(testCase.getTemplate(), this);
		StringBuilder result = new StringBuilder();
		Renderer.render(processor, testCase.getData(), result);
		Assert.assertEquals(testCase.toString(), testCase.getExpected(), result.toString());
	}
	
	public Readable loadPartial(String name) throws IOException {
		String partial = testCase.getPartials().get(name);
		return new StringReader(partial);
	}
	
	@Parameters
	public static Collection<Object[]> loadTestCases() throws IOException {
		List<Object[]> testCases = new ArrayList<Object[]>();
		
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		for (String path : TEST_SUITE_PATHS) {
			InputStream specStream = classLoader.getResourceAsStream(path);
			SpecTestSuite suite = mapper.readValue(specStream, SpecTestSuite.class);
			
			for ( SpecTestCase test : suite.getTests() ) {
				testCases.add( new Object[] {test} );
			}
		}
		
		return testCases;
	}
}