package mustache.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class SectionStack {
	
	private final Deque<Section> sections;
	
	public SectionStack(Object root) {
		this.sections = new ArrayDeque<Section>();
		this.sections.push( Section.rootSection(root) );
	}
	
	public String getValue(String query) {
		Iterator<Section> iterator = sections.descendingIterator();
		while (iterator.hasNext()) {
			Section section = iterator.next();
			if (section.hasBaseVariable(query)) {
				Object value = section.getVariable(query);
				return value == null ? "" : value.toString();
			}
		}
		return "";
	}

	public boolean openSection(String query) {
		Section section = sections.element().open(query, true);
		if (section != null) {
			sections.push(section);
		}
		return section != null;
	}
	
	public boolean openInvertedSection(String query) {
		Section section = sections.element().open(query, true);
		if (section != null) {
			sections.push(section);
		}
		return section != null;
	}
	
	public boolean closeSection(String query) {
		boolean close = sections.element().close(query);
		if (close) {
			sections.pop();
		}
		return close;
	}
	
}
