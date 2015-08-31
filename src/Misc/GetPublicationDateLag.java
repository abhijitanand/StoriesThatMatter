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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.Days;


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
		String outFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/out.csv";
		GetPublicationDateLag pub = new GetPublicationDateLag();
		// pub.parseLargeJson(jsonFile);
		// pub.getWCEPDateSourceURL(SourceIDURLPublicationdate, SourceIDDate,
		// outFile);
		
		
		//pub.getValidURL(outFile);
		//pub.getDomainFrequency(outFile);
		/*String urlResponseCode = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/UrlResponseCode.csv";
		String alreadyPublishedFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/alreadyPublishedFile.csv";
		pub.getDomainForPublishingDate(urlResponseCode, alreadyPublishedFile);*/
		
		String alreadyPublishedFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/alreadyPublishedFile.csv";
		String lagFile= "/home/aanand/workspace/StoriesThatMatter/Data/Misc/PublicationDate/lagFile.csv";
		pub.getLag(outFile, alreadyPublishedFile,lagFile);
	}

	void getLag(String outFile, String alreadyPublishedFile,String lagFile)
	{
		String line = "";
		BufferedReader br = null;
		try {
			Map<String,Integer> UrlWCEP = new HashMap<String, Integer>();
			Map<String,Integer> UrlPublished = new HashMap<String, Integer>();
			br = new BufferedReader(new FileReader(outFile));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				UrlWCEP.put(data[0].trim(), Integer.parseInt(data[1]));				
			}
			br = new BufferedReader(new FileReader(alreadyPublishedFile));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				if(!data[0].equals(""))
				{
					String pubDate = parseDate(data[0]);
					int len = pubDate.length();
					if(!pubDate.equals("") && pubDate.length()==8)
					{
						//System.out.println(data[1].trim());
						UrlPublished.put(data[1].trim(), Integer.parseInt(pubDate));
					}
					
				}				
			}
			Map<String,Integer> UrlLag = new HashMap<String, Integer>();
			Map<Integer,Integer> LagCount = new HashMap<Integer, Integer>();
			for(Map.Entry<String, Integer>pairs: UrlPublished.entrySet())
			{
				if(UrlWCEP.containsKey(pairs.getKey()))
				{
					
					int diff = getdateDiff(UrlWCEP.get(pairs.getKey()),pairs.getValue());
					//if(diff!=0)
					//{
						if(LagCount.containsKey(diff))
						{
							int temp = LagCount.get(diff);
							temp++;
							LagCount.remove(diff);
							LagCount.put(diff, temp);
						}
						else
						{
							LagCount.put(diff, 1);
						}
						System.out.println(pairs.getKey()+"\t"+UrlWCEP.get(pairs.getKey())+"\t"+pairs.getValue()+"\t"+diff);
					//}
				}
			}
			File statText = new File(lagFile);
			FileOutputStream is;
			is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			TreeMap<Integer, Integer> treemap = new TreeMap<Integer, Integer>();
			treemap.putAll(LagCount);
			int count = 0;
			for(Map.Entry<Integer, Integer>pairs: treemap.entrySet())
			{
				if(pairs.getKey()>-365 && pairs.getKey()<(365))
				{
					w.write(pairs.getKey()+"\t"+pairs.getValue()+"\n");
					count= count + pairs.getValue();
				}
				
			}
			System.out.println(count);
			
			w.close();
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
	int getdateDiff(int finalDate, int initialDate) {
		int finalDiff = 0;
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			Date d1 = format.parse(Integer.toString(initialDate));
			Date d2 = format.parse(Integer.toString(finalDate));

			DateTime dt1 = new DateTime(d1);
			DateTime dt2 = new DateTime(d2);
			finalDiff = Days.daysBetween(dt1, dt2).getDays();
			// System.out.print( finalDiff+ " days, ");

		} catch (java.text.ParseException ex) {
			Logger.getLogger(GetPublicationDateLag.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return finalDiff;
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
	public String getDocumentDate(Document doc) {
        Elements date_elements = doc.getElementsByAttributeStarting("date");

        for (Element element : date_elements) {
            String date = parseDate(element.text());
            return date;
        }
        return "N/A";
    }
	public String parseDate(String date_str) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat df01 = new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat df111 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df11 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df3 = new SimpleDateFormat("EEE, MMM d, yyyy");
        SimpleDateFormat df4 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SimpleDateFormat df41 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        SimpleDateFormat df43 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat df42 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat df5 = new SimpleDateFormat("EEE, MMM. dd, yyyy");
        SimpleDateFormat df6 = new SimpleDateFormat("EEE, MMM dd, yyyy");
        SimpleDateFormat df7 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat df8 = new SimpleDateFormat("EEEEE d MMM yyyy");
        SimpleDateFormat df9 = new SimpleDateFormat("EEEEE, MMM. dd, yyyy");
        SimpleDateFormat df10 = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat df101 = new SimpleDateFormat("dd MMM yyyy HH:mm z");
        SimpleDateFormat df102 = new SimpleDateFormat("d MMM yyyy");
        SimpleDateFormat df12 = new SimpleDateFormat("MMM dd, yyyy, HH:mm a");
        SimpleDateFormat df14 = new SimpleDateFormat("MMM dd, yyyy, H:mm a");
        SimpleDateFormat df13 = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat df15 = new SimpleDateFormat("MMMMM dd, yyyy, HH:mm a");
        SimpleDateFormat df16 = new SimpleDateFormat("MMMMM dd, yyyy, H:mm a");
        SimpleDateFormat df17 = new SimpleDateFormat("MMM. dd, yyyy");
        SimpleDateFormat df18 = new SimpleDateFormat("MMMMM dd, yyyy");
        SimpleDateFormat df19 = new SimpleDateFormat("EEEEE, MMM. dd, yyyy");
        SimpleDateFormat df_20 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        SimpleDateFormat df21 = new SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat df22 = new SimpleDateFormat("MMMM dd, yyyy HH:mm a");
        SimpleDateFormat df23 = new SimpleDateFormat("EEE MMMM dd HH:mm:ss z yyyy");
        SimpleDateFormat df24 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df25 = new SimpleDateFormat("yyyyMMddHHmm");
        List<SimpleDateFormat> lst = new ArrayList<>();
        //lst.add(df24);
        lst.add(df);
        lst.add(df1);
        //lst.add(df01);
        lst.add(df2);
        lst.add(df111);
        lst.add(df3);
        lst.add(df4);
        lst.add(df41);
        lst.add(df42);
        lst.add(df43);
        lst.add(df5);
        lst.add(df6);        
        lst.add(df7);
        lst.add(df8);
        lst.add(df9);
        lst.add(df10);
        lst.add(df101);
        lst.add(df102);
        lst.add(df11);
        lst.add(df12);
        lst.add(df13);
        lst.add(df14);
        lst.add(df15);
        lst.add(df16);
        lst.add(df17);
        lst.add(df18);
        lst.add(df19);
        lst.add(df_20);
        lst.add(df21);
        lst.add(df22);
        lst.add(df23);
        lst.add(df24);
        lst.add(df25);
        lst.add(df01);
        
        SimpleDateFormat df_simple = new SimpleDateFormat("yyyyMMdd");
        /*if(date_str.contains("Z"))
    	{
    		SimpleDateFormat df001 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    		try {
    		Date dt = df001.parse(date_str);
    		return df_simple.format(dt);
    		} catch (Exception e) {
            }
    	}*/
        for (SimpleDateFormat df_tmp : lst) {
            try {
            	
                Date dt = df_tmp.parse(date_str);
                return df_simple.format(dt);
            } catch (Exception e) {
                continue;
            }
        }
        return "";

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
					response = Jsoup.connect(data[0].trim()).followRedirects(true).execute();					
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
						//System.out.println(data[0] + "\t" + code);
						String el = "";						
						Document doc = response.parse();
						String er = doc.select("meta[name=REVISION_DATE]").toString();
						String st = doc.select("p.meta.time").toString();
						//String bflag = doc.select("p.meta").first().toString();
						if(!doc.select("meta[property=sm4:pubdate]").toString().equals(""))
						{
							el = doc.select("meta[property=sm4:pubdate]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=OriginalPublicationDate]").toString().equals(""))
						{
							el = doc.select("meta[name=OriginalPublicationDate]").attr("content").toString();
							
						}
						else if(!doc.select("meta[itemprop=datePublished]").toString().equals(""))
						{
							el = doc.select("meta[itemprop=datePublished]").attr("content").toString();
							
						}
						else if(!doc.select("time[itemprop=datePublished]").toString().equals(""))
						{
							el = doc.select("time[itemprop=datePublished]").attr("datetime").toString();
							
						}
						else if(!doc.select("time[itemprop=datePublished]").toString().equals(""))
						{
							el = doc.select("time[itemprop=datePublished]").text().toString();
							
						}
						else if(!doc.select("meta[property=article:published_time]").toString().equals(""))
						{
							el = doc.select("meta[property=article:published_time]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=article:publicationdate]").toString().equals(""))
						{
							el = doc.select("meta[name=article:publicationdate]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=pubdate]").toString().equals(""))
						{
							el = doc.select("meta[name=pubdate]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=publishdate]").toString().equals(""))
						{
							el = doc.select("meta[name=publishdate]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=pdate]").toString().equals(""))
						{
							el = doc.select("meta[name=pdate]").attr("content").toString();
							
						}
						else if(!doc.select("meta[property=og:pubdate]").toString().equals(""))
						{
							el = doc.select("meta[property=og:pubdate]").attr("content").toString();
							
						}
						else if(!doc.select("abbr[class=published]").toString().equals(""))
						{
							el = doc.select("abbr[class=published]").attr("title").toString();
							
						}
						else if(!doc.select("div[class=publish-date]").toString().equals(""))
						{
							el = doc.select("div[class=publish-date]").text().toString();
							
						}
						else if(!doc.select("pubDate").toString().equals(""))
						{
							el = doc.select("pubDate").text().toString();
							
						}
						else if(!doc.select("meta[name=dc.date]").toString().equals(""))
						{
							el = doc.select("meta[name=dc.date]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=DC.date.issued]").toString().equals(""))
						{
							el = doc.select("meta[name=DC.date.issued]").attr("content").toString();
							
						}
						else if(!doc.select("span[class=c-dateline]").toString().equals(""))
						{
							el = doc.select("span[class=c-dateline]").text().toString();
							
						}
						else if(!doc.select("div.txt.timestamp").toString().equals(""))
						{
							el = doc.select("div.txt.timestamp").attr("content").toString();
						}
						else if(!doc.select("div.date.date--v2").toString().equals(""))
						{
							el = doc.select("div.date.date--v2").attr("data-datetime").toString();
						}
						
						else if(!doc.select("meta[property=date]").toString().equals(""))
						{
							el = doc.select("meta[property=date]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=date]").toString().equals(""))
						{
							el = doc.select("meta[name=date]").attr("content").toString();
							
						}
						else if(!doc.select("meta[name=REVISION_DATE]").toString().equals(""))
						{
							el = doc.select("meta[name=REVISION_DATE]").attr("content").toString();
							
						}			
						else if(!doc.select("p[class=article-date]").toString().equals(""))
						{
							el = doc.select("p[class=article-date]").text().toString();
							
						}
						else if(!doc.select("meta[name=sailthru.date]").toString().equals(""))
						{
							el = doc.select("meta[name=sailthru.date]").attr("content").toString();
							
						}
						
						else if(!doc.select("time").toString().equals(""))
						{
							el = doc.select("time").attr("datetime").toString();
							
						}
						else if(!doc.select("span[class=date]").toString().equals(""))
						{
							el = doc.select("span[class=date]").first().text().toString();
							
						}
						else if(!doc.select("span.date").toString().equals(""))
						{
							el = doc.select("span.date").text().toString();
							
						}
						else if(!doc.select("td.text12g").toString().equals(""))
						{
							el = doc.select("td.text12g").text().toString();
							
						}
						else if(!doc.select("meta[name=Last-Modified]").toString().equals(""))
						{
							el = doc.select("meta[name=Last-Modified]").attr("content").toString();
							
						}
						else if(!doc.select("div.last-updated").toString().equals(""))
						{
							String monYear = doc.select("span.last-updated__month-year").text().toString();
							el = monYear.concat(" "+doc.select("span.last-updated__day").text().toString());
						}
						else if(!doc.select("p.meta").toString().equals("") && !doc.select("p.meta").contains("time"))
						{
							if(!doc.select("p.meta").first().toString().equals(""))
							{
								if(!doc.select("p.meta").first().getElementsByAttribute("datetime").toString().equals(""))
								{
									el = doc.select("p.meta").first().child(0).text();
								}
								
							}
							
						}
						if(!el.equals(""))
						{
							System.out.println(parseDate(el)+"\t"+data[0].trim());
						}
						
						/*if(doc.select("meta[itemprop=sm4:pubdate]").first())
						{
							String content = doc.select("meta[property=sm4:pubdate]").first().attr("content");
						}*/
						//connection.disconnect();
						//Document doc = response.parse();
					}

				} catch (IOException ex) {
					/*Logger.getLogger(AggregateSources.class.getName()).log(
							Level.SEVERE, null, ex);*/
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

	void getDomainForPublishingDate(String urlResponseCode, String alreadyPublishedFile)
	{
		String line = "";
		BufferedReader br = null;
		try {
			Set<String> DomainNames = new HashSet<String>();
			Map<String,Integer> notPublishedDomainNames = new HashMap<String,Integer>();
			AggregateSources ag = new AggregateSources();
			br = new BufferedReader(new FileReader(alreadyPublishedFile));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				//String domain =ag.getSourceNameOnly(data[1]);
				DomainNames.add(data[1]);				
			}
			br = new BufferedReader(new FileReader(urlResponseCode));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				String domain = data[0];
				if(!DomainNames.contains(domain))
				{
					int code = Integer.parseInt(data[1].trim());
					if(code==200 ||code==201 ||code==202 || code==203 ||code==204 || code==205 || code==206 ||  code==207 ||  code==208
						|| code==300 || code==301 || code==302 || code==303 || code==304 || code==305 || code==306 ||  code==307 ||  code==308)
					{
						domain =ag.getSourceNameOnly(domain);
						if(notPublishedDomainNames.containsKey(domain))
						{
							int temp = notPublishedDomainNames.get(domain);
							temp++;
							notPublishedDomainNames.remove(domain);
							notPublishedDomainNames.put(domain, temp);
						}
						else
						{
							notPublishedDomainNames.put(domain, 1);
						}
					}
				}
			}
			for(Map.Entry<String, Integer>pairs : notPublishedDomainNames.entrySet())
			{
				System.out.println(pairs.getKey()+"\t"+pairs.getValue());
			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
	void getDomainFrequency(String outFile)
	{
		String line = "";
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(outFile));
			Map<String, Integer> MapDomainFreq = new HashMap<String, Integer>();
			AggregateSources ag = new AggregateSources();
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				String domain =ag.getSourceNameOnly(data[0]);
				if(MapDomainFreq.containsKey(domain))
				{
					int temp = MapDomainFreq.get(domain);
					temp++;
					MapDomainFreq.remove(domain);
					MapDomainFreq.put(domain, temp);
				}
				else
				{
					MapDomainFreq.put(domain, 1);
				}
			}
			
			for(Map.Entry<String, Integer>pairs: MapDomainFreq.entrySet())
			{
				System.out.println(pairs.getKey()+"\t"+pairs.getValue());
			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
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
				/*URL myurl = new URL(url);
				HttpURLConnection connection = (HttpURLConnection) myurl
						.openConnection();
				connection.setRequestMethod("HEAD");
				int code = connection.getResponseCode();*/
				Connection.Response response = Jsoup.connect(url.trim()).followRedirects(true).execute();
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
					//System.out.println(data[0] + "\t" + code);
					String el = "";						
					Document doc = response.parse();
					String er = doc.select("meta[name=REVISION_DATE]").toString();
					String st = doc.select("p.meta.time").toString();
					//String bflag = doc.select("p.meta").first().toString();
					if(!doc.select("meta[property=sm4:pubdate]").toString().equals(""))
					{
						el = doc.select("meta[property=sm4:pubdate]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=OriginalPublicationDate]").toString().equals(""))
					{
						el = doc.select("meta[name=OriginalPublicationDate]").attr("content").toString();
						
					}
					else if(!doc.select("meta[itemprop=datePublished]").toString().equals(""))
					{
						el = doc.select("meta[itemprop=datePublished]").attr("content").toString();
						
					}
					else if(!doc.select("time[itemprop=datePublished]").toString().equals(""))
					{
						el = doc.select("time[itemprop=datePublished]").attr("datetime").toString();
						
					}
					else if(!doc.select("time[itemprop=datePublished]").toString().equals(""))
					{
						el = doc.select("time[itemprop=datePublished]").text().toString();
						
					}
					else if(!doc.select("meta[property=article:published_time]").toString().equals(""))
					{
						el = doc.select("meta[property=article:published_time]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=article:publicationdate]").toString().equals(""))
					{
						el = doc.select("meta[name=article:publicationdate]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=pubdate]").toString().equals(""))
					{
						el = doc.select("meta[name=pubdate]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=publishdate]").toString().equals(""))
					{
						el = doc.select("meta[name=publishdate]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=pdate]").toString().equals(""))
					{
						el = doc.select("meta[name=pdate]").attr("content").toString();
						
					}
					else if(!doc.select("meta[property=og:pubdate]").toString().equals(""))
					{
						el = doc.select("meta[property=og:pubdate]").attr("content").toString();
						
					}
					else if(!doc.select("abbr[class=published]").toString().equals(""))
					{
						el = doc.select("abbr[class=published]").attr("title").toString();
						
					}
					else if(!doc.select("pubDate").toString().equals(""))
					{
						el = doc.select("pubDate").text().toString();
						
					}
					else if(!doc.select("meta[name=dc.date]").toString().equals(""))
					{
						el = doc.select("meta[name=dc.date]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=DC.date.issued]").toString().equals(""))
					{
						el = doc.select("meta[name=DC.date.issued]").attr("content").toString();
						
					}
					else if(!doc.select("span[class=c-dateline]").toString().equals(""))
					{
						el = doc.select("span[class=c-dateline]").text().toString();
						
					}
					else if(!doc.select("div.txt.timestamp").toString().equals(""))
					{
						el = doc.select("div.txt.timestamp").attr("content").toString();
					}
					else if(!doc.select("div.date.date--v2").toString().equals(""))
					{
						el = doc.select("div.date.date--v2").attr("data-datetime").toString();
					}
					
					else if(!doc.select("meta[property=date]").toString().equals(""))
					{
						el = doc.select("meta[property=date]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=date]").toString().equals(""))
					{
						el = doc.select("meta[name=date]").attr("content").toString();
						
					}
					else if(!doc.select("meta[name=REVISION_DATE]").toString().equals(""))
					{
						el = doc.select("meta[name=REVISION_DATE]").attr("content").toString();
						
					}			
					else if(!doc.select("p[class=article-date]").toString().equals(""))
					{
						el = doc.select("p[class=article-date]").text().toString();
						
					}
					else if(!doc.select("meta[name=sailthru.date]").toString().equals(""))
					{
						el = doc.select("meta[name=sailthru.date]").attr("content").toString();
						
					}
					
					else if(!doc.select("time").toString().equals(""))
					{
						el = doc.select("time").attr("datetime").toString();
						
					}
					else if(!doc.select("span[class=date]").toString().equals(""))
					{
						el = doc.select("span[class=date]").first().text().toString();
						
					}
					else if(!doc.select("span.date").toString().equals(""))
					{
						el = doc.select("span.date").text().toString();
						
					}
					else if(!doc.select("td.text12g").toString().equals(""))
					{
						el = doc.select("td.text12g").text().toString();
						
					}
					else if(!doc.select("meta[name=Last-Modified]").toString().equals(""))
					{
						el = doc.select("meta[name=Last-Modified]").attr("content").toString();
						
					}
					else if(!doc.select("div.last-updated").toString().equals(""))
					{
						String monYear = doc.select("span.last-updated__month-year").text().toString();
						el = monYear.concat(" "+doc.select("span.last-updated__day").text().toString());
					}
					else if(!doc.select("p.meta").toString().equals("") && !doc.select("p.meta").contains("time"))
					{
						if(!doc.select("p.meta").first().toString().equals(""))
						{
							if(!doc.select("p.meta").first().getElementsByAttribute("datetime").toString().equals(""))
							{
								el = doc.select("p.meta").first().child(0).text();
							}
							
						}
						
					}
					if(!el.equals(""))
					{
						//GetPublicationDateLag lag = new GetPublicationDateLag();
						System.out.println(el+"\t"+url.trim());
					}
					
				}

			} catch (IOException ex) {

			}
		}

	}
}
