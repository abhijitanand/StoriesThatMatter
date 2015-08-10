package Preprossesing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StartPreprossesing {

	
	
	public static void main(String[] args) {
		String fileEntityDate = "./data/Baseline/fileEntityDate.csv";
		String storyMainArticle = "./data/WikipediaData/EventMainArticle1.csv";
		String mappingFile = "./data/WikipediaData/EventMainArticle1.csv";
		String TimeSeries = "C:\\Users\\aanand\\Documents\\NetBeansProjects\\aster\\data\\WikipediaData\\TimeSeries.csv";
		String Redirect = "C:\\Users\\aanand\\Documents\\NetBeansProjects\\aster\\data\\WikipediaData\\RedirectStoryNames.csv";
		
		StartPreprossesing process = new StartPreprossesing();
		//get Story list from fileEntityDate.
		Set StoryList = process.getStoryListFromFile(fileEntityDate);
		RedirectClass redirect = new RedirectClass();
		//get redirects of stories and write to redirect file.
		redirect.getRedirects(StoryList, Redirect);
		//get timeseries of stories and write to timeseries file.
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
}
