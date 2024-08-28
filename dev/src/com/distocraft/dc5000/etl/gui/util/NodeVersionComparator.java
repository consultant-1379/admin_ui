/**
 * 
 */
package com.distocraft.dc5000.etl.gui.util;


import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author xthobob
 * This is class is used to compare the node versions from Techpack or Node.
 * This is using custom comparator to compare the different versions.
 */
public class NodeVersionComparator {
	
	private TreeSet<String> nodesWithAlphaNum = new TreeSet<String>(new CustomComparisonTry());
	private TreeSet<String> nodesWithSmallNumber = new TreeSet<String>(new CustomComparisonTry());
	private TreeSet<String> nodesWithLargeNumber = new TreeSet<String>(new CustomComparisonTry());
	private TreeSet<String> nodesWithQs = new TreeSet<String>(new CustomComparisonTry());
	private Map<String,String> nodeIndexMap = new HashMap<String,String>();
	private static final String REGEX_1 = ".*[a-pr-zA-PR-Z]+.*";  

	/**
	 * This method is called to find the highest node from the list.
	 * @param List of node versions.
	 * @return Highest version of node.
	 */
	public String highestNodeFromList(String[] nodeList){
		if(nodeList.length == 1){
	    	 return nodeList[0];
	     }
	    for(String token : nodeList){
	    	if(!token.equals(" ") || !(token.equals("NA"))){
	    	String temp = token;
	    	if(token.substring(0,1).matches(".*[rgRGPpTtVv]+.*")){
        		temp = token.substring(1);
        	}
	    	if((token.matches(".*\\d+-\\d*.") | Arrays.toString(nodeList).matches("\\d+\\.[a-pA-Pr-zR-Z]")) | (token.matches(REGEX_1) && token.split("-")[0].matches(REGEX_1) && token.split("\\.")[0].matches(REGEX_1))){
	    		nodesWithAlphaNum.add(temp);
	    	}
	    		
	    	else{
	    		if(temp.matches(".*[qQ]+.*"))
	    			nodesWithQs.add(temp);
	    		else{
	    			String string = temp.split("\\.")[0] + (temp.split("\\.").length > 1 ? temp.split("\\.")[1].charAt(0) : "");
	    			if(Double.parseDouble(string) > 19)
	    				nodesWithLargeNumber.add(temp);
	    			else
	    				nodesWithSmallNumber.add(temp);
	    		}
	    	}
	    	nodeIndexMap.put(temp, token);
	    	}else{
	    		continue;
	    	}
	     }
	     if(!nodesWithSmallNumber.isEmpty()){
	    	 return nodeIndexMap.get(nodesWithSmallNumber.last());
	     }
	     else if(nodesWithQs.isEmpty()){
	    		 if(nodesWithAlphaNum.isEmpty() || (!nodesWithLargeNumber.isEmpty() && Double.parseDouble(nodesWithAlphaNum.last().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0]) < Double.parseDouble(nodesWithLargeNumber.last().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0]))){
	    			 return nodeIndexMap.get(nodesWithLargeNumber.last());
	    		 }
	    		 else
	    			 return nodeIndexMap.get(nodesWithAlphaNum.last());
	    	 }
	    	 else
	    		 return nodeIndexMap.get(nodesWithQs.last());
	}
}

/**
 * @author xthobob
 * This is used to compare the two node / Techpack versions and return the highest among them. 
 *
 */
class CustomComparisonTry implements Comparator<String>{
	private static final String REGEX_2 = ".*[a-zA-Z]+.*";

	public int compare(String element1, String element2) {
  
	      String[] a1 = element1.split("\\.");
	      String[] b1 = element2.split("\\.");
	      int result = 0;
	      int itterator1 = 0;
	      int length = a1.length > b1.length ? b1.length : a1.length;
	      while(itterator1 < length){
	   	   String[] a2 = a1[itterator1].split("-");
	   	   String[] b2 = b1[itterator1].split("-");
	   	   int length2 = a2.length > b2.length ? b2.length : a2.length;
	   	   int itterator2 = 0;
	   	   while(itterator2 < length2){
	   		   if(itterator2 == 0){
	   			   int firstNumber=0;
	   				int secondNumber =0;
	   			   int index1 = 0;
	   			   while(index1 < a2[0].length()){
	   				   if((Character.toString(a2[0].charAt(index1))).matches(REGEX_2)){
	   					   break;
	   				   }
	   				   ++index1;
	   			   }
	   			   if(index1 != 0){
	   				   firstNumber = Integer.parseInt(a2[0].substring(0, index1));
	   			   }
	   			   int index2 = 0;
	   			   while(index2 < b2[0].length()){
	   				   if((Character.toString(b2[0].charAt(index2))).matches(REGEX_2)){
	   					   break;
	   				   }
	   				   ++index2;
	   			   }
	   			   if(index2 != 0){
	   				   secondNumber = Integer.parseInt(b2[0].substring(0, index2));
	   			   }
	   			   if(firstNumber == secondNumber){
	   				   if(index1 == a2[0].length() || index2 == b2[0].length()){
	   					if(a2[0].length() != b2[0].length()){
	   						return (a2[0].length() - b2[0].length()) / Math.abs(a2[0].length() - b2[0].length());
	   					}
	   				   }
	   				   else if (a2[0].substring(index1).compareTo(b2[0].substring(index2))!= 0){
	   					   return a2[0].substring(index1).compareTo(b2[0].substring(index2));
	   			   }
	   			   }
	   			   else{
	   				   return firstNumber > secondNumber ? 1 : -1;
	   			   }
	   		   }
	   		   else{
	   	   if(a2[itterator2].compareTo(b2[itterator2]) > 0){
	   		   return 1;
	   	   }
	   	   else if(b2[itterator2].compareTo(a2[itterator2]) > 0){
	   		   return -1;
	   	   }
	   	   
	   	   }
	   		if(result == 0){
			   	   if(a2.length == b2.length)
			   	      ++itterator2;	
			   	   else
			   	   return (a2.length - b2.length) / Math.abs(a2.length - b2.length);
			      }
	   	   }
	   	   ++itterator1;
	      }
	      if(result == 0){
	   	   if(a1.length == b1.length)
	   		   return result;
	   	   return (a1.length - b1.length) / Math.abs(a1.length - b1.length);
	      }
	   	   else
	   	   return result;
	}
}
