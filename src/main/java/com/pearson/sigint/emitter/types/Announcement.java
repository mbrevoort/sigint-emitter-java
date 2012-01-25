package com.pearson.sigint.emitter.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.pearson.sigint.emitter.PublishProvider;
import com.pearson.sigint.emitter.SIGINTConfig.FORMAT;

public class Announcement extends Emission<Announcement> {
	public static String TYPE() { return "a"; }
	
	public Announcement(String app, String node, PublishProvider publisher, FORMAT format) {
		super(app, node, publisher, format);
	}
	
	@Override
	public String getType() {
		return TYPE();
	}
	
	public Announcement version(String version) {
		getDataMap().put("v", version);
		return this;
	}
	
	public Announcement addStackItem(String name, String version) {
		Map<String,String> tmp = new LinkedHashMap<String, String>();
		tmp.put("n", name);
		tmp.put("v", version);
		getStack().add(tmp);
		return this;
	}
	
	public Announcement addStackItems(Map<String, String> items) {
		for(String key: items.keySet()) {
			addStackItem(key, items.get(key));
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Map<String,String>> getStack() {
		if(!getDataMap().containsKey("s")) {
			getDataMap().put("s", new ArrayList<Map<String,String>>());
		}
		
		return (ArrayList<Map<String,String>>) getDataMap().get("s");
	}
}
