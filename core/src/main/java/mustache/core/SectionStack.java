package mustache.core;

import java.util.ArrayDeque;
import java.util.Deque;

public class SectionStack {
	
	private final Deque<Section> sections;
	
	public SectionStack(Object root) {
		this.sections = new ArrayDeque<Section>();
		this.sections.push( Section.rootSection(root) );
	}
	
	public String getValue(String query) {
		for (Section section : sections) { // TODO iterates from last to first in the deque ?
			if (section.hasBaseVariable(query)) {
				Object value = section.getVariable(query);
				return value == null ? "" : value.toString();
			}
		}
		return "";
	}

	public boolean openSection(String query) {
		// TODO Auto-generated method
		return false;
	}
	
	public boolean openInvertedSection(String query) {
		// TODO Auto-generated method
		return false;
	}
	
	public boolean closeSection(String query) {
		boolean close = sections.element().close(query);
		if (close) {
			sections.pop();
		}
		return close;
	}
	
}
