package pattern_mixture_summarization;

import data_structure.FP_InferenceTree;
import feature_management.FeatureVector;

public class Cluster {
   int clusterID;
   FP_InferenceTree mytree;
   int numChildren=0;
   HashMap<Integer,Cluster> childMap=new HashMap<Integer,Cluster>();
      
   public Cluster(int ID){
	   this.clusterID=ID;
	   mytree=new FP_InferenceTree("itemsets"+"_"+this.clusterID,"summarization"+"_"+this.clusterID);
	   mytree.prepareToReceiveFeatureList();
   }
   
   public void consumeFeatureVector(FeatureVector vec){
		   this.mytree.consumeFeatureList(vec);  
   }
   
   public void consumeFeatureVector(FeatureVector vec, int multiplicity){
		   this.mytree.consumeFeatureList(vec,multiplicity);  
   }
   
   public void buildCluster(){
	   mytree.finishReceivingFeatureList();
	   mytree.buildTree();
   }
   public Integer getNumOfVectors(){
	   return this.mytree.getTotalFeatureSetCount();
   }
   
   public double getError(){
	   return this.mytree.getNaiveSummaryError();
   }
   
   public int getVerbosity(){
	  return this.mytree.getVerbosity();
   }
}
