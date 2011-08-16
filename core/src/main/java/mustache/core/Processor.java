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

import mustache.core.Instruction.Action;

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
	private final String indentation;
	
	private final transient int maxPosition;
	private transient int currentPosition = -1;
	private transient boolean tryOpeningSection;
	private transient boolean tryClosingSection;
	private transient Processor currentPartial;
	
	private Processor(List<Instruction> sequence, String indentation) {
		this.sequence = sequence;
		this.maxPosition = sequence.size() - 1;
		this.partials = new HashMap<String, Processor>();
		this.indentation = indentation;
	}
	
	/**
	 * Creates a {@code Processor} from a processable {@link Sequencer}.
	 * @param sequencer the {@link Sequencer}
	 * @return a newly created {@code Processor}
	 * @throws NullPointerException if {@code sequencer} is {@code null}
	 * @throws IllegalArgumentException if {@code sequencer} is not processable
	 * @see Sequencer#isProcessable()
	 * FIXME this method shall disappear
	 */
	public static Processor fromSequencer(Sequencer sequencer) {
		Map<String, Processor> partials = Collections.emptyMap();
		return newInstance(sequencer, partials, "\t");
	}

	public static Processor newInstance(Sequencer sequencer, Map<String, Processor> partials, String indentation) {
		if (sequencer == null || partials == null || indentation == null) {
			throw new NullPointerException();
		}
		if (indentation.trim().length() != 0) { // FIXME should only match spaces and tabulations
			throw new IllegalArgumentException("Indentation must be blank, not : " + indentation);
		}
		synchronized (sequencer) {
			if ( !sequencer.isProcessable() ) {
				throw new IllegalArgumentException("Sequence not processable");
			}
			// TODO match partials map against sequencer partials list
			return new Processor(sequencer.getSequence(), indentation);
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
	 * @throws IllegalStateException if the current {@link Instruction} is not opening
	 * @see Instruction#opening()
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
	 * @throws IllegalStateException if the current {@link Instruction} is not closing
	 * @see Instruction#closing()
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
		return currentPosition < maxPosition;
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
		
		if (currentPartial != null) {
			return currentPartial.next();
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
		if (instruction.getAction() == Action.APPEND_TEXT) {
			instruction = indentText(instruction);
		}
		tryOpeningSection = instruction.opening();
		tryClosingSection = instruction.closing();
		// TODO manage partials
		return instruction;
	}

	private Instruction indentText(Instruction instruction) {
		if (indentation.length() == 0) {
			return instruction;
		}
		String indentedText = currentPosition == 0
				? Indentation.indentPartial(instruction.getData(), indentation)
				: Indentation.indentPartialExceptFirstLine(instruction.getData(), indentation);
		return Instruction.newInstance(Action.APPEND_TEXT, indentedText);
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
		return instruction.opening() ? 1 : instruction.closing() ? -1 : 0;
	}
	
	/**
	 * Cannot remove {@code Instruction}s from the sequence.
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
		private final String indentation;

		SerializationProxy(Processor processor) {
			this.sequence = processor.sequence;
			this.partials = processor.partials;
			this.indentation = processor.indentation;
		}
		
		private Object readResolve() throws StreamCorruptedException {
			try {
				return Processor.newInstance(new Sequencer().addAll(sequence), partials, indentation);
			} catch (SequenceException e) {
				StreamCorruptedException streamCorruptedException = new StreamCorruptedException( e.getMessage() );
				streamCorruptedException.initCause(e);
				throw streamCorruptedException;
			}
		}
	}
	
}