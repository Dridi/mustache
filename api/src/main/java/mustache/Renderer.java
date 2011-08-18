package mustache;

import java.io.IOException;
import java.io.Writer;

import mustache.core.Instruction;
import mustache.core.Processor;
import mustache.core.SectionStack;

public class Renderer {

	public static void render(Processor processor, Object data, Writer writer) throws IOException {
		if (processor == null | writer == null) {
			throw new NullPointerException();
		}
		new Renderer(processor, data, writer).render();
	}
	
	private final Processor processor;
	private final SectionStack sectionStack;
	private final Writer writer;
	
	private Renderer(Processor processor, Object data, Writer writer) {
		this.processor = processor;
		sectionStack = new SectionStack(data);
		this.writer = writer;
	}

	private void render() throws IOException {
		processor.reset();
		
		while ( processor.hasNext() ) {
			Instruction instruction = processor.next();
			
			switch ( instruction.getAction() ) {
			case APPEND_TEXT:
				writer.append( instruction.getData() );
				break;
			case APPEND_VARIABLE:
				appendVariable( instruction.getData() );
				break;
			case APPEND_UNESCAPED_VARIABLE:
				appendUnescapedVariable( instruction.getData() );
				break;
			case OPEN_SECTION:
				openSection( instruction.getData() );
				break;
			case OPEN_INVERTED_SECTION:
				openInvertedSection( instruction.getData() );
				break;
			case CLOSE_SECTION:
				closeSection( instruction.getData() );
				break;
			}
		}
	}

	private void appendVariable(String interpolation) {
		// TODO Auto-generated method stub
		
	}

	private void appendUnescapedVariable(String interpolation) {
		// TODO Auto-generated method stub
		
	}

	private void openSection(String interpolation) {
		// TODO Auto-generated method stub
		
	}

	private void openInvertedSection(String interpolation) {
		// TODO Auto-generated method stub
		
	}

	private void closeSection(String interpolation) {
		if (sectionStack.closeSection()) {
			processor.exitSection();
		}
	}
	
}