package sql_feature_extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.HashMultiset;

import feature_management.BasisFeature;
import feature_management.Feature;
import feature_management.FeatureVector;
import feature_management.OrderedListAsFeature;
import feature_management.ParentChildRelation;
import feature_management.UnorderedBagAsFeature;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.Union;
import sql_query_regularization.QueryToolBox;

/**
 * extracts simple features out of a query
 * @author tingxie
 *
 */
public class SimpleFeatureExtractor {



	/**
	 * extracts feature vectors out of a query
	 * a list(UNION components) of HashMap(functional sections like SELECT) of feature vectors
	 * if considerUnion parameter is set to false, then all features from UNION component will be merged into one
	 * if secFeatureSeparate is set to true, then features from different functional sections will have its own feature vector
	 * represented as a HashMap<FuncSec,FeatureVector>
	 * @param select
	 * @return
	 */	   
	public static List<HashMap<FuncSec,FeatureVector>> featureExtractFromSelectBody(SelectBody body,HashSet<FuncSec> secList,boolean considerUnion,boolean secFeatureSeparate){

		List<HashMap<FuncSec,FeatureVector>> result=new ArrayList<HashMap<FuncSec,FeatureVector>>();
		if(body instanceof PlainSelect){
			result.addAll(featureExtractFromPlainSelect((PlainSelect) body,secList,considerUnion,secFeatureSeparate));
		}
		else {
			Union u=(Union) body;
			List<PlainSelect> plist=u.getPlainSelects();
			if(considerUnion){
				for (PlainSelect ps: plist){
					result.addAll(featureExtractFromPlainSelect(ps,secList,considerUnion,secFeatureSeparate));  
				}
			}
			//we just merge feature vectors from all plain selects
			else {
				HashMap<FuncSec,FeatureVector> mergedResult=new HashMap<FuncSec,FeatureVector>();
				for (PlainSelect ps: plist){
					HashMap<FuncSec,FeatureVector> mresult=featureExtractFromPlainSelect(ps,secList,considerUnion,secFeatureSeparate).get(0);
					for (Entry<FuncSec, FeatureVector> en: mresult.entrySet()) {
						FeatureVector vec=mergedResult.get(en.getKey());
						if(vec==null)
							mergedResult.put(en.getKey(), en.getValue());
						else
							vec.addWholeFeatureVectorIn(en.getValue());
					}
				}
				result.add(mergedResult);
			}
		}
		return result;
	}

	/**
	 * features from sub-select will create a separate feature vector, a copy of this feature vector 
	 * will be merged to its parent
	 * @param ps
	 * @return
	 */
	public static  List<HashMap<FuncSec,FeatureVector>> featureExtractFromPlainSelect(PlainSelect ps,HashSet<FuncSec> secList,boolean considerUnion,boolean secFeatureSeparate){
		//initialize
		List<HashMap<FuncSec,FeatureVector>> result=initializeResult(secFeatureSeparate, secList);
		//begin extracting
		//dealing with select features
		List<SelectItem> slist=ps.getSelectItems();

		//if user want select features as features
		if(secList.contains(FuncSec.SELECT)){
			BasisFeature projectHeader=BasisFeature.createNewBasisFeatureInstance("PROJECT");   

			for (SelectItem sfeature: slist){
				if(sfeature instanceof SelectExpressionItem){
					SelectExpressionItem sexpfeature=(SelectExpressionItem) sfeature;
					Expression exp=sexpfeature.getExpression();
					FuncSec fs;
					if(secFeatureSeparate)
						fs=FuncSec.SELECT;
					else
						fs=FuncSec.UNIVERSAL;

					if(exp instanceof Function){
						int ID=BasisFeature.createNewBasisFeatureInstance(exp.toString()).getOwnLabel();					
						addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(projectHeader.getOwnLabel(),ID).getOwnLabel(), result, fs);											

//						Function f=(Function) exp;
//						List<Integer> flist=getFeatureFromFunction(f);
//						for (Integer ID: flist){
//							addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(projectHeader.getOwnLabel(),ID).getOwnLabel(), result, fs);												
//						}
					}					
					else{
						int ID=BasisFeature.createNewBasisFeatureInstance(exp.toString()).getOwnLabel();					
						addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(projectHeader.getOwnLabel(),ID).getOwnLabel(), result, fs);											
					}					
				}
				else {
					Feature feature=BasisFeature.createNewBasisFeatureInstance(sfeature.toString());
					FuncSec fs;
					if(secFeatureSeparate)
						fs=FuncSec.SELECT;
					else
						fs=FuncSec.UNIVERSAL;
					addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(projectHeader.getOwnLabel(),feature.getOwnLabel()).getOwnLabel(), result, fs);											
				}
			}
		}

		//dealing with having 
		Expression having=ps.getHaving();
		//if user want predicates as features
		if(having!=null){
			result=crossMergeVectors(result,featureExtractFromBooleanExpression(having, secList, considerUnion, secFeatureSeparate));			
		}

		//dealing with FromItem
		FromItem from=ps.getFromItem();
		result=crossMergeVectors(result,featureExtractFromFromItem(from,secList,considerUnion,secFeatureSeparate));	

		//dealing with Joins
		List<Join> jlist=ps.getJoins();
		if(jlist!=null){
			for (Join j: jlist){
				result=crossMergeVectors(result,featureExtractFromJoin(j,secList,considerUnion,secFeatureSeparate,ps.getFromItem()));	
			}
		}


		//dealing with where
		Expression where=ps.getWhere();
		if(where!=null){
			result=crossMergeVectors(result,featureExtractFromBooleanExpression(where,secList,considerUnion,secFeatureSeparate));
		}

		//dealing with Limit and TOP and orderby
		//order by or limit alone is not meaningful
		Limit limit=ps.getLimit();
		Top top=ps.getTop();
		String s;
		if(limit!=null)
			s=Long.toString(limit.getRowCount());
		else if (top!=null)
			s=Long.toString(top.getRowCount());
		else
			s=null;

		if(s!=null&&ps.getOrderByElements()!=null&&secList.contains(FuncSec.ORDERBY)){
			BasisFeature orderbyHeader=BasisFeature.createNewBasisFeatureInstance("ORDER BY");
			List olist=ps.getOrderByElements();
			for (Object obj:olist){
				OrderByElement oele=(OrderByElement) obj;
				Expression exp=oele.getExpression();
				boolean isAscend=oele.isAsc();
				//add orderby expressions and ascending in
				FuncSec fs;
				if(secFeatureSeparate)
					fs=FuncSec.ORDERBY;
				else
					fs=FuncSec.UNIVERSAL;	
				String content;
				if(isAscend)
					content="ASCEND ON ";
				else
					content="DESCEND ON ";
				content+=exp.toString();
				int ID=BasisFeature.createNewBasisFeatureInstance(content).getOwnLabel();
				//add in expression
				addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(orderbyHeader.getOwnLabel(), ID).getOwnLabel(), result, fs);
				//add in expression with ascend property
				//addFeatureInResult(BasisFeature.createNewBasisFeatureInstance(content).getOwnLabel(), result, fs);				
			}

			//add limit feature in
			FuncSec fs;
			if(secFeatureSeparate)
				fs=FuncSec.ORDERBY;
			else
				fs=FuncSec.UNIVERSAL;
            BasisFeature bfeature=BasisFeature.createNewBasisFeatureInstance("LIMIT");
     
			ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(bfeature.getOwnLabel(), BasisFeature.createNewBasisFeatureInstance(s).getOwnLabel());
			addFeatureInResult(vfeature.getOwnLabel(), result, fs);											
		}

		//dealing with group by
		List<Expression> groupbylist=ps.getGroupByColumnReferences();
		if(groupbylist!=null&&secList.contains(FuncSec.GROUPBY)){
			BasisFeature groupbyHeader=BasisFeature.createNewBasisFeatureInstance("GROUP BY");
			for (Expression exp: groupbylist){
				BasisFeature bfeature=BasisFeature.createNewBasisFeatureInstance(exp.toString());

				FuncSec fs;
				if(secFeatureSeparate)
					fs=FuncSec.GROUPBY;
				else
					fs=FuncSec.UNIVERSAL;

				addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(groupbyHeader.getOwnLabel(),bfeature.getOwnLabel()).getOwnLabel(), result, fs);
			}
		}

		//dealing with distinct
		Distinct distinct=ps.getDistinct();
		if(distinct!=null&&secList.contains(FuncSec.GROUPBY)){
			BasisFeature distinctHeader=BasisFeature.createNewBasisFeatureInstance("DISTINCT ON");
			List<SelectItem> onlist= distinct.getOnSelectItems();
			if(onlist!=null){
				for (SelectItem sfeature: onlist){
					BasisFeature bfeature=BasisFeature.createNewBasisFeatureInstance(sfeature.toString());
					FuncSec fs;
					if(secFeatureSeparate)
						fs=FuncSec.GROUPBY;
					else
						fs=FuncSec.UNIVERSAL;

					addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(distinctHeader.getOwnLabel(),bfeature.getOwnLabel()).getOwnLabel(), result, fs);
				}
			}
		}

		return result;
	}


	private static List<HashMap<FuncSec,FeatureVector>> featureExtractFromBooleanExpression(Expression exp,HashSet<FuncSec> secList,boolean considerUnion,boolean secFeatureSeparate){
		if(exp instanceof AndExpression){
			AndExpression and=(AndExpression) exp;
			Expression left=and.getLeftExpression();
			Expression right=and.getRightExpression();
			List<HashMap<FuncSec,FeatureVector>> leftresult=featureExtractFromBooleanExpression(left,secList,considerUnion,secFeatureSeparate);
			List<HashMap<FuncSec,FeatureVector>> rightresult=featureExtractFromBooleanExpression(right,secList,considerUnion,secFeatureSeparate);						
			return crossMergeVectors(leftresult,rightresult);
		}
		else if (exp instanceof OrExpression){
			OrExpression or=(OrExpression) exp;
			Expression left=or.getLeftExpression();
			Expression right=or.getRightExpression();
			List<HashMap<FuncSec,FeatureVector>> leftresult=featureExtractFromBooleanExpression(left,secList,considerUnion,secFeatureSeparate);
			List<HashMap<FuncSec,FeatureVector>> rightresult=featureExtractFromBooleanExpression(right,secList,considerUnion,secFeatureSeparate);
			return crossMergeVectors(leftresult,rightresult);
		}
		else if (exp instanceof EqualsTo||exp instanceof NotEqualsTo){
			List<HashMap<FuncSec,FeatureVector>> result=initializeResult(secFeatureSeparate, secList);
			if(secList.contains(FuncSec.PREDICATE)){
				BinaryExpression bexp=(BinaryExpression) exp;
				Expression left=bexp.getLeftExpression();
				Expression right=bexp.getRightExpression();

				FuncSec fs;
				if(secFeatureSeparate)
					fs=FuncSec.PREDICATE;
				else
					fs=FuncSec.UNIVERSAL;

				BasisFeature root=BasisFeature.createNewBasisFeatureInstance(bexp.getClass().getSimpleName());
				BasisFeature predicateHeader=BasisFeature.createNewBasisFeatureInstance("Predicate");

				if(left instanceof Function){
					
					int ID=BasisFeature.createNewBasisFeatureInstance(left.toString()).getOwnLabel();					
					addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(),ID).getOwnLabel(), result, fs);											

//					Function f=(Function) left;
//					List<Integer> IDs=getFeatureFromFunction(f);
////					for (Integer ID:IDs){
////						ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), ID);
////						addFeatureInResult(vfeature.getOwnLabel(), result, fs);
////					}
//					HashMultiset<Integer> labelset=HashMultiset.create();
//					labelset.add(root.getOwnLabel());
//					labelset.add(IDs.get(IDs.size()-1));
//					UnorderedBagAsFeature hleft=UnorderedBagAsFeature.createNewUnorderedBagAsFeatureInstance(labelset);
//					ParentChildRelation vleft=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hleft.getOwnLabel());
//					addFeatureInResult(vleft.getOwnLabel(), result, fs);
				}
				else{
					if(left instanceof Column){
						BasisFeature leftbFeature=BasisFeature.createNewBasisFeatureInstance(left.toString());	
						HashMultiset<Integer> labelset=HashMultiset.create();
						//consider together with its operator
						labelset.add(root.getOwnLabel());
						labelset.add(leftbFeature.getOwnLabel());
						UnorderedBagAsFeature hleft=UnorderedBagAsFeature.createNewUnorderedBagAsFeatureInstance(labelset);
						ParentChildRelation vleft=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hleft.getOwnLabel());
						addFeatureInResult(vleft.getOwnLabel(), result, fs);
					}					
				}	

				if(right instanceof Function){
					int ID=BasisFeature.createNewBasisFeatureInstance(right.toString()).getOwnLabel();					
					addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(),ID).getOwnLabel(), result, fs);											

//					Function f=(Function) right;
//					List<Integer> IDs=getFeatureFromFunction(f);
////					for (Integer ID:IDs){
////						ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), ID);
////						addFeatureInResult(vfeature.getOwnLabel(), result, fs);
////					}
//					HashMultiset<Integer> labelset=HashMultiset.create();
//					labelset.add(root.getOwnLabel());
//					labelset.add(IDs.get(IDs.size()-1));
//					UnorderedBagAsFeature hright=UnorderedBagAsFeature.createNewUnorderedBagAsFeatureInstance(labelset);
//					ParentChildRelation vright=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hright.getOwnLabel());
//					addFeatureInResult(vright.getOwnLabel(), result, fs);
				}
				else{
					if(right instanceof Column){
						BasisFeature rightbFeature=BasisFeature.createNewBasisFeatureInstance(right.toString());	
						HashMultiset<Integer> labelset=HashMultiset.create();
						labelset.add(root.getOwnLabel());
						labelset.add(rightbFeature.getOwnLabel());
						UnorderedBagAsFeature hright=UnorderedBagAsFeature.createNewUnorderedBagAsFeatureInstance(labelset);
						ParentChildRelation vright=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hright.getOwnLabel());
						addFeatureInResult(vright.getOwnLabel(), result, fs);
					}
				}
				
				//consider the predicate as a whole		
					BasisFeature leftbFeature=BasisFeature.createNewBasisFeatureInstance(left.toString());
					BasisFeature rightbFeature=BasisFeature.createNewBasisFeatureInstance(right.toString());	
					HashMultiset<Integer> labelset=HashMultiset.create();
					labelset.add(rightbFeature.getOwnLabel());
					labelset.add(leftbFeature.getOwnLabel());
					UnorderedBagAsFeature hleft=UnorderedBagAsFeature.createNewUnorderedBagAsFeatureInstance(labelset);
					ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(root.getOwnLabel(), hleft.getOwnLabel());
					ParentChildRelation vleft=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), vfeature.getOwnLabel());
					addFeatureInResult(vleft.getOwnLabel(), result, fs);
				

			}
			return result;
		}
		else if (exp instanceof GreaterThan||exp instanceof GreaterThanEquals||exp instanceof MinorThan||exp instanceof MinorThanEquals){
			List<HashMap<FuncSec,FeatureVector>> result=initializeResult(secFeatureSeparate, secList);
			if(secList.contains(FuncSec.PREDICATE)){
				BinaryExpression bexp=(BinaryExpression) exp;
				Expression left=bexp.getLeftExpression();
				Expression right=bexp.getRightExpression();

				FuncSec fs;
				if(secFeatureSeparate)
					fs=FuncSec.PREDICATE;
				else
					fs=FuncSec.UNIVERSAL;

				BasisFeature predicateHeader=BasisFeature.createNewBasisFeatureInstance("Predicate");
				BasisFeature root=BasisFeature.createNewBasisFeatureInstance(bexp.getClass().getSimpleName());

				if(left instanceof Function){
					int ID=BasisFeature.createNewBasisFeatureInstance(left.toString()).getOwnLabel();					
					addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(),ID).getOwnLabel(), result, fs);											

//					Function f=(Function) left;
//					List<Integer> IDs=getFeatureFromFunction(f);
////					for (Integer ID:IDs){
////						ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), ID);
////						addFeatureInResult(vfeature.getOwnLabel(), result, fs);
////					}
//					ArrayList<Integer> labellist=new ArrayList<Integer>();
//					labellist.add(IDs.get(IDs.size()-1));
//					labellist.add(root.getOwnLabel());				
//					OrderListAsFeature hleft=OrderListAsFeature.createNewOrderListAsFeatureInstance(labellist);
//					ParentChildRelation vleft=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hleft.getOwnLabel());
//					addFeatureInResult(vleft.getOwnLabel(), result, fs);
				}
				else{
					if(left instanceof Column){
						BasisFeature leftbFeature=BasisFeature.createNewBasisFeatureInstance(left.toString());	
						ArrayList<Integer> labellist=new ArrayList<Integer>();
						labellist.add(leftbFeature.getOwnLabel());
						labellist.add(root.getOwnLabel());				
						OrderedListAsFeature hleft=OrderedListAsFeature.createNewOrderListAsFeatureInstance(labellist);
						ParentChildRelation vleft=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hleft.getOwnLabel());
						addFeatureInResult(vleft.getOwnLabel(), result, fs);
					}
				}

				if(right instanceof Function){
					int ID=BasisFeature.createNewBasisFeatureInstance(right.toString()).getOwnLabel();					
					addFeatureInResult(ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(),ID).getOwnLabel(), result, fs);											

//					Function f=(Function) right;
//					List<Integer> IDs=getFeatureFromFunction(f);
////					for (Integer ID:IDs){
////						ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), ID);
////						addFeatureInResult(vfeature.getOwnLabel(), result, fs);
////					}
//					ArrayList<Integer> labellist=new ArrayList<Integer>();
//					labellist.add(root.getOwnLabel());		
//					labellist.add(IDs.get(IDs.size()-1));		
//					OrderListAsFeature hright=OrderListAsFeature.createNewOrderListAsFeatureInstance(labellist);
//					ParentChildRelation vright=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hright.getOwnLabel());
//					addFeatureInResult(vright.getOwnLabel(), result, fs);
				}
				else{
					if(right instanceof Column){
						BasisFeature rightbFeature=BasisFeature.createNewBasisFeatureInstance(right.toString());	
						ArrayList<Integer> labellist=new ArrayList<Integer>();
						labellist.add(root.getOwnLabel());		
						labellist.add(rightbFeature.getOwnLabel());		
						OrderedListAsFeature hright=OrderedListAsFeature.createNewOrderListAsFeatureInstance(labellist);
						ParentChildRelation vright=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), hright.getOwnLabel());
						addFeatureInResult(vright.getOwnLabel(), result, fs);
					}
				}
				
				//consider the predicate as a whole

					BasisFeature leftbFeature=BasisFeature.createNewBasisFeatureInstance(left.toString());
					BasisFeature rightbFeature=BasisFeature.createNewBasisFeatureInstance(right.toString());	
					ArrayList<Integer> labellist=new ArrayList<Integer>();
					labellist.add(leftbFeature.getOwnLabel());
					labellist.add(rightbFeature.getOwnLabel());
					OrderedListAsFeature hleft=OrderedListAsFeature.createNewOrderListAsFeatureInstance(labellist);
					ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(root.getOwnLabel(), hleft.getOwnLabel());
					ParentChildRelation vleft=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), vfeature.getOwnLabel());
					addFeatureInResult(vleft.getOwnLabel(), result, fs);
								
			}
			return result;		
		}
		else if (exp instanceof ExistsExpression){
			ExistsExpression exists=(ExistsExpression) exp;
			SubSelect sub=(SubSelect) exists.getRightExpression();
			SelectBody body=sub.getSelectBody();
			return featureExtractFromSelectBody(body,secList,considerUnion,secFeatureSeparate);        								
		}
		else {
			List<HashMap<FuncSec,FeatureVector>> result=initializeResult(secFeatureSeparate, secList);
			if(secList.contains(FuncSec.PREDICATE)){

				BasisFeature bfeature=BasisFeature.createNewBasisFeatureInstance(exp.toString());
				BasisFeature predicateHeader=BasisFeature.createNewBasisFeatureInstance("Predicate");
				ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(predicateHeader.getOwnLabel(), bfeature.getOwnLabel());

				FuncSec fs;
				if(secFeatureSeparate)
					fs=FuncSec.PREDICATE;
				else
					fs=FuncSec.UNIVERSAL;

				addFeatureInResult(vfeature.getOwnLabel(), result, fs);
			}
			return result;
		}
	}

	private static List<HashMap<FuncSec,FeatureVector>> featureExtractFromFromItem(FromItem from,HashSet<FuncSec> secList,boolean considerUnion,boolean secFeatureSeparate){
		if(from instanceof SubSelect){
			SubSelect sub=(SubSelect) from;
			SelectBody body=sub.getSelectBody();
			return featureExtractFromSelectBody(body,secList,considerUnion,secFeatureSeparate);									
		}
		//ignore subjoin case
		else if(from instanceof SubJoin){			
			SubJoin subj=(SubJoin) from;
			FromItem left=subj.getLeft();
			Join j=subj.getJoin();
			List<HashMap<FuncSec,FeatureVector>> leftresult=featureExtractFromFromItem(left, secList, considerUnion, secFeatureSeparate);
			List<HashMap<FuncSec,FeatureVector>> rightresult=featureExtractFromJoin(j, secList, considerUnion, secFeatureSeparate,left);
			return crossMergeVectors(leftresult,rightresult);
		}
		else {
			List<HashMap<FuncSec,FeatureVector>> result=initializeResult(secFeatureSeparate, secList);
			if(secList.contains(FuncSec.FROM)){
				BasisFeature header=BasisFeature.createNewBasisFeatureInstance("FROM");
				BasisFeature feature=BasisFeature.createNewBasisFeatureInstance(from.toString());
				ParentChildRelation vfeature=ParentChildRelation.createNewParentChildRelationInstance(header.getOwnLabel(),feature.getOwnLabel());

				FuncSec fs;
				if(secFeatureSeparate)
					fs=FuncSec.FROM;
				else
					fs=FuncSec.UNIVERSAL;
				addFeatureInResult(vfeature.getOwnLabel(), result, fs);
			}
			return result;
		}	
	}

	private static List<HashMap<FuncSec,FeatureVector>> featureExtractFromJoin(Join j,HashSet<FuncSec> secList,boolean considerUnion,boolean secFeatureSeparate,FromItem start){
		List<HashMap<FuncSec,FeatureVector>> result=featureExtractFromFromItem(j.getRightItem(),secList,considerUnion,secFeatureSeparate);
		Expression onExp=j.getOnExpression();
		if(onExp!=null)
			result=crossMergeVectors(result,featureExtractFromBooleanExpression(onExp,secList,considerUnion,secFeatureSeparate));

		List<Column> usinglist=j.getUsingColumns();
		if(usinglist!=null){
			Expression using=QueryToolBox.parseUsing(usinglist, j, start);
			result=crossMergeVectors(result,featureExtractFromBooleanExpression(using,secList,considerUnion,secFeatureSeparate));
		}
		return result;
	}

	public static List<HashMap<FuncSec,FeatureVector>> initializeResult(boolean secFeatureSeparate,HashSet<FuncSec> secList){
		List<HashMap<FuncSec,FeatureVector>> result=new ArrayList<HashMap<FuncSec,FeatureVector>>();
		HashMap<FuncSec,FeatureVector> map=new HashMap<FuncSec,FeatureVector>();
		if(secFeatureSeparate){
			for(FuncSec fs:secList){
				FeatureVector vec=new FeatureVector();				
				map.put(fs,vec);
			}
		}
		else{
			FeatureVector vec=new FeatureVector();
			map.put(FuncSec.UNIVERSAL, vec);
		}
		result.add(map);
		return result;
	}

	public static List<HashMap<FuncSec,FeatureVector>> addFeatureInResult(int ID,List<HashMap<FuncSec,FeatureVector>> result,FuncSec fs){
		for (HashMap<FuncSec,FeatureVector> map: result){
			FeatureVector vec=map.get(fs);
			vec.addOneFeatureIn(ID);
		}
		return result;
	}

	/**
	 * need to make sure two input lists are non-empty
	 * @param left
	 * @param right
	 * @return
	 */
	public static List<HashMap<FuncSec,FeatureVector>> crossMergeVectors(List<HashMap<FuncSec,FeatureVector>> left,List<HashMap<FuncSec,FeatureVector>> right){
		List<HashMap<FuncSec,FeatureVector>> newresult=new ArrayList<HashMap<FuncSec,FeatureVector>>();
		for (HashMap<FuncSec,FeatureVector> leftvec:left)
			for(HashMap<FuncSec,FeatureVector> rightvec:right){
				HashMap<FuncSec,FeatureVector> map=new HashMap<FuncSec,FeatureVector>();
				//for left
				for (Entry<FuncSec, FeatureVector> en: leftvec.entrySet()) {
					FeatureVector vec=map.get(en.getKey());
					if(vec==null)
						map.put(en.getKey(), en.getValue());
					else
						vec.addWholeFeatureVectorIn(en.getValue());
				}
				//for right
				for (Entry<FuncSec, FeatureVector> en: rightvec.entrySet()) {
					FeatureVector vec=map.get(en.getKey());
					if(vec==null)
						map.put(en.getKey(), en.getValue());
					else
						vec.addWholeFeatureVectorIn(en.getValue());
				}
				newresult.add(map);
			}

		return newresult;
	}

	public static List<Integer> getFeatureFromFunction(Function f){
		List<Integer> result=new ArrayList<Integer>();
		String fname=f.getName();
		BasisFeature header=BasisFeature.createNewBasisFeatureInstance(fname);
		ExpressionList explist=f.getParameters();
		if(explist!=null){
			List<Expression> elist=explist.getExpressions();
			HashMultiset<Integer> labelset=HashMultiset.create();			
			for(Expression exp: elist){
				if(exp instanceof Function){
					Function ff=(Function) exp;
					List<Integer> subresult=getFeatureFromFunction(ff);
					result.addAll(subresult);
					//add the last one as its wrapper feature
					labelset.add(subresult.get(subresult.size()-1));
				}
				else if(exp instanceof Column){	
					labelset.add(BasisFeature.createNewBasisFeatureInstance(exp.toString()).getOwnLabel());
				}
			}

			//once sibling ID set is ready
			if(!labelset.isEmpty()){
				UnorderedBagAsFeature hfeature=UnorderedBagAsFeature.createNewUnorderedBagAsFeatureInstance(labelset);
				if(hfeature!=null){
					result.add(ParentChildRelation.createNewParentChildRelationInstance(header.getOwnLabel(), hfeature.getOwnLabel()).getOwnLabel());
				}
				else{
					int ID=ParentChildRelation.createNewParentChildRelationInstance(header.getOwnLabel(), labelset.iterator().next()).getOwnLabel();
					result.add(ID);
				}
			}
			else {
				result.add(header.getOwnLabel());
			}
		}
		else
			result.add(header.getOwnLabel());

		return result;
	}
}
