package mustache.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import mustache.core.AppendText;
import mustache.core.EnterPartial;
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
			while (reader.hasNext()) {
				parseLine(reader.next());
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
	
	private void parseLine(String line) throws SequenceException, ParseException, IOException {
		int position = 0;
		
		while (position < line.length()) {
			int start = position;
			position = delimiter.parse(line, position);
			
			if (insideTag && !delimiter.isInsideTag()) {
				addProcessable();
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

	private void addProcessable() throws ParseException, SequenceException, IOException {
		Instruction processable = delimiter.getProcessable();
		appendCurrentText();
		if (processable != null) {
			sequencer.add(processable);
		}
		if (processable instanceof EnterPartial) {
			loadPartial((EnterPartial) processable);
		}
	}

	public void loadPartial(EnterPartial partial) throws ParseException, IOException {
		String name = partial.getName();
		if (partialLoader == null) {
			throw new IllegalStateException("Templates expects to load a partial : " + partial);
		}
		if ( partials.containsKey(name) ) {
			return;
		}
		partials.put(name, null);
		Readable readable = partialLoader.loadPartial(name);
		Parser parser = new Parser(readable, partialLoader);
		parser.partials.putAll(partials);
        partials.put(name, parser.parse());
	}

	private void appendCurrentText() throws SequenceException {
		// appends the blanks ommited in updateCurrentText() if needed
		currentText.append( delimiter.getTextTrailingBlanks() );
		if (currentText.length() > 0) {
			sequencer.add(AppendText.Action.APPEND_TEXT, currentText.toString());
		}
		currentText = new StringBuilder();
	}

	private void updateCurrentText(String line, int start, int position) {
		if (start == 0 && position == line.length()) {
			currentText.append(line);
			return;
		}
		String textBefore = line.substring(start, position);
		int textBeforeTrimmedLength = textBefore.trim().length();
		// do not update with blanks at the bounds of the line,
		// potentially opening a standalone tag !! see appendCurrentText()
		if (start != 0 || textBeforeTrimmedLength > 0) {
			currentText.append(textBefore);
		}
		else if (position < line.length()  && textBeforeTrimmedLength > 0) {
			currentText.append(textBefore);
		}
	}
}