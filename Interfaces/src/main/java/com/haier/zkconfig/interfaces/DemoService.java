package com.haier.zkconfig.interfaces;

import java.util.List;

public interface DemoService {
	/**
	 * 
	 * <p>Title: helloWorld</p>  
	 * Description: <pre></pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月12日  
	 * @param str
	 * @return
	 */
	String helloWorld(String str);
	
	List<String> getChilden(Long id);
	
}
