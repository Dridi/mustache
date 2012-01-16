package mustache.spring.view;

import java.io.IOException;

import mustache.parser.PartialLoader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

public class MustacheViewResolver extends AbstractTemplateViewResolver implements PartialLoader {
	
	public static final String DEFAULT_ENCODING = "UTF-8";
	
	private String encoding = DEFAULT_ENCODING;

	@Override
	protected MustacheView buildView(String viewName) throws Exception {
		MustacheView view = (MustacheView) super.buildView(viewName);
		view.setEncodedResource( getResource(view) );
		view.setPartialLoader( getPartialLoader() );
		return view;
	}

	protected EncodedResource getResource(MustacheView view) {
		Resource resource = getApplicationContext().getResource( view.getUrl() );
		return new EncodedResource(resource, encoding);
	}

	private PartialLoader getPartialLoader() {
		return this;
	}

	public Readable loadPartial(String name) throws IOException {
		String url = getPrefix() + name + getSuffix();
		Resource resource = getApplicationContext().getResource(url);
		return new EncodedResource(resource, encoding).getReader();
	}

	@Override
	protected final Class<MustacheView> requiredViewClass() {
		return MustacheView.class;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}