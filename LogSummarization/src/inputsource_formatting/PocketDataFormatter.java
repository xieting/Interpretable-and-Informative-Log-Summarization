package inputsource_formatting;

import java.io.StringReader;
import java.sql.Timestamp;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

public class PocketDataFormatter implements DataFormatter{	
	
	@Override
    public String getSelectQueryFromLine(String line){
    	String[] tokens=line.split("~");
    	try {
			Statement stmt=getStatementFromQuery(tokens[0]);
			if(stmt!=null&&stmt instanceof Select)
				return tokens[0];
			else
				return null;
		} catch (ParseException e) {
			return null;
		}
    }
	
	/**
	 * for pocket data
	 * @return
	 * @throws ParseException 
	 */
	public static Statement getStatementFromQuery(String newline) throws ParseException{
		Statement stmt;
			stmt = (new CCJSqlParser(new StringReader(newline))).Statement();
 return stmt;

	}

	@Override
	public Timestamp getTimeStampFromLine(String line) {
		String[] tokens=line.split("~");
		long timestamp=Long.parseLong(tokens[1]);
		return new Timestamp(timestamp);
	}
}
