package mustache.parser;

import mustache.core.Processor;

public interface PartialLoader {
	Processor loadPartial() throws ParseException, RecursivePartialException;
}
