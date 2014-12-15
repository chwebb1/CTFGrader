import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.eventbus.*;

public class ServerCheck implements Runnable {
	private String serverAddress; 	//server address, either IP Address or DNS name (ie google.com)
	private String serverName;		//expected NETBIOS name of the server
	private String expectedSvr;		//expected HTTP server running on the computer
	private String expectedPwr;		//expected PHP engine
	private EventBus eb;			//event bus to push objects to CSV writer for writing.

	public ServerCheck(String srvname, String srvaddress, String expSvr,
			String expPwr, EventBus eventbus) {
		serverAddress = srvaddress;
		serverName = srvname;
		expectedSvr = expSvr;
		expectedPwr = expPwr;
		eb = eventbus;
	}

	@Override
	public void run() {
		String hostname = "";
		String address = "";
		boolean reachable;
		boolean httpUp = false;
		try {
			InetAddress serverInfo = InetAddress.getByName(serverAddress);	
			hostname = serverInfo.getHostName();
			address = serverInfo.getHostAddress();
			reachable = serverInfo.isReachable(3000);	//checks to see if the server is reachable.
		} catch (UnknownHostException e) {
			reachable = false;
		} catch (IOException e) {
			reachable = false;
		}
		Date now = new Date();
		SimpleDateFormat rfc2822Fmt = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss Z");					//format the date in the expected format to compare with PHP.
		rfc2822Fmt.setTimeZone(TimeZone.getTimeZone("GMT"));	//sets the expected time zone so we match the php script.
		Date correctDate = now;									//stores the correct date for comparison later.
		String correctDateTime = rfc2822Fmt.format(correctDate);
		String userClaimedName = null;
		String userClaimedDate = null;
		String poweredBy = null;
		String server = null;
		int httpStatusCode;
		final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		GenericUrl url = new GenericUrl("http://" + hostname + "/index.php");	//set the url to index.php on the hostname we were given.
		HttpRequest request = null;
		try {
			request = HTTP_TRANSPORT.createRequestFactory()
					.buildGetRequest(url);
		} catch (IOException e) {
			httpUp=false;
		}
		HttpResponse response = null;
		try {
			response = request.execute();										//execute the request
		} catch (IOException e) {
			httpUp=false;
		}
		Date userClaimedDatejd = null;	
		if (reachable){
			httpStatusCode = response.getStatusCode();
			HttpHeaders headers = null;
			headers = response.getHeaders();
			server = headers.get("server").toString();							//get the web server running on this machine.
			poweredBy = headers.get("x-powered-by").toString();					//get the PHP powered by string on this machine.
			InputStream is = null;
			try {
				is = response.getContent();										//get the content that we fetched
			} catch (IOException e) {
				httpUp=false;
			}
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));	//create the buffered reader so we can use readLine()
			} catch (UnsupportedEncodingException e1) {
				httpUp=false;
			}
			try {
				userClaimedName = br.readLine(); // first line in PHP script should
													// be the name of the machine
				userClaimedDate = br.readLine(); // second line of the PHP script
													// should be the current
													// date/time on the server.
			} catch (IOException e) {
				httpUp=false;
			}
			try {
				response.disconnect();
			} catch (IOException e) {
				httpUp=false;
			}
			try {
				userClaimedDatejd = rfc2822Fmt.parse(userClaimedDate);
			} catch (ParseException e) {
				httpUp=false;
			}
			long dateDiff = correctDate.getTime() - userClaimedDatejd.getTime();
			if ((Math.abs(dateDiff) < 60000) && (server.equals(expectedSvr))
					&& (poweredBy.equals(expectedPwr))
					&& (serverName.equals(userClaimedName))) { // check to see if the time difference is less than 60 seconds, and that the server name, web server, and powered by matches what we expected 
				httpUp = true;
			} else {
				httpUp = false;
			}	
		} else {
			httpStatusCode = -1;
		}
		if (reachable){
			System.out.print("As of " + correctDateTime + " the server " + serverName + " is reachable ");
		} else {
			System.out.print("As of " + correctDateTime + " the server " + serverName + " is NOT reachable ");
		}
		if (httpUp){
			System.out.println("and the HTTP server is running in its expected configuration.");
		} else {
			System.out.println("and the HTTP server is NOT running in its expected configuration.");
		}
		srvInfo info = new srvInfo();
		info.serverName = serverName;
		info.address = address;
		info.hostName = hostname;
		info.httpStatusCode = httpStatusCode;
		info.claimedHostName = userClaimedName;
		info.dateTime = userClaimedDate;
		info.dateTime_d = userClaimedDatejd;
		info.correctDateTime = correctDateTime;
		info.correctDateTime_d  = now;
		info.HTTPWorking = httpUp;
		info.isReachable = reachable;
		info.poweredBy = poweredBy;
		info.serverAddress = serverAddress;
		info.websrv = server;
		eb.post(info);
	}

}
