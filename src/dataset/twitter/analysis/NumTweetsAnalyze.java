package dataset.twitter.analysis;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import dataset.chart.ChartUtils;
import dataset.db.DBProvider;
import dataset.utils.ConfReader;
import dataset.utils.AnalyzeUtils;

/**
 * Analyze num of tweets and num of followers: If there are more followers for a
 * user, there will be more tweets created by the user.
 * 
 * @author qinjin.wang
 *
 */
public class NumTweetsAnalyze implements IAnalyze {
    public static final String CACHED_TWEETS_NUMBER_FILE = "output/tweets_number_distribution.txt";
    private final Map<Integer, Integer> followerNumberTweetsMap = Maps
	    .newHashMap();

    @Override
    public void executeAnalyze() {
	try {
	    final File tweetsNumberFile = new File(CACHED_TWEETS_NUMBER_FILE);
	    if (tweetsNumberFile.exists()) {
		System.out
			.println("Read tweets number distribution from cached file");
		followerNumberTweetsMap.putAll(AnalyzeUtils
			.readFromFile(tweetsNumberFile));
	    } else {
		System.out
			.println("Start to process tweets number distribution");
		ConfReader confReader = new ConfReader();
		List<UserProfiler> allProfilers = confReader
			.getAllUserProfilers(DBProvider.getInstance()
				.getCityStateMap());
		followerNumberTweetsMap
			.putAll(calcFollowerNumTweetsMap(allProfilers));
		AnalyzeUtils.saveToFile(followerNumberTweetsMap,
			tweetsNumberFile);
	    }

	} catch (Exception ex) {
	    System.err.println("Error on " + this.getClass().getSimpleName());
	    ex.printStackTrace();
	}
    }

    @Override
    public void drawResult() {
	System.out.println("Drawing follower tweets distribution...");
	ChartUtils.drawChart("", "Follower and Tweets number distribution",
		"Number of followers", "Number of tweets",
		"follower_tweets_distribution", AnalyzeUtils.simplefilter(-1,
			-1, -1, -1, followerNumberTweetsMap));
	System.out.println("Done process follower tweets distribution");
    }

    private Map<Integer, Integer> calcFollowerNumTweetsMap(
	    List<UserProfiler> allProfilers) {
	Map<Integer, Integer> result = Maps.newHashMap();
	for (UserProfiler user : allProfilers) {
	    if (result.get(user.followers) == null) {
		result.put(user.followers, user.status);
	    } else {
		int numTweets = result.get(user.followers) + user.status;
		result.put(user.followers, numTweets);
	    }
	}
	return result;
    }

}
