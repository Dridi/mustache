package mustache.parser;

import java.io.IOException;

public interface PartialLoader {
	Readable loadPartial(String partial) throws IOException;
}
