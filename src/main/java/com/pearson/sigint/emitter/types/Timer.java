package com.pearson.sigint.emitter.types;

import com.pearson.sigint.emitter.PublishProvider;
import com.pearson.sigint.emitter.SIGINTConfig.FORMAT;

public class Timer extends Emission<Timer> {
	public static String TYPE() { return "t"; }
	
	public Timer(String app, String node, PublishProvider publisher, FORMAT format) {
		super(app, node, publisher, format);
	}
	
	@Override
	public String getType() {
		return TYPE();
	}
	
	public Timer duration(long i) {
		setData(i);
		return this;
	}
	
	public Timer againstApplication(String a) {
		setAgainst(a);
		return this;
	}
	
	public Timer operation(String o) {
		return _operation(o);
	}
	
	public Stopwatch start() {
		return new Stopwatch(this, System.currentTimeMillis());
	}
	
	protected class Stopwatch {
		private final Timer timer;
		private final long start;

		public Stopwatch(Timer timer, long start) {
			super();
			this.timer = timer;
			this.start = start;
		}
		
		public void emit() {
			timer.duration(System.currentTimeMillis() - start);
			timer.emit();
		}
	}
}
