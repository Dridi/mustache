package mustache.core;

/**
 * Thrown to indicate a misplaced section close in a {@link Sequencer}.
 * @author Dri
 */
public class SequenceException extends Exception {
	
	private static final long serialVersionUID = -50117184669212739L;
	
	/**
	 * {@inheritDoc}
	 */
	SequenceException(String message) {
		super(message);
	}
}
