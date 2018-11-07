package com.haier.zkconfig.sysserver.zookeeper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haier.zkconfig.interfaces.ZookeeperService;
import com.haier.common.utils.zookeeper.ZkACLType;


public class ZkNodeInit {
	
	private static final Logger logger = LoggerFactory.getLogger(ZkNodeInit.class);
	
	public static void init() {
		System.out.println("ZkNodeInit初始化..");
		InputStream in = null;
		try {
			String dubboProPertiesPath = "dubbo/znode.properties";
			in = ZkNodeInit.class.getClassLoader().getResourceAsStream(dubboProPertiesPath);
			Properties properties = new Properties();
			properties.load(in);
			String projects = properties.getProperty("ZKConfig.c.projects");
			String databases = properties.getProperty("ZKConfig.c.databases");
			String caches = properties.getProperty("ZKConfig.c.cache");
			logger.info("projects : " + projects);
			logger.info("databases : " + databases);
			logger.info("caches : " + caches);
	        ZookeeperService zkService = SpringUtils.getBean(ZookeeperService.class);
	        String[] databaseArr = databases.split(",");
	        String[] projectArr = projects.split(",");
	        String[] cacheArr = caches.split(",");
	        for(String project : projectArr) {
	        	for(String database : databaseArr) {
	        		String path = "/com/haier/c/" + project + "/" + database;
	        		Map<String,Map<String,List<ZkACLType>>> map = ZnodeACLUtil.getInitACLList();
	        		if(zkService.isExits(path)) {
	        			System.out.println("初始化节点"+path+"已存在,设置权限");
	        			zkService.setZnodeAcl(path, map);
	        		} else {
	        			System.out.println("初始化节点"+path+"不存在,创建并设置权限");
	        			zkService.recursionCreateNodes(path, map);
	        		}
	        	}
	        	for(String cache : cacheArr) {
	        		String path = "/com/haier/c/" + project + "/" + cache;
	        		Map<String,Map<String,List<ZkACLType>>> map = ZnodeACLUtil.getInitACLList();
	        		if(zkService.isExits(path)) {
	        			System.out.println("初始化节点"+path+"已存在,设置权限");
	        			zkService.setZnodeAcl(path, map);
	        		} else {
	        			System.out.println("初始化节点"+path+"不存在,创建并设置权限");
	        			zkService.recursionCreateNodes(path, map);
	        		}
	        	}
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        
	}
	public void destroy() {
		System.out.println("ZkNodeInit销毁");
	}
}
