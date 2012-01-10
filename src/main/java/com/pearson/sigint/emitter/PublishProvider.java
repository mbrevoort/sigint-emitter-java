package com.pearson.sigint.emitter;

import com.pearson.sigint.emitter.types.Emission;

public interface PublishProvider {
	void publish(Emission<?> msg);
	int getQueueSize();
}
