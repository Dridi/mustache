package mustache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class Section {
	
	private Position position;
	
	private boolean rendered = true;
	
	private String name;
	
	private List<Object> contexts = new ArrayList<Object>();
	
	Object context() {
		return contexts.get(0);
	}
	
	void popContext() {
		contexts.remove(0);
	}
	
	public int size() {
		return contexts.size();
	}
	
	Object interpolateVariable(String[] parts) {
		Object value = context();
		
		for (String part : parts) {
			
			if (!hasVariable(part, value)) {
				return "";
			}
			
			value = getValue(part, value);
		}
		
		return value;
	}
	
	boolean hasVariable(String property) {
		return hasVariable(property, context());
	}
	
	private boolean hasVariable(String property, Object object) {
		
		if (object == null || object.getClass().isArray() || object instanceof Collection) {
			return false;
		}
		
		if (object instanceof Map) {
			return ((Map<?, ?>) object).containsKey(property);
		}
		
		try {
			object.getClass().getDeclaredField(property);
			return true;
		}
		catch (NoSuchFieldException e) {
			return false;
		}
	}
	
	private Object getValue(String property, Object object) {
		
		if (object instanceof Map) {
			return ((Map<?, ?>) object).get(property);
		}
		
		try {
			Field field = object.getClass().getDeclaredField(property);
			field.setAccessible(true);
			return field.get(object);
		}
		catch (NoSuchFieldException e) {
			return false;
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
	static Section newRootSection(Object context) {
		Section section = new Section();
		section.contexts.add(context);
		return section;
	}
	
	static Section newNestedSection(Tag tag, boolean rendered, List<Object> contexts) {
		Section section = new Section();
		
		section.rendered = rendered;
		section.position = tag.forward();
		section.name = tag.getContent();
		
		if (rendered && contexts != null) {
			section.contexts.addAll(contexts);
		}
		else {
			section.contexts.add(null);
		}
		
		return section;
	}
	
	Position getPosition() {
		return position;
	}
	
	boolean isRendered() {
		return rendered;
	}
	
	String getName() {
		return name;
	}
}