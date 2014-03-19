package com.mapbar.search.rank;

import java.math.BigDecimal;
import java.util.List;

import com.mapbar.search.common.pojo.POIObject;
/**
 * 
 * @author liupa
 *
 */
public class DistanceScore {
	private static double minScore;
	private static double maxScore;
	public void init(List<POIObject> list){
		/**
		 * 判断列表是否为空
		 */
		if (list.size() == 0){
			return;
		}
		/**初始化最小值和最大值*/
		minScore = list.get(0).getDistance(); 
		maxScore = list.get(0).getDistance();
		/**
		 * 求最大值和最小值
		 */
		for(POIObject poiObject : list){
			double poiScore = poiObject.getDistance();
			if(poiScore > maxScore){
				maxScore = poiScore;
			}
			if(poiScore < minScore){
				minScore = poiScore;
			}
		}
		if (maxScore == minScore){
			System.out.println("The max distance score is equal the min score.");
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
		double poiScore = poiObject.getDistance();
		/**当最大值等于最小值，说明distanceScore都是一样的，随便赋予一个值，因为此时distanceScore已经对排序没有意义了*/
		if(minScore == maxScore){
			newScore = 0.0f;
		}
		/**注意，距离与得分是负相关，距离越小，归一化之后的得分越高*/
		else{
			newScore = (float) (1.0 - (float) ((float)(poiScore - minScore)/(maxScore-minScore)));
			newScore = getNumfloat(newScore, 5);
		}
		return newScore;
	}
	
	 /**
     * float 类型取后面N位小数 N自定义.
     * @param nodesTemp
     * @return
     */
    public float getNumfloat(float score, int num) {
        BigDecimal bd = new BigDecimal(score);
        float c = bd.setScale(num , BigDecimal.ROUND_HALF_UP).floatValue();
        return c;
    }

}
