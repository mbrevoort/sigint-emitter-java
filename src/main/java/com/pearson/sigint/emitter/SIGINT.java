package com.pearson.sigint.emitter;

import java.io.File;


public  class SIGINT {
	private static final Object configureLock = new Object();
	private static Context instance;
		
	public static Context createContext(File propertiesFile) throws Exception {
		return new Context(SIGINTConfig.fromProperties(propertiesFile));
	}
	
	public static void configureCurrent(File propertiesFile) throws Exception {
		if(instance == null) {
			synchronized (configureLock) {
				if(instance == null) {
					instance = createContext(propertiesFile);
				}
			}
		}
	}
	
	public static Context current() {
		return instance;
	}
}
