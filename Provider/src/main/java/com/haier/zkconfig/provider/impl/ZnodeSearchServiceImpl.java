package com.haier.zkconfig.provider.impl;
import com.haier.zkconfig.interfaces.ZnodeSearchService;
import com.haier.common.utils.zookeeper.ZooKeeperUtil;
import org.springframework.stereotype.Service;

import com.haier.zkconfig.provider.Enum.InitZnodePath;
import com.haier.zkconfig.provider.util.ZookeeperNodeUtil;

@Service(value="znodeSearchService")
public class ZnodeSearchServiceImpl implements ZnodeSearchService {

	private static final String ZK_C_NODE=InitZnodePath.ZK_C_NODE.getValue();
	private ZooKeeperUtil zk = ZookeeperNodeUtil.getZooKeeperUtil();
	
	@Override
	public String searchCZnode(String projectName,String type) {
		String path = ZK_C_NODE +"/"+projectName+"/"+type;
		System.out.println(path);
		if(zk.isExist(path)) {
			return zk.getZnodeData(path);
		}
		return null;
	}
	
}
