package mustache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SectionStack {
	
	private static final String VARIABLE_REGEX = "^\\s*(\\.|([a-z_][a-z0-9_]*(\\.[a-z_][a-z0-9_]*)*))\\s*$";

	private static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX, Pattern.CASE_INSENSITIVE);
	
	List<Section> sections = new ArrayList<Section>();
	
	SectionStack(Object context) {
		sections.add(Section.newRootSection(context));
	}
	
	Section section() {
		return sections.get(0);
	}
	
	void push(Tag tag, boolean rendered) {
		push(tag, rendered, null);
	}
	
	void push(Tag tag, boolean rendered, List<Object> contexts) {
		sections.add(0, Section.newNestedSection(tag, rendered, contexts));
	}
	
	int size() {
		return sections.size();
	}
	
	Object getVariable(Tag tag) throws ParseException {
		String variableName = getVariableName(tag);
		
		if (".".equals(variableName)) {
			Object context = section().context();
			return context != null ? context : "";
		}
		
		String[] parts = variableName.split("\\.");
		String baseName = parts[0];
		
		for (Section section : sections) {
			if (section.hasVariable(baseName)) {
				return section.interpolateVariable(parts);
			}
		}
		
		return "";
	}
	
	void openSection(Tag tag) throws ParseException {
		Object value = getVariable(tag);
		
		if (!section().isRendered() || "".equals(value)) {
			push(tag, false);
		}
		else if (value instanceof Boolean) {
			Boolean bool = (Boolean) value;
			push(tag, bool.booleanValue(), Arrays.asList(section().context()));
		}
		else if (value.getClass().isArray()) {
			Object[] values = (Object[]) value;
			push(tag, values.length > 0, Arrays.asList(values));
		}
		else if (value instanceof Map) {
			push(tag, true, Arrays.asList(value));
		}
		else if (value instanceof Collection) {
			Collection<?> values = (Collection<?>) value;
			push(tag, values.size() > 0, new ArrayList<Object>(values));
		}
		else {
			push(tag, true, Arrays.asList(value));
		}
	}
	
	void openInvertedSection(Tag tag) throws ParseException {
		Object value = getVariable(tag);
		
		if (section().isRendered()) {
			push(tag, isEmpty(value), Arrays.asList(section().context()));
		}
		else {
			push(tag, false);
		}
	}
	
	private String getVariableName(Tag tag) throws ParseException {
		Matcher matcher = VARIABLE_PATTERN.matcher(tag.getContent());
		
		if (!matcher.matches()) {
			throw ParseException.fromTag("Illegal variable name : " + tag.getContent(), tag);
		}
		
		return matcher.group(1);
	}
	
	private boolean isEmpty(Object value) {
		
		if (value == null) {
			return true;
		}
		
		if (value instanceof Boolean) {
			return !((Boolean) value);
		}
		
		if (value instanceof String) {
			return "".equals(value);
		}
		
		if (value.getClass().isArray()) {
			return ((Object[]) value).length == 0;
		}
		
		if (value instanceof Collection) {
			return ((Collection<?>) value).size() == 0;
		}
		
		return false;
	}
	
	Position closeSection() {
		Section section = section();
		
		if (section.size() > 1) {
			section.popContext();
			return section.getPosition();
		}
		
		sections.remove(0);
		return null;
	}
	
}