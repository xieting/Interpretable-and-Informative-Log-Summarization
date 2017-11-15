package pattern_mixture_summarization;

import java.util.LinkedHashMap;

import data_structure.FeatureVector_Trie;
import data_structure.ObservedFeatureOccurrence;
import feature_management.FeatureVector;

public class Cluster {
	int clusterID;
	FeatureVector_Trie mytree;//may or may not have its own data storage

	public Cluster(int ID){
		this.clusterID=ID;	 
		//create a single cluster
		mytree=new FeatureVector_Trie(); 
	}
	
	public LinkedHashMap<ObservedFeatureOccurrence,Double> getNaiveSummary(){
		return mytree.getNaiveSummary();
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
	
	
}
