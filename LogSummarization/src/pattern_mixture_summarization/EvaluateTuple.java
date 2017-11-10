package pattern_mixture_summarization;

import java.util.Comparator;

public class EvaluateTuple{
	int verbosityTotal;
	double verbosityExpectation;
	int numClusters;
	double errorExpectation;
	double runningTime;

	public EvaluateTuple(int verbosityTotal,double verbosityExpectation,int numClusters,double errorExpectation,double runningTime){
		this.verbosityTotal=verbosityTotal;
		this.verbosityExpectation=verbosityExpectation;
		this.numClusters=numClusters;
		this.errorExpectation=errorExpectation;
		this.runningTime=runningTime;
	}
	
	static Comparator<EvaluateTuple> getErrorComparator() {
		return new Comparator<EvaluateTuple>(){
			@Override
			public int compare(EvaluateTuple o1, EvaluateTuple o2) {
				if (o1.errorExpectation<o2.errorExpectation)
					return -1;
				else if  (o1.errorExpectation>o2.errorExpectation)
					return 1;
				else
					return 0;
			}

		};
	}   
	
	static Comparator<EvaluateTuple> getVerbosityExpectationComparator() {
		return new Comparator<EvaluateTuple>(){
			@Override
			public int compare(EvaluateTuple o1, EvaluateTuple o2) {
				if (o1.verbosityExpectation<o2.verbosityExpectation)
					return -1;
				else if  (o1.verbosityExpectation>o2.verbosityExpectation)
					return 1;
				else
					return 0;
			}

		};
	} 
	
	static Comparator<EvaluateTuple> getVerbosityTotalComparator() {
		return new Comparator<EvaluateTuple>(){
			@Override
			public int compare(EvaluateTuple o1, EvaluateTuple o2) {
				if (o1.verbosityTotal<o2.verbosityTotal)
					return -1;
				else if  (o1.verbosityTotal>o2.verbosityTotal)
					return 1;
				else
					return 0;
			}

		};
	} 
}
