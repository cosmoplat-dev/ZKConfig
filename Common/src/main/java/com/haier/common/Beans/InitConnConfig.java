package com.haier.common.Beans;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.haier.common.utils.zookeeper.ZkNodeWatcher;
import com.haier.common.utils.zookeeper.ZooKeeperUtil;
import com.haier.common.znodewatcher.RedisZnodeWatcher;
/**
 * <p>Title: InitConnConfig</p>  
 * Description: <pre>初始化连接属性工具类</pre>   
 */
public class InitConnConfig {
	
	public static void init(String address) {
		initMysqlConfig(getMysqlConnConfig(address, null));
		initRedisConfig(getRedisConnfig(address, null));
		//给redis节点添加监听
		RedisZnodeWatcher redisZnodeWatcher = new RedisZnodeWatcher();
		setZkNodeWatcher(address, "/youlanw/c/main/redis", redisZnodeWatcher);
		
	}
	/**
	 * 
	 * <p>Title: getRedisConnfig</p>  
	 * Description: <pre>从zookeeper中获取redis连接属性RedisConnConfig</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月31日  
	 * @param address zookeeper地址
	 * @param znode redis信息存放节点路径默认为 /youlanw/c/main/redis
	 * @return
	 */
	public static RedisConnConfig getRedisConnfig(String address,String znode) {
		ZooKeeperUtil zkUtil =ZooKeeperUtil.getInstance(address,3000);
		if(StringUtils.isBlank(znode)) {
			znode = "/youlanw/c/main/redis";
		}
		String mysqlInfo = zkUtil.getZnodeData(znode);
		return JSONObject.parseObject(mysqlInfo, RedisConnConfig.class);
	}
	/**
	 * 
	 * <p>Title: getMysqlConnConfig</p>  
	 * Description: <pre>从zookeeper中获取MySQL连接属性MysqlConnConfig</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月31日  
	 * @param address zookeeper地址
	 * @param znode mysql信息存放节点路径默认为 /youlanw/c/main/mysql
	 * @return
	 */
	public static MysqlConnConfig getMysqlConnConfig(String address,String znode) {
		ZooKeeperUtil zkUtil =ZooKeeperUtil.getInstance(address,3000);
		if(StringUtils.isBlank(znode)) {
			znode = "/youlanw/c/main/mysql";
		}
		String mysqlInfo = zkUtil.getZnodeData(znode);
		return JSONObject.parseObject(mysqlInfo, MysqlConnConfig.class);
	}
	/**
	 * 
	 * <p>Title: initMysqlConfig</p>  
	 * Description: <pre>设置mysql属性设置到系统属性中</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月31日  
	 * @param address zookeeper地址
	 * @param znode mysql信息存放节点路径默认为 /youlanw/c/main/mysql
	 */
	public static void initMysqlConfig(MysqlConnConfig mysqlConnConfig) {
		System.setProperty("jdbcDriver", mysqlConnConfig.getJdbcDriver());
		System.setProperty("jdbcUrl", mysqlConnConfig.getJdbcUrl());
		System.setProperty("jdbcUsername", mysqlConnConfig.getJdbcUsername());
		System.setProperty("jdbcPassword", mysqlConnConfig.getJdbcPassword());
	}
	/**
	 * 
	 * <p>Title: initRedisConfig</p>  
	 * Description: <pre>设置redis属性设置到系统属性中</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月31日  
	 * @param address zookeeper地址
	 * @param znode redis信息存放节点路径默认为 /youlanw/c/main/redis
	 */
	public static void initRedisConfig(RedisConnConfig redisConnConfig) {
		System.setProperty("redisAddress", redisConnConfig.getRedisAddress());
		System.setProperty("redisPassword", redisConnConfig.getRedisPassword());
		System.setProperty("redisPort", redisConnConfig.getRedisPort().toString());
		System.setProperty("redisDbindex", redisConnConfig.getRedisDbindex().toString());
	}
	/**
	 * <p>Title: setZkNodeWatcher</p>  
	 * Description: <pre>设置节点监听，subscribeChildChanges，subscribeDataChanges</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月31日  
	 * @param address zk地址
	 * @param znode  监听节点
	 * @param watcher 自定义watcher重写方法
	 */
	public static void setZkNodeWatcher(String address,String znode,ZkNodeWatcher watcher) {
		ZooKeeperUtil.getInstance(address,3000).subscribeChildChanges(znode, watcher);
		ZooKeeperUtil.getInstance(address,3000).subscribeDataChanges(znode, watcher);
	}
	
}
