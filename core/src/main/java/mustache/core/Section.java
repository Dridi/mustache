package mustache.core;

import java.util.LinkedList;
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
		// TODO Auto-generated method stub
		return null;
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
