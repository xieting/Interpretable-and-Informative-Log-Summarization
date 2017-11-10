package pattern_mixture_summarization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import feature_management.FeatureVector;

public class ClusteringResult {
	HashMap<Integer,Cluster> clusterMap=new HashMap<Integer,Cluster>();//data may be essentially stored in its children
    int totalVerbosity=0;
    double averageVerbosity=0;
    double Error=0;
	
	public ClusteringResult (String featureVectorsPath, String multiplicityPath, String clusterAssignmentPath){
		BufferedReader featurebr;
		Scanner multiplicitybr,clusterAssignmentbr;
		try {
			featurebr=new BufferedReader(new FileReader(featureVectorsPath));
			multiplicitybr=new Scanner(new File(multiplicityPath)).useDelimiter("[\\r\\n,\\s]+");
			clusterAssignmentbr=new Scanner(new File(clusterAssignmentPath)).useDelimiter("[\\r\\n,\\s]+");
			String line;
			while ((line=featurebr.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				Integer multiplicity=Integer.parseInt(multiplicitybr.next());
				Integer clusterID=Integer.parseInt(clusterAssignmentbr.next());
				Cluster targetCluster=this.clusterMap.get(clusterID);
				if(targetCluster==null){
					targetCluster=new Cluster(clusterID);
					this.clusterMap.put(clusterID, targetCluster);
				}
				targetCluster.consumeFeatureVector(vector, multiplicity);
			}
			featurebr.close();
			multiplicitybr.close();
			clusterAssignmentbr.close();
			//build clusters
			ArrayList<Integer> verbosityList=new ArrayList<Integer>();
			ArrayList<Double> errorList=new ArrayList<Double>();
			ArrayList<Integer> numOfVectorsList=new ArrayList<Integer>();
			int numOfVectorsSum=0;
			for (Cluster c: this.clusterMap.values()){
				c.buildCluster();
				int verbosity=c.getVerbosity();
				verbosityList.add(verbosity);
				//total verbosity
				this.totalVerbosity+=verbosity;
				
				errorList.add(c.getError());
				
				int numVectors=c.getTotalNumOfVectors();
				numOfVectorsList.add(numVectors);
				numOfVectorsSum+=numVectors;
			}
			ArrayList<Double> weights=new ArrayList<Double>();
			for (int i=0;i<numOfVectorsList.size();i++){
				weights.add((double)numOfVectorsList.get(i)/(double)numOfVectorsSum);
			}
			
			for (int i=0;i<verbosityList.size();i++){
				//average verbosity
				this.averageVerbosity+=verbosityList.get(i)*weights.get(i);
				//error
				this.Error+=errorList.get(i)*weights.get(i);
			}
	        			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ClusteringResult (String featureVectorsPath,String clusterAssignmentPath){
		BufferedReader featurebr,clusterAssignmentbr;
		try {
			featurebr=new BufferedReader(new FileReader(featureVectorsPath));
			clusterAssignmentbr=new BufferedReader(new FileReader(clusterAssignmentPath));
			String line;
			while ((line=featurebr.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				Integer clusterID=Integer.parseInt(clusterAssignmentbr.readLine());
				Cluster targetCluster=this.clusterMap.get(clusterID);
				if(targetCluster==null){
					targetCluster=new Cluster(clusterID);
					this.clusterMap.put(clusterID, targetCluster);
				}
				targetCluster.consumeFeatureVector(vector);
			}
			featurebr.close();
			clusterAssignmentbr.close();
			//build clusters
			ArrayList<Integer> verbosityList=new ArrayList<Integer>();
			ArrayList<Double> errorList=new ArrayList<Double>();
			ArrayList<Integer> numOfVectorsList=new ArrayList<Integer>();
			int numOfVectorsSum=0;
			for (Cluster c: this.clusterMap.values()){
				c.buildCluster();
				int verbosity=c.getVerbosity();
				verbosityList.add(verbosity);
				//total verbosity
				this.totalVerbosity+=verbosity;
				
				errorList.add(c.getError());
				
				int numVectors=c.getTotalNumOfVectors();
				numOfVectorsList.add(numVectors);
				numOfVectorsSum+=numVectors;
			}
			ArrayList<Double> weights=new ArrayList<Double>();
			for (int i=0;i<numOfVectorsList.size();i++){
				weights.add((double)numOfVectorsList.get(i)/(double)numOfVectorsSum);
			}
			
			for (int i=0;i<verbosityList.size();i++){
				//average verbosity
				this.averageVerbosity+=verbosityList.get(i)*weights.get(i);
				//error
				this.Error+=errorList.get(i)*weights.get(i);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
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
	
}
