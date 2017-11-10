package feature_management;

import java.io.Serializable;

/**
 * abstract class describing a feature
 * @author Ting Xie
 *
 */
public abstract class Feature implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 613864778339983704L;
	int label=Integer.MAX_VALUE;//its own label
    int complexity=-1; //complexity of its represented tree structure
    
	public int getOwnLabel(){
		return this.label;
	} 
    
	public Feature(){
		this.label=featureManager.requestForLabel(this);
	}
	
	/**
	 * two features are the same iff their labels are the same
	 */
	@Override
	public boolean equals(Object obj){
		Feature feature= (Feature) obj;
		if (this.label==feature.label)
			return true;
		else
			return false;
	}

	//count number of times feature is contained in another feature
	public abstract int ifContains(Feature feature);
	public abstract String toString();
	public abstract String printMySelfRecursive(int level);
	public int hashCode(){
		return Integer.hashCode(this.label);
	}
	public int getComplexity(){this.calculateComplexity();return this.complexity;}
	public abstract void calculateComplexity();
}
