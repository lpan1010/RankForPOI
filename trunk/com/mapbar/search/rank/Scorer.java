package com.mapbar.search.rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mapbar.search.common.pojo.POIObject;

public class Scorer {
	
	public static final Log LOG = LogFactory.getLog(Scorer.class);
	public static int confidenceNum;
	
	private List<ScoreObject> scoreObjectList = new ArrayList<ScoreObject>();
	/**综合得分哈希表*/
	private String searchType;
	public HashMap<POIObject, Float> objectIntegrateScore = new HashMap<POIObject, Float>();
	public Scorer(){}
	
	public Scorer(List<ScoreObject> scoreObjectList, String searchType){
		this.scoreObjectList = scoreObjectList;
		this.searchType = searchType;
	}
	
	public List<ScoreObject> startScore(){
		/**名称编辑距离相似度得分*/
		float EditDistanceScore;
		/**名称最长公共子串得分**/
		float LCSScore;
		/**lucene计算的score*/
		float LuceneScore;
		/**POI的热度得分*/
		float poiRankScore;
		/**POI的距离得分*/
		float poiDistance;
		/**ATM得分*/
		float poiATMScore;
		/**Park得分*/
		float poiParkScore;
		/**Hit得分*/
		float hitScore;
		/**点密度得分*/
		float pointWeightScore;
		/**获取编辑距离的最小值，最大值，平均值*/
		if(scoreObjectList.size() <= 0){
			return null;
		}
		ArrayList<Float> editDistanceStat = new ArrayList<Float>();
		editDistanceStat = statEDScore(scoreObjectList);
		System.out.println("average score:"+editDistanceStat.get(2));
		/**name完全匹配的结果集合*/
		HashMap<ScoreObject, Float> nameMatchedScore = new HashMap<ScoreObject, Float>();
		/**name不完全匹配，但是得分仍然很高的结果集合**/
		HashMap<ScoreObject, Float> nameHighScore = new HashMap<ScoreObject, Float>();
		/**name不完全匹配，且得分很低的结果集合**/
		HashMap<ScoreObject, Float> nameLowScore = new HashMap<ScoreObject, Float>();
		/**
		 * 排序分成了一部分一部分，名称完全匹配的结果，作为一部分。
		 * 名称不完全匹配的结果，作为另一部分。
		 */
		if(searchType == "NEARBY"){
			for(ScoreObject scoreObject : scoreObjectList){
				EditDistanceScore = scoreObject.getEditDistanceScore();
				LCSScore = scoreObject.getLCSScore();
				LuceneScore = scoreObject.getLuceneScore();
				poiRankScore = scoreObject.getPoiRankScore();
				hitScore = scoreObject.getHitScore();
				pointWeightScore = scoreObject.getPointWeightScore();
				poiDistance = scoreObject.getPoiDistance();
				poiATMScore = scoreObject.getPoiATMScore();
				poiParkScore = scoreObject.getPoiParkScore();
				
				float integrateScore = 8.0f*EditDistanceScore + 12.0f*LCSScore +7.0f*hitScore + 4.0f*pointWeightScore 
				 	+ 14.0f*poiDistance;
			nameMatchedScore.put(scoreObject, integrateScore);
			scoreObject.setTotalScore(integrateScore);
			}
		}
		else{
			for(ScoreObject scoreObject : scoreObjectList){
				EditDistanceScore = scoreObject.getEditDistanceScore();
				LCSScore = scoreObject.getLCSScore();
				LuceneScore = scoreObject.getLuceneScore();
				poiRankScore = scoreObject.getPoiRankScore();
				hitScore = scoreObject.getHitScore();
				pointWeightScore = scoreObject.getPointWeightScore();
				/**关键词搜索的时候，跟距离没有关系*/
				//poiDistance = scoreObject.getPoiDistance();
				poiATMScore = scoreObject.getPoiATMScore();
				poiParkScore = scoreObject.getPoiParkScore();
				
				/**
				 * 分批排序
				 * 目的是想让优良的结果突出，最差的结果垫底，同时不同批次的特征权重可以不同。
				 * 1.如果名称完全匹配，这些结果优先显示。多个完全匹配的结果，按照综合得分排序，由于名称匹配度都是1，综合得分不必计算名称匹配度。
				 * 如果名称完全匹配，以后甚至可以只显示这部分排序。
				 * 2.名称相似度较高的结果（0.2-1.0之间），按照综合得分排序，计入名称相似度。
				 * 3.名称相似度较低的结果（0-0.2之间），这部分结果不需要全部显示，可以砍掉综合得分较低的结果。
				 */
				if(EditDistanceScore == 1.0){
					if(hitScore == 1.0 || pointWeightScore == 1.0){
						/**名称完全匹配并且点击率最高或者点密度最高*/
						nameMatchedScore.put(scoreObject, 100.0f);
						scoreObject.setTotalScore(100.0f);
					}
					else {
						float integrateScore = 16.0f*EditDistanceScore + 16.0f*LCSScore +10.0f*hitScore + 10.0f*pointWeightScore 
							+ 2.0f*LuceneScore + 1.0f*poiRankScore;
						nameMatchedScore.put(scoreObject, integrateScore);
						scoreObject.setTotalScore(integrateScore);
					}
				}
				/**最长公共子串得分为1，并且编辑距离得分要大于平均编辑距离得分*/
				/**这部分结果将会显示在最前面，得分限制比较高，LCS的得分高，是为了使得这部分结果排在最前面**/
				else if(LCSScore >= 0.9f && EditDistanceScore >= editDistanceStat.get(2)){
					float integrateScore = 8.0f*EditDistanceScore + 25.0f*LCSScore + 3.0f*hitScore 
					+ 1.0f*pointWeightScore + 4.0f*poiATMScore + 4.0f*poiParkScore ;
					nameHighScore.put(scoreObject, integrateScore);
					scoreObject.setTotalScore(integrateScore);
				}
				/**这部分结果较差，条件限制：LCS得分要求偏高，同时编辑距离得分要求偏高*/
				else if(LCSScore >= 0.5f && LCSScore < 0.9f && EditDistanceScore >= Math.max(editDistanceStat.get(2), 0.5f*(editDistanceStat.get(0)+editDistanceStat.get(1)))){
					float integrateScore = 14.0f*EditDistanceScore + 10.0f*LCSScore + 7.0f*hitScore 
					+ 3.0f*pointWeightScore + 1.0f*LuceneScore
					+ 5.0f*poiATMScore + 5.0f*poiParkScore ;
					
					if(poiATMScore == -1.0 || poiParkScore == -1.0){
						nameLowScore.put(scoreObject, integrateScore);
						scoreObject.setTotalScore(integrateScore);
					}
					else {
						nameHighScore.put(scoreObject, integrateScore);
						scoreObject.setTotalScore(integrateScore);
					}
				}
				else {
					float integrateScore = 10.0f*EditDistanceScore + 10.0f*LCSScore + 8.0f*hitScore 
						+ 5.0f*pointWeightScore + 2.0f*LuceneScore
						+ 5.0f*poiATMScore + 5.0f*poiParkScore ;
					
					if(poiATMScore == -1.0 || poiParkScore == -1.0){
						nameLowScore.put(scoreObject, integrateScore);
						scoreObject.setTotalScore(integrateScore);
					}
					else {
						nameHighScore.put(scoreObject, integrateScore);
						scoreObject.setTotalScore(integrateScore);
					}
				}
			}
		}
		
		List<ScoreObject> list1 = ResultRank(nameMatchedScore);
		List<ScoreObject> list2 = ResultRank(nameHighScore);
		List<ScoreObject> list3 = ResultRank(nameLowScore);
		
		List<ScoreObject> result = new ArrayList<ScoreObject>();
		result.addAll(list1);
		result.addAll(list2);
		result.addAll(list3);
		/**设置置信度结果个数*/
		if(list1 != null){
			confidenceNum = list1.size();
		}
		else if(list2 != null){
			if(list2.size() >= 10){
				confidenceNum = 10;
			}
			else{
				confidenceNum = list2.size();
			}
		}
		//如果结果集为空，表示结果集中的名称匹配度都非常低
		//按照lucene原有的进行排序
		if(result.size() == 0 && scoreObjectList.size() != 0){
			LOG.debug("name score is too low and the result is null. Lucene Score sort.");
			for(ScoreObject scoreObject : scoreObjectList){
				nameLowScore.put(scoreObject,scoreObject.getLuceneScore());
			}
			result = ResultRank(nameLowScore);
		}
		
		
		return result;
	}

	public HashMap<POIObject, Float> getObjectIntegrateScore() {
		return objectIntegrateScore;
	}
	/***
	 * 统计编辑距离得分的最低分，最高分，平均分
	 * @return
	 */
	public ArrayList<Float> statEDScore(List<ScoreObject> list){
		ArrayList<Float> editDistanceStat = new ArrayList<Float>();
		int size = list.size();
		float averageScore = 0, minScore = 0, maxScore = 0;
		for(int i = 0; i < size; ++i){
			float edScore = list.get(i).getEditDistanceScore();
			averageScore += edScore;
			if(edScore < minScore){
				minScore = edScore;
			}
			if(edScore > maxScore){
				maxScore = edScore;
			}
		}
		editDistanceStat.add(minScore);
		editDistanceStat.add(maxScore);
		editDistanceStat.add(averageScore/size);
		return editDistanceStat;
	}

	public void setObjectIntegrateScore(
			HashMap<POIObject, Float> objectIntegrateScore) {
		this.objectIntegrateScore = objectIntegrateScore;
	}
	
	public List<ScoreObject> ResultRank(HashMap<ScoreObject, Float> RankObject){
		int size = RankObject.size(); 
		ArrayList<Map.Entry<ScoreObject, Float>> list = new ArrayList<Map.Entry<ScoreObject, Float>>(size);
		list.addAll(RankObject.entrySet()); 
		ValueComparator vc = new ValueComparator();  
        Collections.sort(list, vc);  
        List<ScoreObject> keys = new ArrayList<ScoreObject>(size); 
        for (int i = 0; i < size; i++) {  
            keys.add(i, list.get(i).getKey());
        }  
        return keys;
	}
	
	private class ValueComparator implements Comparator<Map.Entry<ScoreObject, Float>>  
    {  
        public int compare(Map.Entry<ScoreObject, Float> mp1, Map.Entry<ScoreObject, Float> mp2)   
        {  
            if(mp1.getValue()>mp2.getValue()){
            	return -1;
            }
            else if(mp1.getValue()<mp2.getValue()){
            	return 1;
            }
            else
            	return 0;
        }  
    }  
}
