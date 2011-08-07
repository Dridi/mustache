package mustache;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class SpecDataDeserializer implements JsonDeserializer<Object> {
	
	public Object deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		
		if (type != Object.class) {
			return context.deserialize(json, type);
		}
		
		if ( json.isJsonPrimitive() ) {
			return deserializePrimitive((JsonPrimitive) json);
		}
		
		if ( json.isJsonArray() ) {
			return deserializeArray((JsonArray) json, context);
		}
		
		if ( json.isJsonObject() ) {
			return deserializeObject((JsonObject) json, context);
		}
		
		return null;
	}

	private Object deserializePrimitive(JsonPrimitive json) {
		
		if ( json.isBoolean() ) {
			return json.getAsBoolean();
		}
		
		return json.getAsString();
	}

	private Object[] deserializeArray(JsonArray json, JsonDeserializationContext context) {
		Object[] array = new Object[ json.size() ];
		
		for (int i = 0; i < array.length; i++) {
			array[i] = context.deserialize(json.get(i), Object.class);
		}
		
		return array;
	}

	private Map<String, Object> deserializeObject(JsonObject json, JsonDeserializationContext context) {
		
		Map<String, Object> object = new LinkedHashMap<String, Object>( json.entrySet().size() );
		
		for (Entry<String, JsonElement> element : json.entrySet()) {
			object.put(element.getKey(), context.deserialize(element.getValue(), Object.class));
		}
		
		return object;
	}
	
	static Gson newGson() {
		return new GsonBuilder().registerTypeAdapter(Object.class, new SpecDataDeserializer()).create();
	}

}
