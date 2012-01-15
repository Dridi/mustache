package mustache;

import java.io.IOException;
import java.util.regex.Pattern;

import mustache.core.AppendText;
import mustache.core.AppendVariable;
import mustache.core.CloseSection;
import mustache.core.Instruction;
import mustache.core.OpenSection;
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
	
	private String previousIndentation;
	
	private Renderer(Processor processor, Object data, Appendable appendable) {
		this.processor = processor;
		sectionStack = new SectionStack(data);
		this.appendable = appendable;
	}

	private void render() throws IOException {
		processor.reset();
		
		while ( processor.hasNext() ) {
			Instruction instruction = processor.next();
			
			if ( AppendText.class.isInstance(instruction) ) {
				appendText((AppendText) instruction);
			} else if ( AppendVariable.class.isInstance(instruction) ) {
				appendVariable((AppendVariable) instruction);
			} else if ( OpenSection.class.isInstance(instruction) ) {
				openSection((OpenSection) instruction);
			} else if ( CloseSection.class.isInstance(instruction) ) {
				closeSection((CloseSection) instruction);
			}
			
			saveIndentation(instruction);
		}
	}

	public void saveIndentation(Instruction instruction) {
		if ( AppendText.class.isInstance(instruction) ) {
			AppendText appendText = (AppendText) instruction;
			previousIndentation = appendText.isEndOfLine() ? appendText.getIndentation() : "";
		} else {
			previousIndentation = "";
		}
	}

	private void appendText(AppendText instruction) throws IOException {
		String text = instruction.getText();
		if ( instruction.isIndented() ) {
			text = INDENT_PARTIAL_TEXT.matcher(text).replaceAll("$0" + instruction.getIndentation());
		}
		appendable.append(text);
	}

	private void appendVariable(AppendVariable instruction) throws IOException {
		String value = sectionStack.getValue( instruction.getName() );
		if ( !instruction.isUnescaped() ) {
			value = StringEscapeUtils.escapeHtml(value);
		}
		appendable.append(previousIndentation).append(value);
	}

	private void openSection(OpenSection instruction) {
		if ( sectionStack.openSection(instruction) ) {
			processor.enterSection();
		}
	}

	private void closeSection(CloseSection instruction) {
		if ( sectionStack.closeSection(instruction) ) {
			processor.exitSection();
		}
	}
}