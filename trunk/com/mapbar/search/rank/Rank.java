package com.mapbar.search.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mapbar.nlp.qas.QASResult;
import com.mapbar.nlp.qas.query.MapbarSearchPattern;
import com.mapbar.search.common.pojo.POIObject;
import com.mapbar.search.common.pojo.QueryPara;
import com.mapbar.search.common.pojo.SearchResult;
import com.mapbar.search.common.pojo.SearchResultToSort;
import com.mapbar.search.common.pojo.SortResult;
import com.mapbar.search.search.POISearcher;

public class Rank {
	
	public static final Log LOG = LogFactory.getLog(Rank.class);
	
	/**结果集*/
	private List<POIObject> list;
	/**query*/
	private String query;
	/**同义词*/
	private Map<String,String[]> syn = new HashMap<String, String[]>();
	/**query分析结果*/
	private QASResult qasResult;
	/**搜索类型*/
	private String searchType;
	/**
	 * 构造方法
	 * @param list 搜索结果集合
	 * @param queryStr 查询字符串
	 */
	
	public Rank(){
		
	}
	
	public SortResult sort(SearchResultToSort srts){
		SortResult sortResult = null;
		/**
		 *排序入口函数，首先从SearchResultToSort对象中获取三个参数，分别是： 
		 * 1. QASResult:这个是对查询词进行解析后的结果，比如同义词等。
		 * 2. QueryPara:包含与查询相关参数，比如查询词，查询城市，返回多少个结果等等
		 * 3. poi list:搜索出来的poi结果集合
		 **/
		QASResult qasResult = srts.getQas();
		QueryPara queryPara = srts.getQueryPara();
		List<POIObject> poiList = srts.getToSortList();
		//初始化步骤
		initialRank(poiList,queryPara,qasResult);
		if(list == null || list.size() == 0){
			LOG.debug("search result is null!");
			return null;
		}
		//开始排序
		sortResult = startRank();
		return sortResult;
	}
	
	public void initialRank(List<POIObject> poiList, QueryPara queryPara, QASResult qasResult){
		this.list = poiList;
		this.qasResult = qasResult;
		this.query = queryPara.getStrQuery();
		//加载词以及词的权重
		ResourceBundle bundle = ResourceBundle.getBundle("rank");
		String wordWeightPath = bundle.getString("word_weight_path");
		MapbarSearchPattern.SearchType  st = qasResult.generateMapbarSearchQuery().get(0).one;
		searchType = st.name();
		System.out.println("search type:"+searchType);
		WordWeight.loadWordWeight(wordWeightPath);
		syn = qasResult.generateSynonymsForSearcher();
	}
	/**
	 * 开始排序
	 */
	public SortResult startRank(){
		SortResult sortResult = new SortResult();
		/**计算特征得分*/
		List<ScoreObject> scoreObjectList = computeFeatureScore();
		/**计算综合特征得分，并排序*/	
		Scorer s = new Scorer(scoreObjectList, searchType);
		List<ScoreObject> result = s.startScore();
		/**打印结果*/
		PrintResult(result);
		/**返回结果*/
		List<POIObject> list = new ArrayList<POIObject>();
		if(result == null || result.size() <= 0){
			LOG.debug("result is null!");
			return null;
		}
		else{
			for(int i = 0; i < result.size(); i++){
				list.add(result.get(i).getPoiObject());
			}
			sortResult.setResult(list);
			return sortResult;
		}
		
	}
	/**
	 * 计算各种特征的分值
	 */
	public List<ScoreObject> computeFeatureScore(){
		List<ScoreObject> scoreObjectList= new ArrayList<ScoreObject>();
		/**创建各种特征对象*/
		poiEditDistance ped = new poiEditDistance();
		ped.init(query,syn);
		poiNameLCS pns = new poiNameLCS();
		pns.init(query, syn);
		LuceneScore ls = new LuceneScore();
		ls.init(list);
		poiRankScore prs = new poiRankScore();
		prs.init(list);
		HitScore hs = new HitScore();
		hs.init(list);
		pointWeightScore pws = new pointWeightScore();
		pws.init(list);
		DistanceScore ds = new DistanceScore();
		ds.init(list);
		ATMScore as = new ATMScore();
		ParkScore ps = new ParkScore();
		
		for(POIObject poiObject : list){
			/**计算某一条结果的各个特征得分*/
			float editDistanceScore = ped.startCompute(poiObject);
			float LCSScore = pns.startCompute(poiObject);
			float luceneScore = ls.Normalization(poiObject);
			float poiRankScore = prs.Normalization(poiObject);
			float hitScore = hs.Normalization(poiObject);
			float pointWeightScore = pws.Normalization(poiObject);
			float distanceScore = ds.Normalization(poiObject);
			float poiATMScore = as.startCompute(poiObject);
			float poiParkScore = ps.startCompute(poiObject);
			/**保存得分*/
			ScoreObject so = new ScoreObject();
			so.setPoiObject(poiObject);
			so.setEditDistanceScore(editDistanceScore);
			so.setLCSScore(LCSScore);
			so.setLuceneScore(luceneScore);
			so.setPoiRankScore(poiRankScore);
			so.setHitScore(hitScore);
			so.setPointWeightScore(pointWeightScore);
			so.setPoiDistance(distanceScore);
			so.setPoiATMScore(poiATMScore);
			so.setPoiParkScore(poiParkScore);
			/**加入得分对象列表*/
			scoreObjectList.add(so);
		}
		return scoreObjectList;
	}
	/**
	 * 结果输出
	 */
	public void PrintResult(List<ScoreObject> result){
		if(result == null || result.size() <= 0){
			return;
		}
		for(ScoreObject so : result){
			System.out.println(so.getPoiObject().getNid()+"\t"+so.getPoiObject().getName()+
					"\t"+so.getPoiObject().getAddress()+"\t"+so.getPoiObject().getProvince());
			System.out.println("TotalScore:"+so.getTotalScore()+"  EditDis:"+so.getEditDistanceScore()+" LCS:"+so.getLCSScore()+
					" HitScore:"+so.getHitScore()+" pointWScore:"+so.getPointWeightScore()+
					" luceneScore:"+so.getLuceneScore()+" rankScore:"+so.getPoiRankScore()+" distance:"+so.getPoiDistance());
		}
	}
	public static void main(String[] args){
		
		String queryStr = "光大";
        String city = "北京市";
        QueryPara para = new QueryPara(city, queryStr, 0, 200);
//      para.setUseSecondSearch(true);
//      para.setRange(5);
//      para.setCity(city);
//      para.setStrQuery(queryStr);
//      para.setStart(0);
//      para.setEnd(200);
//      para.setUseShould(true);
//      para.setType(MapbarSearchPattern.SearchType.KEYWORD);
//      para.setType(MapbarSearchPattern.SearchType.NEARBY);
//      para.setCenterPoint(new MapPoint(116.3134819, 39.97941816));
        /**
         * SearchResult是搜索之后的结果对象
         * SearchResultToSort是用于排序的对象，与SearchResult进行简单的转换。
         * 
         **/
        SearchResult result = POISearcher.search(para);
        SearchResultToSort toSort = new SearchResultToSort();
        toSort.setQas(result.getParserResult().getQueryResult());
        toSort.setQueryPara(result.getPara());
        toSort.setToSortList(result.getPois());
        /***
         * 调用排序算法，输入是SearchResultToSort对象，返回排序结果SortResult对象。
         */
		Rank rank = new Rank();
		SortResult sortResult = rank.sort(toSort);
	}
}
