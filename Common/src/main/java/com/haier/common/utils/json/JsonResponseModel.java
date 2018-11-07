package com.haier.common.utils.json;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 
 * <p>Title: ReqResultModel</p>  
 * Description: <pre>请求响应JSON数据模型</pre>   
 */
public class JsonResponseModel {
	/**
	 * 请求状态码
	 */
	private ResponseCode code;
	/**
	 * 请求响应提示信息
	 */
	private String msg;
	/**
	 * 请求返回的数据
	 */
	private Object data;
	
	public JsonResponseModel(ResponseCode code, String msg){
		this.code = code;
		this.msg = msg;
	}
	public JsonResponseModel(ResponseCode code, String msg, Object data){
		this.code = code;
		this.msg = msg;
		this.data = data;
	}
	public ResponseCode getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
	public Object getData() {
		return data;
	}
	
	public String toJsonString(){
		JSONObject json = new JSONObject();
		if(this.code == null){
			json.put("code", ResponseCode.SYSTEM_INNER_ERROR.getCode());
			json.put("msg", this.msg);
			return json.toString();
		}
		json.put("code", this.code.getCode());
		json.put("msg", this.msg);
		if(this.data != null){
			json.put("data", this.data);
		}
		return JSONObject.toJSONString(json, SerializerFeature.WriteMapNullValue);
	}
	
}
