package mustache.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class Section {
	
	private final String name;
	private final Queue<Context> contexts = new LinkedList<Context>();
	
	private Section(String name) {
		this.name = name;
	}
	
	static Section rootSection(Object root) {
		Context context = Context.newInstance(root);
		Section section = new Section(null);
		section.contexts.add(context);
		return section;
	}

	boolean hasBaseVariable(String query) {
		return contexts.peek().hasBaseVariable(query);
	}

	Object getVariable(String query) {
		return contexts.element().interpolate(query);
	}

	Section open(String query, boolean inverted) {
		Object value = getVariable(query);
		List<Context> contexts = Context.newInstances( coerce(value) );
		if (contexts.isEmpty() ^ inverted) {
			return null;
		}
		return null; //FIXME return the section
	}
	
	private List<?> coerce(Object value) {
		if (value == null) {
			return Collections.emptyList();
		}
		
		if (value instanceof Boolean) {
			return ((Boolean) value) ? Arrays.asList(true) : Collections.emptyList();
		}
		
		if (value.getClass().isArray()) {
			return Arrays.asList( (Object[]) value );
		}
		
		if (value instanceof Collection) {
			return new ArrayList<Object>( (Collection<?>) value );
		}
		
		return Arrays.asList(value);
	}

	boolean close(String query) {
		if (name == null) {
			throw new IllegalStateException("Trying to close the root section with query : " + query);
		}
		if ( !name.equals(query) ) {
			throw new IllegalArgumentException("Expected to close " + name + " not " + query);
		}
		contexts.poll();
		return contexts.isEmpty();
	}
}
