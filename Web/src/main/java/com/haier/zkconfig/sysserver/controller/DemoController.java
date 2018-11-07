package com.haier.zkconfig.sysserver.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haier.zkconfig.interfaces.DemoService;

@Controller
@RequestMapping("demo")
public class DemoController {
	
	@Resource(name="demoService")
	private DemoService demoService;
	
	@ResponseBody
	@RequestMapping(value="/hello")
	public String hello(String str) {
		return demoService.helloWorld(str);
	}
}
