package pattern_mixture_summarization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import data_structure.FP_InferenceTree;
import feature_management.FeatureVector;

public class ClusteringResult implements Comparable<ClusteringResult>{
	TreeMap<Integer,Cluster> clusterIDMap;
	int numOfCluster;
	double runningTime;
	double errorExpectation=-1;
	double divExpectation=-1;
	ArrayList<Double> errorList;
	ArrayList<Integer> verbosityList;
	ArrayList<Integer> clusterMemberNumberList;
	ArrayList<Double> divergenceList;

	public ClusteringResult(int K,double runningTime,ArrayList<Integer> labels,ArrayList<Integer> multiplicity ,String featureVectorPath){
		clusterIDMap =new TreeMap<Integer,Cluster>();
		this.numOfCluster=K;
		this.runningTime=runningTime;
		BufferedReader featurebr;
		try {
			featurebr = new BufferedReader(new FileReader(new File(featureVectorPath)));
			String line;
			int index=0;
			while((line=featurebr.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				int clusterID=labels.get(index);
				Cluster c=this.clusterIDMap.get(clusterID);
				if(c==null){
					c=new Cluster(clusterID);
					this.clusterIDMap.put(clusterID, c);
				}

				if(multiplicity!=null)
					c.consumeFeatureVector(vector, multiplicity.get(index));
				else
					c.consumeFeatureVector(vector);	

				index++;
			}
			//build clusters
			//int totalCount=0;
			for (Cluster c:clusterIDMap.values()){
				c.buildCluster();
			//s	totalCount+=c.mytree.getTotalItemSetCount();
			}
			//System.out.println("total consumed feature vectors are :"+totalCount);
			featurebr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public double getErrorExpectation(){

		if(this.errorExpectation<0){
			ArrayList<Double> errorList=this.getErrorList();
			ArrayList<Double> counts=new ArrayList<Double>();
			for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
				counts.add((double)en.getValue().getNumOfVectors());
			}
			double sum=0;
			for(double count: counts)
				sum+=count;

			double expectKl=0;
			for(int i=0;i<errorList.size();i++)			
				expectKl+=errorList.get(i)*counts.get(i)/sum;

			this.errorExpectation=expectKl;
		}

		return this.errorExpectation;		
	}
	
	public double getVerbosityExpectation(){		
   	 ArrayList<Double> counts=new ArrayList<Double>();
   	 ArrayList<Double> verbosities=new ArrayList<Double>();
   	 
		for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
			Cluster c=en.getValue();
			counts.add((double)c.getNumOfVectors());
			verbosities.add((double)c.getVerbosity());
		}
		
		double sum=0;
		for(double count: counts)
			sum+=count;
		
		double expectation=0;
		for (int i=0;i<verbosities.size();i++){
			expectation+=verbosities.get(i)*counts.get(i)/sum;
		}		
		return expectation;
	}
	
	public int getTotalVerbosity(){
		int sum=0;
		for(Cluster c:this.clusterIDMap.values())
			sum+=c.getVerbosity();
		return sum;
	}
	
	public double getDivergenceExpectation(){
         if(this.divExpectation<0){
        	 ArrayList<Double> divList=this.getDivergenceList();
        	 ArrayList<Double> counts=new ArrayList<Double>();
 			for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
 				counts.add((double)en.getValue().getNumOfVectors());
 			}
 			double sum=0;
 			for(double count: counts)
 				sum+=count;
 			
			double expectDiv=0;
			for(int i=0;i<divList.size();i++)			
				expectDiv+=divList.get(i)*counts.get(i)/sum;
			
			this.divExpectation=expectDiv;
         }
         
         return this.divExpectation;
	}

	public ArrayList<Double> getErrorList(){
		if(this.errorList==null){
			this.errorList=new ArrayList<Double>();
			for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
				errorList.add(en.getValue().getError());
			}
		}		
		return this.errorList;
	}
	
	public ArrayList<Integer> getVerbosityList(){
		if(this.verbosityList==null){
			this.verbosityList=new ArrayList<Integer>();
			for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
				this.verbosityList.add(en.getValue().getVerbosity());
			}
		}		
		return this.verbosityList;
	}
	
	public ArrayList<Integer> getClusterMemberNumberList(){
		if(this.clusterMemberNumberList==null){
			this.clusterMemberNumberList=new ArrayList<Integer>();
			for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
				this.clusterMemberNumberList.add(en.getValue().getNumOfVectors());
			}
		}		
		return this.clusterMemberNumberList;
	}
	

	public ArrayList<Double> getDivergenceList(){

		if(this.clusterIDMap.size()>1){
			if(this.divergenceList==null){
				this.divergenceList=new ArrayList<Double>();
				ArrayList<FP_InferenceTree> mylist=new ArrayList<FP_InferenceTree>();
				for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
					mylist.add(en.getValue().mytree);
				}
				
	        	 ArrayList<Double> counts=new ArrayList<Double>();
	  			for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
	  				counts.add((double)en.getValue().getNumOfVectors());
	  			}
	  			double sum=0;
	  			for(double count: counts)
	  				sum+=count;
	  			
				HashMap<FP_InferenceTree,ArrayList<Double>> divergences=new HashMap<FP_InferenceTree,ArrayList<Double>>();
  	
				for(int i=0;i<mylist.size()-1;i++){
					for(int j=i+1;j<mylist.size();j++){
						double distance=FP_InferenceTree.getNaiveSummaryDistance(mylist.get(i), mylist.get(j));				
						ArrayList<Double> divergence=divergences.get(mylist.get(i));
						if(divergence==null){
							divergence=new ArrayList<Double>();
							divergences.put(mylist.get(i), divergence);
						}
						divergence.add(distance*counts.get(j)/sum);

						divergence=divergences.get(mylist.get(j));
						if(divergence==null){
							divergence=new ArrayList<Double>();
							divergences.put(mylist.get(j), divergence);
						}
						divergence.add(distance*counts.get(i)/sum);
					}
				}
				
				for (Entry<Integer,Cluster> en:this.clusterIDMap.entrySet()){
					double averageDivergence=0;
					ArrayList<Double> dlist=divergences.get(en.getValue().mytree);
					for(double value: dlist)
						averageDivergence+=value;				
					this.divergenceList.add(averageDivergence);
				}
			}
			return this.divergenceList;
		}
		else { 
			System.out.println("to get cross cluster divergence, number of clusters must be greater than one.");
			return null;
		}
	}

//	@Override
//	public int compareTo(ClusteringResult o) {
//		double myExpectError=this.getErrorExpectation();
//		double yourExpectError=o.getErrorExpectation();
//		if(myExpectError< yourExpectError){
//			return -1;
//		}
//		else if (myExpectError> yourExpectError){
//			return 1;
//		}
//		else
//			return 0;
//	}
	
	@Override
	public int compareTo(ClusteringResult o) {
		double myExpectVerbosity=this.getVerbosityExpectation();
		double yourExpectVerbosity=o.getVerbosityExpectation();
		if(myExpectVerbosity< yourExpectVerbosity){
			return -1;
		}
		else if (myExpectVerbosity> yourExpectVerbosity){
			return 1;
		}
		else
		return 0;
	}

}
