package pattern_mixture_summarization;

public class test {

	public static void main(String[] args) {
		String rootDirectory="/Users/tingxie/Documents/workspace/MyLocalInsiderThreat/data/";
        String featureVectorsPath=rootDirectory+"FeatureVectors.txt";
        String multiplicityPath=rootDirectory+"FeatureVectorCounts.txt";
        String clusterAssignmentsPath=rootDirectory+"KmeansEuclidean_labels.txt";     
        ClusteringResult result=new ClusteringResult( featureVectorsPath,multiplicityPath,clusterAssignmentsPath);
        
	}

}
