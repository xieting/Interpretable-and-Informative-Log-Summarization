package feature_management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * if we care about the order of a group of siblings
 * @author Ting Xie
 *
 */
public class OrderedListAsFeature extends CollectionOfFeaturesAsFeature{
		
	  /**
	 * ID
	 */
	private static final long serialVersionUID = 2256548212048544599L;
	private ArrayList<Integer> labelList;//if horizontal, then it is the list of all horizontal labels 
	  
	  /**
	   * create horizontal feature
	   * @param content
	   */
	  private OrderedListAsFeature(ArrayList<Integer> labelList){
		  this.labelList=new ArrayList<Integer>(labelList);
	  }
	  
	  /**
	   * create new horizontalFeature
	   * @param labellist
	   * @return
	   */
	  public static OrderedListAsFeature createNewOrderListAsFeatureInstance(ArrayList<Integer> labellist){
		  if (labellist.size()>1){
			 OrderedListAsFeature feature=featureManager.getOrderListAsFeatureFromComponentLabels(labellist);
		  if (feature==null){
			  feature=new OrderedListAsFeature(labellist);
			  featureManager.registerOrderListAsFeature(feature);
		  }
		  return feature;
		  }
		  else {
			  return null;
		  }
	  }
	 
	 public ArrayList<Integer> getHorizontalList(){
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
				//check if this feature is a sublist of the horizontal list of not
				if (feature instanceof OrderedListAsFeature){
					List<Integer> list=((OrderedListAsFeature) feature).getHorizontalList();
					if (this.labelList.size()>=list.size()){
					  int size=list.size();
					  int size2=this.labelList.size();
					  int start=0;
					  Integer obj;
					  int count1=0;
					  
						for (int i=0;i<size;i++){
							obj=list.get(i);
							for (int j=start;j<size2;j++){
								if (this.labelList.get(j).equals(obj)){
									start=j+1;
									count1++;
									break;
								}
							}
							if (count1!=i+1)
								break;
						}
					    
						if (count1==size)
							count++;						
					}
					
					for (Integer i:this.labelList){
						if (!list.contains(i))
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
		
			line=" ( "+featureManager.getFeatureFromLabel(it.next()).toString();
			while (it.hasNext()){
				Integer label=it.next();
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

	@Override
	public String printMySelfRecursive(int level) {
    	
		String line="";
        
		for (Integer label:this.labelList){
			Feature feature=featureManager.getFeatureFromLabel(label);
			if(feature==null){
				System.out.println("I cannot find label :"+label);
			}
			else{
				line+=feature.printMySelfRecursive(level);	
				if (this.labelList.size()>1)
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
