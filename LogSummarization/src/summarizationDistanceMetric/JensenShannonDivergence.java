package summarizationDistanceMetric;

import java.util.HashSet;

import feature_management.FeatureVector;

public class JensenShannonDivergence implements DistanceMetric{
    double total;
    
    public JensenShannonDivergence(double total){
    	this.total=total;
    }
	@Override
	public double dist(FeatureVector left, FeatureVector right){

		//compute jensenshannon distance
		double agg=0;
		HashSet<Integer> totalSet=new HashSet<Integer>();
		totalSet.addAll(left.getDistinctFeatures());
		totalSet.addAll(right.getDistinctFeatures());

		for(Integer ID: totalSet){
			int leftOccur=left.getFeatureOccurrence(ID);
			int rightOccur=right.getFeatureOccurrence(ID);
			if(leftOccur==rightOccur)
				continue;
			double xd=(double)leftOccur;
			double yd=(double)rightOccur;
			double md=(xd+yd)/2;			
			if(!(md > 0. || md < 0.)) {
				continue;
			}
			if(xd > 0.) {
				agg += xd * Math.log(xd / md);
			}
			if(yd > 0.) {
				agg += yd * Math.log(yd / md);
			}
		}
		
		return 0.5*agg;	
	}

}
