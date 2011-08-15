package mustache;

import mustache.core.Context;

import org.apache.commons.lang.StringEscapeUtils;

@Deprecated
public class Parser {
	
	private final Stream stream;
	
	private final SectionStack sectionStack;
	
	private final PartialLoader partialLoader;
	
	private final StringBuilder output;
	
	private boolean rendering = false;
	
	public Parser(String input, Context context, PartialLoader partialLoader) {
		this.stream = new Stream(input);
		this.sectionStack = new SectionStack(context);
		this.partialLoader = partialLoader;
		this.output = new StringBuilder();
	}
	
	public String merge() throws ParseException {
		checkRendering();
		
		while (stream.hasNext()) {
			renderTag();
		}
		
		if (sectionStack.size() > 1) {
			String message = "Section not closed : " + sectionStack.section();
			throw ParseException.fromPosition(message, stream.getPosition());
		}
		
		return output.toString();
	}
	
	private void checkRendering() {
		if (rendering) {
			throw new IllegalStateException("Rendering already started once.");
		}
		
		rendering = true;
	}
	
	private void appendString(Object object) {
		if (sectionStack.section().isRendered()) {
			output.append(object);
		}
	}
	
	private void renderTag() throws ParseException {
		Tag tag = stream.getNextTag();
		
		appendString(stream.readToTag(tag));
		
		if (tag == null) {
			return;
		}
		
		switch (tag.getType()) {
		case VARIABLE:
			appendString(StringEscapeUtils.escapeHtml( sectionStack.getVariable(tag).toString() ));
			break;
		case UNESCAPED_VARIABLE:
			appendString(sectionStack.getVariable(tag));
			break;
		case SECTION:
			sectionStack.openSection(tag);
			break;
		case INVERTED_SECTION:
			sectionStack.openInvertedSection(tag);
			break;
		case SECTION_END:
			closeSection(tag);
			break;
		case PARTIAL:
			renderPartial(tag);
			break;
		case DELIMITER:
			changeDelimiter(tag);
			break;
		case COMMENT:
			break;
		default:
			throw new UnsupportedOperationException("Unknown Tag type : " + tag.getType().name());
		}
	}
	
	private void closeSection(Tag tag) throws ParseException {
		String sectionName = tag.getContent();
		Position position = sectionStack.closeSection(sectionName);
		stream.rewind(position);
	}
	
	private void renderPartial(Tag tag) throws ParseException {
		if (sectionStack.section().isRendered()) {
			String partial = loadPartial(tag);
			
			Parser parser = new Parser(partial, sectionStack.section().currentContext(), partialLoader);
			appendString(parser.merge());
		}
	}
	
	private String loadPartial(Tag tag) {
		if (partialLoader == null) {
			throw new IllegalStateException("Null partial loader.");
		}
		
		String partial = partialLoader.loadPartial(tag.getContent().trim());
		
		return partial.replaceAll("^|\\n(?!$)", "$0" + tag.getIndentation());
	}
	
	private void changeDelimiter(Tag tag) throws ParseException {
		if ( !sectionStack.section().isRoot() ) {
			String message = "Unexpected delimiter change within a section : " + sectionStack.section();
			throw ParseException.fromTag(message, tag);
		}
		
		stream.changeDelimiter(tag);
	}
}