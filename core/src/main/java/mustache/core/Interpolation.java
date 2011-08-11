package mustache.core;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This is a utility class for {@link Mustache} interpolation. It works with
 * queries applied on objects. A query is a string with dot-separated variable
 * names.
 * 
 * 
 * <p>
 * Dot-separated variable names look like <code>dog.body.tail</code> where dog
 * would be a property of the root object. Variables can be object fields,
 * no-arg methods and {@link Map} keys. If interpolation fails at some point,
 * <code>null</code> is returned. The <code>Interpolation</code> itself does
 * <i>not</i> coerce <code>null</code> or falsey values into empty strings. This
 * feature is only needed for actual {@link Mustache} rendering.
 * </p>
 * 
 * <p>Valid interpolations are matched against {@value #QUERY_REGEX}.</p>
 * 
 * TODO interpolate methods (currently not implemented)
 * TODO interpolate in getters ? (only fields are currently supported)
 * 
 * @author Dri
 * @see Context
 */
public final class Interpolation {
	
	/**
	 * The separator of the nested variable names in queries.
	 */
	public static final String SEPARATOR = ".";
	
	private static final Pattern SEPARATOR_PATTERN = Pattern.compile( Pattern.quote(SEPARATOR) );

	/**
	 * The regex for valid interpolation queries. The fact that variable interpolation might
	 * fail given not-existing variable names within data is irrelevant for validation.
	 */
	public static final String QUERY_REGEX = "^[a-z_][a-z0-9_]*(\\.[a-z_][a-z0-9_]*)*$";
	
	private static final Pattern QUERY_PATTERN = Pattern.compile(QUERY_REGEX, Pattern.CASE_INSENSITIVE);
	
	private Interpolation() {}
	
	/**
	 * Checks whether a quey is valid.
	 * @param query the query to check
	 * @throws IllegalArgumentException if the query is not valid
	 */
	static void checkQuery(String query) {
		if ( !isValidQuery(query) ) {
			throw new IllegalArgumentException("Invalid query : " + query);
		}
	}
	
	/**
	 * Returns <code>true</code> if the query is valid.
	 * @param query the query to check
	 * @return <code>true</code> if the query is valid
	 */
	public static boolean isValidQuery(String query) {
		return QUERY_PATTERN.matcher(query).matches();
	}
	
	/**
	 * This method looks up the query's variables in the object graph. It returns the value
	 * of the last variable unless a variable wasn't found at some point. If the interpolation
	 * failed, it returns <code>null</code>, but returning <code>null</code> does not necessary
	 * mean failure.
	 * 
	 * @param query the context query
	 * @param object the object graph
	 * @return the interpolated value or <code>null</code> if it failed
	 * @throws IllegalArgumentException if the query is not valid
	 */
	public static Object interpolate(String query, Object object) {
		
		checkQuery(query);
		
		Object value = object;
		String[] names = SEPARATOR_PATTERN.split(query);
		
		for (String name : names) {
			
			if ( !hasBaseVariable(name, value) ) {
				return null;
			}
			
			value = getValue(name, value);
		}
		
		return value;
	}
	
	private static Object getValue(String name, Object object) {
		
		if (object instanceof Map) {
			return ((Map<?, ?>) object).get(name);
		}
		
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(object);
		}
		catch (NoSuchFieldException e) {
			return null;
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Indicates whether their is a base variable matching the query in the object graph.
	 * 
	 * <p>
	 * Querying <code>dog.body.tail</code> will return <code>true</code> only if there
	 * is a <code>dog</code> variable in <code>object</code>.
	 * </p>
	 * 
	 * @param query the context query
	 * @param object the object graph
	 * @return <code>true</code> if there is a base variable matching the query
	 * @throws IllegalArgumentException if the query is not valid
	 */
	public static boolean hasBaseVariable(String query, Object object) {

		checkQuery(query);
		
		if (object == null || object.getClass().isArray() || object instanceof Collection) {
			return false;
		}
		
		String baseName = SEPARATOR_PATTERN.split(query, 2)[0];
		
		if (object instanceof Map) {
			return ((Map<?, ?>) object).containsKey(baseName);
		}
		
		try {
			object.getClass().getDeclaredField(baseName);
			return true;
		}
		catch (NoSuchFieldException e) {
			return false;
		}
	}

}