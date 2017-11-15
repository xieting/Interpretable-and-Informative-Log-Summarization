package pattern_mixture_summarization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import data_structure.GlobalVariables;
import feature_management.FeatureVector;

public class ClusteringResult {
	HashMap<Integer,Cluster> clusterMap=new HashMap<Integer,Cluster>();//data may be essentially stored in its children
	int totalVerbosity=0;
	double averageVerbosity=0;
	double Error=0;

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

}
