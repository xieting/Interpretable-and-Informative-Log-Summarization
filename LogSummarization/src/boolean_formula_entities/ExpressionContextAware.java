package boolean_formula_entities;

import net.sf.jsqlparser.expression.Expression;

/**
 * if BooleanNormalClause is under context, you must
 * pass in an object that is context aware
 * @author tingxie
 *
 */
public interface ExpressionContextAware{
   public boolean ifAcrossContext(Expression left,Expression right);
}
