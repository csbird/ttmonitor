package cn.bird.ttmonitor.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.bird.ttmonitor.model.Activity;
import cn.bird.ttmonitor.model.ActivityItem;
import cn.bird.ttmonitor.model.Category;
import cn.bird.ttmonitor.util.HttpTool;

public class QueryService {
	public static final Logger logger = LoggerFactory.getLogger(QueryService.class);
	
	private String cookie;
	private String activityListUrl;
	private String activityItemListUrl;
	private String itemUrl;
	
	public QueryService(String cookie, String activityListUrl, String activityItemListUrl, String itemUrl) {
		super();
		this.cookie = cookie;
		this.activityListUrl = activityListUrl;
		this.activityItemListUrl = activityItemListUrl;
		this.itemUrl = itemUrl;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getActivityListUrl() {
		return activityListUrl;
	}

	public void setActivityListUrl(String activityListUrl) {
		this.activityListUrl = activityListUrl;
	}

	public String getActivityItemListUrl() {
		return activityItemListUrl;
	}

	public void setActivityItemListUrl(String activityItemListUrl) {
		this.activityItemListUrl = activityItemListUrl;
	}

	public String getItemUrl() {
		return itemUrl;
	}

	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}
	
	public List<Category> getCategoryList(){
		List<Category> resultList = new ArrayList<Category>();
		String url = activityListUrl;
		try {
			String[] response = HttpTool.get(url, cookie, "UTF-8", 5000, 5000);
			if(response != null){
				if(response[0].equals("200")){
					JSONObject json = JSON.parseObject(response[1]);
					if(json != null){
						Boolean result = json.getBoolean("Result");
						if(result != null){
							if(result){
								JSONObject data = json.getJSONObject("Data");
								if(data != null){
									JSONArray array = data.getJSONArray("CategoryList");
									if(array != null){
										Iterator<Object> it = array.iterator();
										while(it.hasNext()){
											JSONObject item = (JSONObject)it.next();
											Category category = new Category();
											category.setId(item.getIntValue("Cid"));
											category.setName(item.getString("Name"));
											resultList.add(category);
										}
									}
								}
							}else{
								logger.error("fail to query:{}", response[1]);
							}
						}else{
							logger.error("invalid response json, no result:{}", response[1]);
						}
					}
				}else{
					logger.error("error response code={}", response[0]);
				}
			}
		} catch (Exception e) {
			logger.error("exception while getCategoryList", e);
		}
		return resultList;
	}

	public List<Activity> getActivityList(int cid){
		List<Activity> resultList = new ArrayList<Activity>();
		String url = activityListUrl + "?cid=" + cid;
		try {
			String[] response = HttpTool.get(url, cookie, "UTF-8", 5000, 5000);
			if(response != null){
				if(response[0].equals("200")){
					JSONObject json = JSON.parseObject(response[1]);
					if(json != null){
						Boolean result = json.getBoolean("Result");
						if(result != null){
							if(result){
								JSONObject data = json.getJSONObject("Data");
								if(data != null){
									JSONArray array = data.getJSONArray("ActivityList");
									if(array != null){
										Iterator<Object> it = array.iterator();
										while(it.hasNext()){
											JSONObject item = (JSONObject)it.next();
											Activity activity = new Activity();
											activity.setId(item.getIntValue("QsID"));
											activity.setName(item.getString("Name"));
											activity.setStartTime(item.getString("StartTime"));
											activity.setEndTime(item.getString("ToTime"));
											resultList.add(activity);
										}
									}
								}
							}else{
								logger.error("fail to query:{}", response[1]);
							}
						}else{
							logger.error("invalid response json, no result:{}", response[1]);
						}
					}
				}else{
					logger.error("error response code={}", response[0]);
				}
			}
		} catch (Exception e) {
			logger.error("exception while getActivityList", e);
		}
		return resultList;
	}
	
	public List<ActivityItem> getActivityItemList(Activity activity, int displayMode, int pageIndex, int pageSize){
		List<ActivityItem> resultList = new ArrayList<ActivityItem>();
		String url = String.format("%s?displayMode=%s&pageIndex=%s&pageSize=%s&qsid=%s", activityItemListUrl, displayMode, pageIndex, pageSize, activity.getId());
		try {
			String[] response = HttpTool.get(url, cookie, "UTF-8", 5000, 5000);
			if(response != null){
				if(response[0].equals("200")){
					JSONObject json = JSON.parseObject(response[1]);
					if(json != null){
						Boolean result = json.getBoolean("Result");
						if(result != null){
							if(result){
								JSONObject data = json.getJSONObject("Data");
								if(data != null){
									JSONArray array = data.getJSONArray("NewItems");
									if(array != null){
										Iterator<Object> it = array.iterator();
										while(it.hasNext()){
											JSONObject item = (JSONObject)it.next();
											ActivityItem activityItem = new ActivityItem();
											activityItem.setId(item.getLongValue("ID"));
											activityItem.setTitle(item.getString("Title"));
											activityItem.setCover(item.getString("Cover"));
											activityItem.setPrice(item.getBigDecimal("Price"));
											activityItem.setDealCount(item.getIntValue("DealCount"));
											activityItem.setTotalQty(item.getIntValue("TotalQty"));
											activityItem.setShopId(item.getIntValue("ShopId"));
											activityItem.setChengTuanCount(item.getIntValue("ChengTuanCount"));
											activityItem.setDisplayStatuId(item.getIntValue("DisplayStatuId"));
											activityItem.setDisplayStatu(item.getString("DisplayStatu"));
											resultList.add(activityItem);
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("exception while getActivityItemList", e);
		}
		return resultList;
	}
}
