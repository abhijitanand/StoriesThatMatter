package Misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.Days;

public class MergetrainTest {
	public static void main(String[] args) {
		/*
		 * String inputFile =
		 * "/home/aanand/workspace/StoriesThatMatter/Data/Misc/MergeData/test-1.txt"
		 * ; String outFolder =
		 * "/home/aanand/workspace/StoriesThatMatter/Data/Misc/MergeData/"; int
		 * diff = 0; MergetrainTest merge = new MergetrainTest();
		 * merge.getLag(inputFile, outFolder, diff);
		 */

		/*String folder = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/MergeData/3/";
		String test = folder+"test-1.txt";
		String train = folder+"train-1.txt";
		String outFile = folder+"mergetesttrain-1.txt";
		MergetrainTest merge = new MergetrainTest();
		merge.mergeAllFiles(test, train, outFile);*/
		
		String folder = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/MergeData/3/";
		String mergeTestTrain = folder +"mergetesttrain-1.txt";
		int startDate = 20140101;
		int trainDur = 8;
		int testDuration = 4;
		String ouptutLocation = folder+"newTestTrain/";
		MergetrainTest merge = new MergetrainTest();
		merge.generateTestTrain(mergeTestTrain, startDate, trainDur, testDuration, ouptutLocation);
	}

	// only get lags of +-2 and +-1 day data
	void getLag(String inputFile, String outFolder, int diff) {
		String line = "";
		BufferedReader br = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		try {
			File file = new File(inputFile);
			File statText = new File(outFolder + diff + "/" + file.getName());
			FileOutputStream is;
			is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			br = new BufferedReader(new FileReader(inputFile));
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				if (data[24].contains("From-WCEP")) {
					String[] WCEP = data[24].split(":");
					Date d1 = format.parse(data[1]);
					Date d2 = format.parse(WCEP[1]);
					DateTime dt1 = new DateTime(d1);
					DateTime dt2 = new DateTime(d2);
					int diff1 = Days.daysBetween(dt1, dt2).getDays();
					if ((diff1 < -diff || diff1 > diff)
							&& Integer.parseInt(data[0]) == 1) {
						w.write("0");
						for (int i = 1; i < data.length; i++) {
							w.write("\t" + data[i]);
						}
						w.write("\n");
					} else {
						w.write(line + "\n");
					}
					// System.out.println(WCEP[1]);
				} else {
					w.write(line + "\n");
				}

			}
			w.close();
		} catch (IOException | ParseException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	void mergeAllFiles(String test, String train, String outFile) {
		String line = "";
		BufferedReader br = null;
		try {
			File statText = new File(outFile);
			FileOutputStream is;
			is = new FileOutputStream(statText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			br = new BufferedReader(new FileReader(test));
			while ((line = br.readLine()) != null) {
				w.write(line + "\n");
			}
			br = new BufferedReader(new FileReader(train));
			while ((line = br.readLine()) != null) {
				w.write(line + "\n");
			}

		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	void generateTestTrain(String mergeTestTrain, int startDate, int trainDur,
			int testDuration, String ouptutLocation) {
		String line = "";
		BufferedReader br = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		
		try {
			Calendar cal1 = Calendar.getInstance();
			Date start = format.parse(Integer.toString(startDate));
			cal1.setTime(start);
			for (int i = 1; i < 6; i++) {

				File statText = new File(ouptutLocation + "train-" + i
						+ ".txt");
				FileOutputStream is;
				is = new FileOutputStream(statText);
				OutputStreamWriter osw = new OutputStreamWriter(is);
				Writer w = new BufferedWriter(osw);
				File statText1 = new File(ouptutLocation + "test-" + i
						+ ".txt");
				FileOutputStream is1;
				is1 = new FileOutputStream(statText1);
				OutputStreamWriter osw1 = new OutputStreamWriter(is1);
				Writer w1 = new BufferedWriter(osw1);

				Calendar cal = Calendar.getInstance();
				//start = cal1.getTime();
				cal.setTime(start);
				cal.add(Calendar.MONTH, trainDur); // minus number would
				cal.add(Calendar.DATE, -1);								// decrement
													// the days
				Date trainEnd = cal.getTime();
				cal.add(Calendar.DATE, 1);
				Date testStartDate = cal.getTime();
				cal.add(Calendar.MONTH, testDuration);
				cal.add(Calendar.DATE, -1);
				Date testEndDate = cal.getTime();

				br = new BufferedReader(new FileReader(mergeTestTrain));
				while ((line = br.readLine()) != null) {
					String[] data = line.split("\t");
					Date current = format.parse(data[1]);

					
					if (start.compareTo(current) <= 0
							&& trainEnd.compareTo(current) >= 0) {
						w.write(line + "\n");
					}
					if (testStartDate.compareTo(current) <= 0
							&& testEndDate.compareTo(current) >= 0) {
						w1.write(line + "\n");
					}
				}
				w.close();
				w1.close();
				//cal1.add(Calendar.MONTH, trainDur);
				//cal1.add(Calendar.DATE, 1);
				trainDur++;
			}
		}

		catch (IOException | ParseException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

}
