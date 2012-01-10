package com.pearson.sigint.emitter.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.pearson.sigint.emitter.PublishProvider;

import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonModule;

public abstract class Emission<T extends Emission<T>> {
	private static final BsonFactory factory = new BsonFactory();
	private static final ObjectMapper om = new ObjectMapper(factory);
	
	private final Map<String, Object> data = new LinkedHashMap<String, Object>();
	
	private final PublishProvider publisher;
	
	static {
		om.registerModule(new BsonModule());
	}
	
	public Emission(String app, String node, PublishProvider publisher) {
		this.publisher = publisher;
		
		data.put("w", (new Date()).getTime());
		
		data.put("v", 1);
		
		Map<String, Object> source = new LinkedHashMap<String, Object>();
		source.put("a", app);
		source.put("n", node);
		data.put("s", source);
		
		data.put("t", getType());
	}
	
	@SuppressWarnings("unchecked")
	protected T _operation(String o) {
		data.put("o", o);
		return (T)this;
	}
	
	protected void setAgainst(String app) {
		data.put("g", app);
	}
	
	public abstract String getType();
		
	public byte[] getBody() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		om.writeValue(baos, data);

	    return baos.toByteArray();
	}
	
	public String getJson() throws JsonGenerationException, JsonMappingException, IOException {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper jsonOm = new ObjectMapper(jsonFactory);
		
		StringWriter writer = new StringWriter();
		
		try {
			jsonOm.writeValue(writer, data);
			return writer.toString();
		} finally {
			writer.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected HashMap<String, Object> getDataMap() {
		if(!data.containsKey("d"))
			data.put("d", new LinkedHashMap<String, Object>());
		
		return (HashMap<String, Object>) data.get("d");
	}
	
	protected void setData(Object o) {
		data.put("d", o);
	}
	
	public void emit() {
		publisher.publish(this);
	}
	
	public Map<String, Object> _getInnerData() {
		return this.data;
	}
	
	public long _getTime() {
		return ((Long)data.get("w")).longValue();
	}

}