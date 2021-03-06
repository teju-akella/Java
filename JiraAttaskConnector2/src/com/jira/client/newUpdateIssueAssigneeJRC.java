package com.jira.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import javax.naming.AuthenticationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

public class newUpdateIssueAssigneeJRC {


	private static String BASE_URL = "https://amphorainc.atlassian.net";

	public static void main(String[] args) throws IOException {
		
		//String auth = new String(Base64.encode("abc:xyz"));
		System.out.println("Task Started : "+new Date()+" \n****************************************************************");
		Properties prop = new Properties();
		prop.load(UpdateIssueAssigneeJRC.class.getClassLoader().getResourceAsStream("conf.properties"));
		String up = prop.getProperty("unmpwd");		
		String auth = new String(Base64.encode(up));
		String uname = up.split(":")[0];
		String dateString = new Date().toString().replaceAll(" ", "_"); 
		dateString = dateString.replaceAll(":", "_");
		File f =  new File("D:/jira/jira_logs/log_"+uname+"_"+dateString+".txt");
		FileOutputStream fos = new FileOutputStream(f);
		try {
			FileReader fileReader = new FileReader("D:/jira/IssueListForUpdateAssignee.txt");		
            BufferedReader bufferedReader = new BufferedReader(fileReader);            
            String currentLine = null;
			while ((currentLine = bufferedReader.readLine()) != null) {
				String issueKey = currentLine; // ADSO-1234
				String issue = invokeGetMethod(auth, BASE_URL
						+ "/rest/api/2/issue/" + issueKey
						+ "?fields=assignee,reporter");
				JSONObject issueData = new JSONObject(issue);
				String selfUrl = issueData.getString("self");
				JSONObject issueFields = issueData.getJSONObject("fields");
				String assigneeName = issueFields.getJSONObject("assignee").getString("name");
				String reporterName = issueFields.getJSONObject("reporter").getString("name");				
				// getting reporter data
//				JSONObject assignee = issueFields.getJSONObject("assignee");
//				JSONObject reporter = issueFields.getJSONObject("reporter");
//				assignee = reporter;
				//**************>>>create comments<<<******************
				String editIssueData = "{\"body\": \"This is a comment that only administrators can see.\",\"visibility\": {\"type\": \"role\",\"value\": \"Service Desk Team\"}}";
				//String resp = invokePostMethod(auth, BASE_URL+"/rest/api/2/issue/"+issueKey+"/comment", editIssueData);
				//System.out.println(resp);
				
				
				//**************>>> resolution(status) <<<******************	
				String editIssueData2 = "{\"update\": {\"comment\": [{\"add\": {\"body\": \"Comment added when resolving issue\"}}]},\"transition\": {\"id\": \"141\"}}";						
				System.out.println(invokePostMethod(auth, BASE_URL+"/rest/api/2/issue/ADSO-1250/transitions", editIssueData2));	
				
				String assigneeData = "{\"name\":\""+reporterName+"\"}";
				System.out.println(assigneeData);
				invokePutMethod(auth, BASE_URL+"/rest/api/2/issue/ADSO-1250/assignee", assigneeData);
				
				fos.write("\n".getBytes());
				currentLine = null;
			}
			fos.close();
			System.out.println("**************************************************************** \n Task Completed : "+new Date());
			
		} catch (AuthenticationException e) {
			System.out.println("Username or Password wrong!");
			e.printStackTrace();
		} catch (ClientHandlerException e) {
			System.out.println("Error invoking REST method");
			e.printStackTrace();
		} catch (JSONException e) {
			System.out.println("Invalid JSON output");
			e.printStackTrace();
		}

	}

	private static String invokeGetMethod(String auth, String url) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);		
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").get(ClientResponse.class);
		int statusCode = response.getStatus();
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}		
		return response.getEntity(String.class);
	}	
	public static Response clientResponseToResponse(ClientResponse r) {
	    // copy the status code
	    ResponseBuilder rb = Response.status(r.getStatus());
	    // copy all the headers
	    for (Entry<String, List<String>> entry : r.getHeaders().entrySet()) {
	        for (String value : entry.getValue()) {
	            rb.header(entry.getKey(), value);
	        }
	    }
	    // copy the entity
	    //rb.entity(r.getEntityInputStream());
	    rb.entity(r.getEntity(new GenericType<String>(){}));
	    // return the response
	    return rb.build();
	}
	
	private static String invokePostMethod(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").post(ClientResponse.class, data);
		int statusCode = response.getStatus();
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}
		return response.getEntity(String.class);
	}
	
	private static void invokePutMethod(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").put(ClientResponse.class, data);
		int statusCode = response.getStatus();
		//System.out.println("status code: "+statusCode);
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}
		System.out.println("response : "+response);
	}
	
	private static String invokePutMethod1(String auth, String url, String data) throws AuthenticationException, ClientHandlerException {
		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
				.accept("application/json").put(ClientResponse.class, data);
		int statusCode = response.getStatus();
		//System.out.println("status code: "+statusCode);
		String msg = "";
		if (statusCode == 401) {
			msg = "failed";
			throw new AuthenticationException("Invalid Username or Password");
		}else if (statusCode == 200){
			msg="success";
		}
		return msg;
	}
	
}
