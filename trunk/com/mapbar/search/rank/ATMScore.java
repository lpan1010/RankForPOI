package com.mapbar.search.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mapbar.search.common.pojo.POIObject;
/**
 * 计算ATM得分，类型包含ATM，或者名称包含matchName的poi，得分设置为-1，不包含设置0
 * @author liupa
 *
 */
public class ATMScore {
	/**结果得分的哈希表,<poi对象,得分>*/
	private String matchType = "ATM";
	private String[] matchName = {"自动柜员机","自动取款机","ATM","atm"};
	/**
	 * 开始计算得分
	 */
	public float startCompute(POIObject poiObject){
		float score = 0.0f; 
		String type = poiObject.getTypeName();
		String name = poiObject.getName();
		
		int index_type = -1;
		index_type = type.indexOf(matchType);
		int index_name = -1;
		for (int i = 0; i < matchName.length; i++){
			if(name.indexOf(matchName[i]) != -1){
				index_name = 0;
				break;
			}
		}
		if(index_type != -1 || index_name != -1){
			score = -1.0f;
		}
		else{
			score = 0.0f;
		}
		return score;
	}
}
