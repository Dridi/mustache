package mustache.parser;

public final class ParseException extends Exception {
	private static final long serialVersionUID = 7245280982537468242L;

	ParseException(String message) {
		super(message);
	}
	
	ParseException(String message, Throwable cause) {
		super(message, cause);
	}
}