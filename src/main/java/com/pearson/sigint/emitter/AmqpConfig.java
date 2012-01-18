package com.pearson.sigint.emitter;

public class AmqpConfig {
	private final String connectionString;
	private final String exchangeName;
	private final String key;
	
	public AmqpConfig(String connectionString, String exchangeName, String key) {
		this.connectionString = connectionString;
		this.exchangeName = exchangeName;
		this.key = key;
	}

	public String getConnectionString() {
		return connectionString;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public String getKey() {
		return key;
	}
}
