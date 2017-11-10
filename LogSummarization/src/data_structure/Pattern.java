package data_structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import feature_management.FeatureVector;
import feature_management.featureManager;

/**
 * an FPQuery is a set of Words
 * it can be sorted later
 * @author Ting Xie
 *
 */

public class Pattern implements Comparable<Pattern>{

	private HashMap<Integer,Integer> featureset=new HashMap<Integer,Integer>();
	private HashSet<Word> materializedSet=new HashSet<Word>();
	int multiplicity;
    double utilityMeasure;
    
	public HashMap<Integer,Integer> getSet(){
		return this.featureset;
	}
	
	public HashSet<Word> getMaterializedSet(){
		return this.materializedSet;
	}

	public double getUtilityMeasure(){
		return this.utilityMeasure;
	}
	public void setUtilityMeasure(double utilityMeasure){
		this.utilityMeasure=utilityMeasure;
	}
	
	public int size(){
		return this.materializedSet.size();
	}

	public void setMultiplicity(int value){
		this.multiplicity=value;
	}
	
	public int getMultiplicity(){
		return this.multiplicity;
	}
	

	public Pattern(Pattern q){
		this.featureset=new  HashMap<Integer,Integer>(q.getSet());
		this.materializedSet=new HashSet<Word>(q.getMaterializedSet());
		this.multiplicity=q.multiplicity;
	}

    public Pattern(){
    	this.multiplicity=1;
    }
	/**
	 * reconstruct the features from reading the file line by line
	 * @param line
	 */


	public Pattern(String line,HashMap<Integer,HashMap<Integer,Word>> featureMap){
		Word feature;
		String[] tokens=line.split(GlobalVariables.specialSeparator);
		this.multiplicity=Integer.parseInt(tokens[0]);

		String[] Featuretokens=tokens[1].split(GlobalVariables.FeatureSeparator);
		String featurelabel;
		String[] t;
		try{
			int size=Integer.parseInt(Featuretokens[0]);
			if (size!=Featuretokens.length-1){
				System.out.println("read error, #features parsed does not match recorded# :  "+line+"I am expecting:"+size+" number of labels");
				return;
			}
			else {	
				this.featureset= new HashMap<Integer,Integer>();
				for (int i=1;i<Featuretokens.length;i++){
					featurelabel=Featuretokens[i];
					t=featurelabel.split(GlobalVariables.OccurSeparator);
					feature=Word.createNewWord(Integer.parseInt(t[0]),Integer.parseInt(t[1]),featureMap);
					this.addToSet(feature,featureMap);               
				}
			}
		}
		catch(NumberFormatException e){
			System.out.println("read error, the first string should be the size of this query feature list");
			return;
		}
	}

	/**
	 * add to set with completeness checking e.g. feature (ID=2, occurrence=2) implies
	 * feature (ID=2, occurrence=1)
	 * note: use it for initializing an feature set but not merging two sets
	 * @param feature
	 * @param featureMap
	 */
	public void addToSet(Word feature,HashMap<Integer,HashMap<Integer,Word>> featureMap){
		//add to materialized set	
		int targetoccur=feature.getOccurrence();
		int targetID=feature.getFeatureID();	
		for (int i=1;i<=targetoccur;i++){
			Word sfeature=Word.createNewWord(targetID,i,featureMap);
			this.materializedSet.add(sfeature);
		}
		//update hashmap
        Integer occur=this.featureset.get(targetID);
        if(occur==null||occur<targetoccur)
		this.featureset.put(targetID,targetoccur);
	}

	/**
	 * add to set without checking completeness e.g. feature (ID=2, occurrence=2) implies
	 * feature (ID=2, occurrence=1)
	 * note: use it only when you are sure about completeness
	 * @param feature
	 */
	public void addToSetAnyWay(Word feature){
		//add to materialized set
		this.materializedSet.add(feature);
		//update hashmap
        Integer occur=this.featureset.get(feature.getFeatureID());
        if(occur==null||occur<feature.getOccurrence())
		this.featureset.put(feature.getFeatureID(),feature.getOccurrence());
	}
	
/**
 * merge two feature sets
 * @param featureset
 */
	public void mergeUnderSetSemantic(Pattern featureset){
		//add to materialized set
		this.materializedSet.addAll(featureset.getMaterializedSet());
		//update hashmap
		for (Entry<Integer, Integer> en: featureset.featureset.entrySet()){
			Integer ID=en.getKey();
			Integer occurrence=en.getValue();
        Integer myoccur=this.featureset.get(ID);
        if(myoccur==null||myoccur<occurrence)
		this.featureset.put(ID,occurrence);
		}
	}


	public Boolean contains(Word feature){
		return this.materializedSet.contains(feature);
	}
	
	public Boolean containsAll(Pattern set){
		boolean pass=true;
		for (Entry<Integer, Integer> en:set.featureset.entrySet()){
			int ID=en.getKey();
			int maxOccur=en.getValue();
			Integer matchedOccur=this.featureset.get(ID);
			if(matchedOccur==null||matchedOccur<maxOccur){
				pass=false;
				break;
			}
		}
		return pass;
	}
	
	@Override 
	public String toString(){ 
		String inputline="";
		for(Entry<Integer,Integer> en: this.featureset.entrySet()){
			int ID=en.getKey();
			int occur=en.getValue();			
				inputline+=","+ID+":"+occur;
		}		
		inputline=inputline.substring(1, inputline.length());
		return inputline;
	}
	

	public String toFeatureString(){ 
		String inputline="";
		
		for(Entry<Integer,Integer> en: this.featureset.entrySet()){
			int ID=en.getKey();
			int occur=en.getValue();
			inputline+=","+featureManager.getFeatureFromLabel(ID)+":"+occur;
		}		
		inputline=inputline.substring(1, inputline.length());
		return inputline;
	}

	/**
	 * this function is critical, it determines the format before the read of second round
	 */  
	public String toLabelString(){
		String line =null;
		if (this.materializedSet!=null&&!this.materializedSet.isEmpty()){
			line =Integer.toString(this.multiplicity);
			line+=GlobalVariables.specialSeparator+String.valueOf(this.materializedSet.size());
			for (Word sfeature: this.materializedSet){				
				line+=GlobalVariables.FeatureSeparator+sfeature.toString();
			}
			
		}
		return line;
	}


	@Override
	public int hashCode(){
		return this.featureset.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(this==o)
			return true;
		else{
		Pattern yourset=(Pattern) o;
		return this.featureset.equals(yourset.featureset);
		}
	}

	@Override
	public int compareTo(Pattern obj) {
		Pattern o=(Pattern) obj;	
		if(this.utilityMeasure>o.utilityMeasure)
			return -1;
		else if(this.utilityMeasure<o.utilityMeasure)
			return 1;
		//if entropy is the same, then longer one wins
		else if(this.materializedSet.size()>o.materializedSet.size())
			return -1;
		else if (this.materializedSet.size()<o.materializedSet.size())
			return 1;
		else
		return 0;
	}
	
	/**
	 * transform it into corresponding feature vector
	 * @return
	 */
	public FeatureVector turnToFeatureVector(){
		FeatureVector vector=new FeatureVector();
		for(Entry<Integer,Integer> en: this.featureset.entrySet())
		vector.addFeatureWithOccurrence(en.getKey(), en.getValue());
		return vector;
	}
	
}

