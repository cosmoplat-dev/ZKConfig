package com.haier.zkconfig.provider.impl;

import com.haier.common.utils.zookeeper.ZkNodeWatcher;
import org.I0Itec.zkclient.IZkDataListener;


public class ZkNodeWatcherImpl extends ZkNodeWatcher {

	@Override
	public void subscribeChildChanges(String path) {
		zkClient.subscribeChildChanges(path, (parentPath, currentChilds) -> {
			System.out.println("重写");
			System.out.println("parentPath：" + parentPath);  
            System.out.println("currentChilds：" + currentChilds); 
		});
	}

	@Override
	public void subscribeDataChanges(String path) {
		zkClient.unsubscribeDataChanges(path, new IZkDataListener() {
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				System.out.println("重写");
				System.out.println("节点数据更新事件 >> 节点为：" + dataPath + "，变更数据为：" + data);  
			}
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				System.out.println("重写");
				System.out.println("节点数据删除事件 >> 删除的节点为：" + dataPath);  
			}
		});
	}

}
