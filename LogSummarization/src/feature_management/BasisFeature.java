package feature_management;


/**
 * basis/leaf features has its string-valued content and its label(ID)
 * @author Ting Xie
 *
 */
public class BasisFeature extends Feature{

  /**
	 * 
	 */
	private static final long serialVersionUID = 6327548647940318753L;
private Object content; //cannot be null
 
  
  /**
   * create leaf feature
   * @param content
 * @throws ContentNullException 
   */
  private BasisFeature(Object content) throws ContentNullException{
	  if (content!=null){
	  this.content=content;
	  }
	  else{
	  System.out.println("error, content of basis feature cannot be null");
	  throw new ContentNullException();
	  }
  }
    

  /**
   * create new basis feature
   * @param content
   * @return
 * @throws ContentNullException 
   */
  public static BasisFeature createNewBasisFeatureInstance(Object content){
	  BasisFeature feature=featureManager.getBasisFeatureFromContent(content);
	  if (feature==null){
		  try {
			feature=new BasisFeature(content);
		} catch (ContentNullException e) {			
			System.out.println("feature content cannot be null");
		}
		  featureManager.registerBasisFeature(feature);
	  }
	  return feature;
  }

  
  public Object getContent(){
	  return this.content;
  }

 public int getOwnLabel(){
	 return this.label;
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
		else
		return 0;	
	}
	
@Override
public String toString(){	
	return this.content.toString();
}


@Override
public String printMySelfRecursive(int level) {
	String line="";
	for(int i=0;i<level;i++)
		line+="\t";
	line+=this.content.toString();
	return line;
}

public String printMySelfRecursiveInt(int level) {
	String line="";
	for(int i=0;i<level;i++)
		line+="\t";
	line+=this.label;
	return line;
}



@Override
public void calculateComplexity() {
	if (this.content!="?"&&this.content!="empty")
	this.complexity=1;
	else
	this.complexity=0;
}


}

@SuppressWarnings("serial")
class ContentNullException extends Exception{};
