package dataset.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import dataset.twitter.analysis.UserProfiler;

public class ConfReader {
    private static final List<UserProfiler> allProfiles =  Lists.newArrayList();
    private final Properties configuration;

    public ConfReader() {
	configuration = new Properties();
	String confStr = "conf" + File.separator + "dataset.conf";
	try {
	    configuration.load(new FileInputStream(new File(confStr)));
	} catch (IOException e) {
	    System.err.println("Failed to load configuration " + confStr);
	    e.printStackTrace();
	}
    }

    public File getProfilerFile() {
	return new File(
		configuration.getProperty("dataset.twitter.profiles.file"));
    }

    public File getNetworkFile() {
	return new File(
		configuration.getProperty("dataset.twitter.network.file"));
    }
    
    public File getNetworkDir(){
	return new File(configuration.getProperty("dataset.twitter.network.dir"));
    }

    public File getTweetsDir() {
	return new File(configuration.getProperty("dataset.twitter.tweets.dir"));
    }

    public List<UserProfiler> getAllUserProfilers(
	    Map<String, String> allCityStateMap) throws Exception {
	if (allProfiles.isEmpty()) {
	    File profileFile = getProfilerFile();
	    System.out.println("Start to read profile file at: " + new Date());
	    List<String> allLines = Files.readLines(profileFile,
		    Charset.defaultCharset());
	    System.out.println("Done read profile file at: " + new Date());
	    allProfiles.addAll(parseAllUserProfilesToState(allCityStateMap,
		    allLines));

	    System.out.println("Analyzed " + allProfiles.size()
		    + " user profilers from " + allLines.size() + " records.");
	} else {
	    System.out.println("Read all user profilers from cache.");
	}

	return allProfiles;
    }

    private List<UserProfiler> parseAllUserProfilesToState(
	    Map<String, String> allCityStateMap, List<String> allLines) {
	List<UserProfiler> allProfilers = Lists.newArrayList();
	for (String line : allLines) {
	    try {
		UserProfiler profiler = new UserProfiler(line, allCityStateMap);
		allProfilers.add(profiler);
		// System.out.println("User "+ profiler.id
		// +" followers "+profiler.followers+
		// " location "+profiler.location);
	    } catch (Exception ex) {
		// System.err.println(ex);
	    }
	}
	return allProfilers;
    }

}
