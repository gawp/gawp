package com.metabroadcast;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.metabroadcast.includes.www.IncludesController;

public class SpringTest {

	@Test
	public void testThatTheContextCanBeBuilt() throws Exception {
		ApplicationContext context = new AnnotationConfigApplicationContext(BeigeModule.class);
		context.getBean(IncludesController.class);
	}
	
}
