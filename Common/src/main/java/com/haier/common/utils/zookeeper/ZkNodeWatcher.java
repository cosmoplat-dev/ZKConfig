package com.haier.common.utils.zookeeper;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
public class ZkNodeWatcher{
	
	public ZkClient zkClient;
	
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
			}

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("节点数据删除事件 >> 删除的节点为：" + dataPath);  
			}
			
		});
	}

}
