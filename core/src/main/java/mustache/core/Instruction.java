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
	 * The set of available instructions.
	 */
	public enum Action {
		APPEND_TEXT,
		APPEND_VARIABLE,
		APPEND_UNESCAPED_VARIABLE,
		OPEN_SECTION,
		OPEN_INVERTED_SECTION,
		CLOSE_SECTION;
	}
	
	private final Action action;
	private final String data;

	private Instruction(Action action, String data) {
		this.action = action;
		this.data = data;
	}
	
	/**
	 * Creates a new {@code Instruction}.
	 * @param action the type of {@code Instruction}
	 * @param data the data associated to the {@code Instruction}
	 * @return a newly created {@code Instruction}
	 * @throws NullPointerException if {@code action} or {@code data} is {@code null}
	 */
	public static Instruction newInstance(Action action, String data) {
		if (action == null | data == null) {
			throw new NullPointerException();
		}
		
		if (action != Action.APPEND_TEXT && !Context.isValidQuery(data)) {
			throw new IllegalArgumentException("Invalid interpolation : " + data);
		}
		
		return new Instruction(action, data);
	}
	
	/**
	 * Indicates whether this instruction intends to open a section.
	 * @return {@code true} if it indicates a section opening
	 */
	public boolean opening() {
		return action == Action.OPEN_SECTION | action == Action.OPEN_INVERTED_SECTION;
	}

	/**
	 * Indicates whether this instruction intends to close a section.
	 * @return {@code true} if it indicates a section closing
	 */
	public boolean closing() {
		return action == Action.CLOSE_SECTION;
	}
	
	/**
	 * @return the {@code Instruction} {@link Action}
	 */
	public Action getAction() {
		return action;
	}
	
	/**
	 * @return the {@code Instruction} data
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * @return the {@code Instruction} {@link Action} name
	 */
	@Override
	public String toString() {
		return "Instruction " + action;
	}

	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = -1303026970090245725L;
		
		private final Action action;
		private final String data;

		SerializationProxy(Instruction instruction) {
			this.action = instruction.action;
			this.data = instruction.data;
		}
		
		private Object readResolve() {
			return Instruction.newInstance(action, data);
		}
	}
}