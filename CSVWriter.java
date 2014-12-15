import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import com.google.common.eventbus.*;

class status {
	srvInfo item;
	CSVWriter writer;
	public status(CSVWriter w) {
		item = new srvInfo();
		writer=w;
	}
	@Subscribe
	public void recvStatus(srvInfo in){
		item = in;
		item.toString();
		writer.write(item);
	}
}
public class CSVWriter implements Runnable{
	private EventBus eb;
	private Writer out;
	private CSVPrinter printer;
	public CSVWriter(EventBus eventbus){
		eb = eventbus;
		eb.register(new status(this));
		try {
			out = new FileWriter("scores.csv",true);
			printer = new CSVPrinter(out,CSVFormat.EXCEL); // .withHeader("Server Address","Server Name","Address","Host Name","User Claimed Server Name","Claimed Date/Time","Correct Date/Time","Is Reachable","HTTP Up","HTTP Status Code","Web Server","Powered By")
			printer.printRecord("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	public void write(srvInfo item){
		try {
			printer.printRecord(item.serverAddress,item.serverName,item.address,item.hostName,item.claimedHostName,item.dateTime,item.correctDateTime,item.isReachable,item.HTTPWorking,item.httpStatusCode,item.websrv,item.poweredBy);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			printer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
