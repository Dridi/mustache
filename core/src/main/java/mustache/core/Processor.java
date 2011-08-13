package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.List;

public final class Processor implements Serializable, Iterator<Instruction> {
	
	private static final long serialVersionUID = 289040399110456725L;

	private final List<Instruction> sequence;
	private final transient int maxPosition;
	
	private transient int currentPosition = -1;
	private transient boolean tryOpeningSection;
	private transient boolean tryClosingSection;
	
	private Processor(List<Instruction> sequence) {
		this.sequence = sequence;
		this.maxPosition = sequence.size() - 1;
	}
	
	public static Processor fromSequencer(Sequencer sequencer) {
		if (sequencer == null) {
			throw new NullPointerException();
		}
		
		synchronized (sequencer) {
			if ( !sequencer.isProcessable() ) {
				throw new IllegalArgumentException("Sequence not processable");
			}
			
			return new Processor( sequencer.getSequence() );
		}
	}
	
	public void reset() {
		currentPosition = -1;
		tryOpeningSection = false;
		tryClosingSection = false;
	}
	
	public void enterSection() {
		if (tryOpeningSection == false) {
			throw new IllegalStateException("Unexpected attempt to enter a section.");
		}
		
		tryOpeningSection = false;
	}
	
	public void exitSection() {
		if (tryClosingSection == false) {
			throw new IllegalStateException("Unexpected attempt to exit a section.");
		}
		
		tryClosingSection = false;
	}

	@Override
	public boolean hasNext() {
		return currentPosition < maxPosition;
	}

	@Override
	public Instruction next() {
		if (tryOpeningSection) {
			skipSection();
		}
		else if (tryClosingSection) {
			reenterSection();
		}
		
		currentPosition++;
		
		return nextInstruction();
	}

	private Instruction nextInstruction() {
		Instruction instruction = sequence.get(currentPosition);
		
		tryOpeningSection = instruction.opening();
		tryClosingSection = instruction.closing();
		
		return instruction;
	}

	private void reenterSection() {
		int openedSections = -1;
		
		while (openedSections < 0) {
			currentPosition--;
			openedSections += openCloseDelta();
		}
		
		tryClosingSection = false;
	}

	private void skipSection() {
		int openedSections = 1;
		
		while (openedSections > 0) {
			currentPosition++;
			openedSections += openCloseDelta();
		}
		
		if (currentPosition > maxPosition) {
			currentPosition = maxPosition;
		}
		
		tryOpeningSection = false;
	}

	private int openCloseDelta() {
		Instruction instruction = sequence.get(currentPosition);
		return instruction.opening() ? 1 : instruction.closing() ? -1 : 0;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	private static class SerializationProxy implements Serializable {
		private static final long serialVersionUID = 7682273649183979614L;
		
		private final List<Instruction> sequence;

		SerializationProxy(Processor processor) {
			this.sequence = processor.sequence;
		}
		
		private Object readResolve() throws StreamCorruptedException {
			try {
				return Processor.fromSequencer( new Sequencer().addAll(sequence) );
			} catch (SequenceException e) {
				throw new StreamCorruptedException( e.getMessage() );
			}
		}
	}
	
}