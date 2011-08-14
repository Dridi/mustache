package mustache.parser;


final class Delimiter {
	
	static final String DEFAULT_START = "{{";
	static final String DEFAULT_STOP = "}}";

	static final String UNESCAPED_START = "{{{";
	static final String UNESCAPED_STOP = "}}}";

	private String start;
	private String stop;
	private boolean precede = false;
	
	Delimiter() {
		setBounds(DEFAULT_START, DEFAULT_STOP);
	}

	void setBounds(String start, String stop) {
		
		if (start.equals(UNESCAPED_START) || stop.equals(UNESCAPED_STOP)) {
			throw new IllegalArgumentException("Normal tags cannot override escape tags");
		}
		
		this.precede = start.length() > UNESCAPED_START.length();
		this.start = start;
		this.stop = stop;
	}

	boolean precede() {
		return precede;
	}

	String getStart() {
		return start;
	}

	String getStop() {
		return stop;
	}
}