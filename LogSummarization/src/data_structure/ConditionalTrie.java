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
public class ConditionalTrie {
	private int supportLower;
	private int FPTotalCount;	
	private double entropyLower;
	private HashMap<ObservedFeatureOccurrence,Integer> totalFrequencyMap=new HashMap<ObservedFeatureOccurrence,Integer>();//keep track of totalFrequency of all 1-item sets
	private ConditionalTrie parent;
	private ObservedFeatureOccurrence conditionedItem;//the item that this conditional tree is conditioned on
	private Trie fptree;
	private int totalCount;
	private HashMap<Integer,HashMap<Integer,ObservedFeatureOccurrence>> itemMap;//its own sortable item map two keys itemID+occurrence
	private double hconfidence;
	private HashMap<ObservedFeatureOccurrence,Integer> rootTotalFrequencyMap;
	private int maxCount=-1;
	
	/**
	 * create and initialize a conditional tree
	 * @param supportThreshold
	 * @param content
	 * @param conditionedItem
	 */
	public ConditionalTrie(int supportLower,int FPTotalCount,double entropyLower,ObservedFeatureOccurrence conditionedItem,ConditionalTrie parent,int totalCount){
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
        this.entropyLower=entropyLower;
		this.parent=parent;
		this.conditionedItem=conditionedItem;    	
		this.fptree=new Trie();
		this.totalCount=totalCount;
		this.itemMap=parent.itemMap;
	}
	
	/**
	 * create and initialize a conditional tree
	 * @param supportThreshold
	 * @param content
	 * @param conditionedItem
	 */
	public ConditionalTrie(double hconfidence,int supportLower,int FPTotalCount,double entropyLower,ObservedFeatureOccurrence conditionedItem,ConditionalTrie parent,int totalCount){
		this.hconfidence=hconfidence;
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
		this.entropyLower=entropyLower;
		this.parent=parent;
		this.conditionedItem=conditionedItem;    	
		this.fptree=new Trie();
		this.totalCount=totalCount;
		this.itemMap=parent.itemMap;
		this.rootTotalFrequencyMap=parent.rootTotalFrequencyMap;
		
		int mycount=this.rootTotalFrequencyMap.get(this.conditionedItem);
		
		if(mycount>parent.maxCount)
			this.maxCount=mycount;
		else
			this.maxCount=parent.maxCount;
	
	}

	public ConditionalTrie(int supportLower,int FPTotalCount,double entropyLower,Trie fptree,HashMap<Integer,HashMap<Integer,ObservedFeatureOccurrence>> itemMap){
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
		this.entropyLower=entropyLower;
		this.fptree=fptree;
		//update its totalFrequency map
		HashMap<ObservedFeatureOccurrence, HashSet<TrieNode>> trackMap=fptree.getTrackMap();
		for (Entry<ObservedFeatureOccurrence, HashSet<TrieNode>> en: trackMap.entrySet()){
			ObservedFeatureOccurrence sitem=en.getKey();
			int count=0;
			for (TrieNode node: en.getValue()){
				count+=node.getCount();
			}
			this.totalFrequencyMap.put(sitem, count);
		}
		//we do not need this info here
		this.totalCount=-1;
		this.itemMap=itemMap;
	}
	
	public ConditionalTrie(double hconfidence,int supportLower,int FPTotalCount,double entropyLower,Trie fptree,HashMap<Integer,HashMap<Integer,ObservedFeatureOccurrence>> itemMap){
		this.hconfidence=hconfidence;
		this.supportLower=supportLower;
		this.FPTotalCount=FPTotalCount;
		this.entropyLower=entropyLower;
		this.fptree=fptree;
		//update its totalFrequency map
		HashMap<ObservedFeatureOccurrence, HashSet<TrieNode>> trackMap=fptree.getTrackMap();
		for (Entry<ObservedFeatureOccurrence, HashSet<TrieNode>> en: trackMap.entrySet()){
			ObservedFeatureOccurrence sitem=en.getKey();
			int count=0;
			for (TrieNode node: en.getValue()){
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

	public ConditionalTrie getParent(){
		return this.parent;
	}


	public ObservedFeatureOccurrence getConditionedItem(){
		return this.conditionedItem;
	}

	public Trie getItsFPTree(){
		return this.fptree;
	}

	/**
	 * feed the FP tree inside the conditional tree with FPPaths
	 * needs to update the total frequency of all consumed sortable items
	 * totalFrequencyMap will be built after this step
	 * a must-have step before using function pruneTree()
	 * @param path
	 */
	private void feedInFPPath(TriePath path){
		//accumulate the total frequency of each sortable item met in the path
		for(TrieNode node:path.getList()){
			ObservedFeatureOccurrence sitem=node.getObservedFeatureOccurrence();
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
		HashMap<ObservedFeatureOccurrence,Integer> whitelist=new HashMap<ObservedFeatureOccurrence,Integer>();
		List<ObservedFeatureOccurrence> sortedList=new ArrayList<ObservedFeatureOccurrence>();
		
		for(Entry<ObservedFeatureOccurrence, HashSet<TrieNode>> en: this.fptree.getTrackMap().entrySet()){
			ObservedFeatureOccurrence sitem=en.getKey();
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
			ObservedFeatureOccurrence tailItem=sortedList.get(sortedList.size()-1);
		   //conditioned on the tailItem and build a conditional tree
		    ConditionalTrie childTree=new ConditionalTrie(this.hconfidence,this.supportLower,this.FPTotalCount,this.entropyLower,tailItem,this,whitelist.get(tailItem)); 
		    //track the nodes of this tail item
		   HashSet<TrieNode> nodeset=this.fptree.getTrackMap().get(tailItem);
		    for (TrieNode node: nodeset){
		    	TriePath path=this.fptree.stripPathEndOnNodeExcluding(node);
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
				newpattern.addToSet(this.conditionedItem.getFeatureID(),this.conditionedItem.getOccurrence(),this.itemMap);
				newresult.put(newpattern, en.getValue());
			}
			//its condition itself is a valid pattern, add it in
			if(newresult.isEmpty()){
			Pattern mypattern=new Pattern();
			mypattern.addToSet(this.conditionedItem.getFeatureID(),this.conditionedItem.getOccurrence(),this.itemMap);
			newresult.put(mypattern, this.totalCount);
			}
			
			result=newresult;
		}
		
		return result;
	}


}
