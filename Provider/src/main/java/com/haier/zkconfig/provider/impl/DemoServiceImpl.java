package com.haier.zkconfig.provider.impl;

import java.util.ArrayList;
import java.util.List;

import com.haier.zkconfig.interfaces.DemoService;
import org.springframework.stereotype.Service;

/**
 * 
 * <p>Title: DemoServiceImpl</p>  
 * Description: <pre>provider service demo</pre>   
 */
@Service(value="demoService")
public class DemoServiceImpl implements DemoService {
	
	@Override
	public String helloWorld(String str) {
		return "hello " + str;
	}

	@Override
	public List<String> getChilden(Long id) {
		List<String> demo = new ArrayList<String>();
		demo.add(String.format("Permission_%d", id - 1));
		demo.add(String.format("Permission_%d", id));
		demo.add(String.format("Permission_%d", id + 1));

		return demo;
	}
	
	

}
