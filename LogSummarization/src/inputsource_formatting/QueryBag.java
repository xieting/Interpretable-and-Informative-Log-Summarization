package inputsource_formatting;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map.Entry;

public class QueryBag {
	public Timestamp start;
	public Timestamp end;
	
	public HashMap<String,Integer> content=new HashMap<String,Integer>();  
	
	public void addToBag(String query,Timestamp time){
		Integer multiplicity=this.content.get(query);
		if(multiplicity==null)
			this.content.put(query, 1);
		else
			this.content.put(query, multiplicity+1);
		//record the timestamp
		if(start==null)
			start=time;
		else{
			if(time.before(start))
				start=time;
		}
		
		if(end==null)
			end=time;
		else{
			if(time.after(end))
				end=time;
		}
	}

	public void mergeToMe(QueryBag input){
		for(Entry<String, Integer> en: input.content.entrySet()){		   
			Integer yourmultiplicity=en.getValue();
			String query=en.getKey();
			Integer mymultiplicity=this.content.get(query);	   
			if(mymultiplicity==null)
				this.content.put(query,yourmultiplicity);
			else
				this.content.put(query, mymultiplicity+yourmultiplicity); 
		}
		//merge time
		if(this.start==null)
			this.start=input.start;
		else if(input.start.before(this.start))
			this.start=input.start;
		if(this.end==null)
			this.end=input.end;
		else if(input.end.after(this.end))
			this.end=input.end;				
	}
}
