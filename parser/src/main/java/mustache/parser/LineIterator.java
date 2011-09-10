package mustache.parser;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class LineIterator {
	
	private final Readable readable;
	protected final char[] buffer = new char[4096];
	private final CharBuffer charBuffer = CharBuffer.wrap(buffer);
	
	private StringBuilder currentLine = new StringBuilder();
	private Queue<String> lines = new LinkedList<String>();
	private boolean continueFromCarriageReturn = false;
	private boolean finished = false;
	
	private LineIterator(Readable readable) {
		this.readable = readable;
	}
	
	public static LineIterator fromReadable(Readable readable) {
		if (readable == null) {
			throw new NullPointerException();
		}
		if ( Reader.class.isInstance(readable) ) {
			return new LineReader((Reader) readable);
		}
		return new LineIterator(readable);
	}
	
	public boolean hasNext() {
		return !finished || !lines.isEmpty();
	}
	
	public String next() throws IOException {
		if ( !hasNext() ) {
			throw new IllegalStateException();
		}
		while ( lines.isEmpty() ) {
			readLines();
		}
		return lines.poll();
	}
	
	private void readLines() throws IOException {
		int size = readChars();
		
		if (size < 0) {
			finished = true;
			lines.add( currentLine.toString() );
			return;
		}
		
		addLines(size);
	}

	protected int readChars() throws IOException {
		charBuffer.clear();
		return readable.read(charBuffer);
	}

	private void addLines(int size) {
		int start = 0;
		int position = 0;
		
		while (position < size) {
			if (buffer[position] == '\n') {
				start = addLine(start, position + 1);
				continueFromCarriageReturn = false;
			}
			else if (continueFromCarriageReturn) {
				start = addLine(start, position);
				continueFromCarriageReturn = false;
				continue;
			}
			else if (buffer[position] == '\r') {
				continueFromCarriageReturn = true;
			}
			position++;
		}
		
		currentLine.append(buffer, start, position - start);
	}

	private int addLine(int start, int position) {
		currentLine.append(buffer, start, position - start);
		lines.add( currentLine.toString() );
		currentLine = new StringBuilder();
		return position;
	}
	
	private static class LineReader extends LineIterator {
		private Reader reader;
		
		private LineReader(Reader reader) {
			super(reader);
			this.reader = reader;
		}
		
		@Override
		protected int readChars() throws IOException {
			return reader.read(buffer, 0, buffer.length);
		}
	}
}