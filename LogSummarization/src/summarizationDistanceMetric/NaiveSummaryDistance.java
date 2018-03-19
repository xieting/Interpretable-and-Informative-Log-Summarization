package summarizationDistanceMetric;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import data_structure.Trie;
import feature_management.FeatureVector;

public class NaiveSummaryDistance implements DistanceMetric{
	private Trie mytree;
	private boolean isSimilarity;
	private LinkedHashMap<FeatureVector,Integer> leftRightbuffer;
	private int bufferMaxsize=1000000;//default buffer size
	public NaiveSummaryDistance(Trie mytree,boolean isSimilarity){
		this.mytree=mytree;
		this.isSimilarity=isSimilarity;
		this.leftRightbuffer=new LinkedHashMap<FeatureVector,Integer>();
	}

	@Override
	public double dist(FeatureVector left, FeatureVector right){
		HashSet<Integer> intersec=new HashSet<Integer>();
		if(left.size()<right.size()){
			for (Integer ID:left.getDistinctFeatures()){
                 Integer leftOccur=left.getFeatureOccurrence(ID);
                 Integer rightOccur=right.getFeatureOccurrence(ID);
                 if(leftOccur==rightOccur)
                	 intersec.add(ID);
			}
		}
		else{
			for (Integer ID:right.getDistinctFeatures())  {
                Integer leftOccur=left.getFeatureOccurrence(ID);
                Integer rightOccur=right.getFeatureOccurrence(ID);
                if(leftOccur==rightOccur)
               	 intersec.add(ID);
			}
		}

		if(!intersec.isEmpty()){
			Integer leftCount=this.getCountFromLeftRightBuffer(left);
			if(leftCount==null){
				leftCount=this.mytree.getCountOfExactlyMatchingFeatureVector(left);
			}
			
			Integer rightCount=this.getCountFromLeftRightBuffer(right);
			if(rightCount==null){
				rightCount=this.mytree.getCountOfExactlyMatchingFeatureVector(right);
			}
			
			double leftEstimate=(double)leftCount;
			double rightEstimate=(double)rightCount;			
			double p=leftEstimate/(leftEstimate+rightEstimate);
			double entropy=-p*Math.log(p)-(1-p)*Math.log(1-p);
			double result=(left.getDistinctFeatures().size()+right.getDistinctFeatures().size()-2*intersec.size()-1)*entropy;			
			
			if(!this.isSimilarity)
				return result;
			else
				return Math.exp(-result*result);
		}		
		else {
			if(!this.isSimilarity){
				Integer leftCount=this.getCountFromLeftRightBuffer(left);
				if(leftCount==null){
					leftCount=this.mytree.getCountOfExactlyMatchingFeatureVector(left);
					this.saveToLeftRightBuffer(left, leftCount);
				}
				
				Integer rightCount=this.getCountFromLeftRightBuffer(right);
				if(rightCount==null){
					rightCount=this.mytree.getCountOfExactlyMatchingFeatureVector(right);
					this.saveToLeftRightBuffer(right, rightCount);
				}
				
				double leftEstimate=(double)leftCount;
				double rightEstimate=(double)rightCount;			

				double p=leftEstimate/(leftEstimate+rightEstimate);
				double entropy=-p*Math.log(p)-(1-p)*Math.log(1-p);
				return entropy*(left.getDistinctFeatures().size()+right.getDistinctFeatures().size()-1);							
			}
			else
				return 0;
		}
	}
	
	/**
	 * buffer control strategy
	 * @param vector
	 * @param count
	 */
	private void saveToLeftRightBuffer(FeatureVector vector ,Integer count){
		if(this.leftRightbuffer.size()>this.bufferMaxsize){
			Entry<FeatureVector, Integer> en=this.leftRightbuffer.entrySet().iterator().next();
			this.leftRightbuffer.remove(en.getKey(), en.getValue());
		}
		this.leftRightbuffer.put(vector,count);	
	}
	
	private Integer getCountFromLeftRightBuffer(FeatureVector vector){
		Integer result=this.leftRightbuffer.get(vector);
		if(result!=null){
			//update its hotness
			this.leftRightbuffer.remove(vector,result);
			this.leftRightbuffer.put(vector, result);
		}			
		return result;
	}

}
