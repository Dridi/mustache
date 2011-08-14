package mustache.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import mustache.core.Instruction;
import mustache.core.Instruction.Type;
import mustache.core.SequenceException;
import mustache.core.Sequencer;

import org.apache.commons.io.IOUtils;

public class Parser {
	
	public static Sequencer parseString(String string) throws ParseException, IOException {
		Reader reader = new StringReader(string);
		return new Parser(reader).parse().sequencer;
	}
	
	public static Sequencer parseFile(File file) throws ParseException, IOException {
		Reader reader = null;
		try {
			reader = new FileReader(file);
			return new Parser(reader).parse().sequencer;
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	public static Sequencer parseFile(String path) throws ParseException, IOException {
		return parseFile( new File(path) );
	}
	
	private final LineReader reader;
	private final Delimiter delimiter = new Delimiter();
	private final Sequencer sequencer = new Sequencer();
	
	private Parser(Readable readable) {
		this.reader = new LineReader(readable);
	}
	
	private Parser parse() throws ParseException, IOException {
		try {
			int lineNumber = 1;
			while (reader.hasNext()) {
				parseLine(reader.next(), lineNumber);
				lineNumber++;
			}
			addLastToken();
			return this;
		}
		catch (SequenceException e) {
			throw new ParseException(e.getMessage(), e);
		}
	}

	private void addLastToken() throws ParseException, SequenceException {
		if (currentToken.length() == 0) {
			return;
		}
		if (insideTag || insideUnescapedTag) {
			throw new ParseException("Unterminated tag " + currentToken);
		}
		addInstruction(Instruction.Type.APPEND_TEXT);
	}
	
	private StringBuilder currentToken = new StringBuilder();
	private boolean insideTag = false;
	private boolean insideUnescapedTag = false;
	
	private void parseLine(String line, int lineNumber) throws SequenceException {
		int position = 0;
		
		while (position < line.length()) {
			if (insideTag) {
				int tagPosition = line.indexOf(delimiter.getStop(), position);
				
				if (tagPosition >= 0) {
					currentToken.append( line.substring(position, tagPosition) );
					addInstruction(Instruction.Type.APPEND_TEXT);
					position = tagPosition + delimiter.getStop().length();
					insideTag = false;
					continue;
				}
			}
			else if (insideUnescapedTag ) {
				int unescapedTagPosition = line.indexOf(Delimiter.UNESCAPED_STOP, position);
				
				if (unescapedTagPosition >= 0) {
					currentToken.append( line.substring(position, unescapedTagPosition) );
					addInstruction(Instruction.Type.APPEND_TEXT);
					position = unescapedTagPosition + Delimiter.UNESCAPED_STOP.length();
					insideUnescapedTag = false;
					continue;
				}
			}
			else {
				int tagPosition = line.indexOf(delimiter.getStart(), position);
				int unescapedTagPosition = line.indexOf(Delimiter.UNESCAPED_START, position);
				
				insideTag = tagPosition >= 0;
				insideUnescapedTag = unescapedTagPosition >= 0;
				
				if (insideTag || insideUnescapedTag) {
					currentToken.append( line.substring(position, tagPosition) );
					addInstruction(Instruction.Type.APPEND_TEXT);
					position = tagPosition;
					continue;
				}
			}
			
			currentToken.append( line.substring(position) );
			position = line.length();
		}
	}

	private void addInstruction(Type appendText) throws SequenceException {
		sequencer.add(appendText, currentToken.toString());
		System.out.println("instruction added : " + currentToken);
		currentToken = new StringBuilder();
	}
}