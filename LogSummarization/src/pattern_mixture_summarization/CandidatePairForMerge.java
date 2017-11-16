package pattern_mixture_summarization;

import java.util.HashSet;

public class CandidatePairForMerge implements Comparable<CandidatePairForMerge>{
    HashSet<Cluster> pair;
    double predictedEntropyIncreaseAfterMerge;
    
    public CandidatePairForMerge(Cluster left, Cluster right, double distance){
    	this.pair=new HashSet<Cluster>();
    	this.pair.add(left);
    	this.pair.add(right);
    	this.predictedEntropyIncreaseAfterMerge=distance;
    }

	@Override
	public int compareTo(CandidatePairForMerge o) {
		if (o.predictedEntropyIncreaseAfterMerge>this.predictedEntropyIncreaseAfterMerge)
			return -1;
		else if (o.predictedEntropyIncreaseAfterMerge<this.predictedEntropyIncreaseAfterMerge)
			return 1;
		else
		return 0;
	}
}
