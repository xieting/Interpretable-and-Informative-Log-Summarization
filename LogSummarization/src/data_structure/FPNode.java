
package data_structure;

import java.util.HashMap;

/**
 * a node in the FP-tree
 * @author Ting Xie
 *
 */
public class FPNode implements Comparable<FPNode>{
      private Word feature;
      private int depth;//depth of this node
      private int count;//count of # of occurrences of the pattern represented by this node
	  private HashMap<Integer, HashMap<Integer,FPNode>> chMap;// hashmap of its children branch, the first Integer is the feature label, second Integer is the occurrence
	  private FPNode parent;//its parent node
	  private int offset;//used to offset the occurrences of its sortableFeature
	  
    public FPNode(Word feature){
    	this.feature=feature;
    	this.count=1;
    	this.chMap=new HashMap<Integer, HashMap<Integer,FPNode>>();
    	this.depth=0;
    	this.offset=0;
    }
    
    public FPNode(FPNode node){
    	this.feature=node.feature;
    	this.count=0;
    	this.chMap=new HashMap<Integer, HashMap<Integer,FPNode>>();
    	this.depth=0;
    	this.offset=0;
    }
    
    public int getOffSet(){
    	return this.offset;
    }
    
    public void setOffSet(int count){
    	if (count>this.offset)
    	this.offset=count;
    }
    
    public void addOffSet(int count){
    	this.offset+=count;
    }
   
    /**
     * count of # of occurrences of the pattern represented by this node
     * @return
     */
    public int getCount(){
    	return this.count;
    }
    
    public FPNode getParent(){return this.parent;}
    
    public void setParent(FPNode n){this.parent=n;}
    
    public Word getWord(){
    	return this.feature;
    }

    
    /**
     * add a single node as its child
     * @param node
     */
    public void addChild(FPNode node){
    	int label=node.getWord().getFeatureID();
    	int occurrence=node.getWord().getOccurrence();
    	
    	HashMap<Integer,FPNode> map=this.chMap.get(label);
    	if (map==null){
    		map=new HashMap<Integer,FPNode> ();
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
    public void removeChild(FPNode child){
    	Word sfeature=child.getWord();
    	int occurrence=sfeature.getOccurrence();   	
    	int label=sfeature.getFeatureID();
    	
    	HashMap<Integer, FPNode> map=this.chMap.get(label);
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
	
	
	public HashMap<Integer, HashMap<Integer,FPNode>>  getChildren(){
		return this.chMap;
	}


	@Override
	public int compareTo(FPNode o) {		
		return this.getWord().compareTo(o.getWord());
	}
}

