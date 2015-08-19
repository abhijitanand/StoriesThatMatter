package Misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AggregateSources {

	Set notFound = new HashSet<>();
	Map<String, String> CodeCountry = new HashMap<String, String>();

	public static void main(String[] args) {
		String sourceFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/stics-urls.txt";
		String sourceCountryFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/DomainCodeCountry.csv";
		String outFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/SourceCountyMapping.csv";
		String CountyCodeCountry = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/CountyCodeCountry.csv";

		String vinayFile = "/home/aanand/workspace/StoriesThatMatter/Data/Misc/750Mapping.txt";
		AggregateSources agg = new AggregateSources();
		// agg.aggregateSourcesByCountry(sourceFile, sourceCountryFile, outFile,
		// CountyCodeCountry);
		agg.getVinayResults(sourceCountryFile, CountyCodeCountry);

	}

	void getVinayResults(String sourceCountryFile, String CountyCodeCountry) {
		String line = "";
		BufferedReader br = null;
		// Map<String, String> SourceCountry =
		// getSourceCountryMap(sourceCountryFile);
		Map<String, String> CountryCodeCountry = populateCodeCountry(CountyCodeCountry);
		try {

			br = new BufferedReader(new FileReader(sourceCountryFile));
			line = br.readLine();
			Map<String, Integer> MapCountrySourceCount = new HashMap<String, Integer>();
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				String source = data[0];
				// System.out.println(source);
				String Code = data[1];
				String Country = data[2];
				if (CountryCodeCountry.containsKey(Code)
						&& CountryCodeCountry.get(Code).equals(Country)) {
					if (MapCountrySourceCount.containsKey(Country)) {
						int temp = MapCountrySourceCount.get(Country);
						temp++;
						MapCountrySourceCount.remove(Country);
						MapCountrySourceCount.put(Country, temp);
					} else {
						MapCountrySourceCount.put(Country, 1);
					}
				}
				/*
				 * else { System.out.println(source+"\t"+Code+"\t"+Country); }
				 */
			}
			for (Map.Entry<String, Integer> pairs : MapCountrySourceCount
					.entrySet()) {
				System.out.println(pairs.getKey() + "\t" + pairs.getValue());
			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	void aggregateSourcesByCountry(String sourceFile, String sourceCountryFile,
			String outFile, String CountyCodeCountry) {
		Map<String, Set> SourceMapping = getAllSourcesMap(sourceFile);
		Map<String, String> SourceCountry = getSourceCountryMap(sourceCountryFile);
		Map<String, String> CountryCodeCountry = populateCodeCountry(CountyCodeCountry);

		File statText = new File(outFile);
		FileOutputStream is;
		try {
			is = new FileOutputStream(statText);

			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			for (Map.Entry<String, Set> pairs : SourceMapping.entrySet()) {
				String key = pairs.getKey();
				String country = "";

				if (!SourceCountry.containsKey(key)) {
					String key1 = key;
					key1 = key1.substring(key1.indexOf(".") + 1, key1.length());
					String ext = key.substring(key.lastIndexOf(".") + 1,
							key.length());
					if (SourceCountry.containsKey(key1)) {
						String value = SourceCountry.get(key1);
						w.write(key + "\t" + value + "\t"
								+ CodeCountry.get(value) + "\n");

					} else if (CountryCodeCountry.containsKey(ext)) {
						if (ext.equals("gov") || ext.equals("mil")
								|| ext.equals("cc")) {
							w.write(key + "\t" + "US" + "\t"
									+ CountryCodeCountry.get("US") + "\n");
						} else {
							w.write(key + "\t" + ext.toUpperCase() + "\t"
									+ CountryCodeCountry.get(ext) + "\n");
						}

					} else {
						System.out.println(key);
					}
				}

			}

			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Map getSourceCountryMap(String sourceCountryFile) {
		String line = "";
		BufferedReader br = null;
		Map<String, String> SourceCountry = new HashMap<String, String>();
		try {
			br = new BufferedReader(new FileReader(sourceCountryFile));
			line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				if (data.length > 2) {
					SourceCountry.put(data[0], data[1]);
					CodeCountry.put(data[1], data[2]);
				}

			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return SourceCountry;
	}

	// Creates a map with all related sources
	Map getAllSourcesMap(String sourceFile) {
		String line = "";
		BufferedReader br = null;
		Map<String, Set> SourceMapping = new HashMap<String, Set>();
		try {
			br = new BufferedReader(new FileReader(sourceFile));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(" ");
				String key = "";
				int next = 0;
				if (data[next].equals("")) {
					next++;
					// key = getFinalURL(data[next]);
					key = getSourceNameOnly(data[next]);
					next++;
				} else {
					// key = getFinalURL(data[1]);
					key = getSourceNameOnly(data[next]);
					next++;

				}
				Set mapping = new HashSet<>();
				if (data.length > next) {
					for (int i = next; i < data.length; i++) {
						if (!data[i].equals("")) {
							// String str = getFinalURL(data[next]);
							String str = getSourceNameOnly(data[next]).trim();

							mapping.add(str);
						}

					}
				}
				SourceMapping.put(key, mapping);

			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return SourceMapping;
	}

	String getFinalURL(String url) {
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) new URL(url).openConnection();

			con.setInstanceFollowRedirects(false);
			con.connect();
			// int res = con.getResponseCode();
			con.getInputStream();
			// res = con.getResponseCode();
			// url = con.getURL().toString();
			if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
					|| con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				url = con.getHeaderField("Location");
				// return getFinalURL(redirectUrl);
			}
			if (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN
					|| con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
				notFound.add(url);
			}
			con.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// con.disconnect();
		return url;
	}

	// remove http and https prefix from the url and get domain names
	String getSourceNameOnly(String source) {
		String url = source;
		if (source.contains("http://www.")) {
			// System.out.println(source.lastIndexOf("http://www."));
			url = url.replace("http://www.", "");
			if (url.contains("/")) {
				source = url.substring(0, url.indexOf("/"));
			} else {
				source = url;
			}

		}
		if (source.contains("http://")) {
			url = url.replace("http://", "");
			if (url.contains("/")) {
				source = url.substring(0, url.indexOf("/"));
			} else {
				source = url;
			}
		}
		if (source.contains("https://www.")) {
			url = url.replace("https://www.", "");
			if (url.contains("/")) {
				source = url.substring(0, url.indexOf("/"));
			} else {
				source = url;
			}
		}
		if (source.contains("https://")) {
			url = url.replace("https://", "");
			if (url.contains("/")) {
				source = url.substring(0, url.indexOf("/"));
			} else {
				source = url;
			}
		}
		return source;
	}

	Map<String, String> populateCodeCountry(String CountryCodeCountry) {
		String line = "";
		BufferedReader br = null;
		Map<String, String> CodeCountry = new HashMap<String, String>();
		try {
			br = new BufferedReader(new FileReader(CountryCodeCountry));
			line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\t");
				CodeCountry.put(data[0].toUpperCase(), data[1]);
			}
		} catch (IOException ex) {
			Logger.getLogger(AggregateSources.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return CodeCountry;
	}
}
