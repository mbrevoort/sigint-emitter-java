package com.pearson.sigint.emitter;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

import com.pearson.sigint.emitter.SIGINTConfig.FORMAT;
import com.pearson.sigint.emitter.types.Emission;
import com.pearson.sigint.emitter.types.Timer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AmqpPublishRunnableTest {
	private static int TEST_SLEEP = 100;
	
	@Test
	public void connectsAndPublishes() throws Exception
	{
		Channel channel = mock(Channel.class);
		
		Connection conn = mock(Connection.class);
		when(conn.createChannel()).thenReturn(channel);
		
		ConnectionFactory factory = mock(ConnectionFactory.class);
		when(factory.newConnection()).thenReturn(conn);
		
		BlockingQueue<Emission<?>> queue = new ArrayBlockingQueue<Emission<?>>(1);
		
		AmqpPublishRunnable runnable = new AmqpPublishRunnable(factory, "blah", queue, "foo");
		Thread runner = new Thread(runnable);
		runner.start();
		
		Timer timer = new Timer("app", "node", null, FORMAT.BSON);
		queue.offer(timer);
		
		Thread.sleep(TEST_SLEEP);
		
		runnable.stop();
		
		verify(channel, times(1)).exchangeDeclare("foo", AmqpPublishRunnable.EXCHANGE_TYPE, AmqpPublishRunnable.EXCHANGE_DURABLE);
		verify(channel, times(1)).basicPublish("foo", Timer.TYPE(), AmqpPublishRunnable.MESSAGE_PROPS, timer.getBody());
	}
	
	@Test
	public void onlyOneConnectionAttemptBeforeAnEmissionIsQueued() throws Exception {
		ConnectionFactory factory = mock(ConnectionFactory.class);
		when(factory.newConnection()).thenThrow(new IOException("test case"));
		
		BlockingQueue<Emission<?>> queue = new ArrayBlockingQueue<Emission<?>>(1);
		
		AmqpPublishRunnable runnable = new AmqpPublishRunnable(factory, "blah", queue, "foo");
		Thread runner = new Thread(runnable);
		runner.start();
		
		Thread.sleep(TEST_SLEEP);
		
		runnable.stop();
		
		verify(factory, times(1)).newConnection();		
	}
	
	@Test
	public void retriesToConnectIdenfinately() throws Exception {
		ConnectionFactory factory = mock(ConnectionFactory.class);
		when(factory.newConnection()).thenThrow(new IOException("test case"));
		
		BlockingQueue<Emission<?>> queue = new ArrayBlockingQueue<Emission<?>>(1);
		
		AmqpPublishRunnable runnable = new AmqpPublishRunnable(factory, "blah", queue, "foo");
		Thread runner = new Thread(runnable);
		runner.start();
		
		Timer timer = new Timer("app", "node", null, FORMAT.BSON);
		queue.offer(timer);
		
		Thread.sleep(TEST_SLEEP);
		
		runnable.stop();
		
		verify(factory, atLeast(10)).newConnection();		
	}
	
	@Test
	public void cleansUpAndReconnectsUponErrorDuringPublish() throws Exception {
		Timer timer = new Timer("app", "node", null, FORMAT.BSON);

		Channel channel = mock(Channel.class);
		doThrow(new IOException("test case")).when(channel).basicPublish("foo", Timer.TYPE(), AmqpPublishRunnable.MESSAGE_PROPS, timer.getBody());
		
		Connection conn = mock(Connection.class);
		when(conn.createChannel()).thenReturn(channel);
		
		ConnectionFactory factory = mock(ConnectionFactory.class);
		when(factory.newConnection()).thenReturn(conn);
		
		BlockingQueue<Emission<?>> queue = new ArrayBlockingQueue<Emission<?>>(1);
		
		AmqpPublishRunnable runnable = new AmqpPublishRunnable(factory, "blah", queue, "foo");
		Thread runner = new Thread(runnable);
		runner.start();
		
		queue.offer(timer);
		
		Thread.sleep(TEST_SLEEP);
		
		runnable.stop();
		
		verify(conn, atLeast(10)).close();
		verify(channel, atLeast(10)).exchangeDeclare("foo", AmqpPublishRunnable.EXCHANGE_TYPE, AmqpPublishRunnable.EXCHANGE_DURABLE);
		verify(channel, atLeast(10)).basicPublish("foo", Timer.TYPE(), AmqpPublishRunnable.MESSAGE_PROPS, timer.getBody());
		
	}
	
	@Test
	public void handlesExceptionDuringCleanup() throws Exception {
		Timer timer = new Timer("app", "node", null, FORMAT.BSON);

		Channel channel = mock(Channel.class);
		doThrow(new IOException("test case")).when(channel).basicPublish("foo", Timer.TYPE(), AmqpPublishRunnable.MESSAGE_PROPS, timer.getBody());
		
		Connection conn = mock(Connection.class);
		when(conn.createChannel()).thenReturn(channel);
		doThrow(new IOException("test case")).when(conn).close();
		
		ConnectionFactory factory = mock(ConnectionFactory.class);
		when(factory.newConnection()).thenReturn(conn);
		
		BlockingQueue<Emission<?>> queue = new ArrayBlockingQueue<Emission<?>>(1);
		
		AmqpPublishRunnable runnable = new AmqpPublishRunnable(factory, "blah", queue, "foo");
		Thread runner = new Thread(runnable);
		runner.start();
		
		queue.offer(timer);
		
		Thread.sleep(TEST_SLEEP);
		
		runnable.stop();
		
		verify(conn, atLeast(10)).close();
		verify(channel, atLeast(10)).exchangeDeclare("foo", AmqpPublishRunnable.EXCHANGE_TYPE, AmqpPublishRunnable.EXCHANGE_DURABLE);
		verify(channel, atLeast(10)).basicPublish("foo", Timer.TYPE(), AmqpPublishRunnable.MESSAGE_PROPS, timer.getBody());
	}
}
