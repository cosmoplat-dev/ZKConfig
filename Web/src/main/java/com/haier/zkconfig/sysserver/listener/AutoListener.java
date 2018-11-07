package com.haier.zkconfig.sysserver.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.haier.zkconfig.sysserver.zookeeper.SpringUtils;

public class AutoListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("销毁...");
		SpringUtils.destroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("AutoListener初始化...");
		setSystemProperty();
	}
	
	
	private void setSystemProperty() {
		InputStream in = null;
		String dubboEnv = System.getProperty("dubboEnv");
		System.out.println("dubboEnv : " + dubboEnv);
		if(dubboEnv!= null && dubboEnv != "") {
			if(dubboEnv.startsWith("file")) {
				dubboEnv = dubboEnv.substring(7);
			}
		} else {
			throw new RuntimeException("系统属性dubboEnv为空");
		}
		File file = new File(dubboEnv);
		try {
			in = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(in);
			String znodeAclIp = (String) properties.get("znode.acl.ip");
			if(znodeAclIp != null && znodeAclIp != "") {
				System.setProperty("znode.acl.ip", znodeAclIp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(in!= null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
