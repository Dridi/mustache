package mustache;

import java.util.Map;

public class SpecTestCase {
	
	private String name;
	
	private String desc;
	
	private Map<String, Object> data;
	
	private Map<String, String> partials;
	
	private String template;
	
	private String expected;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Map<String, String> getPartials() {
		return partials;
	}

	public void setPartials(Map<String, String> partials) {
		this.partials = partials;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getExpected() {
		return expected;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}
	
	@Override
	public String toString() {
		return name + " : " + desc;
	}
	
}