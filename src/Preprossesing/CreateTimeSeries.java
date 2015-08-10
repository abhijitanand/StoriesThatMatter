package Preprossesing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class CreateTimeSeries {
	
	boolean bGetNext = false;
	String lastRevisionDate = "";
	Map<String, String> RedirectMap = new HashMap<String, String>();
	
	//3. A function to read from the above folder and create a story, timeseries file(Use redirect function)
	//4. A function to read from folder and create a story, timeseries and frequency file(Use redirect file)
	//5. Get timeseries for all stories present in wikitimes and from folders
	void getTimeSeries(Set storyList, String Redirect, String outTimeSeries)
	{
		loadRedirectMapping(Redirect);
	}
	void loadRedirectMapping(String Redirect)
	{
		String line = "";
		BufferedReader br = null;		
		try {
		
		// Populate redirects
		br = new BufferedReader(new InputStreamReader(new FileInputStream(
				Redirect), "UTF8"));
		while ((line = br.readLine()) != null) {
			String[] redirect = line.split(";");
			String redirectTo = redirect[0];
			for (int i = 1; i < redirect.length; i++) {
				if (!redirect[i].equals("")) {
					RedirectMap.put(redirect[i],
							redirectTo);
				}
			}
		}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
	String getRedirects(String storyName)
	{
		String story = "";
		if (RedirectMap.containsKey(storyName)) {
			story = RedirectMap.get(storyName);
		} 
		else{
			story = storyName;
		}
		return story;
	}
	void getTimeSeriesForExtendedStories(String fileEntityDate,
			String TimeSeries, String Redirect) {
		try {
			/*Map<String, String> StoryPageNames = getStoryList(fileEntityDate,
					Redirect);*/
			Set StoryPageNames = getStoryLIstFromEventMainArticle(fileEntityDate);
			Iterator it = StoryPageNames.iterator();
			while(it.hasNext()) {
				String storyName = it.next().toString();
				String storyUrl = "https://en.wikipedia.org/w/api.php?action=query&titles="
						+ storyName
						+ "&format=json&prop=extracts&rvprop=timestamp&rvdir=newer&rvlimit=max&continue&redirects&prop=revisions";
				URL url = new URL(storyUrl);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						url.openStream()));
				String storyJson = in.readLine();
				Map<Integer, Integer> DateCount = new HashMap<Integer, Integer>();
				getTimeSeriesFromJson(storyJson, DateCount);
				while (!bGetNext && !lastRevisionDate.equals("")) {
					String nextStoryUrl = "https://en.wikipedia.org/w/api.php?action=query&titles="
							+ storyName
							+ "&format=json&prop=extracts&rvprop=timestamp&rvdir=newer&rvlimit=max&rvstart="
							+ lastRevisionDate
							+ "&continue&redirects&prop=revisions";
					URL url1 = new URL(nextStoryUrl);
					BufferedReader in1 = new BufferedReader(
							new InputStreamReader(url1.openStream()));
					String storyNextRevJson = in1.readLine();
					getTimeSeriesFromJson(storyNextRevJson, DateCount);
				}
				Map<Integer, Integer> treeMap = new TreeMap<Integer, Integer>();
				treeMap.putAll(DateCount);

				File statText = new File(TimeSeries);
				FileOutputStream is;
				is = new FileOutputStream(statText, true);
				OutputStreamWriter osw = new OutputStreamWriter(is);
				Writer w = new BufferedWriter(osw);
				w.write(storyName);
				if (treeMap.isEmpty()) {
					System.out.println(storyName);
				}
				for (Map.Entry<Integer, Integer> pairs1 : treeMap.entrySet()) {
					w.write(";" + pairs1.getKey() + "," + pairs1.getValue());
				}
				w.write("\n");
				w.close();
				bGetNext = false;
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
	
	Set getStoryLIstFromEventMainArticle(String EventMainArticle)
	{		
		String line = "";
		BufferedReader br = null;
		Set StoryPageNames = new HashSet();
		Set newStoryPageNames = new HashSet<>();
		//Map<String, String> newStoryPageNames = new HashMap<String, String>();
		try {
			br = new BufferedReader(new FileReader(EventMainArticle));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(";");
				for(int i=0;i<data.length;i++)
				{
					if(!data[i].equals(""))
					{
						StoryPageNames.add(data[i]);
					}					
				}
			}
			Iterator it = StoryPageNames.iterator();
			while (it.hasNext()) {
				String storyName = it.next().toString();
				String storyPageName = getRedirects(storyName);
				newStoryPageNames.add(storyPageName);
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return newStoryPageNames;
	}
	
	void getTimeSeriesFromJson(String storyJson, Map<Integer, Integer> DateCount) {
		try {
			DateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");
			DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd",
					Locale.ENGLISH);
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(storyJson);
			JSONObject jsonStory = (JSONObject) obj;
			JSONObject jsonQuery = (JSONObject) jsonStory.get("query");
			JSONObject jsonPages = (JSONObject) jsonQuery.get("pages");
			String temppageID = jsonPages.toJSONString().split("\\{")[1]
					.split(":")[0];
			String pageID = temppageID.substring(1,
					temppageID.lastIndexOf("\""));
			JSONObject jsonPageID = (JSONObject) jsonPages.get(pageID);
			JSONArray revisions = (JSONArray) jsonPageID.get("revisions");
			if (!pageID.equals("-1")) {
				if (revisions.size() > 1) {
					for (Object revision : revisions) {
						JSONObject objects = (JSONObject) revision;
						String timestamp = objects.get("timestamp").toString();
						Date date1 = originalFormat.parse(timestamp);
						int formattedDate = Integer.parseInt(targetFormat
								.format(date1));
						if (DateCount.containsKey(formattedDate)) {
							int count = DateCount.get(formattedDate);
							count++;
							DateCount.remove(formattedDate);
							DateCount.put(formattedDate, count);
						} else {
							DateCount.put(formattedDate, 1);
						}
						// System.out.println(formattedDate);
					}
					JSONObject lastRev = (JSONObject) revisions.get(revisions
							.size() - 1);
					lastRevisionDate = lastRev.get("timestamp").toString();
				} else {
					bGetNext = true;
					lastRevisionDate = "";
				}
			}
		} catch (ParseException ex) {
			Logger.getLogger(storyJson).log(Level.SEVERE, null, ex);
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (java.text.ParseException ex) {
			Logger.getLogger(CreateTimeSeries.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
}
