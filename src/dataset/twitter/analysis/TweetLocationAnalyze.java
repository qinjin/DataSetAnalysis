package dataset.twitter.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

import dataset.chart.ChartUtils;
import dataset.utils.AnalyzeUtils;
import dataset.utils.ConfReader;

/**
 * Analyze: Users are always tweet from the same data center.
 * 
 * @author qinjin.wang
 *
 */
public class TweetLocationAnalyze implements IAnalyze {
    private static final Logger logger = LogManager
	    .getLogger(TweetLocationAnalyze.class);
    private static final String TWEET_LOCATION_DISTRIBUTION = "3_tweet_location_distribution";
    private static final String TWEET_LOCATION_PERCENTAGE = "3_tweet_location_percentage";
    public static final String CACHED_TWEETS_LOCATION_FILE = "output/"
	    + TWEET_LOCATION_DISTRIBUTION + ".txt";
    // [user ID, percentage of the tweets from the most tweeted timezone]
    private final Map<Integer, Double> tweetsLocationMap;

    public TweetLocationAnalyze() {
	this.tweetsLocationMap = Maps.newHashMap();
    }

    @Override
    public void executeAnalyze() {
	logger.info("START ANALYZE " + this.getClass().getSimpleName());
	try {
	    ConfReader confReader = new ConfReader();
	    File tweetsDir = confReader.getTweetsDir();
	    File cachedFile = new File(CACHED_TWEETS_LOCATION_FILE);
	    if (tweetsDir.exists() && tweetsDir.isDirectory()) {
		if (cachedFile.exists()) {
		    tweetsLocationMap.putAll(AnalyzeUtils
			    .readFromDoubleFile(cachedFile));
		} else {
		    tweetsLocationMap.putAll(calcTweetsLocation(tweetsDir));
		    AnalyzeUtils
			    .saveDoubleToFile(tweetsLocationMap, cachedFile);
		}
	    } else {
		throw new Exception("Can not find dir: " + tweetsDir);
	    }
	} catch (Exception ex) {
	    logger.fatal("Error on " + this.getClass().getSimpleName() + ": "
		    + ex.getMessage());
	    ex.printStackTrace();
	}
    }

    private Map<Integer, Double> calcTweetsLocation(File tweetsDir) {
	Map<Integer, UserTweetsLocation> map = Maps.newHashMap();
	doCalcTweetsLocation(map, tweetsDir);
	logger.debug("Done cacluate tweet location.");
	return calcPercentage(map);
    }

    private void doCalcTweetsLocation(Map<Integer, UserTweetsLocation> map,
	    File file) {
	if (file.isDirectory()) {
	    for (File child : file.listFiles()) {
		doCalcTweetsLocation(map, child);
	    }
	} else {
	    try {
		List<String> lines = Files.readLines(file,
			Charset.defaultCharset());
		Integer currentID = Integer.valueOf(file.getName());
		for (String line : lines) {
		    // Parse time zone.
		    if (line.startsWith("Time") && currentID != null) {
			if (map.get(currentID) == null) {
			    map.put(currentID,
				    new UserTweetsLocation(currentID));
			}
			String timezone = parseTimeZone(line);
			if (!timezone.isEmpty()) {
			    map.get(currentID).stepNumTweetsForTimeZone(
				    timezone);
			}
		    }
		}
	    } catch (IOException e) {
		logger.fatal("Error on parse file " + file + ": "
			+ e.getMessage());
	    }
	}
    }

    private String parseID(String line) {
	return line.substring(line.indexOf("ID:") + 3).trim();
    }

    // Time record is like: Time: Wed Dec 15 08:30:53 CST 2010
    private String parseTimeZone(String timeRecord) {
	if (timeRecord.length() < 9) {
	    logger.fatal("Error format for Time: " + timeRecord);
	    return "";
	}

	return timeRecord.substring(timeRecord.length() - 8,
		timeRecord.length() - 5);
    }

    private Map<Integer, Double> calcPercentage(
	    Map<Integer, UserTweetsLocation> map) {

	Map<Integer, Double> percentageMap = Maps.newHashMap();
	for (Map.Entry<Integer, UserTweetsLocation> entry : map.entrySet()) {
	    percentageMap.put(entry.getKey(), entry.getValue()
		    .calcTweetsFromOneTimeZonePercentage());
	}

	logger.debug("Done caclulate percentage for tweet location distribution.");
	return percentageMap;
    }

    @Override
    public void drawResult() {
	// AnalyzeUtils.printMap(tweetsLocationMap);
	// Draw raw chart for location distribution.
	logger.info("Drawing tweet location distribution...");
	ChartUtils.drawDoubleChart("", "Tweet location distribution",
		"User IDs", "Tweet location centerlization",
		TWEET_LOCATION_DISTRIBUTION, tweetsLocationMap);

	// Draw aggregated chart for the percentages.
	Map<Integer, Integer> aggMap = aggTweetsLocationNums(tweetsLocationMap);
	AnalyzeUtils.printMap(aggMap);
	ChartUtils.drawBarChart("", "Aggregated tweet location distribution",
		"Probabilities of tweeting in the same time-zone",
		"Number of users", TWEET_LOCATION_PERCENTAGE,
		aggMap, 10, 110);
	logger.info("Done drawing tweet location  distribution");
	logger.info("DOND ANALYZE " + this.getClass().getSimpleName());
    }

    // The aggregated maps is [the probabilities, the numbers]
    private Map<Integer, Integer> aggTweetsLocationNums(Map<Integer, Double> map) {
	Map<Integer, Integer> result = Maps.newHashMap();
	for (Map.Entry<Integer, Double> entry : map.entrySet()) {
	    int probablity = (int) (entry.getValue() * 100);
	    roundAndSaveValue(result, probablity);
	}
	return result;
    }

    private void roundAndSaveValue(Map<Integer, Integer> map,
	    int valueBeforeRound) {
	if (valueBeforeRound > 100) {
	    logger.warn("Invalid probability: " + valueBeforeRound);
	    return;
	}
	
	if (valueBeforeRound % 10 > 4) {
	    valueBeforeRound = valueBeforeRound + (10 - valueBeforeRound % 10);
	} else {
	    valueBeforeRound = valueBeforeRound - valueBeforeRound % 10;
	}

	if (map.get(valueBeforeRound) == null) {
	    map.put(valueBeforeRound, 1);
	} else {
	    int newNum = map.get(valueBeforeRound) + 1;
	    map.put(valueBeforeRound, newNum);
	}
    }

    @Test
    public void testParseID() {
	String ID = "ID: 23018800500";
	String actual = parseID(ID);
	Assert.assertEquals("23018800500", actual);
    }

    @Test
    public void testParseTimeZone() {
	String timeRecord = "Time: Wed Dec 15 08:30:53 CST 2010";
	String actual = parseTimeZone(timeRecord);
	Assert.assertEquals("CST", actual);
    }
}
