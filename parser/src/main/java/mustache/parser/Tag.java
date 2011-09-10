package mustache.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mustache.core.AppendText;
import mustache.core.EnterPartial;
import mustache.core.Instruction;
import mustache.util.Context;

final class Tag {
	
	private static final String TAG_CONTENT_REGEX = "^(&|#|\\^|/|\\>|\\=|\\!)?\\s*(.*?)$";
	private static final Pattern TAG_CONTENT_PATTERN = Pattern.compile(TAG_CONTENT_REGEX, Pattern.DOTALL);
	
	private final Type type;
	private final String content;
	
	static Tag newTag(String string) throws ParseException, ChangeDelimiterException {
		Matcher matcher = TAG_CONTENT_PATTERN.matcher(string);
		
		if ( !matcher.matches() ) {
			throw new ParseException("Invalid tag content : " + string);
		}
		
		Type type = Type.fromToken( matcher.group(1) );
		String content = matcher.group(2);
		
		if ( type != Type.PARTIAL && type.action != null && !Context.isValidQuery(content) ) {
			throw new ParseException("Invalid tag content : " + content);
		}
		
		Tag tag = new Tag(type, content);
		
		if (type == Type.DELIMITER) {
			throw new  ChangeDelimiterException(tag);
		}
		
		return tag;
	}
	
	static Tag newUnescapedTag(String string) throws ParseException {
		String content = string.trim();
		
		if ( !Context.isValidQuery(content) ) {
			throw new ParseException("Invalid tag content : " + content);
		}
		
		return new Tag(Type.UNESCAPED_VARIABLE, content);
	}
	
	private Tag(Type type, String content) {
		this.type = type;
		this.content = content;
	}

	boolean canBeStandalone() {
		return type != Type.VARIABLE & type != Type.UNESCAPED_VARIABLE;
	}

	boolean isPartial() {
		return type == Type.PARTIAL;
	}
	
	Instruction toProcessable() {
		if (type == Type.PARTIAL) {
			return EnterPartial.newInstance(content);
		}
		if (type.action == null) {
			return null;
		}
		return AppendText.newInstance(type.action, content);
	}
	
	private enum Type {
		VARIABLE("", AppendText.Action.APPEND_VARIABLE),
		UNESCAPED_VARIABLE("&", AppendText.Action.APPEND_UNESCAPED_VARIABLE),
		SECTION("#", AppendText.Action.OPEN_SECTION),
		INVERTED_SECTION("^", AppendText.Action.OPEN_INVERTED_SECTION),
		SECTION_END("/", AppendText.Action.CLOSE_SECTION),
		PARTIAL(">", null),
		DELIMITER("=", null),
		COMMENT("!", null);
		
		private final String token;
		private final AppendText.Action action;
		
		private Type(String token, AppendText.Action action) {
			this.token = token;
			this.action = action;
		}
		
		private static Type fromToken(String token) {
			for (Type type : values()) {
				if (type.token.equals(token)) {
					return type;
				}
			}
			return VARIABLE;
		}
	}
}