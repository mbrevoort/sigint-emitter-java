package com.pearson.sigint.emitter;

import com.pearson.sigint.emitter.types.Emission;

public class NoopPublishProvider implements PublishProvider {

	@Override
	public void publish(Emission<?> msg) {
		//noop
	}

	@Override
	public int getQueueSize() {
		return 0;
	}

}
