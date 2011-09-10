package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.MessageFormat;

public final class EnterPartial extends Instruction {
	private static final long serialVersionUID = 4694509995100968343L;
	
	private final String name;
	private final String indentation;
	
	private EnterPartial(String name, String indentation) {
		this.name = name;
		this.indentation = indentation;
	}

	public static Instruction newInstance(String name) {
		return newIndentedInstance(name, "");
	}

	public static EnterPartial newIndentedInstance(String name, String indentation) {
		if (name == null | indentation == null) {
			throw new NullPointerException();
		}
		if (name.trim().length() == 0) {
			throw new IllegalArgumentException("Invalid name : " + name);
		}
		if ( !isIndentation(indentation) ) {
			throw new IllegalArgumentException("Invalid indentation : " + indentation);
		}
		return new EnterPartial(name, indentation);
	}

	public String getName() {
		return name;
	}
	
	public String getIndentation() {
		return indentation;
	}
	
	/**
	 * @return the {@code Instruction} as a {@link String}
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}[{1}, indentation:{2}]", getClass().getSimpleName(), name, indentation);
	}
	
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = 1616022726830459017L;
		
		private final String name;
		private final String indentation;
		
		SerializationProxy(EnterPartial instruction) {
			this.name = instruction.name;
			this.indentation = instruction.indentation;
		}
		
		private Object readResolve() {
			return EnterPartial.newIndentedInstance(name, indentation);
		}
	}
}
