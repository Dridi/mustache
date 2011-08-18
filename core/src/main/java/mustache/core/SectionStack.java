package mustache.core;

import java.util.ArrayDeque;
import java.util.Deque;

public class SectionStack {
	
	private final Deque<Section> sections;
	
	public SectionStack(Object root) {
		this.sections = new ArrayDeque<Section>();
		this.sections.addFirst( Section.rootSection(root) );
	}
	
	public Object getVariable(String query) {
		return sections.getLast().getVariable(query);
	}
	
	public boolean openSection(String query) {
		// TODO Auto-generated method
		return false;
	}
	
	public boolean openInvertedSection(String query) {
		// TODO Auto-generated method
		return false;
	}
	
	public boolean closeSection() {
		boolean close = sections.getFirst().close();
		if (close) {
			sections.removeLast();
		}
		return close;
	}
	
}
