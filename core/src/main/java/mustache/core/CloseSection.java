package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.MessageFormat;

import mustache.util.Context;


public final class CloseSection extends Instruction {
	private static final long serialVersionUID = -2324870032322436178L;

	private final String name;
	
	private transient int openIndex = -1;
	
	public CloseSection(String name) {
		this.name = name;
	}
	
	public static CloseSection newInstance(String name) {
		if ( !Context.isValidQuery(name) ) {
			throw new IllegalArgumentException("Invalid section name : " + name);
		}
		return new CloseSection(name);
	}
	
	public String getName() {
		return name;
	}
	
	public int getOpenIndex() {
		if (openIndex < 0) {
			throw new IllegalStateException("Negative index : " + openIndex);
		}
		return openIndex;
	}
	
	public CloseSection setOpenIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("Negative index : " + index);
		}
		CloseSection instruction = new CloseSection(name);
		instruction.openIndex = index;
		return instruction;
	}
	
	/**
	 * @return the {@code Instruction} as a {@link String}
	 */
	@Override
	public String toString() {
		return MessageFormat.format("{0}[{1}]", getClass().getSimpleName(), name);
	}
	
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -534794496024743306L;
		
		private final String name;
		
		SerializationProxy(CloseSection instruction) {
			this.name = instruction.name;
		}
		
		private Object readResolve() {
			return CloseSection.newInstance(name);
		}
	}
}
