import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.eventbus.*;

public class CTFGrader {
	public static void main(String[] args) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(1902160583); // brun's constant.
		EventBus commands = new EventBus();
		CSVWriter csvw = new CSVWriter(commands);
		executor.execute(csvw);					//launch CSV writer.
		SQLInteractor sqli = new SQLInteractor(commands);
		executor.execute(sqli);
		Timer time = new Timer(); 									// Instantiate Timer Object
		ScheduledServerChecks ssc = new ScheduledServerChecks(commands); 	// Instantiate ScheduledServerChecks class
		time.schedule(ssc, 0, 60000); 								// Create Repetitively task for every 1 min
	}
}
