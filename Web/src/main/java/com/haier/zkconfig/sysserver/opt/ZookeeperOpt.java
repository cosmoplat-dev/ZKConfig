package com.haier.zkconfig.sysserver.opt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import com.haier.common.utils.json.JsonResponseModel;
import com.haier.common.utils.json.ResponseCode;
import com.haier.common.utils.zookeeper.ZkACLType;
import com.haier.zkconfig.sysserver.zookeeper.ZkNodeInit;
import com.haier.zkconfig.sysserver.zookeeper.ZnodeACLUtil;
import org.springframework.stereotype.Component;

import com.haier.zkconfig.interfaces.ZookeeperService;
import com.haier.zkconfig.interfaces.model.Znode;

@Component
public class ZookeeperOpt {
	
	@Resource(name="zookeeperService")
	private ZookeeperService zookeeperService;
	
	private static Properties properties;
	
	static {
		InputStream in = null;
		in = ZkNodeInit.class.getClassLoader().getResourceAsStream("dubbo/znode.properties");
		properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
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
	
	public JsonResponseModel search(String menu,String project) {
		
		String youlanC = properties.getProperty("zk.c.node");
		String typeName = new StringBuffer("youlanw.c.").append(menu).toString();
		String types = properties.getProperty(typeName);
		if(types == null || types == "") {
			return new JsonResponseModel(ResponseCode.SUCCESS,"菜单内容为空");
		}
		String pathProject = youlanC + "/" + project;
		String[] typeArr = types.split(",");
		List<Znode> list = new ArrayList<>();
		for(String str : typeArr) {
			String path = pathProject + "/" + str;
			Znode node = zookeeperService.getChildenAndData(path);
			if(node!= null) {
				list.add(node);
			}
		}
		return new JsonResponseModel(ResponseCode.SUCCESS,"success",list);
	}
	
	public JsonResponseModel submitZkData(String project,String typeName,String value) {
		StringBuffer sb = new StringBuffer("/youlanw/c/");
		sb.append(project).append("/").append(typeName);
		System.out.println(sb.toString());
		boolean flag = zookeeperService.updateZnodeData(sb.toString(), value);
		if(flag) {
			return new JsonResponseModel(ResponseCode.SUCCESS,"更新成功");
		} else {
			return new JsonResponseModel(ResponseCode.BUSINESS_ERROR,"更新失败");
		}
	}
	
	/**
	 * 
	 * <p>Title: saveZnodeAndDate</p>  
	 * Description: <pre>保存节点数据</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月19日  
	 * @param path
	 * @param value
	 * @return
	 */
	public JsonResponseModel saveZnodeAndDate(String path,String value) {
		boolean falg = false;
		JsonResponseModel json = null;
		path = "/youlanw/c" + path;
		if(zookeeperService.isExits(path)) {
			falg = zookeeperService.updateZnodeData(path, value);
			if(falg) {
				Znode znode = zookeeperService.getChildenAndData(path);
				json = new JsonResponseModel(ResponseCode.SUCCESS,"更新成功",znode);
			} else {
				json = new JsonResponseModel(ResponseCode.SYSTEM_INNER_ERROR,"更新失败");
			}
		} else {
			Map<String,Map<String,List<ZkACLType>>> map = ZnodeACLUtil.getInitACLList();
			String parentPath = path.substring(0,path.lastIndexOf("/"));
			if(zookeeperService.recursionCreateNodes(parentPath, map)) {
				falg = zookeeperService.addPersistentZnode(path, value, map);
				if(falg) {
					Znode znode = zookeeperService.getChildenAndData(path);
					json = new JsonResponseModel(ResponseCode.SUCCESS,"保存成功",znode);
				} else {
					json = new JsonResponseModel(ResponseCode.SYSTEM_INNER_ERROR,"保存失败");
				}
			}
		}
		return json;
	}

}
