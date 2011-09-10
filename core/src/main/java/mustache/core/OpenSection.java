package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.MessageFormat;

import mustache.util.Context;


public class OpenSection extends Instruction {
	private static final long serialVersionUID = -5005066097122767130L;
	
	private final String name;
	private final boolean inverted;
	
	private transient int closeIndex = -1;
	
	public OpenSection(String name, boolean inverted) {
		this.name = name;
		this.inverted = inverted;
	}
	
	public static OpenSection newInstance(String name, boolean inverted) {
		if ( !Context.isValidQuery(name) ) {
			throw new IllegalArgumentException("Invalid section name : " + name);
		}
		return new OpenSection(name, inverted);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isInverted() {
		return inverted;
	}
	
	public int getCloseIndex() {
		if (closeIndex < 0) {
			throw new IllegalStateException("Negative index : " + closeIndex);
		}
		return closeIndex;
	}
	
	public OpenSection setCloseIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("Negative index : " + index);
		}
		OpenSection instruction = new OpenSection(name, inverted);
		instruction.closeIndex = index;
		return instruction;
	}
	
	/**
	 * @return the {@code Instruction} as a {@link String}
	 */
	@Override
	public String toString() {
		String type = inverted ? "inverted" : "normal";
		return MessageFormat.format("{0}[{1}:{2}]", getClass().getSimpleName(), name, type);
	}
	
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = 5229196132720478735L;
		
		private final String name;
		private final boolean inverted;
		
		SerializationProxy(OpenSection instruction) {
			this.name = instruction.name;
			this.inverted = instruction.inverted;
		}
		
		private Object readResolve() {
			return OpenSection.newInstance(name, inverted);
		}
	}
}
