
package data_structure;

import java.util.HashMap;

/**
 * a node in the FP-tree
 * @author Ting Xie
 *
 */
public class TrieNode implements Comparable<TrieNode>{
      private ObservedFeatureOccurrence feature;
      private int depth;//depth of this node
      private int count;//count of # of occurrences of the pattern represented by this node
	  private HashMap<Integer, HashMap<Integer,TrieNode>> chMap;// hashmap of its children branch, the first Integer is the feature label, second Integer is the occurrence
	  private TrieNode parent;//its parent node
	  
    public TrieNode(ObservedFeatureOccurrence feature){
    	this.feature=feature;
    	this.count=1;
    	this.chMap=new HashMap<Integer, HashMap<Integer,TrieNode>>();
    	this.depth=0;
    }
    
    public TrieNode(TrieNode node){
    	this.feature=node.feature;
    	this.count=0;
    	this.chMap=new HashMap<Integer, HashMap<Integer,TrieNode>>();
    	this.depth=0;
    }
   
    /**
     * count of # of occurrences of the pattern represented by this node
     * @return
     */
    public int getCount(){
    	return this.count;
    }
    
    public TrieNode getParent(){return this.parent;}
    
    public void setParent(TrieNode n){this.parent=n;}
    
    public ObservedFeatureOccurrence getObservedFeatureOccurrence(){
    	return this.feature;
    }

    
    /**
     * add a single node as its child
     * @param node
     */
    public void addChild(TrieNode node){
    	int label=node.getObservedFeatureOccurrence().getFeatureID();
    	int occurrence=node.getObservedFeatureOccurrence().getOccurrence();
    	
    	HashMap<Integer,TrieNode> map=this.chMap.get(label);
    	if (map==null){
    		map=new HashMap<Integer,TrieNode> ();
    		this.chMap.put(label, map);
    	}

    	map.put(occurrence,node);   	   	   	
    	//let it know I am your parent
    	node.setParent(this);
    	//set its depth
    	node.depth=this.depth+1;
    }
    
    /**
     * assume child is already in its children map
     * @param child
     */
    public void removeChild(TrieNode child){
    	ObservedFeatureOccurrence sfeature=child.getObservedFeatureOccurrence();
    	int occurrence=sfeature.getOccurrence();   	
    	int label=sfeature.getFeatureID();
    	
    	HashMap<Integer, TrieNode> map=this.chMap.get(label);
    	if(map!=null){
    	map.remove(occurrence,child);
    	//if map is empty
    	if(map.isEmpty())
    		this.chMap.remove(label,map);
    	}
    	child.setParent(null);
    	
    }   

    public int getDepth(){
    	return this.depth;
    }
	public void addCount(int count) {
		if (count<0){
			System.out.println("negative input found, try to use deductCount method: "+count);
		}
		else
			this.count+=count;	
	}
	
	public void deductCount(int count) {
		if (count<0){
			System.out.println("negative input found, try to use addCount method: "+count);
		}
		else
			this.count-=count;	
	}
	
    
	@Override
	public String toString(){
		return this.feature.toString();
	}
	
	
	public HashMap<Integer, HashMap<Integer,TrieNode>>  getChildren(){
		return this.chMap;
	}


	@Override
	public int compareTo(TrieNode o) {		
		return this.getObservedFeatureOccurrence().compareTo(o.getObservedFeatureOccurrence());
	}
}

