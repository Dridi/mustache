package mustache.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * candidates : linker, sequencer, assembler, compiler
 *
 */
public final class Sequencer {

	private List<Instruction> sequence;
	
	public Sequencer add(Instruction instruction) {
		// TODO checks
		// TODO manage section stack
		sequence.add(instruction);
		return this;
	}
	
	public Sequencer addAll(List<Instruction> instructions) {
		for (Instruction instruction : instructions) {
			add(instruction);
		}
		return this;
	}
	
	public boolean isProcessable() {
		return false;
	}
	
	public List<Instruction> getSequence() {
		List<Instruction> copy = new ArrayList<Instruction>(sequence);
		return Collections.unmodifiableList(copy);
	}
	
	public Sequencer clear() {
		sequence.clear();
		// TODO clear section stack
		return this;
	}
}
