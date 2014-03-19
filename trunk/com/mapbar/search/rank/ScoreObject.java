package com.mapbar.search.rank;

import com.mapbar.search.common.pojo.POIObject;

public class ScoreObject {
	private POIObject poiObject;
	/**名称编辑距离相似度得分*/
	private float EditDistanceScore;
	/**名称最长公共子串得分**/
	private float LCSScore;
	/**lucene计算的score*/
	private float LuceneScore;
	/**POI的热度得分*/
	private float poiRankScore;
	/**POI的距离得分*/
	private float poiDistance;
	/**ATM得分*/
	private float poiATMScore;
	/**Park得分*/
	private float poiParkScore;
	/**点击率得分*/
	private float hitScore;
	/**点密度得分,一个poi点附近*/
	private float pointWeightScore;
	/**总分*/
	private float totalScore;
	
	public float getEditDistanceScore() {
		return EditDistanceScore;
	}
	public void setEditDistanceScore(float editDistanceScore) {
		EditDistanceScore = editDistanceScore;
	}
	public float getLCSScore() {
		return LCSScore;
	}
	public void setLCSScore(float score) {
		LCSScore = score;
	}
	public float getLuceneScore() {
		return LuceneScore;
	}
	public void setLuceneScore(float luceneScore) {
		LuceneScore = luceneScore;
	}
	public float getPoiRankScore() {
		return poiRankScore;
	}
	public void setPoiRankScore(float poiRankScore) {
		this.poiRankScore = poiRankScore;
	}
	public float getPoiDistance() {
		return poiDistance;
	}
	public void setPoiDistance(float poiDistance) {
		this.poiDistance = poiDistance;
	}
	public float getPoiATMScore() {
		return poiATMScore;
	}
	public void setPoiATMScore(float poiATMScore) {
		this.poiATMScore = poiATMScore;
	}
	public float getPoiParkScore() {
		return poiParkScore;
	}
	public void setPoiParkScore(float poiParkScore) {
		this.poiParkScore = poiParkScore;
	}
	public float getHitScore() {
		return hitScore;
	}
	public void setHitScore(float hitScore) {
		this.hitScore = hitScore;
	}
	public float getPointWeightScore() {
		return pointWeightScore;
	}
	public void setPointWeightScore(float pointWeightScore) {
		this.pointWeightScore = pointWeightScore;
	}
	public POIObject getPoiObject() {
		return poiObject;
	}
	public void setPoiObject(POIObject poiObject) {
		this.poiObject = poiObject;
	}
	public float getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(float totalScore) {
		this.totalScore = totalScore;
	}

}
