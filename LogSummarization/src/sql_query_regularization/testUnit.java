package sql_query_regularization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;

public class testUnit {

	public static void main(String[] args) {
		PrintWriter wrr = null;	
		String path="/Users/tingxie/Documents/bank_data/";
		File[] files=new File(path).listFiles();
		try {
			wrr=new PrintWriter(path+"testout");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
//
//		for (File f: files){
//			if (f.getName().contains("_")&&f.getName().endsWith(".txt")){
//				List<HashSet<String>> originalList=new ArrayList<HashSet<String>>();
//				List<HashSet<String>> firstList=new ArrayList<HashSet<String>>();
//				List<HashSet<String>> secondList=new ArrayList<HashSet<String>>();
//				List<String> contentlist=new ArrayList<String>();
//				List<String> transformedList=new ArrayList<String>();
//				List<String> filenamelist=new ArrayList<String>();
//				
//				BufferedReader br;				 						
//				System.out.println("begin predicate decomposing file"+"  "+f.getName());
//				String line = null;							
//				try{
//					br = new BufferedReader(new FileReader(f));
//					while ((line = br.readLine()) != null) {
//						String[] tokens=line.split(";");
//						try {
//							Statement stmt = (new CCJSqlParser(new StringReader(tokens[1]))).Statement();
//							Select select=(Select)stmt;
//							contentlist.add(select.toString());
//							originalList.add(turnSelectBodyIntoHashSet(select.getSelectBody()));
//							Select firstS=CombinedRegularizer.regularize(select);
//							firstList.add(turnSelectBodyIntoHashSet(firstS.getSelectBody()));
//							transformedList.add(firstS.toString());
//							filenamelist.add(f.getName());
//						} catch (ParseException e) {
//							e.printStackTrace();
//						}
//					}
//					br.close();
//				}									
//				catch (IOException e) {			
//					e.printStackTrace();
//				}
//
//				try{
//					br = new BufferedReader(new FileReader(f));
//					while ((line = br.readLine()) != null) {
//						String[] tokens=line.split(";");
//						try {
//							Statement stmt = (new CCJSqlParser(new StringReader(tokens[1]))).Statement();
//							Select select=(Select)stmt;
//							Select secondS=CombinedRegularizer.regularize(select);
//							secondList.add(turnSelectBodyIntoHashSet(secondS.getSelectBody()));
//						} catch (ParseException e) {
//							e.printStackTrace();
//						}
//					}
//				}									
//				catch (IOException e) {			
//					e.printStackTrace();
//				}
//
//				if(firstList.size()!=originalList.size()||secondList.size()!=originalList.size()){
//					System.out.println("error");
//				}
//				else{
//					for(int i=0;i<originalList.size();i++){
////						HashSet<String> first=firstList.get(i);
////						HashSet<String> second=secondList.get(i);
////						HashSet<Integer> firstset=new HashSet<Integer>();
////						HashSet<Integer> secondset=new HashSet<Integer>();
////						SelectBody body=contentlist.get(i);
////						for(String s:first)
////							firstset.add(s.length());
////						for(String s:second)
////							secondset.add(s.length());
////
////						if(!firstset.equals(secondset)){
////								System.out.println("-----------");
////								System.out.println(originalList.get(i));
////								System.out.println();
////								System.out.println(firstList.get(i));
////								System.out.println();
////								System.out.println(secondList.get(i));
////								System.out.println("!!!!!!!!!!!!!");
////							
////						}
//						if(Math.abs(contentlist.get(i).toString().length()-transformedList.get(i).toString().length())>10)
//						{
//							wrr.println(filenamelist.get(i));
//							wrr.println("--before--");
//							wrr.println(contentlist.get(i));
//							wrr.println();
//							wrr.println("--after--");
//							wrr.println(transformedList.get(i));
//							wrr.println();
//						}
//						
//					}
//				}
//
//				System.out.println("finished processing file");	
//			}
//		}
//		wrr.close();
         String query="SELECT * FROM takes AS t1 WHERE (t1.grade <> '' OR (SELECT id FROM takes AS t2 WHERE t1.course_id = t2.course_id AND t1.id = t2.id AND t2.grade <> '') IS NULL) AND grade IS NOT NULL";
         		Statement stmt;
			try {
				stmt = (new CCJSqlParser(new StringReader(query))).Statement();
				Select select=(Select)stmt;
			//	Select transformed=CombinedRegularizer.regularize(select);
			//	System.out.println(transformed);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public static HashSet<String> turnSelectBodyIntoHashSet(SelectBody body){
		HashSet<String> result =new HashSet<String>();
		if(body instanceof Union){
			Union u=(Union) body;
			List<PlainSelect> plist=u.getPlainSelects();
			for (PlainSelect ps: plist){
				result.add(ps.toString());
			}
		}
		else
			result.add(body.toString());
		return result;
	}
}
