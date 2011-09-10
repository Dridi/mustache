package mustache.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mustache.core.AppendVariable;
import mustache.core.CloseSection;
import mustache.core.EnterPartial;
import mustache.core.Instruction;
import mustache.core.OpenSection;
import mustache.util.Context;

final class Tag {
	private static final String TAG_CONTENT_REGEX = "^(&|#|\\^|/|\\>|\\=|\\!)?\\s*(.*?)$"; // TODO check non greedy pattern
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
		
		if ( hasInterpolation(type) ) {
			checkInterpolation(content);
		}
		
		Tag tag = new Tag(type, content);
		
		if (type == Type.DELIMITER) {
			throw new  ChangeDelimiterException(tag);
		}
		
		return tag;
	}
	
	static Tag newUnescapedTag(String string) throws ParseException {
		String content = string.trim();
		checkInterpolation(content);
		return new Tag(Type.UNESCAPED_VARIABLE, content);
	}

	private static boolean hasInterpolation(Type type) {
		return type != Type.PARTIAL & type != Type.DELIMITER & type != Type.COMMENT;
	}

	private static void checkInterpolation(String content) throws ParseException {
		if ( !Context.isValidQuery(content) ) {
			throw new ParseException("Invalid tag content : " + content);
		}
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
	
	Instruction toInstruction() {
		return type.toInstruction(content);
	}
	
	private enum Type {
		VARIABLE("") {
			@Override
			protected Instruction toInstruction(String content) {
				return AppendVariable.newInstance(content, false);
			}
		},
		UNESCAPED_VARIABLE("&") {
			@Override
			protected Instruction toInstruction(String content) {
				return AppendVariable.newInstance(content, true);
			}
		},
		SECTION("#") {
			@Override
			protected Instruction toInstruction(String content) {
				return OpenSection.newInstance(content, false);
			}
		},
		INVERTED_SECTION("^") {
			@Override
			protected Instruction toInstruction(String content) {
				return OpenSection.newInstance(content, true);
			}
		},
		SECTION_END("/") {
			@Override
			protected Instruction toInstruction(String content) {
				return CloseSection.newInstance(content);
			}
		},
		PARTIAL(">") {
			@Override
			protected Instruction toInstruction(String content) {
				return EnterPartial.newInstance(content);
			}
		},
		DELIMITER("=") {
			@Override
			protected Instruction toInstruction(String content) {
				return null;
			}
		},
		COMMENT("!") {
			@Override
			protected Instruction toInstruction(String content) {
				return null;
			}
		};
		
		private final String token;
		
		private Type(String token) {
			this.token = token;
		}
		
		protected abstract Instruction toInstruction(String content);
		
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