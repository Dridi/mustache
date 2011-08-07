package mustache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class SectionStack {
	
	List<Section> sections = new ArrayList<Section>();
	
	SectionStack(Context context) {
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
		
		String variable = getVariableName(tag);
		
		for (Section section : sections) {
			if (section.hasVariable(variable)) {
				Object context = section.interpolateVariable(variable);
				return context != null ? context : "";
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
			push(tag, bool.booleanValue(), null);
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
			push(tag, isEmpty(value), null);
		}
		else {
			push(tag, false);
		}
	}
	
	private String getVariableName(Tag tag) throws ParseException {
		
		String variableName = tag.getContent().trim();
		
		if ( !Context.isValidQuery(variableName) ) {
			throw ParseException.fromTag("Illegal variable name : " + variableName, tag);
		}
		
		return variableName;
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
	
	Position closeSection(String name) throws ParseException {
		Position position = section().close(name);
		
		if (position == null) {
			sections.remove(0);
		}
		
		return position;
	}
}