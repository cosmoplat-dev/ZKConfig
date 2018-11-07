package com.haier.common.znodewatcher;

import org.I0Itec.zkclient.IZkDataListener;

import com.alibaba.fastjson.JSONObject;
import com.haier.common.Beans.RedisConnConfig;
import com.haier.common.utils.redis.RedisPoolUtil;
import com.haier.common.utils.zookeeper.ZkNodeWatcher;
/**
 * 
 * <p>Title: RedisZnodeWatcher</p>  
 * Description: <pre>redis节点监听器，监听数据变化并重建RedisPoolUtil中的JedisPool</pre>   
 */
public class RedisZnodeWatcher extends ZkNodeWatcher{
	
	public void subscribeChildChanges(String path) {
		zkClient.subscribeChildChanges(path, (parentPath, currentChilds) -> {
			System.out.println("parentPath：" + parentPath);  
            System.out.println("currentChilds：" + currentChilds); 
		});
	}

	public void subscribeDataChanges(String path) {
		zkClient.subscribeDataChanges(path, new IZkDataListener() {
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				System.out.println("节点数据更新事件 >> 节点为：" + dataPath + "，变更数据为：" + data);  
				RedisConnConfig redisConnConfig = JSONObject.parseObject(data.toString(), RedisConnConfig.class);
				System.out.println("节点数据更新事件 >>" + redisConnConfig.getRedisAddress());
	            System.out.println("节点数据更新事件 >>" + redisConnConfig.getRedisPassword());
	            System.out.println("节点数据更新事件 >>" + redisConnConfig.getRedisPort());
	            System.out.println("节点数据更新事件 >>" + redisConnConfig.getRedisDbindex());
				//重建pool
				RedisPoolUtil.resetJedisPool(null, 
						redisConnConfig.getRedisAddress(),
						redisConnConfig.getRedisPassword(),
						redisConnConfig.getRedisPort(),
						redisConnConfig.getRedisDbindex()
						);
				System.out.println("重建redis pool");
			}

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("节点数据删除事件 >> 删除的节点为：" + dataPath);  
			}
			
		});
	}
}
