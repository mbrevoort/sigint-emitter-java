package com.pearson.sigint.emitter;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import com.pearson.sigint.emitter.SIGINTConfig.FORMAT;
import com.pearson.sigint.emitter.types.Announcement;
import com.pearson.sigint.emitter.types.Counter;
import com.pearson.sigint.emitter.types.Timer;
import com.pearson.sigint.emitter.types.Error;

public class EmissionTest {

	@Test
	public void counter() throws JsonGenerationException, JsonMappingException, IOException {
		Counter withTargetApp = new Counter("app1", "node1", null, FORMAT.BSON).againstApplication("app2").operation("op");
		Counter withoutTargetApp = new Counter("app1", "node1", null, FORMAT.BSON).operation("op");
		Counter withIncrement = new Counter("app1", "node1", null, FORMAT.BSON).operation("op").times(10);

		String expected_withTargetApp = "{\"w\":" + withTargetApp._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"c\",\"d\":1,\"g\":\"app2\",\"o\":\"op\"}";
		String expected_withoutTargetApp = "{\"w\":" + withoutTargetApp._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"c\",\"d\":1,\"o\":\"op\"}";
		String expected_withIncrement = "{\"w\":" + withIncrement._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"c\",\"d\":10,\"o\":\"op\"}";
		
		Assert.assertEquals("withTargetApp", expected_withTargetApp, withTargetApp.getJson());
		Assert.assertEquals("withoutTargetApp", expected_withoutTargetApp, withoutTargetApp.getJson());
		Assert.assertEquals("withIncrement", expected_withIncrement, withIncrement.getJson());
	}
	
	@Test
	public void announcement() throws JsonGenerationException, JsonMappingException, IOException {
		Map<String, String> stack = new HashMap<String, String>();
		stack.put("java", "1.2.5");
		stack.put("junit", "1.2.6");
		
		Announcement withSingleStack = new Announcement("app1", "node1", null, FORMAT.BSON).version("1.2.3").addStackItem("java", "1.2.4");
		Announcement withListStack = new Announcement("app1", "node1", null, FORMAT.BSON).version("1.2.3").addStackItems(stack);

		String expected_withSingleStack = "{\"w\":" + withSingleStack._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"a\",\"d\":{\"v\":\"1.2.3\",\"s\":[{\"n\":\"java\",\"v\":\"1.2.4\"}]}}";
		String expected_withListStack = "{\"w\":" + withListStack._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"a\",\"d\":{\"v\":\"1.2.3\",\"s\":[{\"n\":\"java\",\"v\":\"1.2.5\"},{\"n\":\"junit\",\"v\":\"1.2.6\"}]}}";
		
		Assert.assertEquals("withSingleStack", expected_withSingleStack, withSingleStack.getJson());
		Assert.assertEquals("withListStack", expected_withListStack, withListStack.getJson());
	}
	
	@Test
	public void timer() throws JsonGenerationException, JsonMappingException, IOException {
		Timer withTargetApp = new Timer("app1", "node1", null, FORMAT.BSON).againstApplication("app2").operation("op").duration(10);
		Timer withoutTargetApp = new Timer("app1", "node1", null, FORMAT.BSON).operation("op").duration(11);

		String expected_withTargetApp = "{\"w\":" + withTargetApp._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"t\",\"g\":\"app2\",\"o\":\"op\",\"d\":10}";
		String expected_withoutTargetApp = "{\"w\":" + withoutTargetApp._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"t\",\"o\":\"op\",\"d\":11}";
		
		Assert.assertEquals("withTargetApp", expected_withTargetApp, withTargetApp.getJson());
		Assert.assertEquals("withoutTargetApp", expected_withoutTargetApp, withoutTargetApp.getJson());
	}
	
	@Test
	public void error() throws JsonGenerationException, JsonMappingException, IOException {
		Error withTargetApp = new Error(new Exception("something bad"), "app1", "node1", null, FORMAT.BSON).againstApplication("app2").operation("op");
		Error withoutTargetApp = new Error(new Exception("something else bad"), "app1", "node1", null, FORMAT.BSON).operation("op");

		String expected_withTargetApp = "{\"w\":" + withTargetApp._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"e\",\"d\":{\"msg\":\"something bad\"},\"g\":\"app2\",\"o\":\"op\"}";
		String expected_withoutTargetApp = "{\"w\":" + withoutTargetApp._getTime() + ",\"v\":1,\"s\":{\"a\":\"app1\",\"n\":\"node1\"},\"t\":\"e\",\"d\":{\"msg\":\"something else bad\"},\"o\":\"op\"}";
		
		Assert.assertEquals("withTargetApp", expected_withTargetApp, withTargetApp.getJson());
		Assert.assertEquals("withoutTargetApp", expected_withoutTargetApp, withoutTargetApp.getJson());
	}

	@Test
	public void emitCallsPublish() {
		ManagedAmqp amqp = mock(ManagedAmqp.class);
		
		new Timer("app1", "node1", amqp, FORMAT.BSON).emit();
		
		verify(amqp, times(1)).publish(any(Timer.class));
	}
}
