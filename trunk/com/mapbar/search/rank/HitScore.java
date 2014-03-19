package com.mapbar.search.rank;

import java.math.BigDecimal;
import java.util.List;

import com.mapbar.search.common.pojo.POIObject;
/**
 * 
 * @author liupa
 *
 */
public class HitScore {
	/**
	 * 归一化处理
	 * @param list
	 */
	private static int minScore;
	private static int maxScore;
	
	public void init(List<POIObject> list){
		/**
		 * 判断列表是否为空
		 */
		if (list.size() == 0){
			return;
		}
		/**初始化最小值和最大值*/
		minScore = 0; 
		maxScore = 0;
		/**
		 * 求最大值和最小值
		 */
		for(POIObject poiObject : list){
			int poiScore = poiObject.getHit();;
			if(poiScore > maxScore){
				maxScore = poiScore;
			}
			if(poiScore < minScore){
				minScore = poiScore;
			}
		}
		if(maxScore == minScore){
			System.out.println("The max hit score equals the min hit score");
		}
	}
	public float Normalization(POIObject poiObject){
		/**
		 * 归一化处理
		 * 采用线性函数转换，表达式如下：
         * y=(x-MinValue)/(MaxValue-MinValue)
         * 说明：x、y分别为转换前、后的值，MaxValue、MinValue分别为样本的最大值和最小值。
		 */
		float newScore = 0;
		int poiScore = poiObject.getHit();
		if(maxScore == minScore){
			newScore = 0.0f;
		}
		else{
			newScore = (float)(poiScore-minScore)/(maxScore-minScore);
			newScore = getNumfloat(newScore, 5);
		}
		return newScore;
	}
	
	 /**
     * float 类型取后面N位小数 N自定义.
     * @param 
     * @return
     */
    public float getNumfloat(float score, int num) {
        BigDecimal bd = new BigDecimal(score);
        float c = bd.setScale(num , BigDecimal.ROUND_HALF_UP).floatValue();
        return c;
    }


}
