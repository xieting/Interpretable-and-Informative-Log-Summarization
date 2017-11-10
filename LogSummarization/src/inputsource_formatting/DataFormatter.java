package inputsource_formatting;

import java.sql.Timestamp;

public interface DataFormatter {
   public String getSelectQueryFromLine(String line);
   public Timestamp getTimeStampFromLine(String line);
}
