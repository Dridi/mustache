package mustache.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import mustache.core.Instruction;
import mustache.core.Processor;
import mustache.core.SequenceException;
import mustache.core.Sequencer;

import org.apache.commons.io.IOUtils;

public class Parser {
	
	public static Processor parseReadable(Readable readable, PartialLoader partialLoader) throws ParseException, IOException {
		return new Parser(readable, partialLoader).parse();
	}
	
	public static Processor parseString(String string, PartialLoader partialLoader) throws ParseException, IOException {
		Reader reader = new StringReader(string);
		return parseReadable(reader, partialLoader);
	}
	
	public static Processor parseFile(File file, PartialLoader partialLoader) throws ParseException, IOException {
		Reader reader = null;
		try {
			reader = new FileReader(file);
			return parseReadable(reader, partialLoader);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	public static Processor parseFile(String path, PartialLoader partialLoader) throws ParseException, IOException {
		return parseFile(new File(path), partialLoader);
	}
	
	private final LineIterator reader;
	private final PartialLoader partialLoader;
	private final Delimiter delimiter = new Delimiter();
	private final Sequencer sequencer = new Sequencer();
	private final Map<String, Processor> partials;
	
	private Parser(Readable readable, PartialLoader partialLoader) {
		this.reader = LineIterator.fromReadable(readable);
		this.partialLoader = partialLoader;
		this.partials = new HashMap<String, Processor>();
	}
	
	private Processor parse() throws ParseException, IOException {
		try {
			int lineNumber = 1;
			while (reader.hasNext()) {
				parseLine(reader.next(), lineNumber);
				lineNumber++;
			}
			addLastToken();
			if ( !sequencer.isProcessable() ) {
				throw new ParseException("Invalid template");
			}
			return Processor.newInstance(sequencer, partials);
		}
		catch (SequenceException e) {
			throw new ParseException(e.getMessage(), e);
		}
	}

	private void addLastToken() throws ParseException, SequenceException {
		if (currentText.length() == 0) {
			return;
		}
		if (insideTag || delimiter.isInsideTag()) {
			throw new ParseException("Unterminated tag");
		}
		appendCurrentText();
	}
	
	private StringBuilder currentText = new StringBuilder();
	private boolean insideTag = false;
	
	private void parseLine(String line, int lineNumber) throws SequenceException, ParseException, IOException {
		int position = 0;
		
		while (position < line.length()) {
			int start = position;
			position = delimiter.parse(line, position);
			
			if (insideTag && !delimiter.isInsideTag()) {
				addInstruction();
			}
			else if (!insideTag && delimiter.isInsideTag()) {
				updateCurrentText(line, start, position);
				position += delimiter.tagStartLength();
			}
			else if (!insideTag && !delimiter.isInsideTag()) {
				updateCurrentText(line, start, position);
			}
			
			insideTag = delimiter.isInsideTag();
		}
	}

	private void addInstruction() throws ParseException, SequenceException, IOException {
		Instruction instruction = delimiter.getInstruction();
		appendCurrentText();
		if (instruction != null) {
			sequencer.add(instruction);
			loadPartial(instruction);
		}
	}

	public void loadPartial(Instruction instruction) throws ParseException, IOException {
		if (instruction.getAction() != Instruction.Action.ENTER_PARTIAL) {
			return;
		}
		
		String partial = instruction.getPartial();
		if (partialLoader == null) {
			throw new IllegalStateException("Templates expects to load a partial : " + partial);
		}
		if ( partials.containsKey(partial) ) {
			return;
		}
		partials.put(partial, partialLoader.loadPartial(partial) );
	}

	private void appendCurrentText() throws SequenceException {
		// appends the blanks ommited in updateCurrentText() if needed
		currentText.append( delimiter.getTextTrailingBlanks() );
		if (currentText.length() > 0) {
			sequencer.add(Instruction.Action.APPEND_TEXT, currentText.toString());
		}
		currentText = new StringBuilder();
	}

	private void updateCurrentText(String line, int start, int position) {
		String textBefore = line.substring(start, position);
		// do not update with blanks at the begining of the line,
		// potentially opening a standalone tag !! see appendCurrentText()
		if (start != 0 || textBefore.trim().length() > 0) {
			currentText.append(textBefore);
		}
	}
}