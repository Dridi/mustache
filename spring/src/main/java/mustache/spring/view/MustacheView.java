package mustache.spring.view;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mustache.Renderer;
import mustache.core.Processor;
import mustache.parser.ParseException;
import mustache.parser.Parser;
import mustache.parser.PartialLoader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.web.servlet.view.AbstractTemplateView;

public class MustacheView extends AbstractTemplateView {
	
	private EncodedResource encodedResource;
	private PartialLoader partialLoader;
	
	protected final Resource getResource() {
		return encodedResource.getResource();
	}

	final void setEncodedResource(EncodedResource encodedResource) {
		this.encodedResource = encodedResource;
	}

	public final void setPartialLoader(PartialLoader partialLoader) {
		this.partialLoader = partialLoader;
	}

	@Override
	public boolean checkResource(Locale locale) {
		return encodedResource.getResource().isReadable();
	}
	
	@Override
	protected void renderMergedTemplateModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
		
		Processor processor = Parser.parseReadable(encodedResource.getReader(), partialLoader);
		Renderer.render(processor, model, response.getWriter());
	}
}