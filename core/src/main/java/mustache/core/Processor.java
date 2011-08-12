package mustache.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public final class Processor implements Serializable, Iterator<Instruction> {
	
	private transient int currentPosition;
	
	private final List<Instruction> sequence;
	
	private transient String openingSection;
	private transient String closingSection;
	
	private Processor(List<Instruction> sequence) {
		this.sequence = sequence;
	}
	
	public static Processor fromSequence(Sequencer sequencer) {
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
	
	public void enterSection(String name) {
		// TODO Auto-generated method stub
	}
	
	public void exitSection(String name) {
		// TODO Auto-generated method stub
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
		
		if ( instruction.getType().opening() ) {
			openingSection = instruction.getData();
		}
		else if ( instruction.getType().closing() ) {
			closingSection = instruction.getData();
		}
		
		return instruction;
	}

	private void reenterSection(String closingSection) {
		// TODO Auto-generated method stub
		
	}

	private void skipSection(String openingSection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	// add SerializationProxy
}