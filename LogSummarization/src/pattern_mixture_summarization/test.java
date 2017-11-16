package pattern_mixture_summarization;

public class test {

	public static void main(String[] args) {
		String rootDirectory="/Users/tingxie/Documents/workspace/MyLocalInsiderThreat/data/";
        String featureVectorsPath=rootDirectory+"FeatureVectors.txt";
        String multiplicityPath=rootDirectory+"FeatureVectorCounts.txt";
        String clusterAssignmentsPath=rootDirectory+"hamming_labels.txt";     
        ClusteringResult result=new ClusteringResult( featureVectorsPath,multiplicityPath,clusterAssignmentsPath);
        int index=0;
        for(NaiveSummary summary: result.getNaiveSummaryHierarchy()){
        	System.out.println();
        	System.out.println("Cluster Error "+summary.getError());
            System.out.println(summary);
            System.out.println();  
            index++;
        } 
        System.out.println(index+" clusters after merge.");
        System.out.println("Average Error is "+result.getError());
	}

}
