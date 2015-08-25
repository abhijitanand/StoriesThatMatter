package Preprossesing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StartPreprossesing {

	
	
	public static void main(String[] args) {
		String fileEntityDate = "./data/Baseline/fileEntityDate.csv";
		String redirectFileEntityDate = "./data/Baseline/RedirectfileEntityDate.csv";
		String storyMainArticle = "./data/WikipediaData/EventMainArticle1.csv";
		String mappingFile = "./data/WikipediaData/EventMainArticle1.csv";
		String TimeSeries = "C:\\Users\\aanand\\Documents\\NetBeansProjects\\aster\\data\\WikipediaData\\TimeSeries.csv";
		String Redirect = "C:\\Users\\aanand\\Documents\\NetBeansProjects\\aster\\data\\WikipediaData\\RedirectStoryNames.csv";
		
		//Here we assume that the fileEntityDate contains stories which are right i.e correct redirect story names
		StartPreprossesing process = new StartPreprossesing();
		//get Story list from fileEntityDate.
		Set StoryList = process.getStoryListFromFile(fileEntityDate);
		RedirectClass redirect = new RedirectClass();
		//get redirects of stories and write to redirect file.
		redirect.getRedirects(StoryList, Redirect);
		
		//Change the story names to their redirected names and write to a file redirectFileEntityDate
		process.writeRedirectFileEntityDate(fileEntityDate, Redirect, redirectFileEntityDate);
		//get timeseries of stories and write to timeseries file.
		Set newStoryList = process.getStoryListFromFile(fileEntityDate);
		CreateTimeSeries time = new CreateTimeSeries();
		time.getTimeSeries(newStoryList, TimeSeries);
	}
	Set getStoryListFromFile(String fileEntityDate)
	{
		String line = "";
		BufferedReader br = null;
		Set storyName = new HashSet();
		try {
			br = new BufferedReader(new FileReader(fileEntityDate));
			while ((line = br.readLine()) != null) {
				String story = line.split(";")[0].trim();
				if (story.contains("#")) {
					story = story.substring(0, story.indexOf("#"));
				}
				storyName.add(story);
			}

		} catch (IOException ex) {
			Logger.getLogger(StartPreprossesing.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return storyName;
	}
	void writeRedirectFileEntityDate(String fileEntityDate, String Redirect,String redirectFileEntityDate)
	{
		String line = "";
		BufferedReader br = null;
		RedirectClass red = new RedirectClass();
		Map<String,String> RedirectStoryMap = red.returnRedirectMap(Redirect);
		try {
			File statText = new File(redirectFileEntityDate);
			FileOutputStream is;
			is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			br = new BufferedReader(new FileReader(fileEntityDate));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(";");
				String story = data[0].trim();
				if (story.contains("#")) {
					story = story.substring(0, story.indexOf("#"));
				}
				if(RedirectStoryMap.containsKey(story))
				{
					story = RedirectStoryMap.get(story);
				}
				w.write(story);
				for(int i=1;i<data.length;i++)
				{
					w.write(";"+data[i]);
				}
				w.write("\n");
			}
			w.close();
		} catch (IOException ex) {
			Logger.getLogger(StartPreprossesing.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
}
