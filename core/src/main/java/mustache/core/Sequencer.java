package mustache.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
	private Deque<Integer> sectionsIndices = new ArrayDeque<Integer>();
	private List<Instruction> sequence = new ArrayList<Instruction>();
	private Set<String> partials = new HashSet<String>();
	
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
		if (instruction instanceof OpenSection) {
			pushSection((OpenSection) instruction);
		}
		if (instruction instanceof CloseSection) {
			sequence.add(popSection((CloseSection) instruction));
		} else {
			sequence.add(instruction);
		}
		return this;
	}
	
	// todo appendText, appendVariable, openSection, closeSection, enterPartial

	private void pushSection(OpenSection instruction) {
		String name = instruction.getName();
		sections.push(name);
		sectionsIndices.push( sequence.size() );
	}

	private CloseSection popSection(CloseSection instruction) throws SequenceException {
		String name = instruction.getName();
		
		if ( sections.isEmpty() ) {
			throw new SequenceException("No section currently open, cannot close " + name);
		}
		
		String current = sections.peek();
		
		if ( !current.equals(name) ) {
			throw new SequenceException("Expected to close " + current + " not " + name);
		}
		
		replaceOpenSection();
		sections.pop();
		return instruction.setOpenIndex( sectionsIndices.pop() );
	}
	
	private void replaceOpenSection() {
		int index = sectionsIndices.peek();
		OpenSection openSection = ((OpenSection) sequence.get(index));
		openSection = openSection.setCloseIndex( sequence.size() );
		sequence.set(index, openSection);
	}

	/**
	 * Adds legal {@link Instruction}s in the sequence.
	 * @param processables the {@link Instruction}s to add
	 * @return this {@code Sequencer} object
	 * @throws SequenceException for misplaced close {@link Instruction}s
	 * @throws NullPointerException if {@code instruction}s is {@code null} or contains {@code null} items.
	 */
	public Sequencer addAll(List<Instruction> processables) throws SequenceException {
		if (processables == null) {
			throw new NullPointerException();
		}
		
		Sequencer proxy = new Sequencer();
		proxy.sequence.addAll(this.sequence);
		proxy.partials.addAll(this.partials);
		proxy.sections.addAll(this.sections);
		proxy.sectionsIndices.addAll(this.sectionsIndices);
		
		for (Instruction instruction : processables) {
			proxy.add(instruction);
		}
		
		this.sequence = proxy.sequence;
		this.partials = proxy.partials;
		this.sections = proxy.sections;
		this.sectionsIndices = proxy.sectionsIndices;
		
		return this;
	}
	
	/**
	 * Indicates whether the sequence is instruction in its current state. To be
	 * instruction, a sequence needs at least one instruction and no currently
	 * open sections.
	 * 
	 * @return {@code true} if the sequence is processable
	 * @see Processor#fromSequencer(Sequencer)
	 */
	public boolean isProcessable() {
		return sequence.size() > 0 && sections.size() == 0;
	}

	public List<String> getPartials() {
		return new ArrayList<String>(partials);
	}
	
	/**
	 * Returns an unmodifiable copy of the current sequence.
	 * @return the current sequence of processables
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
		sectionsIndices.clear();
		return this;
	}
}