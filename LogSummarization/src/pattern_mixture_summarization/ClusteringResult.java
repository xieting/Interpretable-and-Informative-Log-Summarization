package pattern_mixture_summarization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Scanner;

import data_structure.FeatureVector_Trie;
import feature_management.FeatureVector;
import feature_management.GlobalVariables;

public class ClusteringResult {
	HashMap<Integer,Cluster> clusterMap=new HashMap<Integer,Cluster>();//data may be essentially stored in its children
	int totalVerbosity=0;
	double averageVerbosity=0;
	double Error=0;
	ArrayList<NaiveSummary> naiveSummaries;
	
	//criteria for cluster split
    double entropyIncreaseLowerBound=1.3;
    //criteria for skipping too low marginals
    double supportThreshold=0.05;
    
	public ClusteringResult (String featureVectorsPath, String multiplicityPath, String clusterAssignmentPath){
		ArrayList<Integer> multiplicities = null; 
		ArrayList<Integer> clusterIDs=new ArrayList<Integer>();
		if (multiplicityPath!=null){
			multiplicities=new ArrayList<Integer>();
			try {
				Scanner multiplicitybr = new Scanner(new File(multiplicityPath)).useDelimiter(GlobalVariables.inputDataDelimiter);
				while(multiplicitybr.hasNextInt()){
					multiplicities.add(multiplicitybr.nextInt());
				}
				multiplicitybr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		try{
			BufferedReader featurebr=new BufferedReader(new FileReader(featureVectorsPath));		
			Scanner clusterAssignmentbr=new Scanner(new File(clusterAssignmentPath)).useDelimiter(GlobalVariables.inputDataDelimiter);
			String line;
			int index=0;
			while ((line=featurebr.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				if (!vector.isEmpty()){
					Integer clusterID=Integer.parseInt(clusterAssignmentbr.next());
					clusterIDs.add(clusterID);
					Cluster targetCluster=this.clusterMap.get(clusterID);
					if(targetCluster==null){
						targetCluster=new Cluster(clusterID);
						this.clusterMap.put(clusterID, targetCluster);
					}
					if (multiplicities!=null)
						targetCluster.registerFeatureVector(vector, multiplicities.get(index));
					else
						targetCluster.registerFeatureVector(vector);
					index++;
				}
			}
			featurebr.close();
			clusterAssignmentbr.close();

			//build clusters by consuming feature vectors
			featurebr=new BufferedReader(new FileReader(featureVectorsPath));		
			index=0;
			while ((line=featurebr.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				if (!vector.isEmpty()){
					int clusterID=clusterIDs.get(index);
					Cluster targetCluster=this.clusterMap.get(clusterID);
					if (multiplicities!=null)
						targetCluster.consumeFeatureVector(vector, multiplicities.get(index));
					else
						targetCluster.consumeFeatureVector(vector);
					index++;
				}
			}
			featurebr.close();	
		}
		catch(IOException e){
			e.printStackTrace();
		}
		//merge clusters and build a hieararchy
		this.hierarchiallyMergeClusters();
		//get statistics
		this.prepareStatistics();
	}

	/**
	 * merge existing clusters into bigger clusters hierarchically
	 */
	private void hierarchiallyMergeClusters(){
		//create a hierarchy for each cluster first
		ArrayList<Cluster> clusterlist=new ArrayList<>(this.clusterMap.values());
		for (Cluster c: clusterlist){
			this.splitIntoHierarchy(c);
		}
		
		//begin merge the existing hierarchies hierarchically
		PriorityQueue<CandidatePairForMerge> queue=new PriorityQueue<CandidatePairForMerge>();
		for (int i=0;i<clusterlist.size()-1;i++){
			Cluster left=clusterlist.get(i);
			for (int j=i+1;j<clusterlist.size();j++){
				Cluster right=clusterlist.get(j);
				double predictedEntropyIncrease=FeatureVector_Trie.getPredictedNaiveSummaryEntropy(left.mytree, right.mytree,this.supportThreshold);
				if(predictedEntropyIncrease<this.entropyIncreaseLowerBound)
				queue.add(new CandidatePairForMerge(left,right,predictedEntropyIncrease));
			}
		}		
		//while we still have clusters to merge
		while (!queue.isEmpty()){
			CandidatePairForMerge topPair=queue.poll();
			Iterator<Cluster> it=topPair.pair.iterator();
			Cluster left=it.next();
			Cluster right=it.next();
			//merge
			Cluster mergedCluster=Cluster.mergeClusters(left, right);	
			//delete entries in the original queue that involves the two merged clusters
			Iterator<CandidatePairForMerge> qit=queue.iterator();
			while(qit.hasNext()){
				CandidatePairForMerge currentPair=qit.next();
				if(currentPair.pair.contains(left)||currentPair.pair.contains(right))
					qit.remove();
			}
			//delete corresponding entries in the cluster list
			Iterator<Cluster> cit=clusterlist.iterator();
			while(cit.hasNext()){
				Cluster c=cit.next();
				if(c==left||c==right)
					cit.remove();
			}			
			//add the distances between new cluster to all other existing clusters
			for (Cluster c:clusterlist){
				double predictedEntropyIncrease=FeatureVector_Trie.getPredictedNaiveSummaryEntropy(mergedCluster.mytree,c.mytree,this.supportThreshold);
				if(predictedEntropyIncrease<this.entropyIncreaseLowerBound)
				queue.add(new CandidatePairForMerge(mergedCluster,c,predictedEntropyIncrease));
			}
			//add the new cluster to the tail
			clusterlist.add(mergedCluster);			
		}	
		ArrayList<NaiveSummary> naiveSummaries=new ArrayList<NaiveSummary>();
		for (int i=0;i<clusterlist.size();i++)
			naiveSummaries.add(clusterlist.get(i).getNaiveSummary());
		this.naiveSummaries=naiveSummaries;	
	}
	
	/**
	 * create a hierarchy of naive summaries by splitting target cluster
	 * it returns the same object as the input cluster but add it with a hierarchy
	 * of naive summaries
	 * @param c
	 */
	private void splitIntoHierarchy(Cluster c){
		//TODO
	}
	
	private void prepareStatistics(){
		//get total/average verbosity and Error
		ArrayList<Double> weights=new ArrayList<Double>();
		ArrayList<Integer> verbosityList=new ArrayList<Integer>();
		ArrayList<Double> errorList=new ArrayList<Double>();
		ArrayList<Integer> numOfVectorsList=new ArrayList<Integer>();
		int numOfVectorsSum=0;
		for (Cluster c: this.clusterMap.values()){				
			int verbosity=c.getVerbosity();
			verbosityList.add(verbosity);
			//total verbosity
			this.totalVerbosity+=verbosity;
			errorList.add(c.getError());
			int numVectors=c.getTotalNumOfVectors();
			numOfVectorsList.add(numVectors);
			numOfVectorsSum+=numVectors;
		}

		for (int i=0;i<numOfVectorsList.size();i++){
			weights.add((double)numOfVectorsList.get(i)/(double)numOfVectorsSum);
		}

		for (int i=0;i<verbosityList.size();i++){
			//average verbosity
			this.averageVerbosity+=verbosityList.get(i)*weights.get(i);
			//error
			this.Error+=errorList.get(i)*weights.get(i);
		}
	}

	public int getTotalVerbosity(){
		return this.totalVerbosity;
	}

	public double getError(){
		return this.Error;
	}

	public double getAverageVerbosity(){
		return this.averageVerbosity;
	}

	public HashMap<Integer,Cluster> getClusters(){
		return this.clusterMap;
	}
	
	public ArrayList<NaiveSummary> getNaiveSummaryHierarchy(){
		return this.naiveSummaries;
	}
	
	

}
