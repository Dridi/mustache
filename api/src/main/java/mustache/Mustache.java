package mustache;

import java.io.File;
import java.io.IOException;

import mustache.core.Processor;
import mustache.parser.ParseException;
import mustache.parser.Parser;
import mustache.parser.PartialLoader;

public abstract class Mustache {
	public final void renderReadable(Readable readable, Appendable appendable, PartialLoader partialLoader)
			throws ParseException, IOException {
		Processor processor = Parser.parseReadable(readable, partialLoader);
		Renderer.render(processor, this, appendable);
	}
	
	public final void renderFile(File file, Appendable appendable, PartialLoader partialLoader)
			throws ParseException, IOException {
		Processor processor = Parser.parseFile(file, partialLoader);
		Renderer.render(processor, this, appendable);
	}
	
	public final void renderFile(String path, Appendable appendable, PartialLoader partialLoader)
			throws ParseException, IOException {
		Processor processor = Parser.parseFile(path, partialLoader);
		Renderer.render(processor, this, appendable);
	}
	
	public final void renderString(String string, Appendable appendable, PartialLoader partialLoader)
			throws ParseException, IOException {
		Processor processor = Parser.parseString(string, partialLoader);
		Renderer.render(processor, this, appendable);
	}
}
