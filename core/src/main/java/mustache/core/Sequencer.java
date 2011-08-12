package mustache.core;

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
		return Collections.unmodifiableList(sequence);
	}
}
