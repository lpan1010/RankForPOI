package com.mapbar.search.rank;

import java.util.ArrayList;
import java.util.HashMap;

import com.mapbar.nlp.cws.MapbarCWS;

/**
 *  基于词的编辑距离
 * @author liupa
 *
 */
public class StrEditDistance {
	/**
	 * 
	 * 这是一种字符串之间相似度计算的方法。
	 * 给定字符串S、T，将S转换T所需要的插入、删除、替代、交换位置操作的数量叫做S到T的编辑路径。
	 * 其中最短的路径叫做编辑距离。
	 * 
	 * 这里使用了一种动态规划的思想求编辑距离。
	 * 
	 */
	/**字符串X的字符数组*/
	private String[] ArrayX=null;
	/**字符串Y的字符数组*/
	private String[] ArrayY=null;

	public void init(String[] ArrayX, String[] ArrayY){
		setArrayX(ArrayX);
		setArrayY(ArrayY);
	}
	
	/**
	 * 计算基于词的编辑距离
	 * @return 编辑距离
	 */
	public float wordEditDistance(String[] ArrayX, String[] ArrayY){
		init(ArrayX,ArrayY);
		float score = 0;
		/**
		 * 特殊情况处理，query长度为0,或者name长度为0
		 */
		if (ArrayX.length==0&&ArrayY.length==0){
			score = Integer.MAX_VALUE;
		}
		else if (ArrayX.length==0&&ArrayY.length>=0){
			for(int i = 0; i < ArrayY.length; i++){
				score = score + 1*(WordWeight.getWeight(ArrayY[i]));
			}
		}
		else if(ArrayX.length>=0&&ArrayY.length==0){
			 score = Integer.MAX_VALUE;
		}
		/**query和name的长度都不为零*/
		else {
			/**
			 * 基于词的位置交换
			 * 返回交换次数
			 * 这里相当于分两步去计算编辑距离
			 * 1、计算交换的原子操作，返回交换的编辑距离
			 * 2、计算插入、删除、替换的原子操作，返回插入、删除、替换的编辑距离
			 */
			int swapcount = exchange();
			if(swapcount != 0){
				score = editDistance(ArrayX.length - 1, ArrayY.length - 1)+swapcount*5;
			}
			else{
				score = editDistance(ArrayX.length - 1, ArrayY.length - 1);
			}
			/**调用童童的没有交换的编辑距离*/
			//score = getLevenshteinDistance()+swapcount*5.0;
		}
		return score;
}
	/**
	 * 交换位置
	 * 查找ArrayX和ArrayY中的相同子字符串，如果存在两个相同字符串，且位置相反，则交换位置。
	 * 如：ArrayX = "西直门地铁"，ArrayY = "地铁西直门"
	 */
//	public static void main(String[] args) {
//		StrEditDistance sed = new StrEditDistance();
//		//ArrayList<>
//	}
	private int exchange()
	{
		int swapcount = 0;//交换次数
		int i = 0, j = 0;
		//字符串x交换的初始位置数组
		ArrayList<Integer> indexx = new ArrayList<Integer>();
		//字符串x交换的各段长度
		ArrayList<Integer> lenx = new ArrayList<Integer>();
		//字符串y与x相同部分的起始位置数组
		ArrayList<Integer> indexy = new ArrayList<Integer>();
		while(i < ArrayX.length){
			//每次循环，置origin=i,j=0
			//origin参数用于记录每次循环的i的初始位置
			int origin = i;
			j=0;
			//k表示有多少个词相同
			int k = 0;
			while(j < ArrayY.length){
				
				if(ArrayX[i].equals(ArrayY[j]) ){
					
					k = k + 1;
					indexx.add(i);
					indexy.add(j);
					while(j+k<ArrayY.length && i+k < ArrayX.length){
						if(ArrayY[j+k].equals(ArrayX[i+k])){
							k++;
						}
						else{
							break;
						}
					}
					lenx.add(k);
					//如果ArrayX中找到k个字符相同，那么i往前移动k个位置，再进一步比较。
					i = i + k;
					break;
				}
				else{
					j++;
				}
			}
			//如果i没有移动k个位置，即没有找到相同字符串，那么继续往前移动1个位置，再进一步比较。
			if (origin == i){
				i++;
			}
		}
		if(indexx.size() > 1){
			swapcount = swap(indexx,indexy,lenx);
		}
		return swapcount;
	}
	/**
	 * 动态规划解决编辑距离
	 * 
	 * editDistance(i,j)表示字符串X中[0.... i]的子串 Xi 到字符串Y中[0....j]的子串Y1的编辑距离。
	 * 
	 * @param i 字符串X第i个字符
	 * @param j 字符串Y第j个字符
	 * @return 字符串X(0...i)与字符串Y(0...j)的编辑距离
	 */
	private float editDistance(int i,int j){
		if(i==0 && j==0){
			//System.out.println("edit["+i+","+j+"]="+isModify(i,j));
			return isModify(i,j);
		}
		else if(i==0 || j==0){
			if(j > 0){
				//System.out.println("edit["+i+","+j+"]=edit["+i+","+(j-1)+"]+1");
				if(isModify(i,j) == 0){
					return editDistance(i, j-1);	
				} 
				else
					return editDistance(i, j-1) + 1*(WordWeight.getWeight(ArrayY[j]));
			}
			else{
				//System.out.println("edit["+i+","+j+"]=edit["+(i-1)+","+j+"]+1");
				if(isModify(i,j) == 0) {
					return editDistance(i-1,j);
				}
                return editDistance(i-1,j) + 1*(WordWeight.getWeight(ArrayX[i]));
			}
		}
		else {
			float a = editDistance(i-1,j)+1*(WordWeight.getWeight(ArrayX[i]));
			float b = editDistance(i,j-1)+1*(WordWeight.getWeight(ArrayY[j]));
			float c = editDistance(i-1,j-1)+isModify(i,j);
			float min = minDistance(a,b,c);
			return min;
		}
	
	}
	/**
	 * 交换位置,有了相同子串的起始位置以及子串的长度，就可以进行位置交换
	 * @param indexx 字符串X与字符串Y相同的子串的起始位置数组
	 * @param indexy 字符串Y与字符串X相同的子串的起始位置数组
	 * @param len 相同子串的长度数组
	 * @return swapcount 交换的次数
	 */
	int  swap(ArrayList<Integer> indexx, ArrayList<Integer> indexy, ArrayList<Integer> len){
		int swapcount = 0;
		if(indexx.size() <= 1){
			return swapcount;
		}
		int first,second,length1,length2;
		for (int i = 1; i < indexx.size(); i++){
			if(i%2 == 1 ){
				first = indexx.get(i-1);
				second = indexx.get(i);
				//如果位置下标是交错的，交换位置
				if(indexy.get(i-1) > indexy.get(i)){
					//交换动作
					length1 = len.get(i-1);
					length2 = len.get(i);
					String[] temp = new String[length1];
					for (int j = first; j < first+length1;j++){
						temp[j-first] = ArrayX[j];
					}
					for (int j = second; j < second+length2;j++){
						ArrayX[j-length1] = ArrayX[j];
					}
					for (int j = 0; j < temp.length; j++){
						ArrayX[first+length2+j] = temp[j];
					}
					swapcount++;
				}	
			}
		}
		return swapcount;
	}
	/**
	 * 求最小值
	 * @param disa 编辑距离a
	 * @param disb 编辑距离b
	 * @param disc 编辑距离c
	 */
	private float minDistance(float disa,float disb,float disc){
		float dismin=Integer.MAX_VALUE;
		if(dismin>disa) dismin=disa;
		if(dismin>disb) dismin=disb;
		if(dismin>disc) dismin=disc;
		return dismin;
	}
	/**
	 * 两个词之间是否替换
	 * 
	 * isModify(i,j)表示X中第i个词x(i)转换到Y中第j个词y(j)所需要的操作次数。
	 * 如果x(i)==y(j)，则不需要任何操作isModify(i, j)=0； 否则，需要替换操作，考虑替换的词的权重，isModify(i, j)=1。
	 * @param i 字符串X第i个字符
	 * @param j 字符串Y第j个字符
	 * @return 需要替换，返回1；否则，返回0
	 */
	private float isModify(int i,int j){
		//System.out.println("i== "+i+"  j=="+j);
		if(ArrayX[i].equals(ArrayY[j]))
			return 0;
		else return 1*(WordWeight.getWeight(ArrayX[i]));
	}
	/**
	 * 
	 * @return
	 */
	public float getLevenshteinDistance(char[] ArrayX, char[] ArrayY) {
		if (ArrayX == null || ArrayY == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}
		int n = ArrayX.length; // length of s
		int m = ArrayY.length; // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			// swap the input strings to consume less memory
			char[] tmp = ArrayX;
			ArrayX = ArrayY;
			ArrayY = tmp;
			n = m;
			m = ArrayY.length;
		}

		float p[] = new float[n + 1]; // 'previous' cost array, horizontally
		float d[] = new float[n + 1]; // cost array, horizontally
		float _d[]; // placeholder to assist in swapping p and d

		// indexes floato strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		float cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = ArrayY[j - 1];
			d[0] = j;
			for (i = 1; i <= n; i++) {
				cost = (ArrayX[i - 1] == t_j) ? 0 : 1;
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}
			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}
		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}
	public String[] getArrayX() {
		return ArrayX;
	}
	public void setArrayX(String[] arrayX) {
		ArrayX = arrayX;
	}
	public String[] getArrayY() {
		return ArrayY;
	}
	public void setArrayY(String[] arrayY) {
		ArrayY = arrayY;
	}
}
