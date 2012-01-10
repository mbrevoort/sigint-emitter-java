package com.pearson.sigint.emitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;


public class SIGINTConfig {
	private String appName;
	private String nodeName;
	private String amqpConnectionString;
	private int numPublisherThreads;
	private int maxQueueSize;
	private String exchangeName;
	private String mode;
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public String getAmqpConnectionString() {
		return amqpConnectionString;
	}
	public void setAmqpConnectionString(String amqpConnectionString) {
		this.amqpConnectionString = amqpConnectionString;
	}
	
	public int getNumPublisherThreads() {
		return numPublisherThreads;
	}
	public void setNumPublisherThreads(int numPublisherThreads) {
		this.numPublisherThreads = numPublisherThreads;
	}
	
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}
	
	public String getExchangeName() {
		return exchangeName;
	}
	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) throws IllegalArgumentException {
		if( !"noop".equals(mode) && !"amqp".equals(mode))
			throw new IllegalArgumentException("Mode must be either 'noop' or 'amqp'");
		
		this.mode = mode;
	}
	
	public boolean isNoop() {
		return "noop".equals(this.mode);
	}
	
	private static void ensureProperties(Properties properties) {
		String[] requiredProperties = new String[] {"ampq.connection", "application", "node", "amqp.exchange", "queue.size", "mode", "publishers"};
		ArrayList<String> missingProps = new ArrayList<String>();
		
		for(String key: requiredProperties) {
			if(!properties.containsKey(key))
				missingProps.add(key);
		}
		
		if(missingProps.size() > 0)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("Invalid SIGINT configuration file: Missing properties [");
			
			String delim = "";
			for(String prop: missingProps)
			{
				builder.append(delim);
				builder.append(prop);
				delim = ", ";
			}
			builder.append("]");

			throw new IllegalArgumentException(builder.toString());
		}
	}
	
	public static SIGINTConfig fromProperties(Properties properties)
	{
		ensureProperties(properties);

		SIGINTConfig result = new SIGINTConfig();
		
		result.setAmqpConnectionString(properties.getProperty("ampq.connection"));
		result.setAppName(properties.getProperty("application"));
		result.setNodeName(properties.getProperty("node"));
		result.setExchangeName(properties.getProperty("amqp.exchange"));
		result.setMaxQueueSize(Integer.parseInt(properties.getProperty("queue.size")));
		result.setMode(properties.getProperty("mode"));
		result.setNumPublisherThreads(Integer.parseInt(properties.getProperty("publishers")));
		
		return result;		
	}
	
	public static SIGINTConfig fromProperties(File file) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		
		return fromProperties(properties);
	}
	
}
