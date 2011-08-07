package mustache;

/**
 * Represents a tag in a template.
 * 
 * @author Dri
 */
final class Tag {
	
	private String tag;
	
	private Type type;
	
	private String content;
	
	private Position position;
	
	private String indentation = "";
	
	private Tag(String tag, Type type, String content, Position position) {
		this.tag = tag;
		this.type = type;
		this.content = content;
		this.position = position;
	}
	
	/**
	 * Creates a new Tag instance.
	 * 
	 * @param tag The content of the tag with the delimiters
	 * @param token The token indicating the type of Tag or <code>null</code>
	 * @param content The content of the tag, without the delimiters, the token and spaces
	 * @param position The position in the current template
	 * @return A new Tag instance
	 * @throws IllegalArgumentException If <code>token</code> is unknown
	 */
	static Tag newTag(String tag, String token, String content, Position position) {
		Type type = Type.fromToken(token);
		return new Tag(tag, type, content, position);
	}
	
	/**
	 * Updates the Tag so that it represents the whole line.
	 * 
	 * @param leadingBlanks The blank characters at the begining of the line
	 * @param trailingBlanks The blank characters at the end of the line
	 */
	void makeStandalone(String leadingBlanks, String trailingBlanks) {
		indentation = leadingBlanks;
		tag = leadingBlanks + tag + trailingBlanks;
		position = new Position(position.offset() - leadingBlanks.length(), position.line(), 1);
	}
	
	/**
	 * The different kinds of <code>Tag</code>.
	 */
	enum Type {
		VARIABLE(""),
		UNESCAPED_VARIABLE("&"),
		SECTION("#"),
		INVERTED_SECTION("^"),
		SECTION_END("/"),
		PARTIAL(">"),
		DELIMITER("="),
		COMMENT("!");
		
		private String token;
		
		private boolean standalone;
		
		private Type(String token) {
			this.token = token;
			this.standalone = !name().endsWith("VARIABLE");
		}
		
		/**
		 * @return the token
		 */
		String token() {
			return token;
		}
		
		/**
		 * Indicates whether the Tag can be considered standalone (the only item on a line).
		 * 
		 * @return <code>true</code> if the tag can be standalone
		 * @exception IllegalArgumentException if the token is unknown
		 */
		boolean canBeStandalone() {
			return standalone;
		}
		
		private static Type fromToken(String token) {
			
			String actualToken = token == null ? "" : token;
			
			for (Type type : values()) {
				if (type.token.equals(actualToken)) {
					return type;
				}
			}
			
			throw new IllegalArgumentException("Unknown token : " + token);
		}
	}
	
	/**
	 * @return the type
	 */
	Type getType() {
		return type;
	}
	
	/**
	 * @return the content
	 */
	String getContent() {
		return content;
	}
	
	/**
	 * @return the position
	 */
	Position getPosition() {
		return position;
	}
	
	/**
	 * @return the full length of the Tag
	 */
	int getLength() {
		return tag.length();
	}
	
	/**
	 * @return the indentation
	 */
	String getIndentation() {
		return indentation;
	}
	
	/**
	 * @return the position after the Tag
	 */
	public Position forward() {
		return position.forward(tag);
	}
	
	/**
	 * @return the full Tag
	 */
	@Override
	public String toString() {
		return tag;
	}
}