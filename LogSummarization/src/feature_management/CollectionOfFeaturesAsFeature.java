package feature_management;

import java.util.Collection;


/**
 * represents a collection of features or a group of siblings
 * @author Ting Xie
 *
 */
public abstract class CollectionOfFeaturesAsFeature extends Feature{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6571804540351875588L;

	public abstract Collection<Integer> getHorizontalList();
}
