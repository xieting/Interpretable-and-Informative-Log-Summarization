package feature_management;

import java.util.ArrayList;


/**
 * represents parent-child relationship of two features
 * @author Ting Xie
 *
 */
public class ParentChildRelation extends Feature{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5178799213256099262L;
	private int parentlabel;
	private int childlabel;
	
	/**
	 * create vertical relationship
	 * @param content
	 */
	private ParentChildRelation(int parentlabel,int child){
		this.parentlabel=parentlabel;
		this.childlabel=child;
	}

	/**
	 * create new vertical feature
	 * @param content
	 * @param childlabel
	 * @return
	 */
	public static ParentChildRelation createNewParentChildRelationInstance(int parentlabel,int childlabel){
		ArrayList<Integer> list=new ArrayList<Integer>();
		list.add(parentlabel);
		list.add(childlabel);
		ParentChildRelation feature=featureManager.getParentChildRelationFromComponentLabels(list);
		if (feature==null){
			feature=new ParentChildRelation(parentlabel,childlabel);
			featureManager.registerParentChildRelation(feature);
		}
		return feature;
	}

	public int getOwnLabel(){
		return this.label;
	} 


	public int getChildLabel(){
		return this.childlabel;
	}

	public int getParentLabel(){
		return this.parentlabel;
	}

	/**
	 * check if this feature's structure contains the target feature's structure
	 * @param feature
	 * @return
	 */
	public int ifContains(Feature feature){
		//check if it is the same with itself
		if (feature.getOwnLabel()==this.label)
			return 1;
		else {
			//check if this feature is vertical subtree of not
			int count=0;	
			if (feature instanceof ParentChildRelation){
				Feature myhead=featureManager.getFeatureFromLabel(this.parentlabel);
				Feature mytail=featureManager.getFeatureFromLabel(this.childlabel);
				ParentChildRelation vfeature=(ParentChildRelation) feature;
				Feature head=featureManager.getFeatureFromLabel(vfeature.getParentLabel());
				Feature tail=featureManager.getFeatureFromLabel(vfeature.getChildLabel());
				int hc=myhead.ifContains(head);
				int tc=mytail.ifContains(tail);
				if(hc>=1&&tc>=1){
					count+=hc*tc;
				}
			}
		
		//check if its label is contained its parent label
		count+= featureManager.getFeatureFromLabel(this.parentlabel).ifContains(feature);
		//check if its label is contained in its child label
		count+= featureManager.getFeatureFromLabel(this.childlabel).ifContains(feature);		
		return count;	

	}
}

@Override
public String toString(){
	String line=" ( ";
	line+=featureManager.getFeatureFromLabel(this.parentlabel).toString()+GlobalVariables.parentChildrenSeparator+featureManager.getFeatureFromLabel(this.childlabel).toString();
	line+= " ) ";	
	return line;
}

@Override
public String printMySelfRecursive(int level) {
	Feature feature=featureManager.getFeatureFromLabel(this.childlabel);

	String line=featureManager.getFeatureFromLabel(this.getParentLabel()).printMySelfRecursive(level);
	line+=GlobalVariables.parentChildrenSeparator;	
	line+="\n";
    line+=feature.printMySelfRecursive(level+1);    
    return line;
}

@Override
public void calculateComplexity() {
	if(this.complexity==-1){
	this.complexity=0;
	int ch,pa;
	Feature feature;
		feature=featureManager.getFeatureFromLabel(this.childlabel);
		ch=feature.getComplexity();
		feature=featureManager.getFeatureFromLabel(this.parentlabel);
		pa=feature.getComplexity();		
		this.complexity=ch*pa*GlobalVariables.treeComplexityCoef;
	}
}

}

