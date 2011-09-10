package mustache.core;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The {@code Processor} class iterates through a sequence of {@link Instruction}s
 * and needs to be notified to enter or exit sections. {@code Processor}s can be
 * serialized and reused at will but their {@link Instruction}s sequence can not
 * be modified.
 * 
 * <p>This class is not meant for concurrent manipulation by several threads.</p>
 * 
 * TODO add a partial counter to detect infinite recursive partial nesting
 * 
 * @author Dri
 */
public final class Processor implements Serializable, Iterator<Instruction> {
	private static final long serialVersionUID = 289040399110456725L;

	private final List<Instruction> sequence;
	private final Map<String, Processor> partials;
	
	private final transient int maxPosition;
	private transient int currentPosition = -1;
	private transient boolean tryOpeningSection;
	private transient boolean tryClosingSection;
	private transient Processor currentPartial;
	private transient String indentation = "";
	
	private Processor(List<Instruction> sequence) {
		this.sequence = sequence;
		this.maxPosition = sequence.size() - 1;
		this.partials = new HashMap<String, Processor>();
	}
	
	/**
	 * Creates a {@code Processor} from a instruction {@link Sequencer}.
	 * @param sequencer the {@link Sequencer}
	 * @return a newly created {@code Processor}
	 * @throws NullPointerException if {@code sequencer} is {@code null}
	 * @throws IllegalArgumentException if {@code sequencer} is not instruction
	 * @see Sequencer#isProcessable()
	 */
	public static Processor fromSequencer(Sequencer sequencer) {
		Map<String, Processor> partials = Collections.emptyMap();
		return newInstance(sequencer, partials);
	}

	public static Processor newInstance(Sequencer sequencer, Map<String, Processor> partials) {
		if (sequencer == null || partials == null) {
			throw new NullPointerException();
		}
		synchronized (sequencer) {
			if ( !sequencer.isProcessable() ) {
				throw new IllegalArgumentException("Sequence not instruction");
			}
			// TODO match partials map against sequencer partials list
			Processor processor = new Processor(sequencer.getSequence());
			processor.partials.putAll(partials);
			return processor;
		}
	}
	
	/**
	 * Resets the {@code Processor} to its initial state.
	 */
	public void reset() {
		currentPosition = -1;
		tryOpeningSection = false;
		tryClosingSection = false;
		currentPartial = null;
	}
	
	/**
	 * Notifies the {@code Processor} to enter the section.
	 * @throws IllegalStateException if the current {@link AppendText} is not opening
	 * @see AppendText#opening()
	 */
	public void enterSection() {
		if (currentPartial != null) {
			currentPartial.enterSection();
			return;
		}
		if (tryOpeningSection == false) {
			throw new IllegalStateException("Unexpected attempt to enter a section.");
		}
		tryOpeningSection = false;
	}

	/**
	 * Notifies the {@code Processor} to exit the section.
	 * @throws IllegalStateException if the current {@link AppendText} is not closing
	 * @see AppendText#closing()
	 */
	public void exitSection() {
		if (currentPartial != null) {
			currentPartial.exitSection();
			return;
		}
		if (tryClosingSection == false) {
			throw new IllegalStateException("Unexpected attempt to exit a section.");
		}
		tryClosingSection = false;
	}
	
	/**
	 * Indicates whether there is still {@code Instruction}s to process.
	 */
	@Override
	public boolean hasNext() {
		return currentPosition < maxPosition || (currentPartial != null && currentPartial.hasNext());
	}

	/**
	 * Returns the next {@code Instruction}s to process.
	 * @throws IllegalStateException if there is no next Instruction
	 * @see #enterSection()
	 * @see #exitSection()
	 */
	@Override
	public Instruction next() {
		if ( !hasNext() ) {
			throw new IllegalStateException();
		}
		
		if (currentPartial != null && currentPartial.hasNext()) {
			return currentPartial.next();
		}
		else {
			currentPartial = null;
		}
		
		if (tryOpeningSection) {
			skipSection();
		}
		else if (tryClosingSection) {
			reenterSection();
		}
		
		currentPosition++;
		return nextInstruction();
	}

	private Instruction nextInstruction() {
		Instruction instruction = sequence.get(currentPosition);
		if (instruction instanceof EnterPartial) {
			EnterPartial enterPartial = (EnterPartial) instruction;
			initSafePartial(enterPartial);
			return new AppendText( enterPartial.getIndentation() );
		}
		if (instruction instanceof AppendText) {
			return ((AppendText) instruction).indent(indentation);
		}
		tryOpeningSection = instruction instanceof OpenSection;
		tryClosingSection = instruction instanceof CloseSection;
		return instruction;
	}

	private void initSafePartial(EnterPartial partial) {
		Processor processor = partials.get( partial.getName() );
		if (processor == null) {
		    processor = this; // FIXME better management of recursive partials
		}
		currentPartial = new Processor(processor.sequence);
		currentPartial.partials.putAll(processor.partials);
		currentPartial.indentation = indentation + partial.getIndentation();
	}

	private void reenterSection() {
		int openedSections = -1;
		
		while (openedSections < 0) {
			currentPosition--;
			openedSections += openCloseDelta();
		}
		
		tryClosingSection = false;
	}

	private void skipSection() {
		int openedSections = 1;
		
		while (openedSections > 0) {
			currentPosition++;
			openedSections += openCloseDelta();
		}
		
		if (currentPosition > maxPosition) {
			currentPosition = maxPosition;
		}
		
		tryOpeningSection = false;
	}

	private int openCloseDelta() {
		Instruction instruction = sequence.get(currentPosition);
		return instruction instanceof OpenSection ? 1 : instruction instanceof CloseSection ? -1 : 0;
	}
	
	/**
	 * Cannot remove {@code Processable}s from the sequence.
	 * @throws UnsupportedOperationException
	 */
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
		private final Map<String, Processor> partials;

		SerializationProxy(Processor processor) {
			this.sequence = processor.sequence;
			this.partials = processor.partials;
		}
		
		private Object readResolve() throws StreamCorruptedException {
			try {
				return Processor.newInstance(new Sequencer().addAll(sequence), partials);
			} catch (SequenceException e) {
				StreamCorruptedException streamCorruptedException = new StreamCorruptedException( e.getMessage() );
				streamCorruptedException.initCause(e);
				throw streamCorruptedException;
			}
		}
	}
	
}