package com.pearson.sigint.emitter;

import java.util.ArrayList;

public class SampleEmitter {

	public static void main(String[] args) throws Exception {
		final int numProducerThreads = 1;
		final int numRunsPerThread = 10;
		final int numEmissionsPerRun = 1000;
		final int sleepTimeAtEndOfRun = 2000;
		final int sleepTimeBetweenEmissions = 0;
		
		
		final ArrayList<Thread> threads = new ArrayList<Thread>();
				
		for(int t=0; t < numProducerThreads; t++) {
			final String name = "thread " + t;
			final String nodeName = "node" + t;
			final String appName = "javaApp1";
			
			SIGINTConfig config = new SIGINTConfig();
			config.addAmqp(new AmqpConfig("amqp://localhost", "sigint", "one"));
			config.addAmqp(new AmqpConfig("amqp://localhost", "sigint", "two"));
			config.setMaxQueueSize(300000);
			config.setMode("amqp");

			config.setAppName(appName);
			config.setNodeName(nodeName);
			
			final Context sigint = new Context(config);

			Thread me = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
					for(int i=0; i < numRunsPerThread; i++) {

						sigint.announce()
							.version("1.2.3")
							.addStackItem("java", System.getProperty("java.version"))
							.emit();
						
						for(int c=0; c < numEmissionsPerRun; c++) {
							sigint.time("op1").duration((int)(Math.random() * 100)).emit();
							sigint.count("op2").emit();
							
							try {
								Thread.sleep(sleepTimeBetweenEmissions);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						try {
							Thread.sleep(sleepTimeAtEndOfRun);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					}
					catch(Exception e) {
						e.printStackTrace(System.out);
					}
					
					System.out.println("Thread " + name + " completed publishing.");
					
					while(sigint.getQueueSize() > 0)
					{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					System.out.println("Thread " + name + " completed.");
					threads.remove(Thread.currentThread());
				}
			});
			threads.add(me);
			me.start();
		}
				
		final Thread exitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					if(threads.size() == 0) {
						System.out.println("Exiting...");
						System.exit(0);
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		exitThread.start();

	}	
}
