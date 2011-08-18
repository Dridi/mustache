package mustache.core;

import java.util.LinkedList;
import java.util.Queue;

class Section {
	
	private final Queue<Context> contexts = new LinkedList<Context>();
	
	private Section() { }
	
	static Section rootSection(Object root) {
		Context context = Context.newInstance(root);
		Section section = new Section();
		section.contexts.add(context);
		return section;
	}

	Object getVariable(String query) {
		return contexts.peek().interpolate(query);
	}
	
	boolean close() {
		contexts.poll();
		return contexts.isEmpty();
	}

}
