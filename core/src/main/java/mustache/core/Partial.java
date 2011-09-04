package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Partial implements Processable {
	private static final long serialVersionUID = 4694509995100968343L;
	
	private final String name;
	private final String indentation;
	
	private Partial(String name, String indentation) {
		this.name = name;
		this.indentation = indentation;
	}

	public static Processable newInstance(String name) {
		return newIndentedInstance(name, "");
	}

	public static Partial newIndentedInstance(String name, String indentation) {
		if (name == null | indentation == null) {
			throw new NullPointerException();
		}
		if (name.trim().length() == 0) {
			throw new IllegalArgumentException("Invalid name : " + name);
		}
		if ( !Indentation.isIndentation(indentation) ) {
			throw new IllegalArgumentException("Invalid indentation : " + indentation);
		}
		return new Partial(name, indentation);
	}

	public String getName() {
		return name;
	}
	
	public String getIndentation() {
		return indentation;
	}
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = 1605348054856340659L;
		
		private final String name;
		private final String indentation;
		
		SerializationProxy(Partial processor) {
			this.name = processor.name;
			this.indentation = processor.indentation;
		}
		
		private Object readResolve() {
			return Partial.newIndentedInstance(name, indentation);
		}
	}
}
