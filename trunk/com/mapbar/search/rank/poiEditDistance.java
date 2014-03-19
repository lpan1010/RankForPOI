package com.mapbar.search.rank;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mapbar.search.common.pojo.POIObject;
/***
 * 计算编辑距离
 * 包含计算基于词的编辑距离（附加词的权重信息）
 * 以及计算基于字的编辑距离，两者统一为最后的编辑距离得分
 * @author liupa
 *
 */

public class poiEditDistance {
	public static final Log LOG = LogFactory.getLog(poiEditDistance.class);
	private String query;
	/**同义词*/
	private Map<String,String[]> syn = new HashMap<String, String[]>();
	/**分词之后的字符串数组*/
	private String[] queryArray;
	private String[] nameArray;
	/**分成一个一个的单字数组*/
	private char[] queryLDArray;
	private char[] nameLDArray;
	
	public poiEditDistance(){
	
	}
	
	public void init(String query,Map<String,String[]> syn){
		this.query = query.replace(" ", "").replace("-", "");
		this.syn = syn;
		this.queryArray = Segment.segment(this.query);
		this.queryLDArray = this.query.toCharArray();
	}
	
	/**
	 * 为每条结果计算编辑距离
	 */
	public float startCompute(POIObject poiObject){
		
		/**获取POI对象的名称*/
		String name = poiObject.getName();
		name = name.replace("-", "").replace(" ", "");
		/**分词*/
		nameArray = Segment.segment(name);
		nameLDArray = name.toCharArray();
		/**计算名称和query的编辑距离,返回一个数组，下标0表示基于词的编辑距离，下标1表示基于字的编辑距离*/
		float editDistance[] = computeEditDistance();
		//LOG.debug(name+"\t0: "+editDistance[0]+"\t1: "+editDistance[1]);
		/**计算相似度*/
		float maxscore = max(queryArray.length,nameArray.length);
		int maxLen = (query.length()>name.length()) ? query.length() : name.length();
		float simility = 0.5f*(1 - editDistance[0]/maxscore)+0.5f*(1 - editDistance[1]/maxLen);
		//LOG.debug("maxscore=="+maxscore+"\tmaxLen=="+maxLen+"\tsimility=="+simility);
		/**取小数点后5位*/
		simility = getNumfloat(simility, 5);
		/**存放到哈希表*/
	//	LOG.debug(name+"  name simility=="+simility);
		return simility;
	}
	/**
	 * 计算两个字符串数组的编辑距离
	 * @param name 
	 * @return 编辑距离（带有词的权重）
	 */
	public float[] computeEditDistance(){
		StrEditDistance sed = new StrEditDistance();
		float[] editDistance = new float[2];
		//计算query与name的编辑距离
		float queryEDScore = sed.wordEditDistance(queryArray, nameArray);
		float queryLDScore = sed.getLevenshteinDistance(queryLDArray, nameLDArray);
		//同义词处理
		float MinSynScore = Integer.MAX_VALUE;
		float MinSynLDScore = Integer.MAX_VALUE;
		
		if(syn != null){
			String synQuery;
			Set<String> keys = syn.keySet();
			for(String key : keys){
	        	String[] temp = syn.get(key);
	        	for(int i = 0; i < temp.length; i++){
	        		synQuery = query.replaceAll(key, temp[i]);
	        		String[] synQueryArray = Segment.segment(synQuery);
	        		char[] synQueryLDArray = synQuery.toCharArray();
	        	//	LOG.debug("同义词:"+temp[i]+"\t替换："+key+"后的查询词为："+synQuery);
	        		float synLDScore = sed.getLevenshteinDistance(synQueryLDArray, nameLDArray); 
	        		float synScore = sed.wordEditDistance(synQueryArray, nameArray);
	        		if(synScore < MinSynScore){
	        			MinSynScore = synScore;
	        		}
	        		if(synLDScore < MinSynLDScore){
	        			MinSynLDScore = synLDScore;
	        		}
	        	}
	        }
		}
		
		/**查询词得分*/
		
		if(queryEDScore < MinSynScore){
			editDistance[0] = queryEDScore;
		}
		else{
			editDistance[0] = MinSynScore;
		}
		if(queryLDScore < MinSynLDScore){
			editDistance[1] = queryLDScore;
		}
		else {
			editDistance[1] = MinSynLDScore;
		}
		return editDistance;
	}

	/**
	 * 取长度最大值
	 * @param qlen 查询词的长度
	 * @param nlen POI对象名称的长度
	 * @return
	 */
	public float max(int qlen, int nlen){
		float score1 = 0, score2 = 0;
		for(int i = 0; i < qlen; i++){
			score1 = score1 + WordWeight.getWeight(queryArray[i]);
		}
		for(int i = 0; i < nlen; i++){
			score2 = score2 + WordWeight.getWeight(nameArray[i]);
		}
		if(score1 > score2)
			return score1;
		else
			return score2;

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
    
    public static void main(String[] args){
    	String X = "中关村家乐福";
    	String Y = "家乐福中关村";
    	poiEditDistance ped = new poiEditDistance();
    	ped.init(X, null);
    }
}
