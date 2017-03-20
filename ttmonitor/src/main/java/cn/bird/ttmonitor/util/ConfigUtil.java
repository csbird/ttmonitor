package cn.bird.ttmonitor.util;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {
	public static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
	public static PropertiesConfiguration config;
	
	public static boolean init(String file){
		try{
			config = new PropertiesConfiguration();
			config.setEncoding("UTF-8");
			config.load(file);
			FileChangedReloadingStrategy strategy = new FileChangedReloadingStrategy();
			strategy.setRefreshDelay(1000);
			config.setReloadingStrategy(strategy);
			return true;
		}catch(Exception e){
			logger.error("fail to init configuration", e);
		}
		return false;
	}
}
