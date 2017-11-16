package pattern_mixture_summarization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import feature_management.GlobalVariables;

public class NaiveSummary {
	ArrayList<NaiveSummaryEntry> entries;
	double Error;
	HashSet<NaiveSummary> children=new HashSet<NaiveSummary>();
	
	public NaiveSummary(ArrayList<NaiveSummaryEntry> entries,double Error){
       this.entries=entries;
       this.Error=Error;
	}
	
	public void addAsChild(NaiveSummary child){
		this.children.add(child);
	}
	
	public double getError(){
		return this.Error;
	}
	
	public ArrayList<NaiveSummaryEntry> getContent(){
		return this.entries;
	}
	
	@Override
	public String toString(){
		String line="";
		for (int i=0;i<this.entries.size();i++){
			NaiveSummaryEntry entry=this.entries.get(i);
			if(entry.occurrence>1)
			line+=entry.featureID+GlobalVariables.OccurSeparator+entry.occurrence+GlobalVariables.CountSeparator+String.format("%6.6e",entry.marginal)+" , ";
			else
			line+=entry.featureID+GlobalVariables.CountSeparator+String.format("%6.6e",entry.marginal)+" , ";
		}
		line+="\n";
		for (NaiveSummary summary:this.children){
			line+=summary.toString(0)+"\n";
		}
		return line.substring(0, line.length()-1);
	}
	
	private String toString(int offset){
		String line=String.join("", Collections.nCopies(GlobalVariables.parentChildrenSeparator.length()+offset, " "));
		for (int i=0;i<this.entries.size();i++){
			NaiveSummaryEntry entry=this.entries.get(i);
			if(entry.occurrence>1)
			line+=entry.featureID+GlobalVariables.OccurSeparator+entry.occurrence+GlobalVariables.CountSeparator+String.format("%6.6e",entry.marginal)+" , ";
			else
			line+=entry.featureID+GlobalVariables.CountSeparator+String.format("%6.6e",entry.marginal)+" , ";
		}
		line+="\n";
		if(!this.children.isEmpty())
		for (NaiveSummary summary:this.children){
			line+=summary.toString(offset+GlobalVariables.parentChildrenSeparator.length())+"\n";
		}
		return line;	
	}
	
	
	
}
