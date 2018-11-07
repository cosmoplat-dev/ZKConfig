package com.haier.zkconfig.sysserver.zookeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.haier.common.utils.zookeeper.ZkACLType;

public class ZnodeACLUtil {
	
	public static Map<String,Map<String,List<ZkACLType>>> getInitACLList(){
		Map<String,Map<String,List<ZkACLType>>> map = new HashMap<>();
		Map<String,List<ZkACLType>> ipMap = new HashMap<>();
		List<ZkACLType> ipAclList = new ArrayList<>();
		ipAclList.add(ZkACLType.ALL);
		try {
			String ips = System.getProperty("znode.acl.ip");
			if(ips == null || ips == "") {
				return null;
			}
			String[] ipArr = ips.split(",");
			for(String ip : ipArr) {
				ipMap.put(ip, ipAclList);
			}
			map.put("ip", ipMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String,List<ZkACLType>> worldMap = new HashMap<>();
		List<ZkACLType> worldAclList = new ArrayList<>();
		worldAclList.add(ZkACLType.READ);
		worldMap.put("anyone", worldAclList);
		map.put("world", worldMap);
		return map;
	}

}
