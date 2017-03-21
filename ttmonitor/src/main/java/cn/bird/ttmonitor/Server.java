package cn.bird.ttmonitor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.bird.ttmonitor.model.Activity;
import cn.bird.ttmonitor.model.ActivityItem;
import cn.bird.ttmonitor.model.Category;
import cn.bird.ttmonitor.service.QueryService;
import cn.bird.ttmonitor.util.ConfigUtil;

public class Server {
	public static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	public static void main(String[] args) throws Exception{
		if(!ConfigUtil.init("server.properties")){
			logger.error("fail to load configuration");
			return;
		}
		logger.info("server starting...");
		QueryService queryService = new QueryService(ConfigUtil.config.getString("tt.cookie"), ConfigUtil.config.getString("tt.activity_url"),
				ConfigUtil.config.getString("tt.activity_items_url"), ConfigUtil.config.getString("tt.item_url"));
		List<Category> categoryList = queryService.getCategoryList();
		for(Category category : categoryList){
			logger.info("category|{}|{}", category.getId(), category.getName());
			List<Activity> activityList = queryService.getActivityList(category.getId());
			for(Activity activity : activityList){
				logger.info("activity|{}|{}|{}|{}|{}", category.getId(), activity.getId(), activity.getName(), activity.getStartTime(), activity.getEndTime());
				int displayMode = 0;
				int pageIndex = 1;
				while(true){
					if(pageIndex == 1){
						displayMode = 0;
					}else{
						displayMode = 1;
					}
					
					List<ActivityItem> itemList = queryService.getActivityItemList(activity, displayMode, pageIndex, 20);
					if(itemList.size() > 0){
						for(ActivityItem item : itemList){
							logger.info("item|{}|{}|{}|{}|{}|{}|{}|{}", category.getId(),activity.getId(),item.getId(),
									item.getDealCount(),item.getTotalQty(),item.getPrice().toPlainString(),item.getTitle());
						}
						pageIndex++;
						try {
							Thread.sleep(1000 * 3);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						break;
					}
				}
			}
			Thread.sleep(1000 * 5);
		}
		
		
		
		/**
		int displayMode = 0;
		int pageIndex = 1;
		while(true){
			if(pageIndex == 1){
				displayMode = 0;
			}else{
				displayMode = 1;
			}
			
			List<ActivityItem> itemList = queryService.getActivityItemList(activityList.get(1), displayMode, pageIndex, 20);
			if(itemList.size() > 0){
				for(ActivityItem item : itemList){
					logger.info("item:{}", JSON.toJSONString(item));
				}
				pageIndex++;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				logger.info("total page:{}", pageIndex-1);
				break;
			}
		}
		**/
	}
}
