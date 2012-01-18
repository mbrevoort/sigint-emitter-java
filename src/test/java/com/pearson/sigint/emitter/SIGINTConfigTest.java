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
		properties.setProperty("amqp", "one, two");
		properties.setProperty("amqp.one.connection", "amqpconn");
		properties.setProperty("application", "app1");
		properties.setProperty("node", "node1");
		properties.setProperty("amqp.one.exchange", "exchange");
		properties.setProperty("queue.size", "10");
		properties.setProperty("mode", "amqp");
		properties.setProperty("amqp.two.connection", "amqpconn");
		properties.setProperty("amqp.two.exchange", "exchange");
		
		SIGINTConfig config = SIGINTConfig.fromProperties(properties);
		
		Assert.assertEquals(2, config.getAmqpConfigs().size());
		Assert.assertEquals("amqpconn", config.getAmqpConfigs().get(0).getConnectionString());
		Assert.assertEquals("app1", config.getAppName());
		Assert.assertEquals("node1", config.getNodeName());
		Assert.assertEquals("exchange", config.getAmqpConfigs().get(0).getExchangeName());
		Assert.assertEquals(10, config.getMaxQueueSize());
		Assert.assertEquals("amqp", config.getMode());
	}
	
	@Test
	public void missingPropertiesShouldThrow() {
		Properties properties = new Properties();
		properties.setProperty("ampq.connection", "amqpconn");
		properties.setProperty("application", "app1");
		properties.setProperty("amqp.exchange", "exchange");
		properties.setProperty("queue.size", "10");
		
		thrown.expectMessage("Invalid SIGINT configuration file: Missing properties [node, mode]");
		
		SIGINTConfig.fromProperties(properties);
	}
	
	@Test
	public void incompleAmqpPropertiesShouldThrow() {
		Properties properties = new Properties();
		properties.setProperty("amqp", "one");
		properties.setProperty("application", "app1");
		properties.setProperty("node", "node1");
		properties.setProperty("mode", "amqp");
		properties.setProperty("amqp.exchange", "exchange");
		properties.setProperty("queue.size", "10");
		
		thrown.expectMessage("Invalid SIGINT configuration file: Missing property amqp.one.connection");
		
		SIGINTConfig.fromProperties(properties);
	}
}
