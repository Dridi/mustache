package mustache.parser;

class ChangeDelimiterException extends Exception {
	private static final long serialVersionUID = -8458152952341847469L;
	
	private final Tag tag;
	
	ChangeDelimiterException(Tag tag) {
		this.tag = tag;
	}
	
	Tag getTag() {
		return tag;
	}
}
