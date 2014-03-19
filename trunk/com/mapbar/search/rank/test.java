package com.mapbar.search.rank;

import java.io.File;

public class test {
	public static void main(String[] args){
		String s1 = "北京邮电大学图书馆";
		String s2 = "北京邮电大学";
		StrEditDistance sed = new StrEditDistance();
		float lD = sed.getLevenshteinDistance(s2.toCharArray(), s1.toCharArray());
		System.out.println("lD:"+lD);
	}

}
