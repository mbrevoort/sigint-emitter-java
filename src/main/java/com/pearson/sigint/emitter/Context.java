package com.pearson.sigint.emitter;

import com.pearson.sigint.emitter.types.Announcement;
import com.pearson.sigint.emitter.types.Counter;
import com.pearson.sigint.emitter.types.Timer;

public class Context {
	private final PublishProvider publisher;
	
	private final SIGINTConfig config;
	
	public Context(SIGINTConfig c) throws Exception {
		config = c;
		publisher = (config.isNoop()) ? new NoopPublishProvider() : new ManagedAmqp(config);
	}
		
	public int getQueueSize() {
		return publisher.getQueueSize();
	}
	
	public Timer time(String operation) throws Exception {
		return new Timer(config.getAppName(), config.getNodeName(), publisher, config.getFormat()).operation(operation);
	}

	public Counter count(String operation) throws Exception {
		return new Counter(config.getAppName(), config.getNodeName(), publisher, config.getFormat()).operation(operation);
	}

	public Announcement announce() throws Exception {
		return new Announcement(config.getAppName(), config.getNodeName(), publisher, config.getFormat());
	}
}
