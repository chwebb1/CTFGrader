import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.google.common.eventbus.EventBus;
public class ScheduledServerChecks extends TimerTask {
	private static Iterable<CSVRecord> serverInfo;
	EventBus commands;
	public ScheduledServerChecks(EventBus eb){
		commands=eb;
	}
 
	// Add your task here
	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(1902160583); // brun's constant.
		Reader in = null;
		try {
			in = new FileReader("servers.csv");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.print("Please place servers.csv at: ");
			File here = new File(".");
			System.out.println(here.getAbsolutePath());
		}
		try {
			serverInfo = CSVFormat.EXCEL
					.withHeader("ServerName", "ServerAddress", "ExpectedWebServer",
							"ExpectedPoweredBy").withSkipHeaderRecord(true)
					.parse(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (CSVRecord server : serverInfo) {
			String ServerName = server.get("ServerName");
			String ServerAddress = server.get("ServerAddress");
			String expectedWebSvr = server.get("ExpectedWebServer");
			String expectedPoweredBy = server.get("ExpectedPoweredBy");
			ServerCheck sc = new ServerCheck(ServerName, ServerAddress,
					expectedWebSvr, expectedPoweredBy, commands);
			executor.execute(sc);
		}
	}
}