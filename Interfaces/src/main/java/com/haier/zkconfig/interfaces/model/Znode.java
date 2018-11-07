package com.haier.zkconfig.interfaces.model;

import java.io.Serializable;
import java.util.List;

public class Znode implements Serializable{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7697125982165018137L;
	/**
	 * 节点全路径
	 */
	private String path;
	/**
	 * 节点名称
	 */
	private String name;
	/**
	 * 节点内容
	 */
	private String data;
	/**
	 * 父节点路径
	 */
	private String parentName;
	/**
	 * 子节点列表
	 */
	private List<Znode> childen;
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getParentName() {
		return parentName;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public List<Znode> getChilden() {
		return childen;
	}
	public void setChilden(List<Znode> childen) {
		this.childen = childen;
	}
	
}
