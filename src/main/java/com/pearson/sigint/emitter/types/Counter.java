package com.pearson.sigint.emitter.types;

import com.pearson.sigint.emitter.PublishProvider;

public class Counter extends Emission<Counter> {
	public static String TYPE() { return "c"; }
	
	public Counter(String app, String node, PublishProvider publisher) {
		super(app, node, publisher);
		times(1);
	}
	
	@Override
	public String getType() {
		return TYPE();
	}
	
	public Counter times(int i) {
		setData(i);
		return this;
	}
	
	public Counter againstApplication(String a) {
		setAgainst(a);
		return this;
	}
	
	public Counter operation(String o) {
		return _operation(o);
	}
}
