package com.sundoctor.quartz.cluster.example;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service("simpleService")
public class SimpleService implements Serializable{
	
	private static final long serialVersionUID = 122323233244334343L;
	private static final Logger logger = LoggerFactory.getLogger(SimpleService.class);
	
	public void testMethod1(){  
        logger.info("testMethod1.......1");  
    }  
	
	public void testMethod2(){
		logger.info("testMethod2......2");	
	}
}
