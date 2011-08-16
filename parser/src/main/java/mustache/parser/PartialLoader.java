package mustache.parser;

import java.io.IOException;

import mustache.core.Processor;

public interface PartialLoader {
	Processor loadPartial(String partial) throws ParseException, IOException;
}
