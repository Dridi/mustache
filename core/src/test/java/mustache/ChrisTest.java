package mustache;

import java.io.IOException;

import junit.framework.Assert;
import mustache.parser.ParseException;

import org.junit.Test;


public class ChrisTest {

	String template = "Hello {{name}}\nYou have just won ${{value}}!\n{{#in_ca}}\nWell, ${{taxed_value}}, after taxes.\n{{/in_ca}}\n";
	String expected = "Hello Chris\nYou have just won $10000!\nWell, $6000.0, after taxes.\n";
	
	@Test
	public void shouldRenderAsExcpected() throws ParseException, IOException {
		
		@SuppressWarnings("unused")
		Mustache chrisExample = new Mustache() {
			String name = "Chris";
			int value = 10000;
			float taxed_value() {
				return value - (value * .4F);
			}
			boolean in_ca = true;
		};
		
		StringBuilder result = new StringBuilder();
		chrisExample.renderString(template, result, null);
		
		Assert.assertEquals(expected, result.toString());
	}
	
	
}