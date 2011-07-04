package mustache;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Stream {
	
	private static final String NORMAL_TAG_REGEX = "(?s){0}\\s*(&|#|\\^|/|\\>|\\=|\\!)?\\s*(.*?){1}";
	
	private static final String ESCAPE_TAG_REGEX = "\\{\\{\\{\\s*(\\.|([a-z_][a-z0-9_]*(\\.[a-z_][a-z0-9_]*)*))\\s*\\}\\}\\}";
	
	private static final Pattern ESCAPE_TAG_PATTERN = Pattern.compile(ESCAPE_TAG_REGEX);
	
	private static final Pattern CHANGE_DELIMITER_PATTERN = Pattern.compile("^\\s*([^= ]+)\\s*([^= ]+)\\s*\\=$");

	private String input;
	
	private Position position = Position.START;
	
	private Delimiter delimiter = new Delimiter();
	
	Stream(String input) {
		if (input == null) {
			throw new NullPointerException("Input is null.");
		}
		
		this.input = input;
	}
	
	void changeDelimiter(Tag tag) throws ParseException {
		Matcher matcher = CHANGE_DELIMITER_PATTERN.matcher(tag.getContent());
		
		if (!matcher.matches()) {
			throw ParseException.fromTag("Illegal delimiters : " + tag.getContent(), tag);
		}
		
		try {
			delimiter.setTags(matcher.group(1), matcher.group(2));
		}
		catch (IllegalArgumentException e) {
			throw ParseException.fromTag(e.getMessage(), tag, e);
		}
	}
	
	boolean hasNext() {
		if (position.offset() > input.length()) {
			throw new IllegalStateException("Position exceeds the size of the input");
		}
		
		return position.offset() < input.length();
	}
	
	private static class StartDelimiter {
		
		boolean normal;
		
		int offset;
	}
	
	Tag getNextTag() throws ParseException {
		StartDelimiter start = findStartDelimiter();
		
		if (start == null) {
			return null;
		}
		
		Position startPosition = this.position.forward(input.substring(this.position.offset(), start.offset));
		Tag tag = start.normal ? parseNormalTag(startPosition) : parseEscapeTag(startPosition);
		
		makeStandalone(tag);
		
		return tag;
	}
	
	String readToTag(Tag tag) {
		String string = null;
		
		if (tag == null) {
			string = input.substring(position.offset());
			position = position.forward(string);
		}
		else {
			string = input.substring(position.offset(), tag.getPosition().offset());
			position = position.forward(string + tag);
		}
		
		return string;
	}
	
	Position getPosition() {
		return position;
	}
	
	void rewind(Position position) {
		if (position != null) {
			this.position = position;
		}
	}
	
	private StartDelimiter findStartDelimiter() {
		StartDelimiter normal = new StartDelimiter();
		normal.normal = true;
		normal.offset = input.indexOf(delimiter.getUnquotedStart(), position.offset());
		
		StartDelimiter escape = new StartDelimiter();
		escape.normal = false;
		escape.offset = input.indexOf(Delimiter.UNESCAPED_START, position.offset());
		
		if (normal.offset == escape.offset) {
			return getPriorityTag(normal, escape);
		}
		
		return getFirstTag(normal, escape);
	}
	
	private StartDelimiter getPriorityTag(StartDelimiter normal, StartDelimiter escape) {
		if (normal.offset == -1) {
			return null;
		}
		
		return delimiter.precede() ? normal : escape;
	}
	
	private StartDelimiter getFirstTag(StartDelimiter normal, StartDelimiter escape) {
		if (normal.offset == -1) {
			return escape;
		}
		else if (escape.offset == -1) {
			return normal;
		}
		
		return normal.offset <= escape.offset ? normal : escape;
	}
	
	private Tag parseNormalTag(Position position) throws ParseException {
		String regex = MessageFormat.format(NORMAL_TAG_REGEX, delimiter.getStart(), delimiter.getStop());
		Matcher matcher = parseTag(Pattern.compile(regex), position);
		
		return Tag.newTag(matcher.group(0), matcher.group(1), matcher.group(2), position);
	}
	
	private Tag parseEscapeTag(Position position) throws ParseException {
		Matcher matcher = parseTag(ESCAPE_TAG_PATTERN, position);
		
		return Tag.newTag(matcher.group(0), Tag.Type.UNESCAPED_VARIABLE.token(), matcher.group(1), position);
	}
	
	private Matcher parseTag(Pattern pattern, Position position) throws ParseException {
		Matcher matcher = pattern.matcher(input);
		int offset = position.offset();
		
		if (!matcher.find(offset) || matcher.start() != offset) {
			throw ParseException.fromPosition("Invalid tag", position);
		}
		
		return matcher;
	}
	
	private void makeStandalone(Tag tag) {
		if (!tag.getType().canBeStandalone()) {
			return;
		}
		
		String previousPart = getTagPreviousPart(tag);
		String nextPart = getTagNextPart(tag);
		
		if ((previousPart + nextPart).matches("^\\s*$")) {
			tag.makeStandalone(previousPart, nextPart);
		}
	}
	
	private String getTagPreviousPart(Tag tag) {
		int linePosition = input.lastIndexOf('\n', tag.getPosition().offset()) + 1;
		
		return input.substring(linePosition, tag.getPosition().offset());
	}
	
	private String getTagNextPart(Tag tag) {
		int tagEndPosition = tag.getPosition().offset() + tag.getLength();
		
		int linePosition = input.indexOf('\n', tagEndPosition);
		linePosition = linePosition >= 0 ? linePosition + 1 : input.length();
		
		return input.substring(tagEndPosition, linePosition);
	}
	
}