package mustache.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class Sequencer {

	private Deque<String> sections = new ArrayDeque<String>();
	private List<Instruction> sequence = new ArrayList<Instruction>();
	
	public Sequencer add(Instruction instruction) throws SequenceException {
		if (instruction == null) {
			throw new NullPointerException();
		}
		
		if (instruction.getType() == Instruction.Type.NOP) {
			throw new IllegalArgumentException("Un expected instruction " + instruction);
		}
		
		updateSectionStack(instruction);
		sequence.add(instruction);
		
		return this;
	}
	
	private void updateSectionStack(Instruction instruction) throws SequenceException {
		if ( instruction.getType().opening() ) {
			sections.addFirst( instruction.getData() );
		}
		else if( instruction.getType().closing() ) {
			closeSection( instruction.getData() );
		}
	}

	private void closeSection(String section) throws SequenceException {
		if ( sections.isEmpty() ) {
			throw new SequenceException("No section currently open, cannot close " + section);
		}
		
		String current = sections.peekFirst();
		
		if ( current.equals(section) ) {
			sections.removeFirst();
		}
		else {
			throw new SequenceException("Expected to close " + current + " not " + section);
		}
	}

	public Sequencer addAll(List<Instruction> instructions) throws SequenceException {
		for (Instruction instruction : instructions) {
			add(instruction);
		}
		return this;
	}
	
	public boolean isProcessable() {
		return sequence.size() > 0 && sections.size() == 0;
	}
	
	public List<Instruction> getSequence() {
		List<Instruction> copy = new ArrayList<Instruction>(sequence);
		return Collections.unmodifiableList(copy);
	}
	
	public Sequencer clear() {
		sequence.clear();
		sections.clear();
		return this;
	}
}