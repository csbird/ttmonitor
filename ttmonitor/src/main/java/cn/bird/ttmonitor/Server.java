package cn.bird.ttmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bird.ttmonitor.util.ConfigUtil;

public class Server {
	public static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	public static void main(String[] args){
		if(!ConfigUtil.init("server.properties")){
			logger.error("fail to load configuration");
			return;
		}
		logger.info("server starting...{}", ConfigUtil.config.getString("tt.url"));
	}
}
