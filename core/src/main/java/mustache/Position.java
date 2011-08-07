package mustache;

import java.text.MessageFormat;

/**
 * Represents a position in a template. Line and column numbers are calculated
 * according to line feeds only. Instances of this class are immutable and safe
 * for use by multiple concurrent threads.
 * 
 * @author Dri
 */
final class Position {
	
	/**
	 * The initial position (ie. line 1, column 1).
	 */
	static final Position START = new Position();
	
	private int offset = 0;
	
	private int line = 1;
	
	private int column = 1;
	
	private Position() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param offset The offset in characters {@code >= 0}
	 * @param line The line number {@code >= 1}
	 * @param column The column number {@code >= 1}
	 */
	Position(int offset, int line, int column) {
		this.offset = offset;
		this.line = line;
		this.column = column;
	}
	
	/**
	 * @return the offset
	 */
	int offset() {
		return offset;
	}
	
	/**
	 * @return the line number
	 */
	int line() {
		return line;
	}
	
	/**
	 * @return the column number
	 */
	int column() {
		return column;
	}
	
	/**
	 * Moves forward from the current position through given text.
	 * 
	 * @param text The text to move through
	 * @return The position after <code>this</code> and <code>text</code>
	 */
	Position forward(String text) {
		Position forward = new Position();
		
		forward.offset = offset + text.length();
		
		if (text.indexOf('\n') < 0) {
			forward.line = line;
			forward.column = column + text.length();
		}
		else {
			forward.line = line + countLineFeeds(text);
			forward.column = text.substring(text.lastIndexOf('\n')).length();
		}
		
		return forward;
	}
	
	private int countLineFeeds(String text) {
		int count = 0;
		
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				count++;
			}
		}
		
		return count;
		// return text.replaceAll("[^\\n]", "").length();
	}
	
	/**
	 * @return "Position {offset} ({line}, {column})"
	 */
	@Override
	public String toString() {
		return MessageFormat.format("Position {0} ({1}, {2})", offset, line, column);
	}
}