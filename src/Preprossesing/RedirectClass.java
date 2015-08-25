package Preprossesing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RedirectClass {

	
	//1. A function to extract from wikitimes dump and save in a folder
	//2. Get all redirects for Stories, also consider the stories in Redirect file if present.
	//3. A function to read from the above folder and create a story, timeseries file(Use redirect function)
	//4. A function to read from folder and create a story, timeseries and frequency file(Use redirect file)
	//5. Get timeseries for all stories present in wikitimes and from folders
	//6. Get main article, suggested nad related articles for all stories.
	//7. Remove blacklist and entities from the story list.
	
	//2. Get all redirects for Stories, also consider the stories in Redirect file if present.
	//Called from: StartPreprossesing.main
	void getRedirects(Set StoryList, String Redirect) {
		String line = "";
		BufferedReader br = null;
		Map<String, String> StoryRedirects = new HashMap<String, String>();
		Set StoryPageNames = StoryList;
		Set RedirectStoryNames = new HashSet();
		try {
			br = new BufferedReader(new FileReader(Redirect));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(";");
				for(int i= 0;i<data.length;i++)
				{
					RedirectStoryNames.add(data[i].trim());
				}				
			}
			StoryPageNames.addAll(RedirectStoryNames);
			Iterator it = StoryPageNames.iterator();
			while (it.hasNext()) {
				String storyName = it.next().toString();
				String storyPageName = isRedirect(storyName);
				if (StoryRedirects.containsKey(storyPageName)) {
					String temp = StoryRedirects.get(storyPageName);
					temp = temp.concat(";" + storyName);
					StoryRedirects.remove(storyPageName);
					StoryRedirects.put(storyPageName, temp);					
				} else if (storyPageName.equals("")) {
					if (!StoryRedirects.containsKey(storyName)) {
						StoryRedirects.put(storyName, ";");
					}
				} else {
					StoryRedirects.put(storyPageName, storyName);					
				}				
			}			
			File statText = new File(Redirect);
			FileOutputStream is;
			is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			for (Map.Entry<String, String> pairs : StoryRedirects.entrySet()) {
				w.write(pairs.getKey() + ";" + pairs.getValue() + "\n");
			}
			w.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(RedirectClass.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(RedirectClass.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
	
	//Get redirects from Wikipedia. 
	//Called from: ExtractFromFileFolder.getRedirects
	public String isRedirect(String storyName) {
		String storyPageName = "";
		try {
			String redirects = "https://en.wikipedia.org/w/api.php?action=query&titles="
					+ storyName
					+ "&format=json&prop=extracts&continue&redirects&prop=revisions";
			URL url = new URL(redirects);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String storyJson = in.readLine();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(storyJson);
			JSONObject jsonStory = (JSONObject) obj;
			JSONObject jsonQuery = (JSONObject) jsonStory.get("query");

			if (jsonQuery.containsKey("redirects")) {
				JSONArray jsonPages = (JSONArray) jsonQuery.get("redirects");
				for (Object revision : jsonPages) {
					JSONObject objects = (JSONObject) revision;
					storyPageName = objects.get("to").toString();
				}
			}
			if (!storyPageName.equals("") && storyPageName.contains("#")) {
				storyPageName = storyPageName.substring(0,
						storyPageName.indexOf("#"));
			}
			storyPageName.replaceAll(" ", "_");
			System.out.println(storyName+";"+storyPageName);
		} catch (MalformedURLException ex) {
			Logger.getLogger(RedirectClass.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(RedirectClass.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (ParseException ex) {
			Logger.getLogger(RedirectClass.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return storyPageName.replaceAll(" ", "_");
	}
	
	//Reads from Redirect file and returns a Map with Redirected to and the story page names
	Map returnRedirectMap(String Redirect)
	{
		Map<String,String> RedirectStoryMap = new HashMap<String, String>();
		String line = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(Redirect));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(";");
				String redirect = data[0];
				Set story = new HashSet();
				for(int i=1;i<data.length;i++)
				{
					if (!data[i].equals("")) {
						RedirectStoryMap.put(data[i],
								redirect);
					}
				}
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(RedirectClass.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(RedirectClass.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return RedirectStoryMap;
	}
}
