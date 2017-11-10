package boolean_formula_entities;

import java.util.Comparator;

/**
 * used to sort clauses in DNF or CNF form to remove duplicates
 * @author tingxie
 *
 */
public class ClauseSizeComparator implements Comparator<BooleanNormalClause>{

	@Override
	public int compare(BooleanNormalClause o1, BooleanNormalClause o2) {
		if (o1.getSize()>o2.getSize())
			return -1;
		else if (o1.getSize()<o2.getSize())
			return 1;
		else
		return 0;
	}


      
}
