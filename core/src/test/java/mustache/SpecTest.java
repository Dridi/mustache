package mustache;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.Gson;

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
	public void parserShouldComplyWithSpecs() throws ParseException {
		Context context = Context.newInstance( testCase.getData() );
		Parser parser = new Parser(testCase.getTemplate(), context, this);
		Assert.assertEquals(testCase.toString(), testCase.getExpected(), parser.merge());
	}
	
	public String loadPartial(String name) {
		return testCase.getPartials().get(name);
	}
	
	@Parameters
	public static Collection<Object[]> loadTestCases() {
		List<Object[]> testCases = new ArrayList<Object[]>();
		
		Gson gson = SpecDataDeserializer.newGson();
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		
		for (String path : TEST_SUITE_PATHS) {
			InputStream specStream = classLoader.getResourceAsStream(path);
			SpecTestSuite suite = gson.fromJson(new InputStreamReader(specStream), SpecTestSuite.class);
			
			for ( SpecTestCase test : suite.getTests() ) {
				testCases.add( new Object[] {test} );
			}
		}
		
		return testCases;
	}
	
}