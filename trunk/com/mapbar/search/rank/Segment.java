package com.mapbar.search.rank;

import com.mapbar.nlp.cws.MapbarCWS;

public class Segment {

	public static String[] segment(String segString){
		//分词处理
		return MapbarCWS.segment(segString).toArray();
	}
}
