package mustache.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indentation {
	private static final Pattern REPLACE = Pattern.compile("^|\\r\\n|\\r|\\n");
	private static final Pattern REPLACE_WITHOUT_FIRST_LINE = Pattern.compile("\\r\\n|\\r|\\n");
	private static final Pattern REPLACE_WITHOUT_LAST_EMPTY_LINE = Pattern.compile("^|\\r\\n|\\r|\\n(?!$)");
	
	public static String indent(String string, String indentation) {
		return indent(string, indentation, REPLACE);
	}
	
	public static String indentExceptFirstLine(String string, String indentation) {
		return indent(string, indentation, REPLACE_WITHOUT_FIRST_LINE);
	}
	
	public static String indentExceptLastEmptyLine(String string, String indentation) {
		return indent(string, indentation, REPLACE_WITHOUT_LAST_EMPTY_LINE);
	}
	
	private static String indent(String string, String indentation, Pattern pattern) {
		if (string == null || indentation == null) {
			throw new NullPointerException();
		}
		String replacement = "$0" + Matcher.quoteReplacement(indentation);
		return pattern.matcher(string).replaceAll(replacement);
	}
}
