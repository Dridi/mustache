package mustache.parser;

final class Delimiter {
	static final String DEFAULT_START = "{{";
	static final String DEFAULT_STOP = "}}";

	static final String UNESCAPED_START = "{{{";
	static final String UNESCAPED_STOP = "}}}";

	private String start = DEFAULT_START;
	private String stop = DEFAULT_STOP;
	private StringBuilder currentTag = new StringBuilder();
	
	private boolean normalPrecedesUnescaped = false;
	private boolean insideTag = false;
	private boolean insideUnescapedTag = false;
	
	Delimiter() { }

	void setBounds(String start, String stop) throws ParseException {
		if (UNESCAPED_START.equals(start) || UNESCAPED_STOP.equals(stop)) {
			throw new ParseException("Normal tags cannot override escape tags");
		}
		
		this.normalPrecedesUnescaped = start.length() > UNESCAPED_START.length();
		this.start = start;
		this.stop = stop;
	}
	
	boolean isInsideTag() {
		return insideTag || insideUnescapedTag;
	}
	
	int tagStartLength(int position) {
		return insideTag ? start.length() : UNESCAPED_START.length();
	}
	
	int parse(String line, int position) {
		if (insideTag) {
			return parseTag(line, position);
		}
		else if (insideUnescapedTag ) {
			return parseUnescapedTag(line, position);
		}
		return searchTag(line, position);
	}

	private int parseTag(String line, int position) {
		int tagPosition = line.indexOf(stop, position);
		
		if (tagPosition >= 0) {
			currentTag.append( line.substring(position, tagPosition) );
			insideTag = false;
			return tagPosition + stop.length();
		}
		
		currentTag.append( line.substring(position) );
		return line.length();
	}
	
	private int parseUnescapedTag(String line, int position) {
		int unescapedTagPosition = line.indexOf(Delimiter.UNESCAPED_STOP, position);
		
		if (unescapedTagPosition >= 0) {
			currentTag.append( line.substring(position, unescapedTagPosition) );
			insideUnescapedTag = false;
			return unescapedTagPosition + Delimiter.UNESCAPED_STOP.length();
		}

		currentTag.append( line.substring(position) );
		return line.length();
	}
	
	private int searchTag(String line, int position) {
		int tagPosition = line.indexOf(start, position);
		int unescapedTagPosition = line.indexOf(Delimiter.UNESCAPED_START, position);
		
		insideTag = tagPosition >= 0;
		insideUnescapedTag = unescapedTagPosition >= 0;
		
		if (insideTag || insideUnescapedTag) {
			return tagPosition;
		}
		
		return line.length();
	}
	
}