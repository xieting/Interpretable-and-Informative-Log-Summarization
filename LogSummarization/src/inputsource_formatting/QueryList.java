package inputsource_formatting;

import java.util.ArrayList;

import net.sf.jsqlparser.statement.Statement;

public class QueryList {
   public ArrayList<Statement> content;
   public ArrayList<Integer> multiplicity;
   
   public QueryList(ArrayList<Statement> content,ArrayList<Integer> multiplicity){
	  this.content=content;
	  this.multiplicity=multiplicity;
   }
   
}
