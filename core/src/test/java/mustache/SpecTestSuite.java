package mustache;

import java.util.List;

public class SpecTestSuite {
	
	private String overview;
	
	private List<SpecTestCase> tests;

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public List<SpecTestCase> getTests() {
		return tests;
	}

	public void setTests(List<SpecTestCase> tests) {
		this.tests = tests;
	}
	
}
