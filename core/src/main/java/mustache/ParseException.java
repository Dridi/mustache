package mustache;

import java.text.MessageFormat;

import mustache.core.Position;

/**
 * Thrown to indicate a syntax error in a Mustache template.
 * 
 * @author Dri
 */
public final class ParseException extends Exception {
	
	private ParseException(String message, Position position) {
		this(message, position, null);
	}
	
	private ParseException(String message, Position position, Throwable cause) {
		super(formatMessage(message, position), cause);
	}
	
	/**
	 * Creates a <code>ParseException</code> from a <code>Position</code>.
	 * 
	 * @param message The syntax error message
	 * @param position The position of the parser
	 * @return An instance of <code>ParseException</code>
	 */
	static ParseException fromPosition(String message, Position position) {
		return new ParseException(message, position, null);
	}
	
	/**
	 * Creates a <code>ParseException</code> from a <code>Tag</code>.
	 * 
	 * @param message The syntax error message
	 * @param tag The invalid tag
	 * @return An instance of <code>ParseException</code>
	 */
	static ParseException fromTag(String message, Tag tag) {
		return fromTag(message, tag, null);
	}
	
	/**
	 * Creates a <code>ParseException</code> from a <code>Tag</code>.
	 * 
	 * @param message The syntax error message
	 * @param position The invalid tag
	 * @param cause the cause
	 * @return An instance of <code>ParseException</code>
	 */
	static ParseException fromTag(String message, Tag tag, Throwable cause) {
		return new ParseException(message, tag.getPosition(), cause);
	}
	
	private static String formatMessage(String message, Position position) {
		return MessageFormat.format("{0} at line {1}, column {2}", message, position.line(), position.column());
	}
}