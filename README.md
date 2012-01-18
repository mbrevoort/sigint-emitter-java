A client API for emitting measurements and counters into the SIGINT analytics system.

Configuration
=============

	application=app1
	node=node1
	queue.size=10000
	mode=amqp
	amqp=one, two
	ampq.one.connection=amqp://username:password@host1:port/virtualhost
	amqp.one.exchange=sigint
	ampq.two.connection=amqp://username:password@host2:port/virtualhost
	amqp.two.exchange=sigint
	publishers=1

* `application` The name of application

* `node` The name of the node running the application

* `queue.size` The max number of emissions to be queued for sending.  The earliest queued entries will be dropped to make room for newer ones when the queue is full

* `mode` Specifying the publish mode.  Valid values are 'noop' and 'amqp'.

* `amqp` The list of keys for each broker to load-balance publish into

* `amqp.<key>.exchange` The name of the exchange to publish emissions into for the <key> broker

* `ampq.<key>.connection` An AMQP URI describing how to connect to the AMQP broker for <key>

Fluent API
==========

Static Singleton vs Context
===========================
SIGINT can be used as either a static singleton, or from a context (i.e. per-thread).

To configure a static singleton, call `SIGINT.configureCurrent` when your application starts up:

	//Do this at app startup
	SIGINT.configureCurrent(new File("/path/to/sigint.properties"));

	...

	//Then you can do this from anywhere in your app
	SIGINT.current().time("myop").duration(10).emit();

To configure a contextual instance:

	//Create a context that can be passed around or stored with a thread
	Context sigint = SIGINT.createContext(new File("/path/to/sigint.properties"));

	...

	//Then you can do this from anywhere that is injected with the context object
	sigint.time("myop").duration(10).emit();

Counter
-------
	//Emit a counter with a data value of 1
	SIGINT.current().time("myop").emit();

	//Emit a counter with a data value of 14
	SIGINT.current().time("myop").times(14).emit();


Timer (Explicit Duration)
-------------------------
	//Emit a timer with a data value of 100 milliseconds
	SIGINT.current().time("sp_slow_sproc").duration(100).emit();


Timer (Implicit Duration)
-------------------------
	Stopwatch timer = SIGINT.current().time("sp_slow_sproc").start();

	...some timeable work...
	
	//Stop the stopwatch and emit a timer with the data value of the time measured
	timer.emit(); 

Emitting The Result Of A Call To An Upstream Service
----------------------------------------------------
All the operation metrics include an `againstApplication()` method which can be used for specifying the name of the upstream application:

	//Emit a timer with a data value of 10 milliseconds against the validate_token operation of the idm system:
	SIGINT.current().time("validate_token").againstApplication("idm").duration(10).emit();

Announcement
------------
An announcement should be emitted whenever the application is started or restarted.  It includes a hook to supply the version of the application, as well as the versions of any stack dependencies.

	SIGINT.current().announce().version("1.2.3").addStackItem("java", "1.2.3").addStackItem("junit","1.2.6").emit();

	...or many stack items can be added at once...

	Map<String, String> stack = new HashMap<String, String>();
	stack.put("java", "1.2.3");
	stack.put("junit", "1.2.6");
	SIGINT.current().announce().version("1.2.3").addStackItems(stack).emit();

Logging
=======
All logging is performed via the Simple Log4J interface.

Building
========
Run `ant release` and look for the jars in the dist folder.  The "-nodeps.jar" file has all of the dependencies merged into one jar.

ChangeLog
=========

v0.1 - 1/9/2012 - Emission Spec v1
	
	* Initial cut

v0.2 - 1/18/2012 - Emission Spec v1
	
	* Added support for publishing into multiple AMQP brokers in a round-robin way.