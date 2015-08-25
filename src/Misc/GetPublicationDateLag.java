package Misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.*;

public class GetPublicationDateLag {
	Connection.Response response = null;
	public static void main(String[] args) {

		/*
		 * String jsonFile ="./data/testjson.json"; String outFile =
		 * "./data/out.csv";
		 */
		String jsonFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/wcep-new-sources.json";
		String SourceIDURLPublicationdate = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/SourceIDURLPublicationdate.csv";
		String SourceIDDate = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/sourceid-date.1.csv";
		String outFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/out1.csv";
		GetPublicationDateLag pub = new GetPublicationDateLag();
		// pub.parseLargeJson(jsonFile);
		// pub.getWCEPDateSourceURL(SourceIDURLPublicationdate, SourceIDDate,
		// outFile);
		pub.getValidURLs(outFile);
	}

	void getValidURL(String outFile) {
		String line = "";
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(outFile));
			List<String> ListUrl = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				ListUrl.add(data[0].trim());
			}
			System.out.println("Total :" + ListUrl.size());
			int diff = ListUrl.size() / 100;
			int count = 0;
			int iCount = 0;
			while (iCount < ListUrl.size()) {
				List<String> tempListUrl = new ArrayList<>();
				for (int i = iCount; i < ListUrl.size(); i++) {
					iCount++;
					if (count < diff) {
						tempListUrl.add(ListUrl.get(i));
						count++;
					}
					if (count == diff) {
						ThreadDownloadGdelt thr = new ThreadDownloadGdelt(
								tempListUrl, i);
						thr.start();
						count = 0;
						break;
					}
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	void getValidURLs(String outFile) {
		String line = "";
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(outFile));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				//URL myurl = new URL(data[0].trim());
				try {
					/*HttpURLConnection connection = (HttpURLConnection) myurl
							.openConnection();
					connection.setRequestMethod("HEAD");
					int code = connection.getResponseCode();*/
					response = Jsoup.connect(data[0].trim()).execute();
					
					
					//Element meta = doc.select("meta[itemprop=datePublished]").first();
					
					int code = response.statusCode();
					if (code == HttpURLConnection.HTTP_ACCEPTED
							|| code == HttpURLConnection.HTTP_CREATED
							|| code == HttpURLConnection.HTTP_MOVED_PERM
							|| code == HttpURLConnection.HTTP_MOVED_TEMP
							|| code == HttpURLConnection.HTTP_MULT_CHOICE
							|| code == HttpURLConnection.HTTP_NO_CONTENT
							|| code == HttpURLConnection.HTTP_NOT_MODIFIED
							|| code == HttpURLConnection.HTTP_NOT_AUTHORITATIVE
							|| code == HttpURLConnection.HTTP_OK
							|| code == HttpURLConnection.HTTP_PARTIAL
							|| code == HttpURLConnection.HTTP_SEE_OTHER
							|| code == HttpURLConnection.HTTP_USE_PROXY) {
						System.out.println(data[0] + "\t" + code);
						Document doc = response.parse();
						if(doc.select("meta[property=sm4:pubdate]") != null)
						{
							String el = doc.select("meta[property=sm4:pubdate]").first().attr("content");
							System.out.println(el);
						}
						
						/*if(doc.select("meta[itemprop=sm4:pubdate]").first())
						{
							String content = doc.select("meta[property=sm4:pubdate]").first().attr("content");
						}*/
						//connection.disconnect();
						//Document doc = response.parse();
					}

				} catch (IOException ex) {
					Logger.getLogger(AggregateSources.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	void getWCEPDateSourceURL(String SourceIDURLPublicationdate,
			String SourceIDDate, String outFile) {
		String line = "";
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(SourceIDDate));
			Map<Integer, Integer> MapSourceIDDate = new HashMap<Integer, Integer>();
			while ((line = br.readLine()) != null) {
				String[] data = line.split(" ");
				MapSourceIDDate.put(Integer.parseInt(data[0].trim()),
						Integer.parseInt(data[1]));
			}
			File statText = new File(outFile);
			FileOutputStream is;
			is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);

			br = new BufferedReader(new FileReader(SourceIDURLPublicationdate));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				if (MapSourceIDDate.containsKey(Integer.parseInt(data[0]))) {
					w.write(data[1] + "\t"
							+ MapSourceIDDate.get(Integer.parseInt(data[0]))
							+ "\n");
				} else {
					System.out.println(data[0] + "\t" + data[1]);
				}
			}
			w.close();
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	void parseLargeJson(String jsonFile) {
		JsonFactory f = new MappingJsonFactory();
		JsonParser jp;
		try {
			jp = f.createJsonParser(new File(jsonFile));

			JsonToken current;

			current = jp.nextToken();
			if (current != JsonToken.START_OBJECT) {
				System.out.println("Error: root should be object: quiting.");
				return;
			}

			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = jp.getCurrentName();
				// move from field name to field value
				current = jp.nextToken();
				if (fieldName.equals("docs")) {
					if (current == JsonToken.START_ARRAY) {
						// For each of the records in the array
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							// read the record into a tree model,
							// this moves the parsing position to the end of it
							JsonNode node = jp.readValueAsTree();
							// And now we have random access to everything in
							// the object
							String Publicationdate = node
									.get("publicationdate").toString()
									.replace("\"", "").trim();
							String sourceid = node.get("sourceid").toString()
									.replace("\"", "").trim();
							String URL = node.get("URL").toString()
									.replace("\"", "").trim();
							System.out.println(sourceid + "\t" + URL + "\t"
									+ Publicationdate);
						}
					} else {
						System.out
								.println("Error: records should be an array: skipping.");
						jp.skipChildren();
					}
				} else {
					System.out.println("Unprocessed property: " + fieldName);
					jp.skipChildren();
				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

class ThreadDownloadGdelt implements Runnable {
	private Thread t;
	List<String> ListUrlLink = new ArrayList<>();
	private int test;

	ThreadDownloadGdelt(List tempListUrlLink, int test1) {
		ListUrlLink = tempListUrlLink;
		test = test1;
	}

	public void start() {
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	public void run() {
		Iterator it = ListUrlLink.iterator();
		while (it.hasNext()) {
			String url = it.next().toString().trim();

			try {
				URL myurl = new URL(url);
				HttpURLConnection connection = (HttpURLConnection) myurl
						.openConnection();
				connection.setRequestMethod("HEAD");
				int code = connection.getResponseCode();
				Connection.Response response = (Response) Jsoup.connect("url");
				code = response.statusCode();
				if (code == HttpURLConnection.HTTP_ACCEPTED
						|| code == HttpURLConnection.HTTP_CREATED
						|| code == HttpURLConnection.HTTP_MOVED_PERM
						|| code == HttpURLConnection.HTTP_MOVED_TEMP
						|| code == HttpURLConnection.HTTP_MULT_CHOICE
						|| code == HttpURLConnection.HTTP_NO_CONTENT
						|| code == HttpURLConnection.HTTP_NOT_MODIFIED
						|| code == HttpURLConnection.HTTP_NOT_AUTHORITATIVE
						|| code == HttpURLConnection.HTTP_OK
						|| code == HttpURLConnection.HTTP_PARTIAL
						|| code == HttpURLConnection.HTTP_SEE_OTHER
						|| code == HttpURLConnection.HTTP_USE_PROXY) {
					System.out.println(url + "\t" + code);
					connection.disconnect();
					Document doc = response.parse();
					
				}

				System.out.println(url + "\t" + code);
				connection.disconnect();

			} catch (IOException ex) {

			}
		}

	}
}
