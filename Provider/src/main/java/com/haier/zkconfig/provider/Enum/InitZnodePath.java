package com.haier.zkconfig.provider.Enum;

public enum InitZnodePath {

	/**
	 * C端的节点创建应有一个统一的初始化地址,该地址用来存放初始化地址的路径
	 */
	ZK_C_NODE("/com/haier/c");
	
	private String path;
	private InitZnodePath(String path) {
        this.path = path;
    }
	public String getValue() {
        return path;
    }
}
