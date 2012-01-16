package mustache.rendering;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import mustache.util.Context;

final class Section {
	
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
	
	static Section nestedSection(String name, List<Context> contexts) {
		Section section = new Section(name);
		section.contexts.addAll(contexts);
		return section;
	}

	boolean hasBaseVariable(String query) {
	    if ( contexts.isEmpty() ) {
	        return false;
	    }
		return contexts.element().hasBaseVariable(query);
	}

	Object getVariable(String query) {
        if ( contexts.isEmpty() ) {
            return null;
        }
		return contexts.element().interpolate(query);
	}

	Section open(String query, boolean inverted) {
		Object value = getVariable(query);
		List<Context> contexts = Context.newInstances( coerce(value) );
		if (contexts.isEmpty() ^ inverted) {
			return null;
		}
		return nestedSection(query, contexts);
	}
	
	private List<?> coerce(Object value) {
		// FIXME check if empty lists actually work
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

	@Override
	public String toString() {
		if (name == null) {
			return MessageFormat.format("Root{0}({1})", getClass().getSimpleName(), contexts.size());
		}
		return MessageFormat.format("{0}:{1}({2})", getClass().getSimpleName(), name, contexts.size());
	}
}
