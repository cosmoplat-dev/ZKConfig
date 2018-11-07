package com.haier.zkconfig.sysserver.controller;

import java.util.List;

import javax.annotation.Resource;

import com.haier.common.utils.json.JsonResponseModel;
import com.haier.common.utils.json.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haier.zkconfig.interfaces.ZookeeperService;
import com.haier.zkconfig.interfaces.model.Znode;
import com.haier.zkconfig.sysserver.opt.ZookeeperOpt;

@Controller
@RequestMapping("zookeeper")
public class ZookeeperController {

	@Resource(name="zookeeperService")
	private ZookeeperService zookeeperService;
	@Autowired
	private ZookeeperOpt zookeeperOpt;
	
	@ResponseBody
	@RequestMapping(value="/getChilden", method = {RequestMethod.GET,RequestMethod.POST})
	public String getChilden(String path) {
		JsonResponseModel result = null;
		try {
			if(path == null || path == "") {
				return new JsonResponseModel(ResponseCode.PARAMS_IS_NULL, "path不能为空").toJsonString();
			} else if(path != null) {
				if(!path.startsWith("/")) {
					return new JsonResponseModel(ResponseCode.PARAMS_IS_INVALID, "路径请以'/'开头").toJsonString();
				}
				if(path.endsWith("/")) {
					return new JsonResponseModel(ResponseCode.PARAMS_IS_INVALID, "路径不能以'/'结尾").toJsonString();
				}
			}
			List<String> list = zookeeperService.getChilden(path);
			System.out.println(list.size());
			result = new JsonResponseModel(ResponseCode.SUCCESS, "获取成功",list);
		} catch (Exception e) {
			e.printStackTrace();
			result = new JsonResponseModel(ResponseCode.SYSTEM_INNER_ERROR, "系统异常");
		}
		return result.toJsonString();
	}
	@ResponseBody
	@RequestMapping(value="/getChildenAndData", method = {RequestMethod.GET,RequestMethod.POST})
	public String getChildenAndData(String path) {
		JsonResponseModel result = null;
		try {
			if(path == null || path == "") {
				return new JsonResponseModel(ResponseCode.PARAMS_IS_NULL, "path不能为空").toJsonString();

			} else if(path != null) {
				if(!path.startsWith("/")) {
					return new JsonResponseModel(ResponseCode.PARAMS_IS_INVALID, "路径请以'/'开头").toJsonString();
				}
				if(path.endsWith("/")) {
					return new JsonResponseModel(ResponseCode.PARAMS_IS_INVALID, "路径不能以'/'结尾").toJsonString();
				}
			}
			Znode node = zookeeperService.getChildenAndData(path);
			result = new JsonResponseModel(ResponseCode.SUCCESS, "获取成功",node);
		} catch (Exception e) {
			e.printStackTrace();
			result = new JsonResponseModel(ResponseCode.SYSTEM_INNER_ERROR, "系统异常");
		}
		return result.toJsonString();
	}
	
	
	@ResponseBody
	@RequestMapping(value="/search", method = {RequestMethod.GET})
	public String search(String menu,String project) {
		try {
			if(menu == null || menu =="") {
				return new JsonResponseModel(ResponseCode.PARAMS_NOT_COMPLETE, "menu不能为空").toJsonString();
			}
			if(project == null || project =="") {
				return new JsonResponseModel(ResponseCode.PARAMS_NOT_COMPLETE, "project不能为空").toJsonString();
			}
			JsonResponseModel result = zookeeperOpt.search(menu, project);
			return result.toJsonString();
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponseModel(ResponseCode.BUSINESS_ERROR, "系统异常").toJsonString();
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/submitZkData", method = {RequestMethod.POST})
	public String submitZkData(String project,String typeName,String value) {
		try {
			if(typeName == null || typeName =="") {
				return new JsonResponseModel(ResponseCode.PARAMS_NOT_COMPLETE, "typeName不能为空").toJsonString();
			}
			if(project == null || project =="") {
				return new JsonResponseModel(ResponseCode.PARAMS_NOT_COMPLETE, "project不能为空").toJsonString();
			}
			JsonResponseModel result = zookeeperOpt.submitZkData(project, typeName, value);
			return result.toJsonString();
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponseModel(ResponseCode.BUSINESS_ERROR, "系统异常").toJsonString();
		}
	}
	/**
	 * 
	 * <p>Title: saveZnodeAndDate</p>  
	 * Description: <pre>保存自定义节点</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月19日  
	 * @param path
	 * @param value
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/saveZnodeAndDate", method = {RequestMethod.POST})
	public String saveZnodeAndDate(String path,String value) {
		try {
			if(path == null || path == "") {
				return new JsonResponseModel(ResponseCode.PARAMS_NOT_COMPLETE, "path不能为空").toJsonString();
			}
			if(!path.startsWith("/")) {
				return new JsonResponseModel(ResponseCode.PARAMS_TYPE_ERROR, "path必须以 ‘/’开头").toJsonString();
			}
			if(path.endsWith("/")) {
				return new JsonResponseModel(ResponseCode.PARAMS_TYPE_ERROR, "path不能以 ‘/’结尾").toJsonString();
			}
			JsonResponseModel result = zookeeperOpt.saveZnodeAndDate(path, value);
			return result.toJsonString();
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponseModel(ResponseCode.BUSINESS_ERROR, "系统异常").toJsonString();
		}
	}
	
}
