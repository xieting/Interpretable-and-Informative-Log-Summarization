package data_structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;


/**
 * this tree stores paths that:
 * conditioned on the existence of one item listed in 'white list' (with support greater than threshold)
 * based on its parent conditional tree, one can create a conditional tree furthered conditioned on some item:
 * e.g. if the parent is conditioned on item X, then a child can be created by further conditioned on Y, which means
 * the child is conditioned on X,Y both appears
 * @author Ting
 *
 */
public class ConditionalTree {
	private int supportLower;
	private int FPTotalCount;	
	private double entropyLower;
	private HashMap<Word,Integer> totalFrequencyMap=new HashMap<Word,Integer>();//keep track of totalFrequency of all 1-item sets
	private ConditionalTree parent;
	private Word conditionedItem;//the item that this conditional tree is conditioned on
	private FP_InferenceTree fptree;
	private int totalCount;
	private HashMap<Integer,HashMap<Integer,Word>> itemMap;//its own sortable item map two keys itemID+occurrence
	private double hconfidence;
	private HashMap<Word,Integer> rootTotalFrequencyMap;
	private int maxCount=-1;
	
	/**
	 * create and initialize a conditional tree
	 * @param supportThreshold
	 * @param content
	 * @param conditionedItem
	 */
	public ConditionalTree(int supportLower,int FPTotalCount,double entropyLower,Word conditionedItem,ConditionalTree parent,int totalCount){
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
        this.entropyLower=entropyLower;
		this.parent=parent;
		this.conditionedItem=conditionedItem;    	
		String extension="_"+conditionedItem.toString();
		String path=parent.getItsFPTree().getPath()+extension;
		String dumppath=parent.getItsFPTree().getDumpPath()+extension;
		this.fptree=new FP_InferenceTree(path,dumppath);
		this.totalCount=totalCount;
		this.itemMap=parent.itemMap;
	}
	
	/**
	 * create and initialize a conditional tree
	 * @param supportThreshold
	 * @param content
	 * @param conditionedItem
	 */
	public ConditionalTree(double hconfidence,int supportLower,int FPTotalCount,double entropyLower,Word conditionedItem,ConditionalTree parent,int totalCount){
		this.hconfidence=hconfidence;
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
		this.entropyLower=entropyLower;
		this.parent=parent;
		this.conditionedItem=conditionedItem;    	
		String extension="_"+conditionedItem.toString();
		String path=parent.getItsFPTree().getPath()+extension;
		String dumppath=parent.getItsFPTree().getDumpPath()+extension;
		this.fptree=new FP_InferenceTree(path,dumppath);
		this.totalCount=totalCount;
		this.itemMap=parent.itemMap;
		this.rootTotalFrequencyMap=parent.rootTotalFrequencyMap;
		
		int mycount=this.rootTotalFrequencyMap.get(this.conditionedItem);
		
		if(mycount>parent.maxCount)
			this.maxCount=mycount;
		else
			this.maxCount=parent.maxCount;
	
	}

	public ConditionalTree(int supportLower,int FPTotalCount,double entropyLower,FP_InferenceTree fptree,HashMap<Integer,HashMap<Integer,Word>> itemMap){
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
		this.entropyLower=entropyLower;
		this.fptree=fptree;
		//update its totalFrequency map
		HashMap<Word, HashSet<FPNode>> trackMap=fptree.getTrackMap();
		for (Entry<Word, HashSet<FPNode>> en: trackMap.entrySet()){
			Word sitem=en.getKey();
			int count=0;
			for (FPNode node: en.getValue()){
				count+=node.getCount();
			}
			this.totalFrequencyMap.put(sitem, count);
		}
		//we do not need this info here
		this.totalCount=-1;
		this.itemMap=itemMap;
	}
	
	public ConditionalTree(double hconfidence,int supportLower,int FPTotalCount,double entropyLower,FP_InferenceTree fptree,HashMap<Integer,HashMap<Integer,Word>> itemMap){
		this.hconfidence=hconfidence;
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
		this.entropyLower=entropyLower;
		this.fptree=fptree;
		//update its totalFrequency map
		HashMap<Word, HashSet<FPNode>> trackMap=fptree.getTrackMap();
		for (Entry<Word, HashSet<FPNode>> en: trackMap.entrySet()){
			Word sitem=en.getKey();
			int count=0;
			for (FPNode node: en.getValue()){
				count+=node.getCount();
			}
			this.totalFrequencyMap.put(sitem, count);
		}
		//we do not need this info here
		this.totalCount=-1;
		this.itemMap=itemMap;
		this.rootTotalFrequencyMap=this.totalFrequencyMap;
		this.maxCount=-1;
	}

	public ConditionalTree getParent(){
		return this.parent;
	}


	public Word getConditionedItem(){
		return this.conditionedItem;
	}

	public FP_InferenceTree getItsFPTree(){
		return this.fptree;
	}

	/**
	 * feed the FP tree inside the conditional tree with FPPaths
	 * needs to update the total frequency of all consumed sortable items
	 * totalFrequencyMap will be built after this step
	 * a must-have step before using function pruneTree()
	 * @param path
	 */
	private void feedInFPPath(FPPath path){
		//accumulate the total frequency of each sortable item met in the path
		for(FPNode node:path.getList()){
			Word sitem=node.getWord();
			Integer oldcount=this.totalFrequencyMap.get(sitem);
			if(oldcount==null)
				this.totalFrequencyMap.put(sitem,0+node.getCount());
			else
				this.totalFrequencyMap.put(sitem,oldcount+node.getCount());	
		}
		this.fptree.consume(path);
	}
	
	/**
	 * get frequent patterns that pass the support threshold from this conditional tree
	 * @return
	 */
	
	public HashMap<Pattern,Integer> getHyperCliquePatterns(){
		HashMap<Pattern,Integer>  result=new HashMap<Pattern,Integer>();
		
		//first we get a  white list of items
		HashMap<Word,Integer> whitelist=new HashMap<Word,Integer>();
		List<Word> sortedList=new ArrayList<Word>();
		
		for(Entry<Word, HashSet<FPNode>> en: this.fptree.getTrackMap().entrySet()){
			Word sitem=en.getKey();
			int count=this.totalFrequencyMap.get(sitem);
			int maxC=Math.max(this.maxCount, this.rootTotalFrequencyMap.get(sitem));
			double maxCC=(double)maxC;
			double hconfidenceratio=((double)count)/maxCC;
			//System.out.println(hconfidenceratio);
			if(hconfidenceratio>=this.hconfidence&&count>this.supportLower){
			whitelist.put(sitem,count); 			
			sortedList.add(sitem);
			}
		}
		//System.out.println("I am conditioned on "+this.conditionedItem);
		//System.out.println("whitelist "+whitelist);
		//next we sort these items by their order in the tree
		Collections.sort(sortedList);
		
		//starting from the tail item
		while(!sortedList.isEmpty()){
			Word tailItem=sortedList.get(sortedList.size()-1);
		   //conditioned on the tailItem and build a conditional tree
		    ConditionalTree childTree=new ConditionalTree(this.hconfidence,this.supportLower,this.FPTotalCount,this.entropyLower,tailItem,this,whitelist.get(tailItem)); 
		    //track the nodes of this tail item
		   HashSet<FPNode> nodeset=this.fptree.getTrackMap().get(tailItem);
		    for (FPNode node: nodeset){
		    	FPPath path=this.fptree.stripPathEndOnNodeExcluding(node);
		    	//feed the child with the path
		    	childTree.feedInFPPath(path);
		    }
		    //get frequent patterns from child 
		    HashMap<Pattern,Integer> childresult=childTree.getHyperCliquePatterns();
		    result.putAll(childresult);
		    //remove this tail item
		    sortedList.remove(sortedList.size()-1);
		}

		//add its condition in
		if(this.conditionedItem!=null){
			HashMap<Pattern,Integer> newresult=new HashMap<Pattern,Integer>();			
			//then add its condition as header to the frequent patterns got from children	
			for (Entry<Pattern,Integer> en: result.entrySet()){
				Pattern pattern=en.getKey();
				Pattern newpattern=new Pattern(pattern);
				newpattern.addToSet(this.conditionedItem,this.itemMap);
				newresult.put(newpattern, en.getValue());
			}
			//its condition itself is a valid pattern, add it in
			if(newresult.isEmpty()){
			Pattern mypattern=new Pattern();
			mypattern.addToSet(this.conditionedItem,this.itemMap);
			newresult.put(mypattern, this.totalCount);
			}
			
			result=newresult;
		}
		
		return result;
	}


}
