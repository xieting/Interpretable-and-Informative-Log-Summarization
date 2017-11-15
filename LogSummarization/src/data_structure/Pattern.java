package data_structure;

import java.util.HashMap;
import java.util.HashSet;
import feature_management.FeatureVector;

/**
 * a set of observed feature occurrence
 * @author Ting Xie
 *
 */
public class Pattern {

	private HashSet<ObservedFeatureOccurrence> observedFeatureOccurrenceSet; 
	
	public HashSet<ObservedFeatureOccurrence> getObservedFeatureOccurrenceSet(){
		return this.observedFeatureOccurrenceSet;
	}
	
	public int size(){
		return this.observedFeatureOccurrenceSet.size();
	}	

	public Pattern(Pattern q){
		this.observedFeatureOccurrenceSet=new HashSet<ObservedFeatureOccurrence>(q.getObservedFeatureOccurrenceSet());
	}

    public Pattern(){
    	observedFeatureOccurrenceSet=new HashSet<ObservedFeatureOccurrence>();
    }

	/**
	 * add to set with completeness checking e.g. feature (ID=2, occurrence=2) implies
	 * feature (ID=2, occurrence=1)
	 * note: use it for initializing an feature set but not merging two sets
	 * @param feature
	 * @param featureMap
	 */
	public void addToSet(int targetID,int targetoccur,HashMap<Integer,HashMap<Integer,ObservedFeatureOccurrence>> featureMap){
		//add to set making sure all observed occurrences are added
		//namely feature with K occurrence observed is equivalent to 1-K occurrences observed
		for (int i=1;i<=targetoccur;i++){
			ObservedFeatureOccurrence sfeature=ObservedFeatureOccurrence.createNewInstance(targetID,i,featureMap);
			this.observedFeatureOccurrenceSet.add(sfeature);
		}
	}


	public Boolean contains(ObservedFeatureOccurrence feature){
		return this.observedFeatureOccurrenceSet.contains(feature);
	}
	
	public Boolean containsAll(Pattern set){
		return this.observedFeatureOccurrenceSet.containsAll(set.observedFeatureOccurrenceSet);
	}
	
	@Override 
	public String toString(){ 
		String inputline="";
		for(ObservedFeatureOccurrence ofo: this.observedFeatureOccurrenceSet){
			int ID=ofo.getFeatureID();
			int occur=ofo.getOccurrence();			
				inputline+=ID+":"+occur+",";
		}		
		inputline=inputline.substring(0, inputline.length()-1);
		return inputline;
	}

	@Override
	public int hashCode(){
		return this.observedFeatureOccurrenceSet.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(this==o)
			return true;
		else{
		Pattern yourset=(Pattern) o;
		return this.observedFeatureOccurrenceSet.equals(yourset.observedFeatureOccurrenceSet);
		}
	}
	
	/**
	 * transform it into corresponding feature vector
	 * @return
	 */
	public FeatureVector toFeatureVector(){
		FeatureVector vector=new FeatureVector();
		for(ObservedFeatureOccurrence ofo: this.observedFeatureOccurrenceSet)
		vector.addOneFeatureIn(ofo.getFeatureID());
		return vector;
	}
	
}

