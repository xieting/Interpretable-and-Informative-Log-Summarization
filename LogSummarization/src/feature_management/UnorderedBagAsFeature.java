package feature_management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import com.google.common.collect.HashMultiset;

/**
 * if we do not care about order and duplicates in a collection of features
 * @author Ting Xie
 *
 */
public class UnorderedBagAsFeature extends CollectionOfFeaturesAsFeature{


	/**
	 * 
	 */
	private static final long serialVersionUID = 7067816250651487137L;
	
	private HashMultiset<Integer> labelList;//the bag of all feature labels 

	/**
	 * create a bag of features as new feature
	 * @param content
	 */
	private UnorderedBagAsFeature(HashMultiset<Integer> labelList){
		this.labelList=HashMultiset.create(labelList);
	}

	/**
	 * create new bag as a feature
	 * @param labellist
	 * @return
	 */
	public static UnorderedBagAsFeature createNewUnorderedBagAsFeatureInstance(HashMultiset<Integer> labellist){
		if (labellist.size()>1){
			UnorderedBagAsFeature feature=featureManager.getUnorderedBagAsFeatureFromComponentLabels(labellist);
			if (feature==null){
				feature=new UnorderedBagAsFeature(labellist);
				featureManager.registerUnorderedBagAsFeature(feature);
			}	
			return feature;
		}
		else {
			return null;
		}
	}


	public HashMultiset<Integer> getHorizontalList(){
		return this.labelList;
	}

	/**
	 * check if this feature's structure contains the target feature's structure
	 * @param feature
	 * @return
	 */
	@Override
	public int ifContains(Feature feature){
		//check if it is the same with itself
		if (feature.getOwnLabel()==this.label)
			return 1;
		else {
			int count=0;
			if (feature instanceof UnorderedBagAsFeature){
			//check if this feature is a sublist of the horizontal list of not
			HashMultiset<Integer> bag=((UnorderedBagAsFeature) feature).getHorizontalList();
			
			if (this.labelList.containsAll(bag))
				//minor drawback, cannot check whether this.labelist contains multiple of the target bag
				count++;
			
			for (Integer i:this.labelList){
				if (!bag.contains(i))
				count+= featureManager.getFeatureFromLabel(i).ifContains(feature);					
			}
			
			}			
		     
			else{
			for (Integer i:this.labelList){
				count+= featureManager.getFeatureFromLabel(i).ifContains(feature);					
			}
			
			}
			
			return count;			
		}
	}


	@Override
	public String toString(){
		String line;
		Iterator<Integer> it=this.labelList.iterator();
		ArrayList<Integer> list=new ArrayList<Integer> ();
		while (it.hasNext())
        	list.add(it.next());
		Collections.sort(list);
		
		line=" ( "+featureManager.getFeatureFromLabel(list.get(0)).toString();
		
		for (int i=1;i<list.size();i++){
			Integer label=list.get(i);
			Feature feature=featureManager.getFeatureFromLabel(label);
			if(feature==null){
				System.out.println("I cannot find label :"+label);
			}
			else
				line+=GlobalVariables.siblingSeparator+feature.toString();
		}
		line+=" ) ";
		return line;
	}

    public String printMySelfRecursive(int level){
    	
		String line="";
		Iterator<Integer> it=this.labelList.iterator();
        ArrayList<Integer> list=new ArrayList<Integer> ();
        while (it.hasNext())
        	list.add(it.next());
		
       Collections.sort(list);
       
		for (Integer label:list){
			Feature feature=featureManager.getFeatureFromLabel(label);
			if(feature==null){
				System.out.println("I cannot find label :"+label);
			}
			else{				
				line+=feature.printMySelfRecursive(level);
				if (list.size()>1)
				line+=GlobalVariables.siblingSeparator;
				line+="\n";
			}
		}
		
		return line.substring(0,line.length()-1);
    	
    }

	@Override
	public void calculateComplexity() {
		if (this.complexity==-1){
		this.complexity=0;
		Feature feature;
		for (Integer label:this.labelList){
			feature=featureManager.getFeatureFromLabel(label);
			this.complexity+=feature.getComplexity();
		}
		}
		
	}

}
