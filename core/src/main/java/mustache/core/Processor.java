package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.List;

public final class Processor implements Serializable, Iterator<Instruction> {
	
	private static final long serialVersionUID = 289040399110456725L;

	private transient int currentPosition = -1;
	
	private final List<Instruction> sequence;
	
	private transient boolean openingSection;
	private transient boolean closingSection;
	private transient boolean enterSection;
	private transient boolean exitSection;
	
	private Processor(List<Instruction> sequence) {
		this.sequence = sequence;
	}
	
	public static Processor fromSequencer(Sequencer sequencer) {
		if ( !sequencer.isProcessable() ) {
			throw new IllegalArgumentException();
		}
		
		return new Processor( sequencer.getSequence() );
	}
	
	public void reset() {
		currentPosition = -1;
		openingSection = false;
		closingSection = false;
		enterSection = false;
		exitSection = false;
	}
	
	public void enterSection() {
		if (openingSection == false) {
			throw new IllegalStateException("Unexpected attempt to enter a section.");
		}
		
		openingSection = false;
		enterSection = true;
	}
	
	public void exitSection() {
		if (closingSection == false) {
			throw new IllegalStateException("Unexpected attempt to exit a section.");
		}
		
		closingSection = false;
		exitSection = true;
	}

	@Override
	public boolean hasNext() {
		return currentPosition < sequence.size() - 1;
	}

	@Override
	public Instruction next() {
		if (openingSection && !enterSection) {
			skipSection();
		}
		else if (closingSection && !exitSection) {
			reenterSection();
		}
		
		currentPosition++;
		
		return nextInstruction();
	}

	private Instruction nextInstruction() {
		if (currentPosition == sequence.size()) {
			return Instruction.NOP;
		}
		
		Instruction instruction = sequence.get(currentPosition);
		
		openingSection = instruction.opening();
		closingSection = instruction.closing();
		
		return instruction;
	}

	private void reenterSection() {
		int openedSections = -1;
		
		while (openedSections < 0) {
			currentPosition--;
			openedSections += openCloseDelta();
		}
		
		enterSection = false;
	}

	private void skipSection() {
		int openedSections = 1;
		
		while (openedSections > 0) {
			currentPosition++;
			openedSections += openCloseDelta();
		}
		
		exitSection = false;
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