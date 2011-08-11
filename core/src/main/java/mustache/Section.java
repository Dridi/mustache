package mustache;

import java.util.ArrayList;
import java.util.List;

import mustache.core.Context;
import mustache.core.Position;

class Section {
	
	private Position position;
	
	private boolean rendered = true;
	
	private String name;
	
	private List<Context> contexts = new ArrayList<Context>();
	
	Context currentContext() {
		return contexts.get(0);
	}
	
	Object interpolateVariable(String variable) {
		return currentContext().interpolate(variable);
	}
	
	boolean hasVariable(String variable) {
		return currentContext().hasBaseVariable(variable);
	}
	
	Position close(String name) throws ParseException {
		
		if (this.name == null && name != null || !this.name.equals(name)) {
			throw ParseException.fromPosition("Invalid section close : " + name, position);
		}
		
		if (contexts.size() == 0) {
			throw new IllegalStateException("Section closed for good");
		}
		
		contexts.remove(0);
		return contexts.size() > 0 ? position : null;
	}
	
	static Section newRootSection(Context context) {
		Section section = new Section();
		section.contexts.add(context);
		return section;
	}
	
	static Section newNestedSection(Tag tag, boolean rendered, List<Object> data) {
		Section section = new Section();
		
		section.rendered = rendered;
		section.position = tag.forward();
		section.name = tag.getContent();
		
		if (rendered && data != null) {
			section.contexts.addAll( Context.newInstances(data) );
		}
		else {
			section.contexts.add( Context.newInstance(null) );
		}
		
		return section;
	}
	
	boolean isRendered() {
		return rendered;
	}

	public boolean isRoot() {
		return name == null;
	}

	@Override
	public String toString() {
		return "Section " + name + " at " + position;
	}
}