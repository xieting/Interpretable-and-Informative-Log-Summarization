package pattern_mixture_summarization;

import data_structure.Trie;
import feature_management.FeatureVector;

public class Cluster {
	int clusterID;
	Trie mytree;
    NaiveSummary summary;
    
	public Cluster(int ID){
		this.clusterID=ID;	 
		//create a single cluster
		mytree=new Trie(); 
	}
	
	public NaiveSummary getNaiveSummary(){
		if (this.summary==null)
			this.summary=this.mytree.getNaiveSummary();		
		return this.summary;
	}
	
	public void registerFeatureVector(FeatureVector vector){
		mytree.registerFeatureVector(vector);
	}
		
	public void registerFeatureVector(FeatureVector vector,int multiplicity){
		mytree.registerFeatureVector(vector,multiplicity);
	}
	
	public void consumeFeatureVector(FeatureVector vector){
		mytree.consumeFeatureVector(vector);;
	}
	
	public void consumeFeatureVector(FeatureVector vector, int multiplicity){
		mytree.consumeFeatureVector(vector, multiplicity);;
	}

	public Integer getTotalNumOfVectors(){
		return this.mytree.getTotalFeatureSetCount();
	}
	
	public Integer getNumOfDistinctVectors(){
		return this.mytree.getTotalDistinctFeatureSetCount();
	}

	public double getError(){
		return this.mytree.getNaiveSummaryError();
	}

	public int getVerbosity(){
		return this.mytree.getVerbosity();
	}

	/**
	 * merge two clusters into one and update the hierarchy of naive summaries
	 * @param left
	 * @param right
	 * @return
	 */
	public static Cluster mergeClusters(Cluster left, Cluster right){
		NaiveSummary leftSummary=left.getNaiveSummary();
		NaiveSummary rightSummary=right.getNaiveSummary();
		
		//use left ID
		Cluster mergedCluster=new Cluster(left.clusterID);
		//merge the data stored in data structure
		mergedCluster.mytree=Trie.mergeTries(left.mytree, right.mytree);
		//get new naive summary of the merged 
		NaiveSummary currentSummary=mergedCluster.getNaiveSummary();
		//update the structure of the naive summary
		currentSummary.addAsChild(leftSummary);
		currentSummary.addAsChild(rightSummary);
		return mergedCluster;
	}
	
	
}
