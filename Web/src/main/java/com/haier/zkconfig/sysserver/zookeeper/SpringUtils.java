package com.haier.zkconfig.sysserver.zookeeper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class SpringUtils implements BeanFactoryPostProcessor {

	private static ConfigurableListableBeanFactory beanFactory; // Spring应用上下文环境
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println(beanFactory);
		SpringUtils.beanFactory = beanFactory;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) throws BeansException {
        return (T) beanFactory.getBean(name);
    }
	
	public static <T> T getBean(Class<T> clz) throws BeansException {
        T result = (T) beanFactory.getBean(clz);
        return result;
    }
	/**
	 * 销毁
	 */
	public static void destroy() {
		System.out.println("SpringUtils.destroy()");
	}
}
