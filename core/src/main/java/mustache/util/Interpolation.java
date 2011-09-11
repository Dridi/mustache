package mustache.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
 * Dot-separated variable names look like {@code dog.body.tail} where dog
 * would be a property of the root object. Variables can be object fields,
 * no-arg methods and {@link Map} keys. If interpolation fails at some point,
 * {@code null} is returned. The {@code Interpolation} itself does
 * <i>not</i> coerce {@code null} or falsey values into empty strings. This
 * feature is only needed for actual {@link Mustache} rendering.
 * </p>
 * 
 * <p>Valid interpolations are matched against {@value #QUERY_REGEX}.</p>
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
	 * Returns {@code true} if the query is valid.
	 * @param query the query to check
	 * @return {@code true} if the query is valid
	 */
	public static boolean isValidQuery(String query) {
		return query != null ? QUERY_PATTERN.matcher(query).matches() : false;
	}
	
	/**
	 * This method looks up the query's variables in the object graph. It returns the value
	 * of the last variable unless a variable wasn't found at some point. If the interpolation
	 * failed, it returns {@code null}, but returning {@code null} does not necessary
	 * mean failure.
	 * 
	 * @param query the context query
	 * @param object the object graph
	 * @return the interpolated value or {@code null} if it failed
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
		catch (NoSuchFieldException e) {}
		catch (IllegalAccessException e) {}
		
		try {
			Method method = object.getClass().getDeclaredMethod(name);
			if (method.getReturnType() == void.class) {
				return null;
			}
			method.setAccessible(true);
			return method.invoke(object);
		}
		catch (IllegalAccessException e) {}
		catch (InvocationTargetException e) {}
		catch (NoSuchMethodException e) {}
		
		return null;
	}
	
	/**
	 * Indicates whether their is a base variable matching the query in the object graph.
	 * 
	 * <p>
	 * Querying {@code dog.body.tail} will return {@code true} only if there
	 * is a {@code dog} variable in {@code object}.
	 * </p>
	 * 
	 * @param query the context query
	 * @param object the object graph
	 * @return {@code true} if there is a base variable matching the query
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
		catch (NoSuchFieldException e) {}
		
		try {
			object.getClass().getDeclaredMethod(baseName);
			return true;
		}
		catch (NoSuchMethodException e) {
			return false;
		}
	}

}