package inputsource_formatting;

import java.io.StringReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

public class BankDataFormatter implements DataFormatter{
	public int notAQueryCount=0;
	public int parsingErrorCount=0;
	public int notASelectCount=0;
	public int parsedSelectCount=0;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss zzz");
	
	@Override
	public String getSelectQueryFromLine(String line) {
		String[] tokens=line.split(";");
		String newline=tokens[tokens.length-1].trim();
		if (newline.charAt(0)=='N'){
			this.notAQueryCount++;
			return null;
		}
		else if (newline.charAt(0)=='P'){
			this.parsingErrorCount++;
			return null;
		}		
		else{										
			Statement stmt= getStatementFromQuery(newline);
			if (stmt!=null&&stmt instanceof Select){
				this.parsedSelectCount++;
				newline=newline.replaceAll("'{1}.{62}'{1}", "'*'");
				return newline;
			}
			else if (stmt!=null){
				this.notASelectCount++;
				return null;
			}
			else{
				this.parsingErrorCount++;
				return null;
			}
		}
	}
	
	/**
	 * for bank data
	 * @return
	 */
	public static Statement getStatementFromQuery(String newline){
		Statement stmt;
		try {
			//do something to clean the line
			//newline=newline.replace("\'\'", "?");
			//newline=newline.replace("Separator,", "");
			stmt = (new CCJSqlParser(new StringReader(newline))).Statement();
 return stmt;

		} catch (ParseException e) {
			if (newline.charAt(0)=='S'||newline.charAt(0)=='s'){
				System.out.println("invalid sql query: "+newline);
				e.printStackTrace();
			}
			return null;
		}
		catch(NumberFormatException e1){
			if (newline.charAt(0)=='S'||newline.charAt(0)=='s')
				System.out.println("number format error sql line: "+newline);
			return null;
		}
	}

	@Override
	public Timestamp getTimeStampFromLine(String line) {
		String[] tokens=line.split(";");
		Date d = null;
		try {
			d = dateFormat.parse(tokens[0]);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}		
		return new Timestamp(d.getTime());
	}

}
