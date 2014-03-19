package com.mapbar.search.rank;

import com.mapbar.search.common.pojo.POIObject;
/**
 * 计算停车场得分，包含停车场的分数将降低
 * @author liupa
 *
 */
public class ParkScore {
	/**结果得分的哈希表,<poi对象,得分>*/
	private String[] matchName = {"停车场","停车楼","车库"};
	/**
	 * 开始计算得分
	 */
	public float startCompute(POIObject poiObject){
		float score;
		String name = poiObject.getName();
		int index_name = -1;
		for(int i = 0; i < matchName.length; i++){
			if (name.indexOf(matchName[i]) != -1){
				index_name = 0;
				break;
			}
		}
		if(index_name == -1){
			score = 0.0f;
		}
		else{
			score = -1.0f;
		}
		return score;
	}
}
