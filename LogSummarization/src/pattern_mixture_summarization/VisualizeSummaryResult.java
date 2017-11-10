package pattern_mixture_summarization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import data_structure.FP_InferenceTree;
import feature_management.FeatureVector;
import feature_management.featureManager;
import summary_visualization.SummaryVisualizer;

public class VisualizeSummaryResult {
	public static void main(String[] args) {
		String datapath="data/";
		String chosenMetricName="KmeansEuclidean";
		String featureVectorCount = "FeatureVectorCounts";
		String featureVectorPath="FeatureVectors.txt";

		//load multiplicity for feature vectors
		ArrayList<Integer> multiplicity = null;
		File f = new File(datapath+featureVectorCount);
		if(f.exists() && !f.isDirectory()) { 
			try {            
				FileInputStream fileIn = new FileInputStream(datapath+featureVectorCount);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				multiplicity =  (ArrayList<Integer>) in.readObject();
				in.close();
				fileIn.close();	          
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		String line;
		//load clustering labels
		ArrayList<Integer> labelList=new ArrayList<Integer>();
		BufferedReader labelbr;
		try {
			labelbr = new BufferedReader(new FileReader(new File(datapath+chosenMetricName+"_labels.txt")));
			line=labelbr.readLine();
			String [] tokens=line.split("\\s+");
			for(int j=0;j<tokens.length;j++)
				labelList.add(Integer.parseInt(tokens[j]));			
			labelbr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		//load feature vectors
		ArrayList<FeatureVector> vectors=new ArrayList<FeatureVector>();			
		try {
			BufferedReader	featurebr = new BufferedReader(new FileReader(new File(datapath+featureVectorPath)));
			while((line=featurebr.readLine())!=null){
				FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
				vectors.add(vector);
			}
			featurebr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//load feature Map
		featureManager.loadlabelMap();
		//read label and get clusters
		HashMap<Integer,FP_InferenceTree> clusters=new HashMap<Integer,FP_InferenceTree>();
		for (int i=0;i<labelList.size();i++){
			int clusterID=labelList.get(i);
			int multi=multiplicity.get(i);
			FeatureVector vector=vectors.get(i);
			
			FP_InferenceTree tree=clusters.get(clusterID);
			if(tree==null){
				tree=new FP_InferenceTree("itemsets"+"_"+clusterID,"summarization"+"_"+clusterID);
				tree.prepareToReceiveFeatureList();
				clusters.put(clusterID, tree);
			}
			tree.consumeFeatureList(vector, multi);
		}
		float threshold=0.01f;
	    for (Entry<Integer,FP_InferenceTree> en: clusters.entrySet()){
	    	FP_InferenceTree tree=en.getValue();
	    	tree.finishReceivingFeatureList();
	    	tree.buildTree();
	    	//visualize this cluster
	    	SummaryVisualizer vis=new SummaryVisualizer(threshold,en.getKey(),tree);
	    	vis.Visualize();
	    }
		
	}
}
