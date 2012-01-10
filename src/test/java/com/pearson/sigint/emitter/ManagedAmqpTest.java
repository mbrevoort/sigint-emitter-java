package com.pearson.sigint.emitter;

import org.junit.Assert;
import org.junit.Test;

import com.pearson.sigint.emitter.types.Counter;

public class ManagedAmqpTest {

	@Test
	public void earliestEmissionsGetDumpedWhenBufferIsFull() throws Exception {
		SIGINTConfig config = new SIGINTConfig();
		config.setNumPublisherThreads(0); //0 so no publishers start up
		config.setMaxQueueSize(10);
		
		ManagedAmqp ma = new ManagedAmqp(config);
		
		//Put 20 items in there...the last 10 should overwrite the first 10 because the max size is 10.
		for(int i=0; i < 20; i++) {
			new Counter("app1", "node" + i, ma).operation("op" + Integer.toString(i)).emit();
		}
		
		Assert.assertEquals("Queue size", 10, ma.getQueueSize());
		
		Assert.assertEquals("First operation", "op10", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op11", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op12", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op13", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op14", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op15", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op16", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op17", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op18", ma.getQueue().take()._getInnerData().get("o"));
		Assert.assertEquals("First operation", "op19", ma.getQueue().take()._getInnerData().get("o"));
	}
}
