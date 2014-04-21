package dataset.twitter.analysis;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import dataset.utils.ConfReader;

/**
 * Analyze: Users are always tweet from the same data center.
 * 
 * @author qinjin.wang
 *
 */
public class TweetLocationAnalyze implements IAnalyze {
    private static final Logger logger = LogManager.getLogger(TweetLocationAnalyze.class);
    private static final String TWEET_LOCATION_DISTRIBUTION = "3_tweet_location_distribution";
    //[user ID, percentage of the tweets from the most tweeted timezone]
    private final Map<BigDecimal, BigDecimal> tweetsLocationMap;

    public TweetLocationAnalyze() {
	this.tweetsLocationMap = Maps.newHashMap();
    }

    @Override
    public void executeAnalyze() {
	logger.info("Started to analyze tweet location...");
	try {
	    ConfReader confReader = new ConfReader();
	    File tweetsDir = confReader.getTweetsDir();
	    if (tweetsDir.exists() && tweetsDir.isDirectory()) {
		tweetsLocationMap.putAll(calcTweetsLocation(tweetsDir));
	    } else {
		throw new Exception("Can not find dir: " + tweetsDir);
	    }
	} catch (Exception ex) {
	    logger.fatal("Error on " + this.getClass().getSimpleName()
		    + ": " + ex.getMessage());
	    ex.printStackTrace();
	}
    }

    private Map<BigDecimal, BigDecimal> calcTweetsLocation(File tweetsDir) {
	Map<BigDecimal, UserTweetsLocation> map = Maps.newHashMap();
	doCalcTweetsLocation(map, tweetsDir);
	return calcPercentage(map);
    }

    private void doCalcTweetsLocation(Map<BigDecimal, UserTweetsLocation> map,
	    File file) {
	if (file.isDirectory()) {
	    for (File child : file.listFiles()) {
		doCalcTweetsLocation(map, child);
	    }
	} else {
	    try {
		List<String> lines = Files.readLines(file,
			Charset.defaultCharset());
		BigDecimal currentID = BigDecimal.valueOf(Long.valueOf(file.getName()));
		for (String line : lines) {
		    //Parse time zone.
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

    private Map<BigDecimal, BigDecimal> calcPercentage(
	    Map<BigDecimal, UserTweetsLocation> map) {

	Map<BigDecimal, BigDecimal> percentageMap = Maps.newHashMap();
	for (Map.Entry<BigDecimal, UserTweetsLocation> entry : map.entrySet()) {
	    percentageMap.put(entry.getKey(), entry.getValue()
		    .calcTweetsFromOneTimeZonePercentage());
	}

	logger.debug("Caclulated percentage.");
	return percentageMap;
    }

    @Override
    public void drawResult() {
	logger.info("Drawing tweet location distribution...");
	ChartUtils.drawDecimalChart("", "Tweet location distribution",
		"User IDs", "Tweet location centerlization",
		TWEET_LOCATION_DISTRIBUTION, tweetsLocationMap);
	logger.info("Done drawing tweet location  distribution");

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
