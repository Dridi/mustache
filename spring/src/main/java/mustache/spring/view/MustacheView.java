package mustache.spring.view;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mustache.ParseException;
import mustache.Parser;
import mustache.PartialLoader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.FileCopyUtils;
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
		
		String template = loadTemplate();
		Parser parser = new Parser(template, model, partialLoader);
		
		response.getWriter().append( parser.merge() );
	}

	protected String loadTemplate() throws IOException {
		
		return readTemplate(encodedResource);
	}
	
	static String readTemplate(EncodedResource encodedResource) throws IOException {
		return FileCopyUtils.copyToString( encodedResource.getReader() );
	}
}