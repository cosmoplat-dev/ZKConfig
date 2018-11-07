package com.haier.zkconfig.provider.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.haier.zkconfig.provider.Enum.InitZnodePath;
import com.haier.zkconfig.provider.impl.ZookeeperServiceImpl;
import com.haier.common.utils.zookeeper.ZooKeeperUtil;

/**  
 * <p>Title: ZookeeperNodeUtil</p>  
 * Description: <pre>zookeeper初始化连接，及部分节点操作</pre>   
 
 */
public class ZookeeperNodeUtil {
	
    private static final int SESSION_TIMEOUT=3000;
	private static ZooKeeperUtil zk;
	private static String address;
	private static int sessionTimeout;
	/**
	 * 
	 * <p>Title: initConnect</p>  
	 * Description: <pre>初始化zookeeper连接</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月20日
	 */
	static{
		
		InputStream in = null;
		try {
			String dubboEnv = System.getProperty("dubboEnv");
			System.out.println(dubboEnv);
			String dubboProPertiesPath = "";
			if("dubbo/dubbo.properties".equals(dubboEnv) || dubboEnv == null || dubboEnv == "") {
				dubboProPertiesPath = "dubbo/dubbo.properties";
				in = ZookeeperServiceImpl.class.getClassLoader().getResourceAsStream(dubboProPertiesPath);
			} else {
				dubboProPertiesPath = dubboEnv.substring(7);
				File file = new File(dubboProPertiesPath);
				in = new FileInputStream(file);
			}
			Properties properties = new Properties();
			properties.load(in);
			address = properties.getProperty("dubbo.zookeeper.address");
			if(properties.get("dubbo.zookeeper.sessionTimeout") != null) {
				try {
					sessionTimeout = (int) properties.get("dubbo.zookeeper.sessionTimeout");
				} catch (Exception e) {
					sessionTimeout = SESSION_TIMEOUT;
				}
			} else {
				sessionTimeout = SESSION_TIMEOUT;
			}
			zk = ZooKeeperUtil.getInstance(address,sessionTimeout);
			System.out.println("zookeeper连接成功...");
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
	public void init() {
		initCreateNode();
	}
	/**
	 * 
	 * <p>Title: getZooKeeperUtil</p>  
	 * Description: <pre>获取ZooKeeperUtil</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月20日  
	 * @return
	 */
	public static ZooKeeperUtil getZooKeeperUtil() {
		return zk;
	}

	/**
	 * 
	 * <p>Title: initCreateNode</p>  
	 * Description: <pre>初始化创建节点</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月20日
	 */
	public void initCreateNode() {
		for(InitZnodePath node : InitZnodePath.values()) {
			if(!zk.isExist(node.getValue())) {
				zk.recursionCreate(node.getValue());
			}
		}
	}
	
	
}
