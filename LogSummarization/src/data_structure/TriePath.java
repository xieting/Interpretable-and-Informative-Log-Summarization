
package data_structure;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * an FP path is an ordered list of FPNodes
 * each element in the list should be the same count of occurrences
 * @author Ting Xie
 *
 */

public class TriePath implements Comparable<TriePath>{	  
	  private LinkedList<TrieNode> list;
	  
      public TriePath(LinkedList<TrieNode> list){
    	  this.list=list;    
      }
      
      public TriePath(TrieNode n){
    	  this.list=new LinkedList<TrieNode>();
    	  this.list.add(n);
      }
      public TriePath(){
    	  this.list=new LinkedList<TrieNode>();
      }
      
      public void addToTail(TrieNode node){
    	  if (this.list.isEmpty()||node.getCount()==this.list.get(0).getCount())
    	  this.list.add(node);    	  
    	  else
    	  System.out.println("add to pattern failed, need to have the same count");
    	  
      }
      
      public void addAllToTail(TriePath path){
    	  this.list.addAll(path.list);
      }
      
      public void removeLast(){
    	  this.list.removeLast();
      }
      
      public void removeFirst(){
    	  this.list.removeFirst();
      }
      
      public void addToFirst(TrieNode node){
    	  if (this.list.isEmpty()||node.getCount()==this.list.get(0).getCount())
    	  this.list.addFirst(node);    	  
    	  else
    	  System.out.println("add to pattern failed, need to have the same count");

      }
      
      public TrieNode  getFirst(){
    	  return this.list.getFirst();
      }
      
      /**
       * retrive and remove the first element
       * @return
       */
      public TrieNode  pullFirst(){
    	  return this.list.pollFirst();
      }
      
      public TrieNode  getLast(){
    	  return this.list.getLast();
      }          
      
           
      public String getMyLabels(){
    	  String line="";
    	  for (TrieNode n: this.list)
    		  line+=" "+n.getWord().getFeatureID()+":"+n.getWord().getOccurrence()+":"+n.getCount();
		return line;
      }
      
      /**
       * the string format of this pattern such that it is more human readable
       */
      @Override
      public String toString(){
    	  Iterator<TrieNode> it=this.list.iterator();
    	  String line="";
    	  TrieNode node;
    	  
    	 while(it.hasNext()){
    		 node=it.next();
    		  line+=GlobalVariables.leadsTo+node.getCount()+":"+node.toString()+"\n";
    	  }
    	 
		return line;   	  
      }
      
      /**
       * count the total number of queries contained in this pattern
       * @return
       */
      public int countTotal(){
    	  return this.list.getFirst().getCount();
    	  
      }
      
      public int getlength(){
    	  return this.list.size();
      }
      public LinkedList<TrieNode> getList(){
    	  return this.list;
      }

	@Override
	public int compareTo(TriePath arg0) {		
		return Integer.compare(this.list.get(0).getCount(), arg0.getFirst().getCount());
	}
}
