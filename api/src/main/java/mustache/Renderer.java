package mustache;

import java.io.IOException;

import mustache.core.Instruction;
import mustache.core.Processor;
import mustache.core.SectionStack;

import org.apache.commons.lang.StringEscapeUtils;

public class Renderer {

	public static void render(Processor processor, Object data, Appendable appendable) throws IOException {
		if (processor == null | appendable == null) {
			throw new NullPointerException();
		}
		new Renderer(processor, data, appendable).render();
	}
	
	private final Processor processor;
	private final SectionStack sectionStack;
	private final Appendable appendable;
	
	private Renderer(Processor processor, Object data, Appendable appendable) {
		this.processor = processor;
		sectionStack = new SectionStack(data);
		this.appendable = appendable;
	}

	private void render() throws IOException {
		processor.reset();
		
		while ( processor.hasNext() ) {
			Instruction instruction = processor.next();
			
			switch ( instruction.getAction() ) {
			case APPEND_TEXT:
				appendable.append( instruction.getData() );
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

	private void appendVariable(String interpolation) throws IOException {
		String value = sectionStack.getValue(interpolation);
		appendable.append( StringEscapeUtils.escapeHtml(value) );
	}

	private void appendUnescapedVariable(String interpolation) throws IOException {
		appendable.append( sectionStack.getValue(interpolation) );
	}

	private void openSection(String interpolation) {
		if ( sectionStack.openSection(interpolation) ) {
			processor.enterSection();
		}
	}

	private void openInvertedSection(String interpolation) {
		if ( sectionStack.openInvertedSection(interpolation) ) {
			processor.enterSection();
		}
	}

	private void closeSection(String interpolation) {
		if ( sectionStack.closeSection() ) {
			processor.exitSection();
		}
	}
}