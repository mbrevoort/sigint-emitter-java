package com.pearson.sigint.emitter;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SIGINTConfigTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

	@Test
	public void isNoop() throws IllegalArgumentException {
		SIGINTConfig config = new SIGINTConfig();
		config.setMode("noop");
		
		SIGINTConfig config2 = new SIGINTConfig();
		config2.setMode("amqp");
		
		Assert.assertTrue("isNoop() should be true", config.isNoop());
		Assert.assertFalse("isNoop() should be false", config2.isNoop());
	}
	
	@Test
	public void modeEnumeration() throws IllegalArgumentException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Mode must be either 'noop' or 'amqp'");
		
		SIGINTConfig config = new SIGINTConfig();
		config.setMode("foo");
	}
	
	@Test
	public void loadFromProperties() {
		Properties properties = new Properties();
		properties.setProperty("ampq.connection", "amqpconn");
		properties.setProperty("application", "app1");
		properties.setProperty("node", "node1");
		properties.setProperty("amqp.exchange", "exchange");
		properties.setProperty("queue.size", "10");
		properties.setProperty("mode", "amqp");
		properties.setProperty("publishers", "3");
		
		SIGINTConfig config = SIGINTConfig.fromProperties(properties);
		
		Assert.assertEquals("amqpconn", config.getAmqpConnectionString());
		Assert.assertEquals("app1", config.getAppName());
		Assert.assertEquals("node1", config.getNodeName());
		Assert.assertEquals("exchange", config.getExchangeName());
		Assert.assertEquals(10, config.getMaxQueueSize());
		Assert.assertEquals("amqp", config.getMode());
		Assert.assertEquals(3, config.getNumPublisherThreads());
	}
	
	@Test
	public void missingPropertiesShouldThrow() {
		Properties properties = new Properties();
		properties.setProperty("ampq.connection", "amqpconn");
		properties.setProperty("application", "app1");
		properties.setProperty("node", "node1");
		properties.setProperty("amqp.exchange", "exchange");
		properties.setProperty("queue.size", "10");
		
		thrown.expectMessage("Invalid SIGINT configuration file: Missing properties [mode, publishers]");
		
		SIGINTConfig.fromProperties(properties);
	}
}
