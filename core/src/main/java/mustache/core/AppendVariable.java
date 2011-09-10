package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.MessageFormat;

import mustache.util.Context;

/**
 * This class represents a {@link Mustache} instruction. An {@link AppendVariable}
 * is basically represented by an action and data associated to it. Instances of
 * this class are immutable and can be shared safely among multiple threads.
 * 
 * @author Dri
 */
public final class AppendVariable extends Instruction {
	private static final long serialVersionUID = -8724529418198939161L;
	
	private final String name;
	private final boolean unescaped;
	
	private transient String indentation = null;
	
	private AppendVariable(String name, boolean unescaped) {
		this.name = name;
		this.unescaped = unescaped;
	}
	
	public static AppendVariable newInstance(String name, boolean unescaped) {
		if ( !Context.isValidQuery(name) ) {
			throw new IllegalArgumentException("Invalid variable name : " + name);
		}
		return new AppendVariable(name, unescaped);
	}
	
	/**
	 * @return the name of the variable
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Indicates whether the variable is regular or unescaped
	 * @return {@code true} if the variable is unescaped
	 */
	public boolean isUnescaped() {
		return unescaped;
	}

	public boolean isIndented() {
		return indentation != null;
	}
	
	public String getIndentation() {
		return indentation;
	}
	
	/**
	 * @return the {@code Instruction} as a {@link String}
	 */
	@Override
	public String toString() {
		String type = unescaped ? "unescaped" : "normal";
		return MessageFormat.format("{0}[{1}:{2}]", getClass().getSimpleName(), name, type);
	}
	
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -4133596669692285596L;
		
		private final String name;
		private final boolean indentation;
		
		SerializationProxy(AppendVariable instruction) {
			this.name = instruction.name;
			this.indentation = instruction.unescaped;
		}
		
		private Object readResolve() {
			return AppendVariable.newInstance(name, indentation);
		}
	}
}