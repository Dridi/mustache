package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * This class represents a {@link Mustache} instruction. An {@code Instruction}
 * is basically represented by an action and data associated to it. Instances of
 * this class are immutable and can be shared safely among multiple threads.
 * 
 * @author Dri
 */
public final class Instruction implements Serializable {
	
	private static final long serialVersionUID = 7976280972290440553L;
	
	/**
	 * A reusable <i>no operation performed</i> {@code Instruction}.
	 */
	public static final Instruction NOP = new Instruction(Type.NOP, null);
	
	/**
	 * The set of available instructions.
	 */
	public enum Type {
		NOP,
		APPEND_TEXT,
		APPEND_VARIABLE,
		APPEND_UNESCAPED_VARIABLE,
		OPEN_SECTION,
		OPEN_INVERTED_SECTION,
		CLOSE_SECTION;
	}
	
	private final Type type;
	private final String data;

	private Instruction(Type type, String data) {
		this.type = type;
		this.data = data;
	}
	
	/**
	 * Creates a new {@code Instruction}.
	 * @param type the type of {@code Instruction}
	 * @param data the data associated to the {@code Instruction}
	 * @return a newly created {@code Instruction}
	 * @throws NullPointerException if {@code type} or {@code data} is {@code null}
	 */
	public static Instruction newInstance(Type type, String data) {
		if (type == Type.NOP) {
			return NOP;
		}
		
		if (type == null || data == null) {
			throw new NullPointerException();
		}
		
		if (type != Type.APPEND_TEXT && !Context.isValidQuery(data)) {
			throw new IllegalArgumentException("Invalid interpolation : " + data);
		}
		
		return new Instruction(type, data);
	}
	
	/**
	 * Indicates whether this instruction intends to open a section.
	 * @return {@code true} if it indicates a section opening
	 */
	public boolean opening() {
		return type == Type.OPEN_SECTION || type == Type.OPEN_INVERTED_SECTION;
	}

	/**
	 * Indicates whether this instruction intends to close a section.
	 * @return {@code true} if it indicates a section closing
	 */
	public boolean closing() {
		return type == Type.CLOSE_SECTION;
	}
	
	/**
	 * @return the {@code Instruction} {@link Type}
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * @return the {@code Instruction} data
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * @return the {@code Instruction} {@link Type} name
	 */
	@Override
	public String toString() {
		return type.name();
	}

	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -1303026970090245725L;
		
		private final Type type;
		private final String data;

		SerializationProxy(Instruction instruction) {
			this.type = instruction.type;
			this.data = instruction.data;
		}
		
		private Object readResolve() {
			return Instruction.newInstance(type, data);
		}
	}
}