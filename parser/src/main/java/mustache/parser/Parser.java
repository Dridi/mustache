package mustache.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import mustache.core.Instruction;
import mustache.core.SequenceException;
import mustache.core.Sequencer;

import org.apache.commons.io.IOUtils;

public class Parser {
	
	public static Sequencer parseString(String string) throws ParseException, IOException {
		Reader reader = new StringReader(string);
		return new Parser(reader).parse();
	}
	
	public static Sequencer parseReadable(Readable string) throws ParseException, IOException {
		return new Parser(string).parse();
	}
	
	public static Sequencer parseFile(File file) throws ParseException, IOException {
		Reader reader = null;
		try {
			reader = new FileReader(file);
			return new Parser(reader).parse();
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	public static Sequencer parseFile(String path) throws ParseException, IOException {
		return parseFile( new File(path) );
	}
	
	private final LineIterator reader;
	private final Delimiter delimiter = new Delimiter();
	private final Sequencer sequencer = new Sequencer();
	
	private Parser(Readable readable) {
		reader = LineIterator.fromReadable(readable);
	}
	
	private Sequencer parse() throws ParseException, IOException {
		try {
			int lineNumber = 1;
			while (reader.hasNext()) {
				parseLine(reader.next(), lineNumber);
				lineNumber++;
			}
			addLastToken();
			return sequencer;
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
	
	private void parseLine(String line, int lineNumber) throws SequenceException, ParseException {
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

	private void addInstruction() throws ParseException, SequenceException {
		Instruction instruction = delimiter.getInstruction();
		appendCurrentText();
		
		// TODO manage partials
		
		if (instruction != null) {
			sequencer.add(instruction);
		}
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