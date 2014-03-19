package com.mapbar.search.rank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class WordWeight {
	
	public static HashMap<String, Double> word_weight = new HashMap<String, Double>();
	
	public static void loadWordWeight(String path){
		 
		File file = new File(path);
		String line;
		try {
			BufferedReader bd = new BufferedReader(new InputStreamReader(
					new FileInputStream(file),"UTF8"));
			while((line = bd.readLine())!= null){
				String temp[] = line.split("\t");
				if(temp.length != 2)
					continue;
				else 
					word_weight.put(temp[0].trim(), Double.valueOf(temp[1].trim()));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return word_weight;
	}
	
	/**
	 * 查询str词的权重，如果str不在哈希表中，那么返回一个常量
	 * @param str
	 * @return
	 */
	public static float getWeight(String str){
		Object o = word_weight.get(str);
		if(o == null){
			return 5;
		}
		else{
			return Float.parseFloat(o.toString());
		}
	}
}
