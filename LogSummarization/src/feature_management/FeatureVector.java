package feature_management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import java.util.Set;

/**
 * this class hides details about the actual representation of a feature vector
 * you can use given methods to modify the content of this vector 
 * @author tingxie
 *
 */
public class FeatureVector{
	private HashMap<Integer,Integer> labelMap=new HashMap<Integer,Integer>();

	/**
	 * add one feature into this vector
	 * @param featureID
	 */
	public void addOneFeatureIn(int featureID){
		Integer occur=this.labelMap.get(featureID);
		if(occur==null)
			this.labelMap.put(featureID, 1);
		else
			this.labelMap.put(featureID, occur+1);
	}


	/**
	 * add the same feature with multiple occurrences in
	 * @param featureID
	 * @param occurrence
	 */
	public void addFeatureWithOccurrence(int featureID, int occurrence){
		if(occurrence>0){
			Integer occur=this.labelMap.get(featureID);
			if(occur==null)
				this.labelMap.put(featureID, occurrence);
			else
				this.labelMap.put(featureID, occur+occurrence);
		}
		else {
			System.out.println("occurrence must be positive when adding feature to feature vector!");
			return;
		}
	}

	/**
	 * Bag union input feature vector with this vector
	 * (..,x_i,..) + (..,x'_i,..) = (..,x_i+x'_i,..)
	 */
	public void addWholeFeatureVectorIn(FeatureVector input){
		for (Entry<Integer,Integer> en: input.labelMap.entrySet()){
			int featureID=en.getKey();
			int occurrence=en.getValue();
			Integer occur=this.labelMap.get(featureID);
			if(occur==null)
				this.labelMap.put(featureID, occurrence);
			else
				this.labelMap.put(featureID, occur+occurrence);
		}
	}

	/**
	 * get the occurrence number of input feature in this vector
	 * @param featureID
	 * @return
	 */
	public int getFeatureOccurrence(int featureID){
		Integer result=this.labelMap.get(featureID);
		if(result==null)
			return 0;
		else 
			return result;
	}

	/**
	 * get the set of distinct features out from this vector
	 * @return
	 */
	public Set<Integer> getDistinctFeatures(){ 
		return this.labelMap.keySet();
	}

	public int getSize(){
		int sum=0;
		for (Integer occur: this.labelMap.values())
			sum+=occur;
		return sum;
	}

	public int getDistinctSize(){
		return this.getDistinctFeatures().size();
	}

	public static FeatureVector readFeatureVectorFromFormattedString(String line){          
		try{
			FeatureVector featurevector=new FeatureVector();
			//parse into feature vector
			String tokens[]=line.split("\\s+|,");
			int size=Integer.parseInt(tokens[0]);
			//sanity check
			if(size!=tokens.length-1)
				System.out.println("featureVector length does not match the stored record, please check.");

			for (int i=1;i<tokens.length;i++){
				String innertokens[]=tokens[i].split(":");
				int label=Integer.parseInt(innertokens[0]);
				int occur=Integer.parseInt(innertokens[1]);
				featurevector.addFeatureWithOccurrence(label, occur);
			}
			return featurevector;  
		}
		catch(NumberFormatException e){
			return new FeatureVector();
		}  	
	}

	public int size(){
		return this.labelMap.size();
	}

	@Override
	/**
	 * print the feature vector in libsvm format
	 */
	public String toString(){
		String line=Integer.toString(this.size());
		ArrayList<Integer> IDs=new ArrayList<Integer>(this.labelMap.keySet());
		Collections.sort(IDs);
		for(Integer ID: IDs){
			line+=" "+ID+":"+this.labelMap.get(ID);
		}
		return line;
	}

	public boolean isEmpty(){
		return this.labelMap.isEmpty();
	}

	@Override
	public boolean equals(Object o){
		FeatureVector vector=(FeatureVector) o;
		return this.labelMap.equals(vector.labelMap);
	}

	@Override
	public int hashCode(){
		return this.labelMap.hashCode();
	}

	/**
	 * intersection of two feature vectors
	 * (..,x_i,..) \intersect (..,x'_i,..) = (..,min(x_i,x'_i),..)
	 * @param left
	 * @param right
	 * @return
	 */
	public static FeatureVector intersection(FeatureVector left, FeatureVector right){
		FeatureVector intersec=new FeatureVector();
		int leftlength=left.size();
		int rightlength=right.size();
		if(leftlength>rightlength){
			Set<Integer> rightfeatures= right.getDistinctFeatures();
			for(Integer ID: rightfeatures){
				int rightoccur=right.getFeatureOccurrence(ID);
				int leftoccur=left.getFeatureOccurrence(ID);
				int min=Math.min(rightoccur, leftoccur);
				if(min>0)
					intersec.addFeatureWithOccurrence(ID, min);
			}
		}
		else {
			Set<Integer> leftfeatures= left.getDistinctFeatures();
			for(Integer ID: leftfeatures){
				int rightoccur=right.getFeatureOccurrence(ID);
				int leftoccur=left.getFeatureOccurrence(ID);
				int min=Math.min(rightoccur, leftoccur);
				if(min>0)
					intersec.addFeatureWithOccurrence(ID, min);
			}
		}
		return intersec;
	}

	public static FeatureVector setIntersection(FeatureVector left, FeatureVector right){
		FeatureVector intersec=new FeatureVector();
		int leftlength=left.size();
		int rightlength=right.size();
		if(leftlength>rightlength){
			Set<Integer> rightfeatures= right.getDistinctFeatures();
			for(Integer ID: rightfeatures){
				int rightoccur=right.getFeatureOccurrence(ID);
				int leftoccur=left.getFeatureOccurrence(ID);
				if(rightoccur==leftoccur)
					intersec.addFeatureWithOccurrence(ID, leftoccur);
			}
		}
		else {
			Set<Integer> leftfeatures= left.getDistinctFeatures();
			for(Integer ID: leftfeatures){
				int rightoccur=right.getFeatureOccurrence(ID);
				int leftoccur=left.getFeatureOccurrence(ID);

				if(rightoccur==leftoccur)
					intersec.addFeatureWithOccurrence(ID, leftoccur);
			}
		}
		return intersec;
	}


	/**
	 * Set Union of two feature vectors
	 * (..,x_i,..) \setUnion (..,x'_i,..) = (..,max(x_i,x'_i),..)
	 * @param left
	 * @param right
	 * @return
	 */
	public static FeatureVector setUnion(FeatureVector left, FeatureVector right){

		FeatureVector setUnion=new FeatureVector();
		int leftlength=left.size();
		int rightlength=right.size();
		if(leftlength>rightlength){
			setUnion.addWholeFeatureVectorIn(left);
			Set<Integer> rightfeatures= right.getDistinctFeatures();
			for(Integer ID: rightfeatures){
				int rightoccur=right.getFeatureOccurrence(ID);
				int leftoccur=left.getFeatureOccurrence(ID);
				int max=Math.max(rightoccur, leftoccur);
				setUnion.labelMap.put(ID, max);
			}
		}
		else {
			setUnion.addWholeFeatureVectorIn(right);
			Set<Integer> leftfeatures= left.getDistinctFeatures();
			for(Integer ID: leftfeatures){
				int rightoccur=right.getFeatureOccurrence(ID);
				int leftoccur=left.getFeatureOccurrence(ID);
				int max=Math.max(rightoccur, leftoccur);
				setUnion.labelMap.put(ID, max);
			}
		}
		return setUnion;
	}


	/**
	 * bag difference of two feature vectors
	 * (..,x_i,..) - (..,x'_i,..) = (..,max(0,x_i-x'_i),..)
	 * @param left
	 * @param right
	 * @return
	 */
	public static FeatureVector bagDifference(FeatureVector left, FeatureVector right){
		FeatureVector result=new FeatureVector();
		result.addWholeFeatureVectorIn(left);
		Set<Integer> rightfeatures= right.getDistinctFeatures();
		for(Integer ID:rightfeatures){
			int rightoccur=right.getFeatureOccurrence(ID);
			int leftoccur=left.getFeatureOccurrence(ID);
			int value=leftoccur-rightoccur;
			if(value>0)
				result.labelMap.put(ID, value);
			else if(leftoccur>0)
				result.labelMap.remove(ID);    	    	
		}
		return result;	
	}

	public String toTransaction(){
		String line="";
		ArrayList<Integer> IDs=new ArrayList<Integer>(this.labelMap.keySet());
		Collections.sort(IDs);
		for(Integer ID: IDs){
			line+=" "+ID;
		}
		return line.substring(1);
	}


}
