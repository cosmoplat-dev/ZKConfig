package com.haier.zkconfig.provider.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.haier.zkconfig.interfaces.ZookeeperService;
import com.haier.zkconfig.interfaces.model.Znode;
import com.haier.common.utils.zookeeper.ZkACLType;
import com.haier.common.utils.zookeeper.ZooKeeperUtil;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Service;

import com.haier.zkconfig.provider.util.ZookeeperNodeUtil;

/**
 * 
 * <p>Title: ZookeeperServiceImpl</p>  
 * Description: <pre>zookeeper服务实现</pre>   
 */
@Service(value="zookeeperService")
public class ZookeeperServiceImpl implements ZookeeperService {
	
	private static ZooKeeperUtil zk = ZookeeperNodeUtil.getZooKeeperUtil();
	
	/**
	 * 查询子节点,该方法禁止查询根节点（“/”）
	 */
	@Override
	public List<String> getChilden(String path) {
		if("/".equals(path)) {
			return null;
		}
		List<String> list = null;
		list = zk.getChilden(path);
		System.out.println("list.size : " + list.size());
		return list;
	}
	
	@Override
	public String getZnodeDate(String path) {
		return zk.getZnodeData(path);
	}

	@Override
	public boolean addPersistentZnode(String znode, String data,Map<String,Map<String,List<ZkACLType>>> map) {
		if(map==null) {
			return zk.addPersistentZnode(znode, data);
		} else {
			return zk.addZnodeDataWithScheme(znode, data, map, CreateMode.PERSISTENT);
		}
	}

	@Override
	public boolean addEphemeralZnode(String znode, String data) {
		zk.subscribeDataChanges(znode, new ZkNodeWatcherImpl());
		return zk.addEphemeralZnode(znode, data);
	}

	@Override
	public boolean isExits(String path) {
		return zk.isExist(path);
	}
	
	/**
	 * 查询path和path路径下的所有子节点及内容，该方法禁止查询根节点（“/”）
	 */
	@Override
	public Znode getChildenAndData(String path) {
		if(!zk.isExist(path) || "/".equals(path)) {
			return null;
		}
		Znode znode = new Znode();
		znode.setPath(path);
		String name = path.substring(path.lastIndexOf("/")+1);
		if(name == null || name == "" || name.length() <= 0) {
			name = "/";
		}
		znode.setName(name);
		znode.setData(zk.getZnodeData(path));
		String parentPath = path.substring(0, path.lastIndexOf("/"));
		String parentName = "";
		if(parentPath == null || parentPath == "" || parentPath.length() <= 0) {
			parentName = "/";
		} else {
			parentName = parentPath.substring(parentPath.lastIndexOf("/")+1);
		}
		znode.setParentName(parentName);
		znode = getChildenZnode(znode);
		return znode;
	}
	/**
	 * 
	 * <p>Title: getChildenZnode</p>  
	 * Description: <pre>将子节点信息递归添加到Znode中,获取znode和znode下的所有子节点的路径和内容信息</pre>  
	 * @param znode
	 * @return
	 */
	private Znode getChildenZnode(Znode znode) {
		List<Znode> znodelist = new ArrayList<>();
		List<String> list = zk.getChilden(znode.getPath());
		if(list != null && list.size() > 0) {
			list.forEach(childen -> {
				Znode cZnode = new Znode();
				String path = "";
				if("/".equals(znode.getPath())) {
					path = "/" + childen;
				} else {
					path = znode.getPath() + "/" + childen;
				}
				cZnode.setPath(path);
				cZnode.setName(childen);
				cZnode.setData(zk.getZnodeData(path));
				cZnode.setParentName(znode.getName());
				if(zk.getChilden(path) != null && zk.getChilden(path).size() > 0) {
					getChildenZnode(cZnode);
				}
				znodelist.add(cZnode);
			});
		}
		znode.setChilden(znodelist);
		return znode;
	}
	
	@Override
	public boolean updateZnodeData(String path, String data) {
		return zk.updateZnode(path, data);
	}

	@Override
	public boolean deleteZnode(String path) {
		return zk.deleteZnode(path);
	}

	@Override
	public boolean recursionDeleteNodes(String path) {
		return zk.recursionDelete(path);
	}
	
	@Override
	public boolean recursionCreateNodes(String path,Map<String,Map<String,List<ZkACLType>>> map) {
		if(map==null) {
			return zk.recursionCreate(path);
		} else {
			return zk.recursionCreateWithACL(path, map);
		}
	}

	@Override
	public boolean setZnodeAcl(String path, Map<String, Map<String, List<ZkACLType>>> map) {
		return zk.setZnodeACL(path, map);
	}


}
