import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.microsoft.sqlserver.jdbc.*;
class status1 {
	srvInfo item;
	SQLInteractor writer;
	public status1(SQLInteractor sqli) {
		item = new srvInfo();
		writer=sqli;
	}
	@Subscribe
	public void recvStatus(srvInfo in){
		item = in;
		item.toString();
		writer.write(item);
	}
}
public class SQLInteractor implements Runnable {
	Connection con;
	EventBus eb;
	public SQLInteractor(EventBus commands) {
		eb = commands;
		eb.register(new status1(this));
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String connectionUrl = "jdbc:sqlserver://127.0.0.1:1433;" +
				   "databaseName=CTFData;user=ctfgrd;password=REDACTED;";
				try {
					con = DriverManager.getConnection(connectionUrl);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	@Override
	public void run() {
		
		// TODO Auto-generated method stub
		
	}
	public void write(srvInfo item){
		Statement stmt = null;
		Timestamp dateTime = null;
		Timestamp correctDateTime=null;
		try{
			dateTime = new Timestamp(item.dateTime_d.getTime());
		} catch (Exception e){
			dateTime = new Timestamp(new Date().getTime());
		}
		try {
			correctDateTime = new Timestamp(item.correctDateTime_d.getTime());
		} catch (Exception e) {
			correctDateTime = new Timestamp(new Date().getTime());
		}
		try {
			stmt = con.createStatement();
			String cmd = "INSERT INTO ctf_data "+
					"(ServerAddress,ServerName,HostName,"+
					"UserClaimedServerName,ClaimedDate,CorrectDate,"+
					"isReachable,HTTPUp,HTTPStatus,WebServer,"
					+"poweredBy) VALUES ('"+ item.serverAddress +"','"
					+item.serverName+"','"+item.hostName+"','"+
					item.claimedHostName+"',"+dateTime+","+
					correctDateTime+","+item.isReachable+","+
					item.HTTPWorking+","+item.httpStatusCode+",'"+
					item.websrv+"','"+item.poweredBy+"')";
			System.out.println("cmd: " + cmd);
			stmt.executeUpdate("INSERT INTO ctf_data "+
					"(ServerAddress,ServerName,HostName,"+
					"UserClaimedServerName,ClaimedDate,CorrectDate,"+
					"isReachable,HTTPUp,HTTPStatus,WebServer,"
					+"poweredBy) VALUES ('"+ item.serverAddress +"','"
					+item.serverName+"','"+item.hostName+"','"+
					item.claimedHostName+"','"+dateTime+"','"+
					correctDateTime+"','"+item.isReachable+"','"+
					item.HTTPWorking+"',"+item.httpStatusCode+",'"+
					item.websrv+"','"+item.poweredBy+"')");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
