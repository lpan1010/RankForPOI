package com.mapbar.search.rank;

import java.util.HashMap;

import com.mapbar.search.common.pojo.POIObject;
/**
 * 过滤结果逻辑
 * @author liupa
 *
 */
public class Filter {
	
	private HashMap<POIObject,Double> list;
	
	public Filter(HashMap<POIObject,Double> list){
		this.list = list;
	}
}
