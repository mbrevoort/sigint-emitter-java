package com.pearson.sigint.emitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class SIGINTConfig {
	public enum FORMAT { BSON, JSON };
	
	private FORMAT format = FORMAT.BSON;
	private String appName;
	private String nodeName;
	private int maxQueueSize;
	private String mode;	
	private final ArrayList<AmqpConfig> amqps = new ArrayList<AmqpConfig>();
	
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
	
	public void addAmqp(AmqpConfig amqp) {
		amqps.add(amqp);
	}
	
	public List<AmqpConfig> getAmqpConfigs() {
		return amqps;
	}
		
	public int getMaxQueueSize() {
		return maxQueueSize;
	}
	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
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
	
	public FORMAT getFormat() {
		return format;
	}
	
	public void setFormat(FORMAT arg) {
		this.format = arg;
	}
	
	private static void ensureProperties(Properties properties) {
		String[] requiredProperties = new String[] {"application", "node", "queue.size", "mode"};
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
		
		String amqpNamesString = properties.getProperty("amqp");
		if(amqpNamesString != null && !amqpNamesString.isEmpty())
		{
			String[] amqpNames = amqpNamesString.split(",");
			for(String amqpName: amqpNames)
			{
				String prefix = "amqp." + amqpName.trim() + ".";
				String conn = properties.getProperty(prefix + "connection");
				String exchange = properties.getProperty(prefix + "exchange");
				
				conn = (conn != null) ? conn.trim() : conn;
				exchange = (exchange != null) ? exchange.trim() : exchange;
				
				if(conn == null)
					throw new IllegalArgumentException("Invalid SIGINT configuration file: Missing property " + prefix + "connection");
				if(exchange == null)
					throw new IllegalArgumentException("Invalid SIGINT configuration file: Missing property " + prefix + "exchange");

				result.addAmqp(new AmqpConfig(conn, exchange, "amqpName"));	
			}
		}
		result.setAppName(properties.getProperty("application"));
		result.setNodeName(properties.getProperty("node"));
		result.setMaxQueueSize(Integer.parseInt(properties.getProperty("queue.size")));
		result.setMode(properties.getProperty("mode"));
		
		if(properties.containsKey("format")) {
			result.setFormat(Enum.valueOf(FORMAT.class, properties.getProperty("format").trim().toUpperCase()));
		}

		return result;		
	}
	
	public static SIGINTConfig fromProperties(File file) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		
		return fromProperties(properties);
	}
	
}
