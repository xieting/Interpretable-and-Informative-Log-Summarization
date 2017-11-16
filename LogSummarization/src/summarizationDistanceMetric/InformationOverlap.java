package summarizationDistanceMetric;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import data_structure.FeatureVector_Trie;
import feature_management.FeatureVector;

/**
 * a distance metric of feature vectors
 * @author tingxie
 *
 */
public class InformationOverlap implements DistanceMetric{
	private FeatureVector_Trie tree;
	private LinkedHashMap<FeatureVector,Integer> interSecbuffer;
	private LinkedHashMap<FeatureVector,Integer> leftRightbuffer;
	private int bufferMaxsize=100000;//default buffer size

	public InformationOverlap(FeatureVector_Trie tree,int maxsize){
		this.tree=tree;
		this.interSecbuffer=new LinkedHashMap<FeatureVector,Integer>();
		this.leftRightbuffer=new LinkedHashMap<FeatureVector,Integer>();
		this.bufferMaxsize=maxsize;
	}

	public InformationOverlap(FeatureVector_Trie tree){
		this.tree=tree;
		this.interSecbuffer=new LinkedHashMap<FeatureVector,Integer>();
		this.leftRightbuffer=new LinkedHashMap<FeatureVector,Integer>();
	}

	@Override
	public double dist(FeatureVector left, FeatureVector right){
		FeatureVector intersec=FeatureVector.intersection(left, right);	
		Integer leftcount=this.getCountFromLeftRightBuffer(left);
		Integer rightcount=this.getCountFromLeftRightBuffer(right);		
		if(leftcount==null){			
			leftcount=this.tree.getCountOfExactlyMatchingFeatureVector(left);
			this.saveToLeftRightBuffer(left, leftcount);
		}

		if(rightcount==null){
			rightcount=this.tree.getCountOfExactlyMatchingFeatureVector(right);
			this.saveToLeftRightBuffer(right, rightcount);
		}

		if(!intersec.isEmpty()){	
			Integer interseccount=this.getCountFromIntersecBuffer(intersec);
			if(interseccount==null){
				interseccount=this.tree.getCountOfContainingFeatureVector(intersec);
				this.saveToIntersecBuffer(intersec, interseccount);
			}	
			
			double p=(double)interseccount/(double)this.tree.getTotalFeatureSetCount();
			return p*Math.log((double)interseccount/(double)Math.min(rightcount, leftcount));
			//return Math.log((double)interseccount/(double)Math.min(rightcount, leftcount));
		}		
		else {
			return Math.log((double)this.tree.getTotalFeatureSetCount()/(double)Math.min(rightcount, leftcount));
		//return 1;
		}
	}

	/**
	 * buffer control strategy
	 * @param vector
	 * @param count
	 */
	private void saveToIntersecBuffer(FeatureVector vector ,Integer count){
		if(this.interSecbuffer.size()>this.bufferMaxsize){
			Entry<FeatureVector, Integer> en=this.interSecbuffer.entrySet().iterator().next();
			this.interSecbuffer.remove(en.getKey(), en.getValue());
		}
		this.interSecbuffer.put(vector,count);	
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
	

	private Integer getCountFromIntersecBuffer(FeatureVector vector){
		Integer result=this.interSecbuffer.get(vector);
		if(result!=null){
			//update its hotness
			this.interSecbuffer.remove(vector,result);
			this.interSecbuffer.put(vector, result);
		}			
		return result;
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
