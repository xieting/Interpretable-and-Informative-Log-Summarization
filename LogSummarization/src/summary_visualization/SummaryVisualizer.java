package summary_visualization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.collect.HashMultiset;

import data_structure.FP_InferenceTree;
import data_structure.Word;
import feature_management.BasisFeature;
import feature_management.Feature;
import feature_management.GlobalVariables;
import feature_management.UnorderedBagAsFeature;
import feature_management.CollectionOfFeaturesAsFeature;
import feature_management.OrderedListAsFeature;
import feature_management.ParentChildRelation;
import feature_management.featureManager;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

public class SummaryVisualizer {
	HashMap<String, ContentWithOpacity> sectionMap;
	double KL;
	int clusterID;
	float threshold;

	public SummaryVisualizer (float threshold,int clusterID,FP_InferenceTree tree,HashMap<Integer,Word> featureMap){
		this.threshold=threshold;
		this.clusterID=clusterID;
		this.sectionMap=new HashMap<String,ContentWithOpacity>();
		this.KL=tree.getNaiveSummaryError();
		HashMap<Word,Integer> frequencyCount=tree.getFrequencyCount();
		float sum=(float)tree.getTotalFeatureSetCount();

		for(Entry<Word,Integer> en: frequencyCount.entrySet()){
			Word w=en.getKey();
			int ID=w.getFeatureID();
			Word ssfeature=featureMap.get(ID);
			ParentChildRelation vfeature=(ParentChildRelation) featureManager.getFeatureFromLabel(ssfeature.getFeatureID());
			int occurrence=ssfeature.getOccurrence();
			BasisFeature bfeature=(BasisFeature) featureManager.getFeatureFromLabel(vfeature.getParentLabel());
			String section=(String) bfeature.getContent();
			Feature content=featureManager.getFeatureFromLabel(vfeature.getChildLabel());

			float opacity=(float)en.getValue()/sum;
			if(opacity>this.threshold){
				ContentWithOpacity cwo=this.sectionMap.get(section);
				if(cwo==null){
					cwo=new ContentWithOpacity();
					this.sectionMap.put(section, cwo);
				}
				if(occurrence>1)
					cwo.sectionContents.add(content.toString()+" : "+occurrence);
				else
					cwo.sectionContents.add(content.toString());

				cwo.sectionOpacities.add(opacity);
			}
		}

	}

	public SummaryVisualizer (float threshold,int clusterID,FP_InferenceTree tree){
		this.threshold=threshold;
		this.clusterID=clusterID;
		this.sectionMap=new HashMap<String,ContentWithOpacity>();
		this.KL=tree.getNaiveSummaryError();
		HashMap<Word,Integer> frequencyCount=tree.getFrequencyCount();
		float sum=(float)tree.getTotalFeatureSetCount();

		for(Entry<Word,Integer> en: frequencyCount.entrySet()){
			Word sfeature=en.getKey();
			ParentChildRelation vfeature=(ParentChildRelation) featureManager.getFeatureFromLabel(sfeature.getFeatureID());
			int occurrence=sfeature.getOccurrence();

			BasisFeature bfeature=(BasisFeature) featureManager.getFeatureFromLabel(vfeature.getParentLabel());
			String section=(String) bfeature.getContent();
			Feature content=featureManager.getFeatureFromLabel(vfeature.getChildLabel());

			float opacity=(float)en.getValue()/sum;
			if(opacity>this.threshold){
				ContentWithOpacity cwo=this.sectionMap.get(section);
				if(cwo==null){
					cwo=new ContentWithOpacity();
					this.sectionMap.put(section, cwo);
				}
				if(occurrence>1)
					cwo.sectionContents.add(featureInterpret(content)+" : "+occurrence);
				else
					cwo.sectionContents.add(featureInterpret(content));

				cwo.sectionOpacities.add(opacity);
			}
		}

	}

	public static String featureInterpret(Feature content){
		if(content instanceof OrderedListAsFeature){
			OrderedListAsFeature hlfeature=(OrderedListAsFeature) content;
			List<Integer> labels=hlfeature.getHorizontalList();
			String line="(";
			if(labels.size()==2){
				String left=featureInterpret(featureManager.getFeatureFromLabel(labels.get(0)));
				String right=featureInterpret(featureManager.getFeatureFromLabel(labels.get(1)));
				String switchedleft=switchOperator(left);
				if(switchedleft.equals(left)){
					line+=left;
					line+=" "+right;
				}
				else{
					line+=right;
					line+=" "+switchedleft;
				}
			}
			else{
			line+=featureInterpret(featureManager.getFeatureFromLabel(labels.get(0)));
			for(int i=1;i<labels.size();i++){
				line+=" "+featureInterpret(featureManager.getFeatureFromLabel(labels.get(i)));
			}
			}
			line+=")";
			return line;
		}
		else if (content instanceof UnorderedBagAsFeature){
			UnorderedBagAsFeature hlfeature=(UnorderedBagAsFeature) content;
			HashMultiset<Integer> labels=hlfeature.getHorizontalList();
			ArrayList<String> contents=turnMultisetLabelsIntoStrings(labels);

			Iterator<String> it=contents.iterator();
			String line="(";
			line+=it.next();
			while(it.hasNext()){
				line+=" "+it.next();
			}
			line+=")";	
			return line;
		}
		else if (content instanceof ParentChildRelation){
			ParentChildRelation vfeature=(ParentChildRelation) content;
			Feature top=featureManager.getFeatureFromLabel(vfeature.getParentLabel());
			Feature bottom=featureManager.getFeatureFromLabel(vfeature.getChildLabel());
			if(top instanceof BasisFeature&& bottom instanceof CollectionOfFeaturesAsFeature&& ((CollectionOfFeaturesAsFeature)bottom).getHorizontalList().size()==2){
				String line="(";
				String operator=featureInterpret(top);
				if(bottom instanceof UnorderedBagAsFeature){
				ArrayList<String> contents=turnMultisetLabelsIntoStrings(((UnorderedBagAsFeature) bottom).getHorizontalList());               
				line+=contents.get(0);	
				line+=operator;
				line+=contents.get(1);	
				}
				else{
					OrderedListAsFeature hfeature=(OrderedListAsFeature) bottom;
					ArrayList<Integer> labels=hfeature.getHorizontalList();
					String left= featureInterpret(featureManager.getFeatureFromLabel(labels.get(0)));
					boolean pass=true;	
					if (left.equalsIgnoreCase("null"))
						pass=false;
					else if(left.startsWith("'")&&left.endsWith("'")){
						pass=false;
					}
					else if(left.startsWith("\"")&&left.endsWith("\"")){
						pass=false;
					}
					else{
						try{
							Integer.parseInt(left);
							pass=false;	 
						}
						catch(NumberFormatException e){
							try{
							Double.parseDouble(left);
							pass=false;	 
							}
							catch(NumberFormatException ee){
								//do nothing
							}											
						}
					}
					if(pass||operator.equals("+")||operator.equals("-")||operator.equals("*")||operator.equals("/")){
						line+=left+" ";
						line+=operator+" ";
						line+=featureInterpret(featureManager.getFeatureFromLabel(labels.get(1)));
					}	
					//switch operands
					else {	
						line+=featureInterpret(featureManager.getFeatureFromLabel(labels.get(1)))+" ";
						line+=switchOperator(operator)+" ";
						line+=left;
					}
				}
				line+=")";	
				return line;
			}
			else{
				return featureInterpret(top)+GlobalVariables.parentChildrenSeparator+featureInterpret(bottom);
			}
		}
		else if (content instanceof BasisFeature){
			if(content.toString().equals(EqualsTo.class.getSimpleName()))
				return "=";
			else if(content.toString().equals(NotEqualsTo.class.getSimpleName()))
				return "!="; 			 
			else if(content.toString().equals(GreaterThan.class.getSimpleName()))
				return">";
			else if(content.toString().equals(GreaterThanEquals.class.getSimpleName())) 
				return">=";
			else if(content.toString().equals(MinorThanEquals.class.getSimpleName())) 
				return"<=";
			else if(content.toString().equals(MinorThan.class.getSimpleName())) 
				return"<";
			else if(content.toString().equals(Addition.class.getSimpleName())) 
				return"+";
			else if(content.toString().equals(Subtraction.class.getSimpleName())) 
				return"-";
			else if(content.toString().equals(Multiplication.class.getSimpleName())) 
				return"*";
			else if(content.toString().equals(Division.class.getSimpleName())) 
				return"/";
			else 
				return content.toString();
		}
		else return content.toString();
	}

	private static String switchOperator(String op){			 
		if(op.equals("<"))
			return">";
		else if(op.equals("<=")) 
			return">=";
		else if(op.equals(">=")) 
			return"<=";
		else if(op.equals(">")) 
			return"<";
		else 
			return op;
	}
	
	public static ArrayList<String> turnMultisetLabelsIntoStrings(HashMultiset<Integer> labels){
		ArrayList<String> contents=new ArrayList<String>();
		for(Integer ID:labels){
			Feature feature=featureManager.getFeatureFromLabel(ID);				
			String c=featureInterpret(feature).trim();
			if(c.length()<=1)
				contents.add(c);
			else if(c.startsWith("'")&&c.endsWith("'")){
				contents.add(c);
			}
			else if(c.startsWith("\"")&&c.endsWith("\"")){
				contents.add(c);
			}
			else if(c.equals("!=")){
				contents.add(c);
			}
			else{
				try{
					Integer.parseInt(c);
					contents.add(c);	 
				}
				catch(NumberFormatException e){
					try{
					Double.parseDouble(c);
					contents.add(c);	 
					}
					catch(NumberFormatException ee){
						contents.add(0,c); 
					}											
				}
			}
		}
		return contents;
	}

	public void Visualize(){
		JFrame f = new JFrame("Summary Error for cluster "+this.clusterID+" is "+this.KL);

		ArrayList<String> sections=new ArrayList<String>();
		List<ArrayList<String>> sectionContents=new ArrayList<ArrayList<String>>();
		List<ArrayList<Float>> sectionOpacities=new ArrayList<ArrayList<Float>>();

		for(Entry<String, ContentWithOpacity> en: sectionMap.entrySet()){
			sections.add(en.getKey());
			sectionContents.add(en.getValue().sectionContents);
			sectionOpacities.add(en.getValue().sectionOpacities);
		}

		JPanel mainPanel=TextRendering.createMainPanel(sections, sectionContents, sectionOpacities);
		f.setContentPane(mainPanel);
		f.pack();
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setVisible(true);
	}      

}
