package data_structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import feature_management.FeatureVector;




/**
 * this is the main part, FP_inference tree
 * @author Ting Xie
 *
 */
public class FeatureVector_Trie {
	private HashMap<Integer,HashMap<Integer,ObservedFeatureOccurrence>> observedFeatureOccurrenceMap;//its own feature map, two keys featureID+occurrence
	private HashMap<ObservedFeatureOccurrence,HashSet<TrieNode>> trackMap;//tracking the position of sortable features
	private LinkedHashMap<Integer,TreeMap<Integer,Integer>> featureOccurrenceGEQMarginal; // For <Feature_i,Occurrence>, it stores p(Feature_i>=occurrence)
	private LinkedHashMap<Integer,TreeMap<Integer,Integer>> featureOccurrenceEQMarginal; // For <Feature_i,Occurrence>, it stores p(Feature_i=occurrence)	
	private HashMap<TrieNode,Integer> instanceMap=new HashMap<TrieNode,Integer>(); //store the tail Nodes of all multivariate observations
	private TrieNode root;//root of this tree, defined as null
	private int count;//total number of feature sets parsed
	private int distinctcount; //number of feature sets parsed disregarding multiplicity
	private int totalcount;//total number of features parsed
	private double entropyLower;
	private double naiveSummaryError=-1;//KL divergence if KL(P|Q) where P--the actual distribution stored in the tree and Q--the naive distribution assuming independence	
	private double NaiveEntropy=-1;
	private double TrueEntropy=-1;
	private int numOfLeaves=-1;
	private String featurevectorPath=null;
	private String multiplicityPath=null;
	
	public FeatureVector_Trie(){
		this.observedFeatureOccurrenceMap=new HashMap<Integer,HashMap<Integer,ObservedFeatureOccurrence>>();
		this.trackMap=new HashMap<ObservedFeatureOccurrence,HashSet<TrieNode>> ();
		ObservedFeatureOccurrence feature = null;
		this.root=new TrieNode(feature);
		this.count=0;
		this.totalcount=0;
		this.distinctcount=0;
	}
	
	public FeatureVector_Trie(String featureVectorPath,String multiplicityPath){
		this.featurevectorPath=featureVectorPath;
		this.multiplicityPath=multiplicityPath;
		ArrayList<Integer> multiplicities=new ArrayList<Integer>();
		
		//register feature vectors using one linear scan over the data
		try {
			BufferedReader br=new BufferedReader(new FileReader(featureVectorPath));
			Scanner multiplicitybr=new Scanner(new File(multiplicityPath)).useDelimiter(GlobalVariables.inputDataDelimiter);			
			String line;
			while((line=br.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				if(!vector.isEmpty()){
				Integer multiplicity=multiplicitybr.nextInt();
				multiplicities.add(multiplicity);
				this.registerFeatureVector(vector, multiplicity);
				}
			}
			multiplicitybr.close();
			br.close();
		} catch ( IOException e) {
			e.printStackTrace();
		}
		
		//after registration, use one more linear scan to consume and build the trie
		try {
			BufferedReader br=new BufferedReader(new FileReader(featureVectorPath));
			String line;
			int index=0;
			while((line=br.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				if (vector.size()>0){	
					this.consumeFeatureVector(vector, multiplicities.get(index));
					index++;
				}				
			}
			br.close();
		} catch ( IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public FeatureVector_Trie(String featureVectorPath){
		this.featurevectorPath=featureVectorPath;
		//register feature vectors using one linear scan over the data
		try {
			BufferedReader br=new BufferedReader(new FileReader(featureVectorPath));
			String line;
			while((line=br.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				if(!vector.isEmpty()){
				this.registerFeatureVector(vector);
				}
			}
			br.close();
		} catch ( IOException e) {
			e.printStackTrace();
		}
		
		//after registration, use one more linear scan to consume and build the trie
		try {
			BufferedReader br=new BufferedReader(new FileReader(featureVectorPath));
			String line;
			while((line=br.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				this.consumeFeatureVector(vector);				
			}
			br.close();
		} catch ( IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * give a deep copy
	 * @param input
	 */
	public FeatureVector_Trie(FeatureVector_Trie input){
		this.observedFeatureOccurrenceMap=new HashMap<Integer,HashMap<Integer,ObservedFeatureOccurrence>>(input.observedFeatureOccurrenceMap);
		this.trackMap=new HashMap<ObservedFeatureOccurrence,HashSet<TrieNode>>(input.trackMap);
		this.root=input.root;
		this.count=input.count;
		this.distinctcount=input.distinctcount;
		this.totalcount=input.totalcount;
		this.featurevectorPath=input.featurevectorPath;
		this.multiplicityPath=input.multiplicityPath;
	}

	public HashMap<ObservedFeatureOccurrence,HashSet<TrieNode>> getTrackMap(){
		return this.trackMap;
	}

	public void clearTree(){
		this.root.getChildren().clear();
		this.observedFeatureOccurrenceMap.clear();
	}

	/**
	 * this method consumes an FPPath and build the tree step by step
	 * it assumes the input FPPath is correctly ordered
	 * @param list
	 */
	public void consume(TriePath P){
		TrieNode tail=P.getLast();
		int tailCount=tail.getCount();

		//traverse this tree
		TrieNode start=null;//start is the node that last match happens
		if (root.getChildren().isEmpty()){
			//update the record for newly consumed instance
			this.instanceMap.put(tail,tailCount);
			branch(root,P);
		}
		else {			
			start=this.locate(root, P);
			if (!P.getList().isEmpty()){
				//update the record for newly consumed instance
				this.instanceMap.put(tail,tailCount);
				branch(start,P);
			}
			else{
				//update the record for newly got instance
				Integer oldCount=this.instanceMap.get(start);
				if (oldCount==null)
					oldCount=0;

				this.instanceMap.put(start,oldCount+tailCount);
			}
		}
	}

	/**
	 * branch on unmatched parts and add nodes one by one
	 * @param start
	 * @param P
	 */
	private void branch(TrieNode start,TriePath P){

		TrieNode currentnode=start;
		TrieNode node;
		while (!P.getList().isEmpty()){
			node=P.pullFirst();
			currentnode.addChild(node);			
			//register this node in track map whenever a new node is added
			ObservedFeatureOccurrence sfeature=node.getWord();
			if(sfeature!=null){
				HashSet<TrieNode> list=this.trackMap.get(sfeature);
				if(list==null){
					list=new HashSet<TrieNode>();
					//register it into trackmap
					this.trackMap.put(sfeature, list);
				}
				list.add(node);	 
			}
			//then start from the newly added node as the current node	
			currentnode=node;
		}
	}


	public TrieNode getRoot(){
		return this.root;
	}

	/**
	 * assumes root is matched, try to locate the position of the last match of P from root onwards
	 * and add the count of current pattern in the common path
	 * @param feature
	 * @return
	 */
	private TrieNode locate(TrieNode root,TriePath P){

		if (!P.getList().isEmpty()){
			TrieNode targetNode=P.getFirst();
			HashMap<Integer, HashMap<Integer,TrieNode>> chMap=root.getChildren();
			HashMap<Integer,TrieNode> map=chMap.get(targetNode.getWord().getFeatureID());
			TrieNode nextnode=null;
			if (map!=null)
				nextnode=map.get(targetNode.getWord().getOccurrence());

			if (nextnode==null){
				return root;
			}
			else {	
				nextnode.addCount(targetNode.getCount());
				P.removeFirst();
				return locate(nextnode,P);
			}

		}
		else return root;
	}

	private boolean validateNodes(TrieNode root){
		//validate node depths
		if(root==this.root){
			if(root.getDepth()!=0)
				return false;
		}
		else {
			if((root.getDepth()-root.getParent().getDepth())!=1)
				return false;
		}

		HashMap<Integer, HashMap<Integer, TrieNode>> map=root.getChildren();
		//if it has children
		if(!map.isEmpty()){
			int count=0;
			for(Entry<Integer, HashMap<Integer, TrieNode>> en: map.entrySet()){
				HashMap<Integer, TrieNode> featuremap=en.getValue();
				for(Entry<Integer,TrieNode> enn: featuremap.entrySet()){
					if(enn.getValue().getCount()<=0)
						return false;
					else
						count+=enn.getValue().getCount();	

					boolean result=validateNodes(enn.getValue());
					if(result==false)
						return false;				
				}
			}

			if(count>root.getCount()&&root.getWord()!=null)
				return false;
		}

		return true;
	}

	private boolean validateTrackMap(TrieNode root){
		HashMap<Integer, HashMap<Integer, TrieNode>> map=root.getChildren();
		//if it has children
		if(!map.isEmpty()){
			for(Entry<Integer, HashMap<Integer, TrieNode>> en: map.entrySet()){
				HashMap<Integer, TrieNode> featuremap=en.getValue();
				for(Entry<Integer,TrieNode> enn: featuremap.entrySet()){
					TrieNode node=enn.getValue();
					ObservedFeatureOccurrence sfeature=node.getWord();
					if(!this.trackMap.get(sfeature).contains(node))
						return false;
					else {
						if(validateTrackMap(node)==false)
							return false;
					}						
				}
			}
		}
		return true;
	}

	private boolean validateInstanceMap(){
		int sum=0;
		for (Integer count:this.instanceMap.values())
			sum+=count;
		return sum==this.count&& this.instanceMap.values().size()==this.distinctcount;
	}

	/**
	 * validate if this tree is correct
	 */
	public void validateTree(){
		System.out.println("this tree's marginal&joint probabilities can be correctly computed?: "+this.validateProbabilityComputation());
		System.out.println("this tree's nodes are correctly built?: "+this.validateNodes(this.root));
		System.out.println("this tree's trackMap is correctly built?: "+this.validateTrackMap(this.root));
		System.out.println("this tree has "+this.count+" number of feature sets");
		System.out.println("this tree 's instanceMap is correctly built?: "+this.validateInstanceMap());
		System.out.println("this tree has "+this.distinctcount+" number of feature sets disregarding duplicates");
		System.out.println("this tree has "+this.getNumOfLeaves()+" number of leaf nodes.");
		System.out.println("this tree has "+this.getNodeNumber(this.root)+" number of nodes in total");
		System.out.println("total number of consumed features are: "+this.totalcount);
		System.out.println("total number of distinct features are: "+this.getTrackMap().size());
	}

	public int getNumOfLeaves(){
		int num=this.numOfLeaves;
		if(num<0){
			num=0;
			for(Entry<ObservedFeatureOccurrence,HashSet<TrieNode>> en:this.trackMap.entrySet()){  		
				for(TrieNode n: en.getValue()){
					//if it is a leaf node, then calculate P(i) for this path
					if(n.getChildren().isEmpty()){						
						num++;
					}    				
				}   		
			}
		}
		return num;
	}

	/**
	 * a naive summary is simply a bag of features mapped with their marginals 
	 * @return
	 */
	public LinkedHashMap<ObservedFeatureOccurrence,Double> getNaiveSummary(){

		LinkedHashMap<ObservedFeatureOccurrence,Double> naiveSummary=new LinkedHashMap<ObservedFeatureOccurrence,Double>();
		for (Entry<Integer, TreeMap<Integer, Integer>> en: this.getFeatureDistribution().entrySet()) {
			int featureID=en.getKey();
			for (Entry<Integer, Integer> enn: en.getValue().entrySet()){
				int occurrence=enn.getKey();
				int frequency=enn.getValue();
				if(occurrence!=0){
					double marginal=(double)frequency/(double)this.count;
					ObservedFeatureOccurrence w=this.observedFeatureOccurrenceMap.get(featureID).get(occurrence);
					if(w==null){
						System.out.println("cannot find word for featureID "+featureID+" occurrence "+occurrence +". Please check.");
					}
					naiveSummary.put(w, marginal);
				}
			}
		}
		return naiveSummary;		
	}

	private int getNodeNumber(TrieNode root){
		int count=1;
		HashMap<Integer, HashMap<Integer,TrieNode>> map = root.getChildren();
		if (!map.isEmpty()){
			for (Entry<Integer, HashMap<Integer,TrieNode>> en: map.entrySet()){
				for (Entry<Integer, TrieNode> entry: en.getValue().entrySet()){
					count+=getNodeNumber(entry.getValue());
				}
			}
		}
		return count;
	}

	public int getNumberOfNodes(){
		return getNodeNumber(this.root);
	}

	public boolean isEmpty(){
		return this.root.getChildren().isEmpty();
	}

	/**
	 * get the hyper-clique patterns of this tree
	 * @param root
	 * @return
	 */
	public HashMap<Pattern,Integer> getHyperCliquePatterns(double hconfidence,int supportLower){
		//create a conditional tree out of this FP tree which conditioned on nothing at first
		double prob=(double)(supportLower+1)/(double)this.count;
		this.entropyLower=-prob*Math.log(prob)-(1-prob)*Math.log(1-prob);	
		ConditionalTrie ctree=new ConditionalTrie(hconfidence,supportLower,this.count,this.entropyLower,this,this.observedFeatureOccurrenceMap);
		return ctree.getHyperCliquePatterns();
	}

	/**
	 * strip out and make a copy of a path that ends with input node from the trie
	 * the output is a TriePath excluding the input node
	 * @param node
	 * @return
	 */
	public TriePath stripPathEndOnNodeExcluding(TrieNode node){		
		TriePath result=new TriePath();
		//trace the full path that involves this node
		TrieNode parent=node.getParent();
		int nodeCount=node.getCount();

		while(parent!=null&&parent.getWord()!=null){
			//copy this node
			TrieNode parentnode=new TrieNode(parent);
			parentnode.addCount(nodeCount);
			result.addToFirst(parentnode);
			parent=parent.getParent();			
		}		
		return result;
	}

	/**
	 * reset this tree by clear the root's children
	 */
	public void resetTree(){
		this.root.getChildren().clear();
		this.count=0;
		this.totalcount=0;
		this.distinctcount=0;
	}

	/**
	 * register a feature vector in this tree
	 */
	public void registerFeatureVector(FeatureVector vector){
		
		Pattern pattern=this.featurevector2pattern(vector);
		if (pattern.size()>0){										
			this.count++;
			this.distinctcount++;
			this.totalcount+=pattern.size();
			//add the contribution to all observed feature with occcurrences
			int contrib=pattern.size()-1;
			for (ObservedFeatureOccurrence observedFeatureOccurrence: pattern.getObservedFeatureOccurrenceSet()){
				observedFeatureOccurrence.addContribution(contrib);
			}
		}
		else{
			System.out.println("empty feature vector consumed");
		}		
	}
	
	/**
	 * register a feature vector in this tree
	 */
	public void registerFeatureVector(FeatureVector vector,int multiplicity){	
		Pattern pattern=this.featurevector2pattern(vector);
		if (pattern.size()>0){										
			this.count+=multiplicity;
			this.distinctcount++;
			this.totalcount+=pattern.size()*multiplicity;
			//add the contribution to all observed feature with occurrences
			int contrib=pattern.size()-1;
			for (ObservedFeatureOccurrence observedFeatureOccurrence: pattern.getObservedFeatureOccurrenceSet()){
				observedFeatureOccurrence.addContribution(contrib*multiplicity);
			}
		}
		else{
			System.out.println("empty feature vector consumed");
		}		
	}
	
	public void consumeFeatureVector(FeatureVector vector,int multiplicity){
		if (!vector.isEmpty()){
		Pattern p=this.featurevector2pattern(vector);
	    ArrayList<ObservedFeatureOccurrence> qlist=new ArrayList<ObservedFeatureOccurrence>(p.getObservedFeatureOccurrenceSet());
			//sort into sequence to form a path
			Collections.sort(qlist);
			TriePath triepath=new TriePath();
			for (ObservedFeatureOccurrence ofo:qlist){	
				TrieNode n=new TrieNode(ofo);
				n.addCount(multiplicity-1);
				triepath.addToTail(n);
			}
			this.consume(triepath);		
		}
	}
	
	public void consumeFeatureVector(FeatureVector vector){
		if (!vector.isEmpty()){
		Pattern p=this.featurevector2pattern(vector);
	    ArrayList<ObservedFeatureOccurrence> qlist=new ArrayList<ObservedFeatureOccurrence>(p.getObservedFeatureOccurrenceSet());
			//sort into sequence to form a path
			Collections.sort(qlist);
			TriePath triepath=new TriePath();
			for (ObservedFeatureOccurrence ofo:qlist){	
				TrieNode n=new TrieNode(ofo);
				triepath.addToTail(n);
			}
			this.consume(triepath);		
		}
	}
	
	/**
	 * returns the number of feature sets that contains the feature vector
	 * @param vector
	 * @return
	 */
	public int getCountOfContainingFeatureVector(FeatureVector vector){
		Pattern featureset=this.featurevector2pattern(vector);
		ArrayList<ObservedFeatureOccurrence> featurelist=new ArrayList<ObservedFeatureOccurrence>(featureset.getObservedFeatureOccurrenceSet());		
		int sum=0;
		if(!featurelist.isEmpty()){
			//sort by its define feature order
			Collections.sort(featurelist);
			HashSet<TrieNode> candidatepaths=this.trackMap.get(featurelist.get(featurelist.size()-1));

			if(featurelist.size()>1){
				for (TrieNode node: candidatepaths){
					//start from next node to match
					TrieNode startNode=node.getParent();
					//start from last unmatched feature in the target feature list to match
					int lastUnmatchedInd=featurelist.size()-2;
					for(int i=startNode.getDepth();i>0;i--){
						ObservedFeatureOccurrence targetFeature=featurelist.get(lastUnmatchedInd);	
						if(targetFeature.equals(startNode.getWord())){
							lastUnmatchedInd--;
							if(lastUnmatchedInd==-1){
								break;
							}
						}
						startNode=startNode.getParent();
					}
					//if all matched
					if(lastUnmatchedInd==-1){
						sum+=node.getCount();
					}					
				}
			}
			else {
				for (TrieNode node: candidatepaths){
					sum+=node.getCount();
				}
			}
		}
		else
			System.out.println("warning, input feature vector is empty");

		return sum;	
	}

	/**
	 * returns the number of feature sets that exactly matches the feature vector
	 * @param vector
	 * @return
	 */
	public int getCountOfExactlyMatchingFeatureVector(FeatureVector vector){
		Pattern featureset=this.featurevector2pattern(vector);
		ArrayList<ObservedFeatureOccurrence> featurelist=new ArrayList<ObservedFeatureOccurrence>(featureset.getObservedFeatureOccurrenceSet());
		if(!featurelist.isEmpty()){
			//sort by its defined feature order
			Collections.sort(featurelist);
			TrieNode currentMatch=this.root;
			for (int i=0;i<featurelist.size();i++){
				HashMap<Integer, HashMap<Integer, TrieNode>> map=currentMatch.getChildren();
				ObservedFeatureOccurrence currentWord=featurelist.get(i);
				int featureID=currentWord.getFeatureID();
				int occurrence=currentWord.getOccurrence();
				TrieNode nextMatch=map.get(featureID).get(occurrence);
				if (nextMatch==null)
					return 0;
				else
					currentMatch=nextMatch;
			}
			//minus the count of its children and get the count
			int childsum=0;
			HashMap<Integer, HashMap<Integer, TrieNode>> map=currentMatch.getChildren();
			for (Entry<Integer, HashMap<Integer, TrieNode>> en: map.entrySet()){
				HashMap<Integer, TrieNode> childmap=en.getValue();
				for (Entry<Integer, TrieNode> enn: childmap.entrySet()){
					childsum+=enn.getValue().getCount();
				}
			}			
			return currentMatch.getCount()-childsum;
		}
		else{
			System.out.println("warning, input feature vector is empty");
			return 0;
		}
	}

	/**
	 * validate the functionality of computing marginal probability of patterns
	 * @return
	 */
	private boolean validateProbabilityComputation(){
		int sumcount=0;
		HashMap<TrieNode,Integer> instanceMap=this.instanceMap;

		for(Entry<TrieNode,Integer> en:instanceMap.entrySet()){  
			int purecount=en.getValue();	
			TrieNode n=en.getKey();
			FeatureVector vector=new FeatureVector();
			TrieNode startNode=n;
			for(int i=n.getDepth();i>0;i--){
				ObservedFeatureOccurrence sfeature=startNode.getWord();
				int ID=sfeature.getFeatureID();
				int occur=sfeature.getOccurrence();
				FeatureVector piece=new FeatureVector();
				piece.addFeatureWithOccurrence(ID, occur);
				vector=FeatureVector.setUnion(vector, piece);
				startNode=startNode.getParent();
			}
			int c=1;
			startNode=n;
			while(startNode.getParent()!=null&&startNode.getParent().getWord()!=null){
				startNode=startNode.getParent();
				c++;
			}
			if(c!=n.getDepth()){
				System.out.println("path length does not match n depth.");
				return false;
			}
			int mcount=this.getCountOfContainingFeatureVector(vector);
			if(mcount<n.getCount()||mcount>this.count){
				System.out.println("marginal probability computed invalid.");
				return false;
			}
			int count=this.getCountOfExactlyMatchingFeatureVector(vector);
			if( count!=purecount){
				System.out.println("joint probability computed invalid. "+" Original Value "+purecount+" computed "+count);
				return false;
			}					
			sumcount+=purecount;									

		}
		if(sumcount!=this.count)
			System.out.println("sum of feature sets does not match, summed by leaves "+sumcount+" stored "+this.count);

		return sumcount==this.count;
	}

	/**
	 * get the total number of feature sets consumed
	 * @return non-negative integer
	 */
	public int getTotalFeatureSetCount(){
		return this.count;
	}

	/**
	 * get the total number of distinct feature sets consumed 
	 * @return non-negative integer
	 */
	public int getTotalDistinctFeatureSetCount(){
		return this.distinctcount;
	}

	/**
	 * get the total number of features consumed
	 * @return non-negative integer
	 */
	public int getTotalFeaturesCount(){
		return this.totalcount;
	}

	/**
	 * Compute the Summary Error of the Naive Summary
	 * @return
	 */
	public double getNaiveSummaryError(){
		if(this.naiveSummaryError<0){                 
			this.naiveSummaryError=this.getNaiveEntropy()-this.getTrueEntropy();	
		}
		return this.naiveSummaryError;
	}

	/**
	 * Measure the distance between two naive summaries of FP Trees
	 * @param left
	 * @param right
	 * @return
	 */
	public static double getNaiveSummaryDistance(FeatureVector_Trie left,FeatureVector_Trie right){
		//TODO
		double divergence=0;
		HashMap<Integer,TreeMap<Integer,Integer>> leftNaiveSummary=left.getFeatureDistribution();
		HashMap<Integer,TreeMap<Integer,Integer>> rightNaiveSummary=right.getFeatureDistribution();		
		//search for overlap	
		for (Entry <Integer,TreeMap<Integer,Integer>> en: leftNaiveSummary.entrySet()){
			int ID=en.getKey();
			TreeMap<Integer,Integer> leftdistribution=en.getValue();
			TreeMap<Integer,Integer> rightdistribution=rightNaiveSummary.get(ID);
			//if right does not contain the feature, assume p(feature=0)=1
			if(rightdistribution==null){
				rightdistribution=new TreeMap<Integer,Integer>();
				rightdistribution.put(0,1);
			}			
			//calculate JensenShannonDivergence as similarity	
			divergence+=JensenShannonDivergence(leftdistribution,rightdistribution);			
		}

		for (Entry <Integer,TreeMap<Integer,Integer>> en: rightNaiveSummary.entrySet()){
			int ID=en.getKey();
			TreeMap<Integer,Integer> rightdistribution=en.getValue();
			TreeMap<Integer,Integer> leftdistribution=leftNaiveSummary.get(ID);
			//need only deal with features belonging only to right
			if(leftdistribution==null){
				leftdistribution=new TreeMap<Integer,Integer>();
				leftdistribution.put(0,1);
				divergence+=JensenShannonDivergence(leftdistribution,rightdistribution);
			}									
		}		
		return divergence; 	
	}

	private static double JensenShannonDivergence(TreeMap<Integer,Integer> leftDistribution,TreeMap<Integer,Integer> rightDistribution){
		//TODO
		double divergence=0;
		double leftsum=0;
		for (Entry<Integer,Integer> en: leftDistribution.entrySet()){
			leftsum+=en.getValue();
		}
		double rightsum=0;
		for (Entry<Integer,Integer> en: rightDistribution.entrySet()){
			rightsum+=en.getValue();
		}

		for (Entry<Integer,Integer> en: leftDistribution.entrySet()){
			int occurrence=en.getKey();
			Integer leftfreq=en.getValue();
			double leftpi=(double)leftfreq/leftsum;
			Integer rightfreq=rightDistribution.get(occurrence);
			if(rightfreq==null){
				divergence+=leftpi*Math.log(2);
			}
			else{
				double midpi=leftpi/2+((double)rightfreq/rightsum)/2;
				divergence+=leftpi*Math.log(leftpi/midpi);
			}
		}

		for (Entry<Integer,Integer> en: rightDistribution.entrySet()){
			int occurrence=en.getKey();
			Integer rightfreq=en.getValue();
			double rightpi=(double)rightfreq/rightsum;
			Integer leftfreq=leftDistribution.get(occurrence);
			if(leftfreq==null){
				divergence+=rightpi*Math.log(2);
			}
			else{
				double midpi=rightpi/2+((double)leftfreq/leftsum)/2;
				divergence+=rightpi*Math.log(rightpi/midpi);
			}
		}		
		return divergence/2; 	
	}

	/**
	 * get all marginals p(X_i>=k)
	 * @return
	 */
	public HashMap<ObservedFeatureOccurrence,Integer> getFrequencyCount(){
		HashMap<ObservedFeatureOccurrence,Integer> frequencyCount=new HashMap<ObservedFeatureOccurrence,Integer>();  		
		for(Entry<ObservedFeatureOccurrence, HashSet<TrieNode>> en:this.trackMap.entrySet()){
			int sum=0;
			for(TrieNode n:en.getValue())
				sum+=n.getCount();   			
			frequencyCount.put(en.getKey(), sum);
		}
		return frequencyCount;
	} 

	/**
	 * calculate the entropy of naive summary
	 * @return
	 */
	public double getNaiveEntropy(){
		double assumedIC=this.NaiveEntropy;
		if(assumedIC<0){
			assumedIC=0;
			HashMap<Integer,TreeMap<Integer,Integer>> featureDistribution=this.getFeatureDistribution();
			for (Entry<Integer, TreeMap<Integer, Integer>> en: featureDistribution.entrySet()){
				int sum=0;
				TreeMap<Integer, Integer> treeMap=en.getValue();
				double p;
				for(Entry<Integer, Integer> enn: treeMap.entrySet()){
					Integer frequency=enn.getValue();
					p=(double)frequency/(double)this.count;
					assumedIC+=-p*Math.log(p); 
					//sanity check
					sum+=frequency;
				}
				if(sum!=this.count)
					System.out.println("naive summary mariginals does not sum to one, please check.");
			}
			this.NaiveEntropy=assumedIC;
		}
		return assumedIC;
	}

	/**
	 * get the entropy of the true distribution
	 * @return
	 */
	public double getTrueEntropy(){
		double IC=this.TrueEntropy;   
		if(IC<0){
			IC=0;
			HashMap<TrieNode,Integer> instanceMap=this.instanceMap;
			int sum=0;
			for(Entry<TrieNode,Integer> en:instanceMap.entrySet()){  		
				int purecount=en.getValue();
				double pi=(double)purecount;
				pi=pi/(double)this.count;
				IC+=-pi*Math.log(pi); 	
				sum+=purecount;
			}
			if(sum!=this.count)
				System.out.println("error, joint probabilities does not sum to one when calculating entropy of true distribution.");
			this.TrueEntropy=IC;
		}
		return IC;
	}

	/**
	 * The total count of sortable Feature (feature, occurrence) only stores marginal p(Feature_i>=occurrence)
	 * We need to turn them into marginal p(Feature_i=occurrence)
	 * if occurrence=0, we do not store them
	 * @return
	 */
	public LinkedHashMap<Integer,TreeMap<Integer,Integer>> getFeatureDistribution(){

		if(this.featureOccurrenceGEQMarginal==null){
			this.featureOccurrenceGEQMarginal=new LinkedHashMap<Integer,TreeMap<Integer,Integer>>();

			TreeMap<ObservedFeatureOccurrence,Integer> frequencyCount=new TreeMap<ObservedFeatureOccurrence,Integer>(this.getFrequencyCount());
            
			
			for(Entry<ObservedFeatureOccurrence, Integer> en: frequencyCount.entrySet()){
				int ID=en.getKey().getFeatureID();
				int occurrence=en.getKey().getOccurrence();
				int frequency=en.getValue();

				TreeMap<Integer,Integer> treeMap=this.featureOccurrenceGEQMarginal.get(ID);
				if(treeMap==null){
					treeMap=new TreeMap<Integer,Integer>();
					this.featureOccurrenceGEQMarginal.put(ID, treeMap);
				}
				treeMap.put(occurrence, frequency);
			}
		}

		if(this.featureOccurrenceEQMarginal==null){
			this.featureOccurrenceEQMarginal=new LinkedHashMap<Integer,TreeMap<Integer,Integer>>(); 

			//calculate featureEQDistribution
			for (Entry<Integer, TreeMap<Integer, Integer>> en: this.featureOccurrenceGEQMarginal.entrySet()){
				TreeMap<Integer, Integer> GEQtreeMap=en.getValue();
				TreeMap<Integer, Integer> EQtreeMap=this.featureOccurrenceEQMarginal.get(en.getKey());
				if (EQtreeMap==null){
					EQtreeMap=new TreeMap<Integer, Integer>();
					this.featureOccurrenceEQMarginal.put(en.getKey(), EQtreeMap);
				}

				for(Entry<Integer, Integer> enn: GEQtreeMap.entrySet()){
					int occurrence=enn.getKey();
					int frequency=enn.getValue();
					Entry<Integer, Integer>  higherFrequencyEntry=GEQtreeMap.higherEntry(occurrence);
					if(higherFrequencyEntry!=null){
						frequency=frequency-higherFrequencyEntry.getValue();        			
					}
					//store it 
					if(frequency>0)
						EQtreeMap.put(occurrence,frequency);
					else if(frequency<0)
						System.out.println("please check marginal subtraction, it is less than 0.");

				}
				//calculate the 0-occurrence	
				int zeroFrequency=this.count-GEQtreeMap.ceilingEntry(1).getValue();
				if(zeroFrequency>0)
					EQtreeMap.put(0,zeroFrequency);
				else if(zeroFrequency<0)
					System.out.println("please check zero frequency count, less than 0.");

				int sum=0;
				for(Integer frequency: EQtreeMap.values())
					sum+=frequency;
				if(sum!=this.count){
					System.out.println("Feature distribution error, does not sum to one, total count is: "+this.count);
					System.out.println("marginals: "+GEQtreeMap);
					System.out.println("marginals: "+EQtreeMap);
				}
			}
		}	
		return featureOccurrenceEQMarginal;
	} 
	
	/**
	 * translate FeatureVector into Pattern which can be understand by FP Tree
	 * @param vector
	 * @return
	 */
	private Pattern featurevector2pattern(FeatureVector vector){
		Pattern pattern=new Pattern();
		Set<Integer> distinctFeatures=vector.getDistinctFeatures();	
		for (Integer feature: distinctFeatures){
			//critical! if an feature happens occur times, then it implies from 1 to occur it all happens
			pattern.addToSet(feature,vector.getFeatureOccurrence(feature),this.observedFeatureOccurrenceMap);
		}
		return pattern;
	}
	
	/**
	 * get the number of non-zero marginals in naive summary
	 * @return
	 */
	public int getVerbosity(){
		int sum=0;	
		HashMap<Integer,TreeMap<Integer,Integer>> featureOccurrenceEQMarginal=this.getFeatureDistribution();
		for(Entry<Integer, TreeMap<Integer, Integer>> en: featureOccurrenceEQMarginal.entrySet()){
			for (Entry<Integer, Integer> enn:en.getValue().entrySet()){
				if(enn.getKey()!=0){
					sum++;
				}
			}
		}
		return sum;
	}


}

