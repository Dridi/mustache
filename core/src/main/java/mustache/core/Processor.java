package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.List;

public final class Processor implements Serializable, Iterator<Instruction> {
	
	private static final long serialVersionUID = 289040399110456725L;

	private transient int currentPosition;
	
	private final List<Instruction> sequence;
	
	private transient String openingSection;
	private transient String closingSection;
	
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
		currentPosition = 0;
		openingSection = null;
		closingSection = null;
	}
	
	public void enterSection(String section) {
		if ( !openingSection.equals(section) ) {
			throw new IllegalArgumentException("Expected to enter " + openingSection + " not " + section);
		}
		openingSection = null;
	}
	
	public void exitSection(String section) {
		if ( !closingSection.equals(section) ) {
			throw new IllegalArgumentException("Expected to exit " + closingSection + " not " + section);
		}
		openingSection = null;
	}

	@Override
	public boolean hasNext() {
		return currentPosition < sequence.size() - 1;
	}

	@Override
	public Instruction next() {
		if (openingSection != null) {
			skipSection(openingSection);
		}
		else if (closingSection != null) {
			reenterSection(closingSection);
		}
		
		currentPosition++;
		
		return nextInstruction();
	}

	private Instruction nextInstruction() {
		if (currentPosition == sequence.size()) {
			return Instruction.NOP;
		}
		
		Instruction instruction = sequence.get(currentPosition);
		
		if ( instruction.opening() ) {
			openingSection = instruction.getData();
		}
		else if ( instruction.closing() ) {
			closingSection = instruction.getData();
		}
		
		return instruction;
	}

	private void reenterSection(String closingSection) {
		
		int openedSections = 1;
		
		while (openedSections > 0) {
			currentPosition--;
			openedSections -= openCloseDelta();
		}
	}

	private void skipSection(String openingSection) {
		
		int openedSections = 1;
		
		while (openedSections > 0) {
			currentPosition++;
			openedSections += openCloseDelta();
		}
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