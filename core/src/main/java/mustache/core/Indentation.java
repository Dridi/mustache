package mustache.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO arg checks
public class Indentation {
	private static final Pattern INDENT_PARTIAL_TEXT = Pattern.compile("\\r\\n|\\r|\\n(?!$)");
	
	public static boolean isIndentation(String indentation) {
		for (int i = indentation.length(); --i > 0;) {
			char c = indentation.charAt(i);
			if (c != ' ' && c != '\t') {
				return false;
			}
		}
		return true;
	}
	
	public static String indentPartialText(String string, String indentation) {
		if (string == null || indentation == null) {
			throw new NullPointerException();
		}
		if ( !isIndentation(indentation) ) {
			throw new IllegalArgumentException("Invalid indentation : " + indentation);
		}
		String replacement = "$0" + Matcher.quoteReplacement(indentation);
		return INDENT_PARTIAL_TEXT.matcher(string).replaceAll(replacement);
	}
}
