package pattern_mixture_summarization;

public class test {

	public static void main(String[] args) {
		String rootDirectory="/Users/tingxie/Documents/workspace/MyLocalInsiderThreat/data/";
        String featureVectorsPath=rootDirectory+"FeatureVectors.txt";
        String multiplicityPath=rootDirectory+"FeatureVectorCounts.txt";
        String clusterAssignmentsPath=rootDirectory+"KmeansEuclidean_labels.txt";     
        ClusteringResult result=new ClusteringResult( featureVectorsPath,multiplicityPath,clusterAssignmentsPath);
        System.out.println(result.getAverageVerbosity());
        System.out.println(result.getError());
        System.out.println(result.getTotalVerbosity());
        
        for(Cluster c: result.getClusters().values()){
        	System.out.println("--------");
        	System.out.println(c.getTotalNumOfVectors());
        	System.out.println(c.getVerbosity());
        	System.out.println(c.getError());
        	System.out.println(c.getNaiveSummary());
        }
        
	}

}
