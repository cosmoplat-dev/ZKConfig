package com.haier.common.utils.redis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.BinaryClient.LIST_POSITION;

/**
 * <p>Title: RedisUtil</p>  
 * Description: <pre>Redis 操作工具类</pre>  只能操作非集群redis
 */
public class RedisPoolUtil {
	private volatile static JedisPool pool;
	static JedisPoolConfig poolConfig;
	static JedisCluster cluster;
	static final int connectionTimeOut = 3000;
	static RedisPack pack;

	// 开发环境
	static String connectionPassword;
	static String connectionAddress;
	static int connectionPort = 6379;
	static int connectionDb = 0; // 注意：redis 数据库索引从0开始

	public static JedisPool getJedisPool(final GenericObjectPoolConfig poolConfig, final String host, int port,
		      int timeout, final String password, final int database) {
		if(pool == null) {
			synchronized(JedisPool.class) {
				if(pool == null) {
					pool = new JedisPool(poolConfig, connectionAddress, connectionPort, connectionTimeOut, connectionPassword,
							connectionDb);
				}
			}
		}
		return pool;
	}
	
	public static JedisPool getNewPool() {
		return pool;
	}
	
	// region 构造函数重载

	/*
	 * 构造函数
	 */
	public RedisPoolUtil() {
		CreateJedisPool();
	}

	/*
	 * 构造函数传入配置
	 */
	public RedisPoolUtil(JedisPoolConfig config) {
		poolConfig = config;
		CreateJedisPool();
	}

	/*
	 * 构造函数传入配置和连接密码
	 */
	public RedisPoolUtil(JedisPoolConfig config, String password) {
		connectionPassword = password;
		poolConfig = config;
		CreateJedisPool();
	}

	/*
	 * 构造函数传入配置、连接密码、服务器地址、端口、连接的数据库编号
	 */
	public RedisPoolUtil(JedisPoolConfig config,String addr, String password, Integer port, Integer dbIndex) {
		if(config != null) {
			poolConfig = config;
		}
		
		if(!StringUtils.isBlank(addr)) {
			connectionAddress = addr;
		}
		if(!StringUtils.isBlank(password)) {
			connectionPassword = password;
		}
		if(port != null) {
			connectionPort = port;
		}
		if(dbIndex != null) {
			connectionDb = dbIndex;
		}
		
		CreateJedisPool();
	}

	// end

	/*
	 * 创建连接池
	 */
	static synchronized void CreateJedisPool() {
		if (pool != null) {
			pack = new RedisPack(pool);
			return;
		}

		// 添加默认配置
		if (poolConfig == null) {
			poolConfig = new JedisPoolConfig();

			// 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
			poolConfig.setBlockWhenExhausted(true);
			// 最大连接数, 默认8个
			poolConfig.setMaxTotal(100);
			// 最大空闲连接数, 默认8个
			poolConfig.setMaxIdle(8);
			// 最小空闲连接数, 默认0
			poolConfig.setMinIdle(0);
			// 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
			poolConfig.setMinEvictableIdleTimeMillis(1800000 / 6);

			// (如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间, 默认-1
			// 等待可用连接的最大时间,单位毫秒,默认值为-1,表示永不超时/如果超过等待时间,则直接抛出异常
			poolConfig.setMaxWaitMillis(-1);
			// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
			poolConfig.setNumTestsPerEvictionRun(3);

			// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断
			// (默认逐出策略)
			poolConfig.setSoftMinEvictableIdleTimeMillis(1800000);

			// 在获取连接的时候检查有效性, 默认false
			poolConfig.setTestOnBorrow(false);
			// 在空闲时检查有效性, 默认false
			poolConfig.setTestWhileIdle(false);

			// 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
			poolConfig.setTimeBetweenEvictionRunsMillis(-1);

		}

		pool = getJedisPool(poolConfig, connectionAddress, connectionPort, connectionTimeOut, connectionPassword,connectionDb);
		pack = new RedisPack(pool);
	}

	public static void resetJedisPool(JedisPoolConfig config,String addr, String password, Integer port, Integer dbIndex) {
		pool.destroy();
		pool = null;
		if(config != null) {
			poolConfig = config;
		}
		
		if(!StringUtils.isBlank(addr)) {
			connectionAddress = addr;
		}
		if(!StringUtils.isBlank(password)) {
			connectionPassword = password;
		}
		if(port != null) {
			connectionPort = port;
		}
		if(dbIndex != null) {
			connectionDb = dbIndex;
		}
		CreateJedisPool();
	}
	
	// region 内部类，包装 Redis操作，避免忘记释放连接池资源
	interface RedisCallback<T> {
		public T handle(Jedis jedis);
	}

	/*
	 * 包装Redis操作，避免忘记归还连接池资源
	 */
	static class RedisPack {

		JedisPool jedisPool;

		public RedisPack(JedisPool jedisPool) {
			this.jedisPool = jedisPool;
		}

		public <T> T execute(RedisCallback<T> callback) {
			int i = 0;
			Jedis jedis;
			Boolean done = false;
			while(true) {
				try {
					jedis = jedisPool.getResource();
					break;
				} catch (Exception e) {
					done = true;
					System.out.println("第" + ++i + "次getResource，线程" + Thread.currentThread().getName());
					System.out.println(e.getMessage());
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				jedisPool = RedisPoolUtil.getNewPool();
			}
			if(done) System.out.println(Thread.currentThread().getName() + " ===> Done !!!!");
			try {
				return callback.handle(jedis);
			} catch (Exception e) {
				throw e;
			} finally {
				returnResource(jedis);
			}
		}

		void returnResource(Jedis jedis) {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	// end
	
	// region 设置过期时间

	/*
	 * 设置 key 的过期时间。key 过期后将不再可用。
	 * @param key: 待操作的键值
	 * @param seconds: 从当前开始多少秒后过期
	 * @return Boolean: 设置成功返回 1 。 当 key 不存在或者不能为 key 设置过期时间时返回0
	 */
	public Boolean Expire(final String key, final int seconds) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				return jedis.expire(key, seconds) > 0;
			}
		});
	}
	/*
	 * 以 UNIX 时间戳(unix timestamp)格式设置 key 的过期时间。key 过期后将不再可用。
	 * @param key: 待操作的键值
	 * @param date: 从当前开始到某个时间过期
	 * @return Boolean: 设置成功返回 1 。 当 key 不存在或者不能为 key 设置过期时间时返回0
	 */
	public Boolean ExpireAt(final String key, final Date date) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String t = sdf.format(date);
				Long epoch;
				try {
					epoch = sdf.parse(t).getTime() / 1000;
					return jedis.expireAt(key, epoch) > 0;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
		});
	}
	// end
	
	// region Item 操作
	/*
	 * 根据key查询value
	 * @param key: 待查询的键值
	 * @return String
	 */
	public String ItemGet(final String key) {
		return pack.execute(new RedisCallback<String>() {
			public String handle(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}
	/*
	 * 设置 key-value
	 * @param key: 待操作的键值
	 * @param value: 待设置的值
	 * @return Boolean: 成功返回 true
	 */
	public Boolean ItemSet(final String key, final String value) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				String result = jedis.set(key, value);
				return result != null && result.equals("OK");
			}
		});
	}
	/*
	 * 为指定的 key 设置值及其过期时间。如果 key 已经存在， SETEX 命令将会替换旧的值。
	 * @param key: 待操作的键值
	 * @param value: 从当前开始多少秒过期
	 * @param value: 待设置的值
	 * @return Boolean: 成功返回 true
	 */
	public Boolean ItemSetEx(final String key, final int seconds, final String value) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				String result = jedis.setex(key, seconds, value);
				return result != null && result.equals("OK");
			}
		});
	}

	/*
	 * 给指定key的value尾部追加字符串。如果key不存在，则会自动创建
	 * @param key: 待操作的键值
	 * @param value: 待追加的值
	 * @return Boolean: 成功返回 true
	 */
	public Boolean ItemAppendAutoCreate(final String key, final String value) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				// append 成功返回结果字符串的长度
				Long result = jedis.append(key, value);
				return result != null && result > 0;
			}
		});
	}

	/*
	 * 给指定key的value尾部追加字符串。如果key不存在，返回失败
	 * @param key: 待操作的键值
	 * @param value: 待追加的值
	 * @return Boolean: 成功返回 true
	 */
	public Boolean ItemAppend(final String key, final String value) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				if (!jedis.exists(key))
					return false;

				// append 成功返回结果字符串的长度
				Long result = jedis.append(key, value);
				return result != null && result > 0;
			}
		});
	}

	/*
	 * 删除一个或多个key
	 * @param keys: 待删除的键值数组
	 * @return Long: 返回成功被删除的键的数量
	 */
	public Long ItemDelete(final String[] keys) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.del(keys);
			}
		});
	}

	public Boolean ItemMSet(final String... map) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				return jedis.mset(map).equals("OK");
			}
		});
	}
	/**
	 * 
	 * <p>Title: ItemIncr</p>  
	 * Description: <pre>自增操作+1</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月14日  
	 * @param key
	 * @return
	 */
	public Long ItemIncr(final String key) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.incr(key);
			}
		});
	}
	/**
	 * 
	 * <p>Title: ItemIncrBy</p>  
	 * Description: <pre>自增操作+n</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月14日  
	 * @param key
	 * @param integer
	 * @return
	 */
	public Long ItemIncrBy(final String key, final long integer) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.incrBy(key, integer);
			}
		});
	}
	/**
	 * 
	 * <p>Title: ItemDecr</p>  
	 * Description: <pre>递减操作-1</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月14日  
	 * @param key
	 * @return
	 */
	public Long ItemDecr(final String key) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.decr(key);
			}
		});
	}
	/**
	 * 
	 * <p>Title: ItemDecrBy</p>  
	 * Description: <pre>递减操作-n</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月14日  
	 * @param key
	 * @param integer
	 * @return
	 */
	public Long ItemDecrBy(final String key, final long integer) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.decrBy(key, integer);
			}
		});
	}
	
	public Double ItemIncrByFloat(final String key, final double value) {
		return pack.execute(new RedisCallback<Double>() {
			public Double handle(Jedis jedis) {
				return jedis.incrByFloat(key, value);
			}
		});
	}

	// end

	//*******************hash*********************
	// region hash 操作
	/**
	 * <p>Title: hashSet</p>  
	 * Description: <pre>保存一个hash类型的值</pre>  
	 * @date 2018年5月2日  
	 * @param key hash类型的key
	 * @param field 字段名称
	 * @param value 字段值
	 * @return boolean true 成功 false 失败
	 */
	public Boolean hashSet(final String key,final String field,final String value) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				try {
					jedis.hset(key, field, value);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				
			}
		});
	}
	/**
	 * <p>Title: hashGet</p>  
	 * Description: <pre>获取一个hash类型的某个字段的值</pre>  
	 * @date 2018年5月2日  
	 * @param key hash类型的key
	 * @param field 字段值
	 * @return
	 */
	public String hashGet(final String key,final String field) {
		return pack.execute(new RedisCallback<String>() {
			public String handle(Jedis jedis) {
				return jedis.hget(key, field);
			}
		});
	}
	/**
	 * <p>Title: hashMset</p>  
	 * Description: <pre>保存整个hash,存在更新(新的字段值代替旧的字段值)，不存在创建</pre>  
	 * @date 2018年5月2日  
	 * @param key hash类型的key
	 * @param hash map对象
	 * @return boolean true 成功 false 失败
	 */
	public Boolean hashMset(final String key,final Map<String, String> hash) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				try {
					jedis.hmset(key, hash);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		});
	}
	/**
	 * <p>Title: hashMget</p>  
	 * Description: <pre>获取hash类型的多个字段的值</pre>  
	 * @date 2018年5月2日  
	 * @param key hash类型的key
	 * @param fields 单个或多个字段的字段名称
	 * @return List 返回和请求列表顺序相同的对应的字段值
	 */
	public List<String> hashMget(final String key, final String... fields) {
		return pack.execute(new RedisCallback<List<String>>() {
			public List<String> handle(Jedis jedis) {
				return jedis.hmget(key, fields);
			}
		});
	}
	/**
	 * 
	 * <p>Title: hashGetAll</p>  
	 * Description: <pre>获取哈希中的所有字段和相关值。</pre>  
	 * @date 2018年5月2日  
	 * @param key hash对象的key
	 * @return Map<String, String> 返回哈希中的所有字段和相关值。
	 */
	public Map<String, String> hashGetAll(final String key) {
		return pack.execute(new RedisCallback<Map<String, String>>() {
			public Map<String, String> handle(Jedis jedis) {
				return jedis.hgetAll(key);
			}
		});
	}
	/**
	 * 
	 * <p>Title: hashKeys</p>  
	 * Description: <pre>获取哈希中的所有字段名称。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return Set 哈希中的所有字段名称集合。
	 */
	public Set<String> hashKeys(final String key) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.hkeys(key);
			}
		});
	}
	/**
	 * <p>Title: hashVals</p>  
	 * Description: <pre>返回hash中的所有值</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return List hash中的所有值集合。
	 */
	public List<String> hashVals(final String key) {
		return pack.execute(new RedisCallback<List<String>>() {
			public List<String> handle(Jedis jedis) {
				return jedis.hvals(key);
			}
		});
	}
	/**
	 * <p>Title: hashLength</p>  
	 * Description: <pre>返回hash中的项目数。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return Long 键中存储的hash中包含的条目（字段）的数目。
	 */
	public Long hashLength(final String key) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.hlen(key);
			}
		});
	}
	/**
	 * <p>Title: hashExists</p>  
	 * Description: <pre>判断指定key的hash中某个字段是否存在</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param field
	 * @return boolean true 存在 false 不存在
	 */
	public Boolean hashExists(final String key, final String field) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				return jedis.hexists(key, field);
			}
		});
	}
	/**
	 * <p>Title: hashDelete</p>  
	 * Description: <pre>根据hash对象的key 和fields移除一个或多个字段</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param fields 
	 * @return boolean 
	 */
	public Boolean hashDelete(final String key, final String... fields) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				try {
					jedis.hdel(key, fields);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		});
	}
	/**
	 * 
	 * <p>Title: hashIncrBy</p>  
	 * Description: <pre></pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月14日  
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public Long hashIncrBy(final String key, final String field, final long value) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.hincrBy(key, field, value);
			}
		});
	}
	
	public Double hincrByFloat(final String key, final String field, final double value) {
		return pack.execute(new RedisCallback<Double>() {
			public Double handle(Jedis jedis) {
				return jedis.hincrByFloat(key, field, value);
			}
		});
	}
	
	// end hash
	
	//*******************list*********************
	// region list 操作
	/**
	 * <p>Title: lpush</p>  
	 * Description: <pre>将一个或多个值 value 插入到列表 key 的表头,如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头,
	 * 比如说，对空列表 mylist 执行命令 LPUSH mylist a b c ，列表的值将是 c b a ，
	 * 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param strings
	 * @return boolean
	 */
	public Boolean listPush(final String key, final String... strings) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				try {
					jedis.lpush(key, strings);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		});
	}
	/**
	 * <p>Title: lpop</p>  
	 * Description: <pre>移除并返回列表 key 的头元素。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return String 移除的值，如果key不存在，或者列表已经空，则返回特殊值“nil”
	 */
	public String listPop(final String key) {
		return pack.execute(new RedisCallback<String>() {
			public String handle(Jedis jedis) {
				return jedis.lpop(key);
			}
		});
	}
	/**
	 * <p>Title: listSet</p>  
	 * Description: <pre>在键的索引位置设置一个新的值作为元素。超出范围的索引将生成一个错误。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param index 放置元素位置的索引
	 * @param value
	 * @return
	 */
	public String listSet(String key, long index, String value) {
		return pack.execute(new RedisCallback<String>() {
			public String handle(Jedis jedis) {
				return jedis.lset(key, index, value);
			}
		});
	}
	/**
	 * <p>Title: listIndex</p>  
	 * Description: <pre>返回指定键存储的列表的指定元素。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param index 索引值
	 * @return string  如果在KEY中存储的值不是列表类型，则返回错误。如果索引超出范围，则返回“nil”
	 */
	public String listIndex(String key, long index) {
		return pack.execute(new RedisCallback<String>() {
			public String handle(Jedis jedis) {
				return jedis.lindex(key, index);
			}
		});
	}
	/**
	 * <p>Title: listTrim</p>  
	 * Description: <pre>对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public String listTrim(final String key, final long start, final long end) {
		return pack.execute(new RedisCallback<String>() {
			public String handle(Jedis jedis) {
				return jedis.ltrim(key, start, end);
			}
		});
	}
	/**
	 * <p>Title: listLength</p>  
	 * Description: <pre>返回列表 key 的长度。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return
	 */
	public Long listLength(final String key) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.llen(key);
			}
		});
	}
	/**
	 * <p>Title: listRange</p>  
	 * Description: <pre>返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> listRange(final String key, final long start, final long end) {
		return pack.execute(new RedisCallback<List<String>>() {
			public List<String> handle(Jedis jedis) {
				return jedis.lrange(key, start, end);
			}
		});
	}
	/**
	 * <p>Title: listInsert</p>  
	 * Description: <pre>将值 value 插入到列表 key 当中，位于值 pivot 之前或之后。
	 *当 pivot 不存在于列表 key 时，不执行任何操作。当 key 不存在时， key 被视为空列表，不执行任何操作。
	 *如果 key 不是列表类型，返回一个错误。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param where 插入方式 LIST_POSITION.BEFORE/LIST_POSITION.AFTER
	 * @param pivot 目标值
	 * @param value 插入值
	 * @return
	 */
	public Long listInsert(final String key, final LIST_POSITION where, final String pivot,final String value) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.linsert(key, where, pivot, value);
			}
		});
	}
	//end
	
	//*******************set**********************
	// region set 操作
	/**
	 * <p>Title: setAdd</p>  
	 * Description: <pre>把一个或多个元素添加到指定集合</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param members
	 * @return
	 */
	public Boolean setAdd(final String key, final String... members) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				try {
					jedis.sadd(key, members);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		});
	}
	/**
	 * <p>Title: setMembers</p>  
	 * Description: <pre>返回存储在键中的SET值的所有成员（元素）。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return
	 */
	public Set<String> setMembers(final String key) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.smembers(key);
			}
		});
	}
	/**
	 * <p>Title: setRem</p>  
	 * Description: <pre>从存储在KEY中的SET值中移除指定成员。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param members 要移除的指定的值
	 * @return
	 */
	public Long setRem(String key, String... members) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.srem(key, members);
			}
		});
	}
	/**
	 * <p>Title: setPop</p>  
	 * Description: <pre>从一个集合中删除一个随机元素作为返回值。如果该集合为空或key不存在，则返回一个nil对象</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return string 被移除的值，如果集合为空返回nil
	 */
	public String setPop(String key) {
		return pack.execute(new RedisCallback<String>() {
			public String handle(Jedis jedis) {
				return jedis.spop(key);
			}
		});
	}
	/**
	 * <p>Title: setCard</p>  
	 * Description: <pre>返回集合包含元素的个数，如果key不存在，则返回0，和空集一样</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return
	 */
	public Long setCard(final String key) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.scard(key);
			}
		});
	}
	/**
	 * <p>Title: setIsmember</p>  
	 * Description: <pre>key的集合中是否包含某个元素，如果存在返回1，否则返回0</pre>  
	 * @date 2018年5月2日  
	 * @param key 
	 * @param member
	 * @return
	 */
	public Boolean setIsmember(final String key, final String member) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				return jedis.sismember(key, member);
			}
		});
	}
	/**
	 * <p>Title: setMove</p>  
	 * Description: <pre>将member元素从srckey集合中移除并放入到dstkey集合中</pre>  
	 * @date 2018年5月2日  
	 * @param srckey 源key
	 * @param dstkey 目标key
	 * @param member 被移动的元素
	 * @return 
	 */
	public Long setMove(final String srckey, final String dstkey, final String member) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.smove(srckey, dstkey, member);
			}
		});
	}
	/**
	 * <p>Title: sinter</p>  
	 * Description: <pre>返回一组集合的成员，这些集合是由在指定键中保存的所有集合的交集生成的。</pre>  
	 * @date 2018年5月2日  
	 * @param keys 多个集合的key
	 * @return Set 多个集合的交集的集合
	 */
	public Set<String> sinter(final String... keys) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.sinter(keys);
			}
		});
	}
	/**
	 * <p>Title: sunion</p>  
	 * Description: <pre>返回集合中所有集合的成员,多个集合的并集</pre>  
	 * @date 2018年5月2日  
	 * @param keys 多个集合的key
	 * @return
	 */
	public Set<String> sunion(final String... keys) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.sunion(keys);
			}
		});
	}
	/**
	 * <p>Title: sdiff</p>  
	 * Description: <pre>返回在KEY1中存储的集合与所有集合KEY2之间的差异,多个集合的差集</pre>  
	 * @date 2018年5月2日  
	 * @param keys 多个集合的key
	 * @return
	 */
	public Set<String> sdiff(final String... keys) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.sdiff(keys);
			}
		});
	}
	//end
	
	//*******************sorted set***************
	// region 有序集操作
	public Long zadd(final String key, final double score, final String member) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zadd(key, score, member);
			}
		});
	}
	/**
	 * <p>Title: zadd</p>  
	 * Description: <pre>将一个或多个 member 元素及其 score 值加入到有序集 key 当中。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param scoreMembers 字段值的分数map
	 * @return
	 */
	public Long zadd(final String key, final Map<String, Double> scoreMembers) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zadd(key, scoreMembers);
			}
		});
	}
	/**
	 * <p>Title: zscore</p>  
	 * Description: <pre>在键中返回排序后的集合的指定元素的分数。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param member 指定的元素
	 * @return
	 */
	public Double zscore(final String key, final String member) {
		return pack.execute(new RedisCallback<Double>() {
			public Double handle(Jedis jedis) {
				
				return jedis.zscore(key, member);
			}
		});
	}
	/**
	 * <p>Title: zcard</p>  
	 * Description: <pre>返回排序的集合基数（元素个数）.如果密钥不存在，则返回0，就像空排序集一样。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @return
	 */
	public Long zcard(final String key) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zcard(key);
			}
		});
	}
	/**
	 * <p>Title: zcount</p>  
	 * Description: <pre>返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param min (String) score范围最小值
	 * @param max (String) score范围最大值
	 * @return Long 返回的成员数量 
	 */
	public Long zcount(final String key, final String min, final String max) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}
		});
	}
	/**
	 * <p>Title: zcount</p>  
	 * Description: <pre>返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param min (double) score范围最小值
	 * @param max (double) score范围最大值
	 * @return Long 返回的成员数量 
	 */
	public Long zcount(final String key, final double min, final double max) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}
		});
	}
	/**
	 * <p>Title: zincrby</p>  
	 * Description: <pre>如果成员已经存在于排序后的集合中，则将其增量添加到其得分中，并相应地更新元素在排序后的集合中的位置。
	 * 为有序集 key 的成员 member 的 score 值加上增量 increment 。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 */
	public Double zincrby(final String key, final double score, final String member) {
		return pack.execute(new RedisCallback<Double>() {
			public Double handle(Jedis jedis) {
				return jedis.zincrby(key, score, member);
			}
		});
	}
	/**
	 * <p>Title: zrange</p>  
	 * Description: <pre>按照元素分数从小到大的顺序返回索引从start到end之间的所有元素(包含两端的元素)</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param start 开始的索引值
	 * @param end 结束的索引值
	 * @return
	 */
	public Set<String> zrange(final String key, final long start, final long end) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.zrange(key, start, end);
			}
		});
	}
	/**
	 * <p>Title: zrangeByScore</p>  
	 * Description: <pre>按照元素分数从小到大的顺序返回分数在max和min之间的所有元素(包含两端的元素)</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param min (String) score范围的最小值
	 * @param max (String) score范围的最大值
	 * @return
	 */
	public Set<String> zrangeByScore(final String key, final String min, final String max) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}
		});
	}
	public Set<String> zrangeByScore(final String key, final double min, final double max) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}
		});
	}
	/**
	 * <p>Title: zrangeByScoreWithScores</p>  
	 * Description: <pre>返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param min (String) score范围的最小值
	 * @param max (String) score范围的最大值
	 * @return
	 */
	public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max) {
		return pack.execute(new RedisCallback<Set<Tuple>>() {
			public Set<Tuple> handle(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max);
			}
		});
	}
	public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
		return pack.execute(new RedisCallback<Set<Tuple>>() {
			public Set<Tuple> handle(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max);
			}
		});
	}
	/**
	 * <p>Title: zrank</p>  
	 * Description: <pre>返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。</pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param member 
	 * @return Long 返回有序集 key 中成员 member 的排名
	 */
	public Long zrank(final String key, final String member) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zrank(key, member);
			}
		});
	}
	/**
	 * <p>Title: zrem</p>  
	 * Description: <pre>移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param members 要移除的成员
	 * @return Long 移除的元素个数
	 */
	public Long zrem(final String key, final String... members) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zrem(key, members);
			}
		});
	}
	/**
	 * 
	 * <p>Title: zremrangeByRank</p>  
	 * Description: <pre>移除有序集 key 中，指定排名(rank)区间内的所有成员。区间分别以下标参数 start和 end指出，包含 start和 end在内。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param start
	 * @param end
	 * @return Long 移除的元素个数
	 */
	public Long zremrangeByRank(final String key, final long start, final long end) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zremrangeByRank(key, start, end);
			}
		});
	}
	/**
	 * 
	 * <p>Title: zremrangeByScore</p>  
	 * Description: <pre>移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param start
	 * @param end
	 * @return Long 移除的元素个数
	 */
	public Long zremrangeByScore(final String key, final double start, final double end) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}
		});
	}
	public Long zremrangeByScore(final String key, final String start, final String end) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}
		});
	}
	/**
	 * <p>Title: zremrangeByLex</p>  
	 * Description: <pre>对于一个所有成员的分值都相同的有序集合键 key 来说， 这个命令会移除该集合中， 成员介于 min 和 max 范围内的所有元素。 </pre>  
	 * @date 2018年5月2日  
	 * @param key
	 * @param min
	 * @param max
	 * @return Long 移除的元素个数
	 */
	public Long zremrangeByLex(final String key, final String min, final String max) {
		return pack.execute(new RedisCallback<Long>() {
			public Long handle(Jedis jedis) {
				return jedis.zremrangeByLex(key, min, max);
			}
		});
	}


	/**
	 * @description: 获取List对象
	 * @method: ListGet
	 * @author: Mark
	 * @date: 17:57 2018/5/12
	 * @param key
	 * @return: java.lang.String
	 */
	public byte[] ListGet(final String key) {
		return pack.execute(new RedisCallback<byte[]>() {
			public byte[] handle(Jedis jedis) {
				return jedis.get(key.getBytes());
			}
		});
	}


	/**
	 * @description: 保存List对象
	 * @method: ListSet
	 * @author: Mark
	 * @date: 17:56 2018/5/12
	 * @param key
	 * @param value
	 * @return: java.lang.Boolean
	 */
	public Boolean ListSet(final String key, final byte[] value) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				String result = jedis.set(key.getBytes(), value);
				return result != null && result.equals("OK");
			}
		});
	}
	/**
	 * 
	 * <p>Title: zrevrange</p>  
	 * Description: <pre>按照元素分数从大到小的顺序返回索引从start到end之间的所有元素(包含两端的元素)</pre>  
	 * @author wangchaoqun 
	 * @date 2018年5月14日  
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<String> zrevrange(final String key, final long start, final long end) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.zrevrange(key, start, end);
			}
		});
	}
	/**
	 * 
	 * <p>Title: zrevrangeByLex</p>  
	 * Description: <pre></pre>  
	 * @date 2018年5月14日  
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 */
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				return jedis.zrevrangeByLex(key, max, min);
			}
		});
	}
	/**
	 * 
	 * <p>Title: zrevrangeByScore</p>  
	 * Description: <pre></pre>  
	 * @date 2018年5月14日  
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 */
	public Set<String> zrevrangeByScore(final String key, final String max, final String min) {
		return pack.execute(new RedisCallback<Set<String>>() {
			public Set<String> handle(Jedis jedis) {
				jedis.zrevrangeByScore(key, max, min);
				jedis.zrevrangeByScoreWithScores(key, max, min);
				return jedis.zrevrangeByScore(key, max, min);
			}
		});
	}
	/**
	 * 
	 * <p>Title: zrevrangeByScoreWithScores</p>  
	 * Description: <pre></pre>  
	 * @date 2018年5月14日  
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min) {
		return pack.execute(new RedisCallback<Set<Tuple>>() {
			public Set<Tuple> handle(Jedis jedis) {
				jedis.zrevrangeByScoreWithScores(key, max, min);
				return jedis.zrevrangeByScoreWithScores(key, max, min);
			}
		});
	}
	
	/*
	 * 根据key删除记录
	 * @param keys: 待删除的键值数组
	 * @return Long: 返回成功被删除的键的数量
	 */
	public Boolean delByKey(final String key) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				try {
					jedis.del(key);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		});
	}
	
	//end
	
	/**
	 * <p>Title: isExists</p>  
	 * Description: <pre>判断key是否存在</pre>  
	 * @date 2018年5月14日  
	 * @param key
	 * @return
	 */
	public Boolean isExists(final String key) {
		return pack.execute(new RedisCallback<Boolean>() {
			public Boolean handle(Jedis jedis) {
				return jedis.exists(key);
			}
		});
	}
}
