package com.mapbar.search.rank;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mapbar.search.common.pojo.POIObject;

/***
 * 计算最长公共子串
 * @author liupa
 *
 */
public class poiNameLCS {
	
	public static final Log LOG = LogFactory.getLog(poiNameLCS.class);

	private String query;
	private Map<String,String[]> synQueryMap;
	
	public void init(String query,Map<String,String[]> synQueryMap){
		this.query = query;
		this.synQueryMap = synQueryMap;
	}
	
	public float startCompute(POIObject poiObject){
		float LCSScore = 0;
		char[] queryArray = query.toCharArray();
		String name = poiObject.getName();
		name = name.replace(" ", "").replace("-", "").replace("/", "");
		char[] nameArray = name.toCharArray();
		int querylength = queryArray.length;
		int synlength = 0;
		/**计算查询关键词与名称的最长公共子串**/
		int queryLCS = LCS(queryArray,nameArray);
		/******计算同义词与名称的最长公共子序列***********************/
		int MaxSynLCS = 0;
		if(synQueryMap != null){
			String synQuery;
			Set<String> keys = synQueryMap.keySet();
			for(String key : keys){
	        	String[] temp = synQueryMap.get(key);
	        	for(int i = 0; i < temp.length; i++){
	        		synQuery = query.replaceAll(key, temp[i]);
	        		char[] synQueryArray = synQuery.toCharArray();
//	        		if(synlength < synQueryArray.length){
//	        			synlength = synQueryArray.length;
//	        		}
	        		int synLCS = LCS(synQueryArray,nameArray);
	        		if(synLCS > MaxSynLCS){
	        			MaxSynLCS = synLCS;
	        			synlength = synQueryArray.length;
	        		}
	        	}
	        }
		}
		
		if(querylength != 0 && synlength != 0){
			float qLCSScore = (float)queryLCS/querylength;
			float sLCSScore = (float)MaxSynLCS/synlength;
			return (qLCSScore >= sLCSScore)?qLCSScore:0.9f*sLCSScore;
		}
		else
			return 0.0f;
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
	/**
	 * 计算最长公共子串，时间复杂度：O(m)(n)
	 * @param ArrayX 
	 * @param ArrayY
	 * @return
	 */
	public int LCS(char[] ArrayX, char[] ArrayY){
		int llength = 0;
		int row = ArrayX.length;
		int column = ArrayY.length;
		/***distance是记录公共子串长度的二维数组***/
		int distance[][] = new int[row+1][column+1];
		/***flag是记录路径方向，用于回溯***/
		int flag[][] = new int[row+1][column+1];
		/***初始化数组边缘值****/
		for(int i = 0; i < row+1; i++){
			distance[i][0] = 0;
			flag[i][0] = 0;
		}
		for(int i = 0; i < column+1; i++){
			distance[0][i] = 0;
			flag[0][i] = 0;
		}
		GetLCSLen(ArrayX, ArrayY, distance, flag, row+1, column+1);
		llength = distance[row][column];
		//LOG.debug("LCS == "+llength);
		//TraceBack(ArrayX,flag,row+1,column+1);
		return llength;
	}
	
	public void GetLCSLen(char[] ArrayX, char[] ArrayY,int distance[][],int flag[][],int row, int column){
		for(int i = 1; i < row; i++){
			for(int j = 1; j < column; j++){
				if(ArrayX[i-1] == ArrayY[j-1]){
					distance[i][j] = distance[i-1][j-1]+1;
					flag[i][j] = 1;
				}
				else if(distance[i-1][j] >= distance[i][j-1]){
					distance[i][j] = distance[i-1][j];
					flag[i][j] = 2;
				}
				else{
					distance[i][j] = distance[i][j-1];
					flag[i][j] = 3;
				}
			}
		}
		//System.out.println("LCS == "+distance[row-1][column-1]);
	}
	
	public void TraceBack(String[] ArrayX, int flag[][], int row, int column){
		int temp;
		if(ArrayX == null || flag == null){
			return;
		}
		if(row == 0 || column == 0){
			return;
		}
		temp = flag[row-1][column-1];
		switch(temp){
			case 1:   
				System.out.println((row-1)+"\t"+(column-1)+"\t"+ArrayX[row-2]);//printf("locate:(%d,%d),%4c\n", nrow-1, ncolumn-1, str1[nrow-2]);//打印公共字符，这里下标是nrow-2，因为矩阵的坐标值(i,j)比字符串的实际下标大1
				TraceBack(ArrayX, flag, row-1, column-1);//向左上角递归
				break;
			case 2:
				TraceBack(ArrayX, flag, row-1, column);//向上方向递归
				break;
			case 3:
			    TraceBack(ArrayX, flag, row, column-1);//向左方向递归
			    break;
			default:
			    break;
		}
			
	}
	
	public static void main(String[] args){
		String X = "中关村家乐福";
		String Y = "家乐福中关村";
		char[] ArrayX = X.toCharArray();
		char[] ArrayY = Y.toCharArray();
		poiNameLCS pnl = new poiNameLCS();
		int LCSLength = pnl.LCS(ArrayX, ArrayY);
		System.out.println(X+" and "+Y+" LCS Value: "+LCSLength);
	}
}
