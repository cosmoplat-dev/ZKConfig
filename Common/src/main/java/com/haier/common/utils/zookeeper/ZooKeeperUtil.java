package com.haier.common.utils.zookeeper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
/**
 * 
 * <p>Title: ZooKeeperUtil</p>  
 * Description: <pre>zookeeper操作工具类</pre>   
 */
public class ZooKeeperUtil{
	/**
	 * 默认session时间
	 */
	private static final int SESSION_TIMEOUT=3000;
	private static volatile ZooKeeperUtil instance = null;
	private static ZooKeeper zookeeper;
	private ZkClient zkClient;
	private ZooKeeperUtil() {
		
	}
	/**
	 * 
	 * <p>Title: </p>  
	 * Description: <pre></pre> 
	 * @param address zookeeper连接地址 host:port
	 * @param sessionTimeout session timeout in milliseconds
	 */
	private ZooKeeperUtil(String address, Integer sessionTimeout) {
		try {
			if(sessionTimeout == null) {
				sessionTimeout = SESSION_TIMEOUT;
			}
			zookeeper = new ZooKeeper(address, sessionTimeout,null);
			this.zkClient = new ZkClient(address,sessionTimeout);
			zkClient.setZkSerializer(new ZkSerializerImpl());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ZooKeeperUtil getInstance(String address, Integer sessionTimeout) {
		if(instance == null) {
			synchronized(ZooKeeperUtil.class) {
				if(instance == null) {
					instance = new ZooKeeperUtil(address, sessionTimeout) ;
				}
			}
		}
		return instance;
	}
	
	/**
	 * 
	 * <p>Title: isExist</p>  
	 * Description: <pre>判断节点是否存在</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path
	 * @return
	 */
	public boolean isExist(String path) {
		try {
			Stat stat = zookeeper.exists(path, true);
			if(stat!= null) {
				return true;
			} else {
				return false ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false ;
	}
	/**
	 * 
	 * <p>Title: addZnodeData</p>  
	 * Description: <pre>创建znode结点</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path 节点路径
	 * @param data 节点数据
	 * @param mode 节点类型
	 * @return true 创建结点成功 false表示结点存在
	 */
	public boolean addZnodeData(String path,String data,CreateMode mode) {
		return addZnodeDataWithScheme(path, data, null, null, mode);
	}
	
	public boolean addZnodeDataByIp(String path,String data,Map<String,List<ZkACLType>> map,CreateMode mode) {
		return addZnodeDataWithScheme(path, data, "ip", map, mode);
	}
	public boolean addZnodeDataByDigest(String path,String data,Map<String,List<ZkACLType>> map,CreateMode mode) {
		return addZnodeDataWithScheme(path, data, "digest", map, mode);
	}
	public boolean addZnodeDataByWorld(String path,String data,List<ZkACLType> list,CreateMode mode) {
		Map<String,List<ZkACLType>> map = new HashMap<>();
		map.put("anyone", list);
		return addZnodeDataWithScheme(path, data, "world", map, mode);
	}
	/**
	 * 
	 * <p>Title: addZnodeDataWithScheme</p>  
	 * Description: <pre>添加节点和数据,并指定，权限方案，权限类型，节点类型</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月11日  
	 * @param path 节点类型
	 * @param data 节点数据
	 * @param scheme 权限方案 默认world，可指定为ip或digest
	 * @param map 权限类型组合 <br/>
	 * 			如果scheme是ip，key:放设置的用户名密码（例:"192.168.80.130"）,value:放设置的权限列表([ZkDigestACL.READ,ZkDigestACL.ALL])。<br/>
	 * 			如果scheme是digest，key:放设置的用户名密码（例:"username:password"）,value:放设置的权限列表([ZkDigestACL.READ,ZkDigestACL.ALL])
	 * @param mode 节点类型
	 * @return
	 */
	public boolean addZnodeDataWithScheme(String path,String data,String scheme, Map<String,List<ZkACLType>> map,CreateMode mode) {
		try {
			Stat stat = zookeeper.exists(path,true);
			if(stat == null) {
				List<ACL> acl = Ids.OPEN_ACL_UNSAFE;
				if(map != null) {
					List<ACL> aclList = getACLList(scheme, map);
					if(aclList != null && aclList.size() > 0) {
						acl = aclList;
					}
				}
				if(data != null) {
					zookeeper.create(path, data.getBytes(), acl, mode);
				} else {
					zookeeper.create(path, null, acl, mode);
				}
				return true;
			} else {
				System.out.println("znode:"+path+",已存在");  
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建znode："+path+"出现问题！！",e);
		}
		return false; 
	}
	public boolean addZnodeDataWithScheme(String path,String data,Map<String,Map<String,List<ZkACLType>>> map,CreateMode mode) {
		try {
			Stat stat = zookeeper.exists(path,true);
			if(stat == null) {
				List<ACL> acl = Ids.OPEN_ACL_UNSAFE;
				if(map != null) {
					List<ACL> aclList = getACLList(map);
					if(aclList != null && aclList.size() > 0) {
						acl = aclList;
					}
				}
				if(data != null) {
					zookeeper.create(path, data.getBytes(), acl, mode);
				} else {
					zookeeper.create(path, null, acl, mode);
				}
				return true;
			} else {
				System.out.println("znode:"+path+",已存在");  
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建znode："+path+"出现问题！！",e);
		}
		return false; 
	}
	
	
	/**
	 * 
	 * <p>Title: addPersistentZnode</p>  
	 * Description: <pre>创建永久Znode节点</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path 节点路径
	 * @param data 节点数据
	 * @return true 创建结点成功 false表示结点存在
	 */
	public boolean addPersistentZnode(String path,String data) {
		return addZnodeData(path, data, CreateMode.PERSISTENT);
	}
	/**
	 * 
	 * <p>Title: addEphemeralZnode</p>  
	 * Description: <pre>创建临时Znode节点</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path 节点路径
	 * @param data 节点数据
	 * @return true 创建结点成功 false表示结点存在
	 */
	public boolean addEphemeralZnode(String path,String data) {
		return addZnodeData(path, data, CreateMode.EPHEMERAL);
	}
	/**
	 * 
	 * <p>Title: updateZnode</p>  
	 * Description: <pre>修改节点内容</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path 节点路径
	 * @param data 节点数据
	 * @return true 修改结点成功 false修改结点失败
	 */
	public boolean updateZnode(String path,String data) {
		Stat stat;
		try {
			stat = zookeeper.exists(path, true);
			if( stat != null) {
				if(data != null) {
					zookeeper.setData(path, data.getBytes(), stat.getVersion());
				} else {
					zookeeper.setData(path, null, stat.getVersion());
				}
				return true;
			} else {
				System.out.println("znode:"+path+",不存在");  
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("修改znode："+path+"出现问题！！",e);
		}
		return false;
	}
	/**
	 * 
	 * <p>Title: deleteZnode</p>  
	 * Description: <pre>删除节点</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path 节点路径
	 * @return true 删除结点成功 false删除结点失败
	 */
	public boolean deleteZnode(String path) {
		Stat stat;
		try {
			stat = zookeeper.exists(path, true);
			if(stat != null) {
				List<String> subPaths=zookeeper.getChildren(path, false);
				if(subPaths.isEmpty()) {
					zookeeper.delete(path, stat.getVersion());
					return true;
				} else {
					for(String subPath : subPaths) {
						deleteZnode(path + "/" + subPath);
					}
				}
			} else {
				System.out.println("znode:"+path+",不存在");  
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("删除znode："+path+"出现问题！！",e);
		}
		return false;
	}
	/**
	 * 
	 * <p>Title: getZnodeData</p>  
	 * Description: <pre>获取节点数据</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path 节点路径
	 * @return 节点内容
	 */
	public String getZnodeData(String path) {
		String data = null;
		Stat stat = null;
		try {
			stat = zookeeper.exists(path, true);
			if(stat != null) {
				byte[] datas = zookeeper.getData(path, true, stat);
				if(datas != null) {
					data = new String(datas);
					return data;
				}
			} else {
				System.out.println("znode:"+path+",不存在");  
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("取到znode："+path+"出现问题！！",e);
		}
		return null;
	}
	/**
	 * 
	 * <p>Title: getChilden</p>  
	 * Description: <pre>获取节点列表</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月13日  
	 * @param path
	 * @return 节点list
	 */
	public List<String> getChilden(String path){
		List<String> list = null;
		Stat stat = null;
		try {
			stat = zookeeper.exists(path, true);
			if(stat != null) {
				list = zookeeper.getChildren(path, true);
			} else {
				System.out.println("znode:"+path+",不存在");  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 
	 * <p>Title: recursionCreate</p>  
	 * Description: <pre>递归创建节点</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月19日  
	 * @param path
	 * @param data
	 * @return
	 */
	public boolean recursionCreate(String path) {
		if(!path.startsWith("/")) {
			return false;
		}
		if(!isExist(path)) {
			String parentNode = path.substring(0, path.lastIndexOf("/"));
			if(parentNode == null || parentNode == "" || parentNode.length() <= 0) {
				return addPersistentZnode(path, null);
			}
			if(!isExist(parentNode)) {
				recursionCreate(parentNode);
			}
		}
		return addPersistentZnode(path, null);
	}
//	/**
//	 * 
//	 * <p>Title: recursionCreateWithACL</p>  
//	 * Description: <pre>递归创建节点,只有path有设定权限，父节点都为world all</pre>  
//	 * @author wangchaoqun 
//	 * @date 2018年5月12日  
//	 * @param path
//	 * @param map 权限信息
//	 * @return
//	 */
//	public boolean recursionCreateWithACL(String path,Map<String,Map<String,List<ZkACLType>>> map) {
//		if(!path.startsWith("/")) {
//			return false;
//		}
//		String parentNode = path.substring(0,path.lastIndexOf("/"));
//		if(parentNode == null || parentNode == "" || parentNode.length() <= 0) {
//			return addZnodeDataWithScheme(path, null, map, CreateMode.PERSISTENT);
//		} else {
//			recursionCreate(parentNode);
//		}
//		return addZnodeDataWithScheme(path, null, map, CreateMode.PERSISTENT);
//	}
	/**
	 * 
	 * <p>Title: recursionCreateWithACL</p>  
	 * Description: <pre>递归创建节点,只有path有设定权限，父节点都为world all</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月12日  
	 * @param path
	 * @param map 权限信息
	 * @return
	 */
	public boolean recursionCreateWithACL(String path,Map<String,Map<String,List<ZkACLType>>> map) {
		if(!path.startsWith("/")) {
			return false;
		}
		String parentNode = path.substring(0,path.lastIndexOf("/"));
		if(parentNode == null || parentNode == "" || parentNode.length() <= 0) {
			return addZnodeDataWithScheme(path, null, map, CreateMode.PERSISTENT);
		} else {
			recursionCreateWithACL(parentNode,map);
		}
		return addZnodeDataWithScheme(path, null, map, CreateMode.PERSISTENT);
	}
	
	/**
	 * 
	 * <p>Title: recursionDelete</p>  
	 * Description: <pre>递归删除节点(禁止删除根节点 :/)</pre>  
	 * @author wangchaoqun 
	 * @date 2018年4月19日  
	 * @param path
	 * @return
	 */
	public boolean recursionDelete(String path) {
		if(!path.startsWith("/") || "/".equals(path)) {
			return false;
		}
		List<String> list = getChilden(path);
		if(list == null || list.size() <= 0) {
			return deleteZnode(path);
		}
		list.forEach(childenZnode -> {
			recursionDelete(path + "/" + childenZnode);
		});
		return deleteZnode(path);
	}
	
	/**
	 * 监听节点变化
	 * @param path
	 * @param watcher
	 */
	public void subscribeChildChanges(String path, ZkNodeWatcher watcher) {
		watcher.zkClient = this.zkClient;
		watcher.subscribeChildChanges(path);
	}
	/**
	 * 监听节点数据变化
	 * @param path
	 * @param watcher
	 */
	public void subscribeDataChanges(String path, ZkNodeWatcher watcher) {
		watcher.zkClient = this.zkClient;
		watcher.subscribeDataChanges(path);
	}
	/**
	 * 将指定的方案添加到这个连接中。
	 * <p>Title: addAuthInfo</p>  
	 * Description: <pre></pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月11日  
	 * @param scheme 权限方案
	 * @param auth username:password
	 * @return
	 */
	public boolean addAuthInfo(String scheme, String auth) {
		try {
			if(auth == null || auth == "") {
				return false;
			}
			zookeeper.addAuthInfo(scheme, auth.getBytes());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("zookeeper指定权限方案异常");
		}
	}
	
	private List<ACL> getACLList(String scheme,Map<String,List<ZkACLType>> map){
		if(map.isEmpty()) {
			return null;
		}
		List<ACL> list = new ArrayList<>();
		Set<String> set = map.keySet();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()) {
			String auth = iter.next();
			List<ZkACLType> aclList = map.get(auth);
			if(aclList != null) {
				aclList.forEach(acl -> {
					if("ip".equals(scheme)) {
						Id id = new Id("ip", auth);
						list.add(new ACL(acl.getAcl(),id));
					}
					if("world".equals(scheme)) {
						Id id = new Id("world", auth);
						list.add(new ACL(acl.getAcl(),id));
					}
					if("digest".equals(scheme)) {
						Id id = null;
						try {
							id = new Id("digest", DigestAuthenticationProvider.generateDigest(auth));
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
						list.add(new ACL(acl.getAcl(),id));
					}
				});
			}
		}
		return list;
	}
	
	
	/**
	 * 
	 * <p>Title: getACLList</p>  
	 * Description: <pre>将不同权限方案的各种权限信息放入map中，获取权限列表</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月12日  
	 * @param map
	 * @return
	 */
	public List<ACL> getACLList(Map<String,Map<String,List<ZkACLType>>> map) {
		if(map.isEmpty()) {
			return null;
		}
		List<ACL> list = new ArrayList<>();
		Set<String> schemeSet = map.keySet();
		Iterator<String> schemeIterator = schemeSet.iterator();
		while(schemeIterator.hasNext()) {
			String schemeKey = schemeIterator.next();
			Map<String,List<ZkACLType>> schemeMap = map.get(schemeKey);
			List<ACL> childList = getACLList(schemeKey, schemeMap);
			if(childList != null) {
				for(ACL acl : childList) {
					list.add(acl);
				}
			}
		}
		return list;
	}
	
	
	/**
	 * 
	 * <p>Title: setZnodeACL</p>  
	 * Description: <pre>给某个节点设置权限</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月11日  
	 * @param path 节点路径
	 * @param scheme 权限方案
	 * @param map 权限组合 <br/>
	 * 			如果scheme是ip，key:放设置的用户名密码（例:"192.168.80.130"）,value:放设置的权限列表([ZkDigestACL.READ,ZkDigestACL.ALL])。<br/>
	 * 			如果scheme是world，key:"anyone",value:放设置的权限列表([ZkDigestACL.READ,ZkDigestACL.ALL])。<br/>
	 * 			如果scheme是digest，key:放设置的用户名密码（例:"username:password"）,value:放设置的权限列表([ZkDigestACL.READ,ZkDigestACL.ALL])
	 * @return
	 */
	public boolean setZnodeACL(String path, String scheme,Map<String,List<ZkACLType>> map) {
		List<ACL> list = getACLList(scheme, map);
		Stat stat;
		try {
			stat = zookeeper.exists(path, false);
			if(stat != null) {
				zookeeper.setACL(path, list, stat.getVersion());
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 
	 * <p>Title: setZnodeACL</p>  
	 * Description: <pre>给节点设置权限，将所有权限信息放入map中</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月12日  
	 * @param path
	 * @param map
	 * @return
	 */
	public boolean setZnodeACL(String path,Map<String,Map<String,List<ZkACLType>>> map) {
		List<ACL> aclList = null;
		if(map==null || map.isEmpty()) {
			aclList = Ids.OPEN_ACL_UNSAFE;
		} else {
			aclList = getACLList(map);
		}
		Stat stat;
		try {
			stat = zookeeper.exists(path, false);
			if(stat != null) {
				zookeeper.setACL(path, aclList, -1);
				return true;
			} else {
				throw new Exception("节点"+path+"不存在");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		return false;
//		Set<String> schemeSet = map.keySet();
//		Iterator<String> schemeIterator = schemeSet.iterator();
//		while(schemeIterator.hasNext()) {
//			String schemeKey = schemeIterator.next();
//			Map<String,List<ZkACLType>> schemeMap = map.get(schemeKey);
//			setZnodeACL(path, schemeKey, schemeMap);
//		}
		return true;
	}
	
	
	/**
	 * 
	 * <p>Title: setZnodeIpACL</p>  
	 * Description: <pre>设置ip权限类型</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月11日  
	 * @param path
	 * @param map
	 * @return
	 */
	public boolean setZnodeIpACL(String path,Map<String,List<ZkACLType>> map) {
		return setZnodeACL(path, "ip", map);
	}
	/**
	 * 
	 * <p>Title: setZnodeDigestACL</p>  
	 * Description: <pre>设置digest权限类型</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月11日  
	 * @param path
	 * @param map
	 * @return
	 */
	public boolean setZnodeDigestACL(String path,Map<String,List<ZkACLType>> map) {
		return setZnodeACL(path, "digest", map);
	}
	/**
	 * 
	 * <p>Title: setZnodeWorldACL</p>  
	 * Description: <pre>设置world权限类型</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月11日  
	 * @param path
	 * @param list
	 * @return
	 */
	public boolean setZnodeWorldACL(String path,List<ZkACLType> list) {
		Map<String,List<ZkACLType>> map = new HashMap<>();
		map.put("anyone", list);
		return setZnodeACL(path, "world", map);
	}
}
