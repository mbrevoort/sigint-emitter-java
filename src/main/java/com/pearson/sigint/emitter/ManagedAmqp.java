package com.pearson.sigint.emitter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.pearson.sigint.emitter.types.Emission;
import com.rabbitmq.client.ConnectionFactory;

public class ManagedAmqp implements PublishProvider {
	private final BlockingQueue<Emission<?>> emissionBuffer;

	public ManagedAmqp(SIGINTConfig config) throws Exception {
		this(config, new ConnectionFactory(), new ArrayBlockingQueue<Emission<?>>(config.getMaxQueueSize()));
	}

	public ManagedAmqp(SIGINTConfig config, ConnectionFactory factory, BlockingQueue<Emission<?>> emissionBuffer) throws Exception {
		this.emissionBuffer = emissionBuffer;
		
		for(int i=0; i < config.getNumPublisherThreads(); i++) {
			Thread publisherThread = new Thread(new AmqpPublishRunnable(factory, config.getAmqpConnectionString(), emissionBuffer, config.getExchangeName()));
			publisherThread.setName("ManagedAmqp:publisher:" + i);
			publisherThread.setDaemon(true);
			publisherThread.start();
		}
	}

	public void publish(Emission<?> msg) {
		//Try twice to add the event, otherwise give up.
		if(!emissionBuffer.offer(msg)) {
			emissionBuffer.poll();
			emissionBuffer.offer(msg);
		}
	}
	
	public BlockingQueue<Emission<?>> getQueue() {
		return emissionBuffer;
	}

	public int getQueueSize() {
		return emissionBuffer.size();
	}
}
