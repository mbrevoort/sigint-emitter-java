package com.pearson.sigint.emitter;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pearson.sigint.emitter.types.Emission;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AmqpPublishRunnable implements Runnable {
	private final BlockingQueue<Emission<?>> queue;
	private final String exchangeName;
	private final ConnectionFactory factory;
	
	private Connection conn = null;
	private Channel channel = null;
	
	public static final String EXCHANGE_TYPE = "headers";
	public static final boolean EXCHANGE_DURABLE = true;
	
	private static final Logger log = LoggerFactory.getLogger(AmqpPublishRunnable.class);
	
	private final AtomicBoolean isStopRequested = new AtomicBoolean(false);
	
	public static final AMQP.BasicProperties MESSAGE_PROPS = new AMQP.BasicProperties.Builder()
																	.contentType("application/octet-stream")
																	.deliveryMode(1) //Do not persist the message to disk
																	.build();


	public AmqpPublishRunnable(ConnectionFactory factory, String connectionAddress, BlockingQueue<Emission<?>> queue, String exchangeName) throws Exception {
		super();
		this.factory = factory;
		this.queue = queue;
		this.exchangeName = exchangeName;
		
		factory.setUri(connectionAddress);
	}
	
	private void attemptToConnect() {
		try {
			conn = factory.newConnection();
			channel = conn.createChannel();
			channel.exchangeDeclare(this.exchangeName, EXCHANGE_TYPE, EXCHANGE_DURABLE);
		} catch (IOException e) {
			log.warn("Unable to connect to AMQP broker at " + factory.getHost(), e);
			ensureConnectionCleanedUp();
		}						
	}
	
	private void ensureConnectionCleanedUp() {
		if(conn != null)
		{
			log.debug("Attempting to clean up connection");
			try {
				conn.close();
				log.debug("Closed connection");
			} catch (IOException e1) {
				log.warn("Exception thrown while closing connection", e1);
			}
			finally {
				conn = null;
			}
		}		
		if(channel != null) {
			channel = null;
		}
	}

	public void stop() {
		isStopRequested.set(true);
	}
	
	@Override
	public void run() {				
		attemptToConnect();
		
		while(!isStopRequested.get()) {

			try {
				Emission<?> message = queue.take();
				
				boolean published = false;
				
				while(!published)
				{
					while(channel == null) {
						attemptToConnect();
					}
					
					try {
						channel.basicPublish(this.exchangeName, message.getType(), MESSAGE_PROPS, message.getBody());
						published = true;
					} catch (IOException e) {
						try {
							log.warn("Exception while publishing emission (JSON: " + message.getJson() + ")", e);
						} catch (IOException e1) {
							log.warn("Exception while publishing emission (JSON: not generated - see next log message below)", e);
							log.warn("Exception while generating JSON for log message", e1);
						}
						ensureConnectionCleanedUp();
					}					
				}
			} catch (InterruptedException e) {
				log.warn("InterruptedException while waiting to take an emission from the publish buffer.  Stopping this publisher.", e);
				isStopRequested.set(true);
			}
		}
	}
}
