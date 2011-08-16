package mustache.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mustache.core.Instruction;

final class Delimiter {
	private static final Pattern CHANGE_DELIMITER_PATTERN = Pattern.compile("^\\=\\s*([^= ]+)\\s*([^= ]+)\\s*\\=$");
	
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
	
	private String textTrailingBlanks = "";
	private String tagLineStart = "";
	private String tagLineEnd = "";
	
	boolean isInsideTag() {
		return insideTag | insideUnescapedTag;
	}
	
	int tagStartLength() {
		return insideTag ? start.length() : UNESCAPED_START.length();
	}

	Instruction getInstruction() throws ParseException {
		Tag tag = createTag();
		textTrailingBlanks = calculateTextTrailingBlanks(tag);
		
		Instruction instruction = tag.toInstruction();
		if (instruction.getAction() != Instruction.Action.ENTER_PARTIAL) {
			return instruction;
		}
		
		return Instruction.newInstance(Instruction.Action.ENTER_PARTIAL, getTextTrailingBlanks() + instruction.getPartial());
	}

	private String calculateTextTrailingBlanks(Tag tag) {
		boolean startBlank = tagLineStart.trim().length() == 0;
		if ( !tag.canBeStandalone() ) {
			return startBlank ? tagLineStart : "";
		}
		boolean endBlank = tagLineEnd.trim().length() == 0;
		return startBlank & endBlank ? "" : tagLineStart;
	}

	String getTextTrailingBlanks() {
		String value = textTrailingBlanks;
		textTrailingBlanks = "";
		return value.trim().length() == 0 ? value : "";
	}

	private Tag createTag() throws ParseException {
		String content = currentTag.toString().trim();
		currentTag = new StringBuilder();
		
		if (insideUnescapedTag) {
			return Tag.newUnescapedTag(content);
		}
		
		try {
			return Tag.newTag(content);
		} catch (ChangeDelimiterException e) {
			changeDelimiter(content);
			return e.getTag();
		}
	}
	
	private void changeDelimiter(String content) throws ParseException {
		Matcher matcher = CHANGE_DELIMITER_PATTERN.matcher(content);
		if ( !matcher.matches() ) {
			throw new ParseException("Invalid tag content : " + content);
		}
		setBounds(matcher.group(1), matcher.group(2));
	}

	private void setBounds(String start, String stop) throws ParseException {
		if (UNESCAPED_START.equals(start) || UNESCAPED_STOP.equals(stop)) {
			throw new ParseException("Normal tags cannot override escape tags");
		}
		this.normalPrecedesUnescaped = start.length() > UNESCAPED_START.length();
		this.start = start;
		this.stop = stop;
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
			int tagEndPosition = tagPosition + stop.length();
			tagLineEnd = line.substring(tagEndPosition);
			return tagEndPosition;
		}
		
		currentTag.append( line.substring(position) );
		return line.length();
	}
	
	private int parseUnescapedTag(String line, int position) {
		int unescapedTagPosition = line.indexOf(Delimiter.UNESCAPED_STOP, position);
		
		if (unescapedTagPosition >= 0) {
			currentTag.append( line.substring(position, unescapedTagPosition) );
			insideUnescapedTag = false;
			int tagEndPosition = unescapedTagPosition + Delimiter.UNESCAPED_STOP.length();
			tagLineEnd = line.substring(tagEndPosition);
			return tagEndPosition;
		}

		currentTag.append( line.substring(position) );
		return line.length();
	}
	
	private int searchTag(String line, int position) {
		tagLineEnd = "";
		
		int tagPosition = line.indexOf(start, position);
		int unescapedTagPosition = line.indexOf(Delimiter.UNESCAPED_START, position);
		
		if (tagPosition >= 0 | unescapedTagPosition >= 0) {
			int actualTagPosition = openTag(tagPosition, unescapedTagPosition);
			this.tagLineStart = line.substring(0, actualTagPosition);
			return actualTagPosition;
		}
		
		return line.length();
	}

	private int openTag(int tagPosition, int unescapedTagPosition) {
		if ( foundAndBefore(tagPosition, unescapedTagPosition) ) {
			insideTag = true;
			return tagPosition;
		}
		
		if ( foundAndBefore(unescapedTagPosition, tagPosition) ) {
			insideUnescapedTag = true;
			return unescapedTagPosition;
		}
		
		// found both tag start at the same position
		insideTag = normalPrecedesUnescaped;
		insideUnescapedTag = !normalPrecedesUnescaped;
		
		return insideTag ? tagPosition : unescapedTagPosition;
	}
	
	private boolean foundAndBefore(int a, int b) {
		return a == -1 ? false : b == -1 | a < b;
	}
}