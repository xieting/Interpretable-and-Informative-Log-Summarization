package pattern_mixture_summarization;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import data_structure.ObservedFeatureOccurrence;

public class test {

	public static void main(String[] args) {
		String rootDirectory="/Users/tingxie/Documents/workspace/MyLocalInsiderThreat/data/";
        String featureVectorsPath=rootDirectory+"FeatureVectors.txt";
        String multiplicityPath=rootDirectory+"FeatureVectorCounts.txt";
        String clusterAssignmentsPath=rootDirectory+"hamming_labels.txt";     
        ClusteringResult result=new ClusteringResult( featureVectorsPath,multiplicityPath,clusterAssignmentsPath);
        for(Cluster c: result.getClusters().values()){
        	System.out.println("-----Cluster "+c.clusterID+" -----");
            LinkedHashMap<ObservedFeatureOccurrence,Double> features=c.getNaiveSummary();
            for (Entry<ObservedFeatureOccurrence,Double> en: features.entrySet()){
            	int featureID=en.getKey().getFeatureID();
            	double marginal=en.getValue();
            	System.out.println(featureID+" : "+marginal);
            }
            System.out.println();
        }
	}

}
