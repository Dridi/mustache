package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class represents a {@link Mustache} instruction. An {@link AppendText}
 * is basically represented by an action and data associated to it. Instances of
 * this class are immutable and can be shared safely among multiple threads.
 * 
 * @author Dri
 */
public final class AppendText extends Instruction {
	private static final long serialVersionUID = -5981162238186212143L;

	private final String text;
	
	private transient String indentation = null;

	public AppendText(String text) {
		if (text == null) {
			throw new NullPointerException();
		}
		this.text = text;
	}
	
	/**
	 * @return the text to append
	 */
	public String getText() {
		return text;
	}
	
	public boolean isIndented() {
		return indentation != null;
	}
	
	public String getIndentation() {
		return indentation;
	}

	public boolean isEndOfLine() {
		return text.endsWith("\r") | text.endsWith("\n");
	}
	
	public AppendText indent(String indentation) {
		if ( "".equals(indentation) ) {
			return this;
		}
		if ( indentation == null || !isIndentation(indentation) ) {
			throw new IllegalArgumentException("Invalid indentation : " + indentation);
		}
		AppendText instruction = new AppendText(text);
		instruction.indentation = indentation;
		return instruction;
	}
	
	/**
	 * @return the {@code Instruction} as a {@link String}
	 */
	@Override
	public String toString() {
		String escapedText = StringEscapeUtils.escapeJava(text);
		return MessageFormat.format("{0}[{1}]", getClass().getSimpleName(), escapedText);
	}
	
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -1649968892332016289L;
		
		private final String text;
		
		SerializationProxy(AppendText instruction) {
			this.text = instruction.text;
		}
		
		private Object readResolve() {
			return new AppendText(text);
		}
	}
}