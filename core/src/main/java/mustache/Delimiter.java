package mustache;

import java.util.regex.Pattern;

final class Delimiter {
	
	static final String DEFAULT_START = "{{";

	static final String DEFAULT_STOP = "}}";

	static final String UNESCAPED_START = "{{{";
	
	static final String UNESCAPED_STOP = "}}}";

	private boolean precede = false;
	
	private String unquotedStart;
	
	private String start;
	
	private String stop;
	
	Delimiter() {
		setTags(DEFAULT_START, DEFAULT_STOP);
	}

	void setTags(String start, String stop) {
		
		if (start.equals(UNESCAPED_START) || stop.equals(UNESCAPED_STOP)) {
			throw new IllegalArgumentException("Normal tags cannot override escape tags");
		}
		
		this.unquotedStart = start;
		this.precede = start.length() > UNESCAPED_START.length();
		this.start = Pattern.quote(start);
		this.stop = Pattern.quote(stop);
	}

	boolean precede() {
		return precede;
	}

	String getUnquotedStart() {
		return unquotedStart;
	}

	String getStart() {
		return start;
	}

	String getStop() {
		return stop;
	}
}