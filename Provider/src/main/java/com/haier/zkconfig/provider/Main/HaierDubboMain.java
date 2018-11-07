package com.haier.zkconfig.provider.Main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.container.Container;
import com.alibaba.dubbo.container.Main;
/**
 * 
 * <p>Title: HaierDubboMain</p>  
 * Description: <pre>provider service main
 * 该类为服务启动类，不要修改内容
 * </pre>   
 */
public class HaierDubboMain {
	

    public static final String CONTAINER_KEY = "dubbo.container";

    public static final String SHUTDOWN_HOOK_KEY = "dubbo.shutdown.hook";

    private static final Logger logger = LoggerFactory.getLogger(HaierDubboMain.class);

    private static final ExtensionLoader<Container> loader = ExtensionLoader.getExtensionLoader(Container.class);

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Condition STOP = LOCK.newCondition();

	//优先执行的代码，尝试从环境变量中获取  Zookeeper 地址的配置
	//这段代码在调试时起效，在运行时，如果不传入环境变量，默认值无法赋上
	//java -DdubboEnv="file:///usr/local/soft/dubbo_config/dubbo.properties" -jar UCM-Provider.jar 
	static {
		//用传入的环境变量设置配置文件位置
		String dubboEnv = System.getProperty("dubboEnv");
		if(dubboEnv == null || dubboEnv == "")
			dubboEnv = "dubbo/dubbo.properties";
		System.out.println("dubboEnv ==> " + dubboEnv);
		//在初始化 Dubbo 的代码之前设置环境变量的值
		System.setProperty("dubboEnv", dubboEnv);
	}
    
    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                String config = ConfigUtils.getProperty(CONTAINER_KEY, loader.getDefaultExtensionName());
                args = Constants.COMMA_SPLIT_PATTERN.split(config);
            }
            final List<Container> containers = new ArrayList<Container>();
            for (int i = 0; i < args.length; i++) {
                containers.add(loader.getExtension(args[i]));
            }
            logger.info("Use container type(" + Arrays.toString(args) + ") to run dubbo serivce.");
            if ("true".equals(System.getProperty(SHUTDOWN_HOOK_KEY))) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        for (Container container : containers) {
                            try {
                                container.stop();
                                logger.info("Dubbo " + container.getClass().getSimpleName() + " stopped!");
                            } catch (Throwable t) {
                                logger.error(t.getMessage(), t);
                            }
                            try {
                                LOCK.lock();
                                STOP.signal();
                            } finally {
                                LOCK.unlock();
                            }
                        }
                    }
                });
            }
            for (Container container : containers) {
                container.start();
                logger.info("Dubbo " + container.getClass().getSimpleName() + " started!");
            }
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " Dubbo service server started!");
        } catch (RuntimeException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
        try {
            LOCK.lock();
            STOP.await();
        } catch (InterruptedException e) {
            logger.warn("Dubbo service server stopped, interrupted by other thread!", e);
        } finally {
            LOCK.unlock();
        }
    }
	
}
