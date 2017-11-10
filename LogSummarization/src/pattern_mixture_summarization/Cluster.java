package pattern_mixture_summarization;

import java.util.LinkedHashMap;

import data_structure.FP_InferenceTree;
import data_structure.Word;
import feature_management.FeatureVector;

public class Cluster {
	int clusterID;
	FP_InferenceTree mytree;//may or may not have its own data storage

	public Cluster(int ID){
		this.clusterID=ID;	 
		//create a single cluster
		mytree=new FP_InferenceTree("itemsets"+"_"+this.clusterID,"summarization"+"_"+this.clusterID);
		mytree.prepareToReceiveFeatureList();  
	}

	public void buildCluster(){
		mytree.finishReceivingFeatureList();
		mytree.buildTree(); 
		//mytree.validateTree();
	}
	
	public LinkedHashMap<Word,Double> getNaiveSummary(){
		return mytree.getNaiveSummary();
	}
	
	public void consumeFeatureVector(FeatureVector vector){
		mytree.consumeFeatureList(vector);
	}
	
	public void consumeFeatureVector(FeatureVector vector, int multiplicity){
		mytree.consumeFeatureList(vector,multiplicity);
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
	
}
