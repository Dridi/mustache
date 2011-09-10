package mustache;

import java.io.IOException;
import java.util.regex.Pattern;

import mustache.core.AppendText;
import mustache.core.Processor;
import mustache.rendering.SectionStack;

import org.apache.commons.lang.StringEscapeUtils;

public class Renderer {
	private static final Pattern INDENT_PARTIAL_TEXT = Pattern.compile("\\r\\n|\\r|\\n(?!$)");

	public static void render(Processor processor, Object data, Appendable appendable) throws IOException {
		if (processor == null | appendable == null) {
			throw new NullPointerException();
		}
		new Renderer(processor, data, appendable).render();
	}
	
	private final Processor processor;
	private final SectionStack sectionStack;
	private final Appendable appendable;
	
	private boolean endOfLine;
	
	private Renderer(Processor processor, Object data, Appendable appendable) {
		this.processor = processor;
		sectionStack = new SectionStack(data);
		this.appendable = appendable;
	}

	private void render() throws IOException {
		processor.reset();
		
		while ( processor.hasNext() ) {
			AppendText instruction = processor.next();
			
			switch ( instruction.getAction() ) {
			case APPEND_TEXT:
				appendText(instruction.getData(), instruction);
				break;
			case APPEND_VARIABLE:
				appendVariable(instruction);
				break;
			case APPEND_UNESCAPED_VARIABLE:
				appendUnescapedVariable(instruction);
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
			
			endOfLine = instruction.isEndOfLine();
		}
	}

	private void appendText(String text, AppendText instruction) throws IOException {
		String string = text;
		if ( instruction.isIndented() ) {
			string = INDENT_PARTIAL_TEXT.matcher(string).replaceAll("$0" + instruction.getIndentation());
		}
		appendable.append(string);
	}

	private void appendVariable(AppendText instruction) throws IOException {
		String value = sectionStack.getValue( instruction.getData() );
		appendVariableIndentation(instruction);
		appendable.append( StringEscapeUtils.escapeHtml(value) );
	}

	private void appendUnescapedVariable(AppendText instruction) throws IOException {
		String value = sectionStack.getValue( instruction.getData() );
		appendVariableIndentation(instruction);
		appendable.append(value);
	}

	private void appendVariableIndentation(AppendText instruction) throws IOException {
		if ( endOfLine & instruction.isIndented() ) {
			appendable.append( instruction.getIndentation() );
		}
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
		if ( sectionStack.closeSection(interpolation) ) {
			processor.exitSection();
		}
	}
}