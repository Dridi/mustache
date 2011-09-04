package mustache.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@code Sequencer} class manipulates a sequence of {@link Processable}s
 * and ensures the coherence of the section stack. It prevents from opening
 * sections and not closing them in the <i>LIFO</i> order in a fluent API
 * fashion.
 * 
 * <p>This class is not meant to be manipulated concurrently by several threads.</p>
 * 
 * @author Dri
 * @see Processable
 * @see Processor
 */
public final class Sequencer {
	
	private Deque<String> sections = new ArrayDeque<String>();
	private List<Processable> sequence = new ArrayList<Processable>();
	private Set<String> partials = new HashSet<String>();
	
	/**
	 * Adds a legal {@link Processable} in the sequence.
	 * @param processable the {@link Processable} to add
	 * @return this {@code Sequencer} object
	 * @throws SequenceException for a misplaced close {@link Processable}
	 * @throws NullPointerException if {@code processable} is {@code null}
	 */
	public Sequencer add(Processable processable) throws SequenceException {
		if (processable == null) {
			throw new NullPointerException();
		}
		if (processable instanceof Instruction) {
			updateSectionStack((Instruction) processable);
		}
		sequence.add(processable);
		return this;
	}
	
	public Sequencer add(Instruction.Action action, String data) throws SequenceException {
		return add( Instruction.newInstance(action, data) );
	}
	
	private void updateSectionStack(Instruction instruction) throws SequenceException {
		if ( instruction.opening() ) {
			sections.push( instruction.getData() );
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
			sections.pop();
		}
		else {
			throw new SequenceException("Expected to close " + current + " not " + section);
		}
	}
	
	/**
	 * Adds legal {@link Processable}s in the sequence.
	 * @param processables the {@link Processable}s to add
	 * @return this {@code Sequencer} object
	 * @throws SequenceException for misplaced close {@link Processable}s
	 * @throws NullPointerException if {@code instruction}s is {@code null} or contains {@code null} items.
	 */
	public Sequencer addAll(List<Processable> processables) throws SequenceException {
		if (processables == null) {
			throw new NullPointerException();
		}
		
		Sequencer proxy = new Sequencer();
		proxy.sections.addAll(this.sections);
		proxy.sequence.addAll(this.sequence);
		proxy.partials.addAll(this.partials);
		
		for (Processable processable : processables) {
			proxy.add(processable);
		}
		
		this.sections = proxy.sections;
		this.sequence = proxy.sequence;
		this.partials = proxy.partials;
		
		return this;
	}
	
	/**
	 * Indicates whether the sequence is processable in its current state. To be
	 * processable, a sequence needs at least one processable and no currently
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
	public List<Processable> getSequence() {
		List<Processable> copy = new ArrayList<Processable>(sequence);
		return Collections.unmodifiableList(copy);
	}
	
	/**
	 * Removes all {@link Processable}s from this sequencer.
	 * @return this {@code Sequencer} object
	 */
	public Sequencer clear() {
		sequence.clear();
		sections.clear();
		return this;
	}
}