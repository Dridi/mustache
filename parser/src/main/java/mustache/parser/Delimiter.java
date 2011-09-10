package mustache.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mustache.core.EnterPartial;
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
	private Tag actualTag;
	
	private boolean normalPrecedesUnescaped = false;
	private boolean insideTag = false;
	private boolean insideUnescapedTag = false;
	private boolean isUnescapedTag;
	
	private String textTrailingBlanks = "";
	private String tagLineStart = "";
	private String tagLineEnd = "";
	
	boolean isInsideTag() {
		return insideTag | insideUnescapedTag;
	}
	
	int tagStartLength() {
		return insideTag ? start.length() : UNESCAPED_START.length();
	}

	Instruction getProcessable() throws ParseException {
		
		Instruction processable = actualTag.toProcessable();
		if (processable == null) {
			return null;
		}
		
		if (processable instanceof EnterPartial) {
			return createIndentedPartial((EnterPartial) processable);
		}
		
		calculateTextTrailingBlanks(actualTag);
		return processable;
	}

	private EnterPartial createIndentedPartial(EnterPartial partial) {
		boolean startBlank = tagLineStart.trim().length() == 0;
		String indentation = startBlank ? tagLineStart : "";
		textTrailingBlanks = "";
		return EnterPartial.newIndentedInstance(partial.getName(), indentation);
	}

	private void calculateTextTrailingBlanks(Tag tag) {
		boolean startBlank = tagLineStart.trim().length() == 0;
		if ( !tag.canBeStandalone() ) {
			textTrailingBlanks = startBlank ? tagLineStart : "";
			return;
		}
		boolean endBlank = tagLineEnd.trim().length() == 0;
		textTrailingBlanks = startBlank & endBlank ? "" : tagLineStart;
	}

	String getTextTrailingBlanks() {
		String value = textTrailingBlanks;
		textTrailingBlanks = "";
		return value.trim().length() == 0 ? value : "";
	}

	private void createTag() throws ParseException {
		String content = currentTag.toString().trim();
		currentTag = new StringBuilder();
		
		if (isUnescapedTag) {
			actualTag = Tag.newUnescapedTag(content);
			return;
		}
		
		try {
			actualTag = Tag.newTag(content);
		} catch (ChangeDelimiterException e) {
			changeDelimiter(content);
			actualTag = e.getTag();
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

	int parse(String line, int position) throws ParseException {
		if (insideTag) {
			return parseTag(line, position);
		}
		else if (insideUnescapedTag ) {
			return parseUnescapedTag(line, position);
		}
		return searchTag(line, position);
	}

	private int parseTag(String line, int position) throws ParseException {
		int tagPosition = line.indexOf(stop, position);
		
		if (tagPosition >= 0) {
			currentTag.append( line.substring(position, tagPosition) );
			insideTag = false;
			isUnescapedTag = false;
			int tagEndPosition = tagPosition + stop.length();
			tagLineEnd = line.substring(tagEndPosition);
			createTag();
			if ( isStandalone() ) {
				return line.length();
			}
			return tagEndPosition;
		}
		
		currentTag.append( line.substring(position) );
		return line.length();
	}

	private boolean isStandalone() {
		if (actualTag.canBeStandalone() && tagLineStart.trim().length() == 0 && tagLineEnd.trim().length() == 0) {
			if ( !actualTag.isPartial() ) {
				tagLineStart = "";
			}
			tagLineEnd = "";
			return true;
		}
		return false;
	}
	
	private int parseUnescapedTag(String line, int position) throws ParseException {
		int unescapedTagPosition = line.indexOf(Delimiter.UNESCAPED_STOP, position);
		
		if (unescapedTagPosition >= 0) {
			currentTag.append( line.substring(position, unescapedTagPosition) );
			insideUnescapedTag = false;
			isUnescapedTag = true;
			int tagEndPosition = unescapedTagPosition + Delimiter.UNESCAPED_STOP.length();
			tagLineEnd = line.substring(tagEndPosition);
			createTag();
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
		return a < 0 ? false : b < 0 | a < b;
	}
}