package mustache.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class Sequencer {

	private Deque<String> sections = new ArrayDeque<String>();
	private List<Instruction> sequence = new ArrayList<Instruction>();
	
	public Sequencer add(Instruction instruction) {
		if (instruction == null) {
			throw new NullPointerException();
		}
		updateSectionStack(instruction);
		sequence.add(instruction);
		return this;
	}
	
	private void updateSectionStack(Instruction instruction) {
		if ( instruction.getType().opening() ) {
			sections.add( instruction.getData() );
		}
		else if( instruction.getType().closing() ) {
			closeSection( instruction.getData() );
		}
	}

	private void closeSection(String data) {
		// TODO Auto-generated method stub
		
	}

	public Sequencer addAll(List<Instruction> instructions) {
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