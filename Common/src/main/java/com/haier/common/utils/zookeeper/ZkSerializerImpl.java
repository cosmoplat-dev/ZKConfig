package com.haier.common.utils.zookeeper;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.codec.Charsets;

public class ZkSerializerImpl implements ZkSerializer {

	@Override
	public byte[] serialize(Object data) throws ZkMarshallingError {
		return String.valueOf(data).getBytes(Charsets.UTF_8);
	}

	@Override
	public Object deserialize(byte[] bytes) throws ZkMarshallingError {
		return new String(bytes, Charsets.UTF_8);
	}

}
