package mustache.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * The {@code Sequencer} class manipulates a sequence of {@link Instruction}s
 * and ensures the coherence of the section stack. It prevents from opening
 * sections and not closing them in the <i>LIFO</i> order in a fluent API
 * fashion.
 * 
 * <p>This class is not meant to be manipulated concurrently by several threads.</p>
 * 
 * @author Dri
 * @see Instruction
 * @see Processor
 */
public final class Sequencer {

	private Deque<String> sections = new ArrayDeque<String>();
	private List<Instruction> sequence = new ArrayList<Instruction>();
	
	/**
	 * Adds a legal {@link Instruction} in the sequence.
	 * @param instruction the {@link Instruction} to add
	 * @return this {@code Sequencer} object
	 * @throws SequenceException for a misplaced close {@link Instruction}
	 * @throws NullPointerException if {@code instruction} is {@code null}
	 */
	public Sequencer add(Instruction instruction) throws SequenceException {
		if (instruction == null) {
			throw new NullPointerException();
		}
		
		updateSectionStack(instruction);
		sequence.add(instruction);
		
		return this;
	}
	
	public Sequencer add(Instruction.Action action, String data) throws SequenceException {
		return add( Instruction.newInstance(action, data) );
	}
	
	private void updateSectionStack(Instruction instruction) throws SequenceException {
		if ( instruction.opening() ) {
			sections.addFirst( instruction.getData() );
		}
		else if( instruction.closing() ) {
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
	
	/**
	 * Adds legal {@link Instruction}s in the sequence.
	 * @param instruction the {@link Instruction}s to add
	 * @return this {@code Sequencer} object
	 * @throws SequenceException for misplaced close {@link Instruction}s
	 * @throws NullPointerException if {@code instruction}s is {@code null} or contains {@code null} items.
	 */
	public Sequencer addAll(List<Instruction> instructions) throws SequenceException {
		if (instructions == null) {
			throw new NullPointerException();
		}
		
		Sequencer proxy = new Sequencer();
		proxy.sections.addAll(this.sections);
		proxy.sequence.addAll(this.sequence);
		
		for (Instruction instruction : instructions) {
			proxy.add(instruction);
		}
		
		this.sections = proxy.sections;
		this.sequence = proxy.sequence;
		
		return this;
	}
	
	/**
	 * Indicates whether the sequence is processable in its current state. To be
	 * processable, a sequence needs at least one instruction and no currently
	 * open sections.
	 * 
	 * @return {@code true} if the sequence is processable
	 * @see Processor#fromSequencer(Sequencer)
	 */
	public boolean isProcessable() {
		return sequence.size() > 0 && sections.size() == 0;
	}
	
	/**
	 * Returns an unmodifiable copy of the current sequence.
	 * @return the current sequence of instructions
	 */
	public List<Instruction> getSequence() {
		List<Instruction> copy = new ArrayList<Instruction>(sequence);
		return Collections.unmodifiableList(copy);
	}
	
	/**
	 * Removes all {@link Instruction}s from this sequencer.
	 * @return this {@code Sequencer} object
	 */
	public Sequencer clear() {
		sequence.clear();
		sections.clear();
		return this;
	}
}