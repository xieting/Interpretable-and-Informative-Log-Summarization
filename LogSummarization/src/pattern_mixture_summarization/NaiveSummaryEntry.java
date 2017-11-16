package pattern_mixture_summarization;

public class NaiveSummaryEntry implements Comparable<NaiveSummaryEntry>{
	int featureID;
	int occurrence;
	double marginal;

	public NaiveSummaryEntry (int featureID,int occurrence, double marginal){
		this.featureID=featureID;
		this.occurrence=occurrence;
		this.marginal=marginal;
	}

	@Override
	public int compareTo(NaiveSummaryEntry o) {
		if(o.marginal<this.marginal)
			return -1;
		else if (o.marginal>this.marginal)
			return 1;
		else { 
			if (this.featureID>o.featureID)
				return -1;
			else if (this.featureID<o.featureID)
				return 1;
			else {
				if (this.occurrence>o.occurrence)
					return 1;
				else if (this.occurrence<o.occurrence)
					return -1;
				else
					return 0;
			}
		}
	}
}
