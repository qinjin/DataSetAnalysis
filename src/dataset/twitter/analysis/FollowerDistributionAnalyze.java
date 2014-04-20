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
 * Analyze locality and Number of followers.
 * 
 * @author qinjin.wang
 *
 */
public class FollowerDistributionAnalyze implements IAnalyze {
    public static final String CACHED_FOLLOWER_NUMBER_FILE = "output/follower_number_distribution.txt";
    private final Map<Integer, Integer> followerNumberDistribution;

    public FollowerDistributionAnalyze() {
	followerNumberDistribution = Maps.newHashMap();
    }

    @Override
    public void executeAnalyze() {
	ConfReader confReader = new ConfReader();
	try {
	    final File followerNumberFile = new File(
		    CACHED_FOLLOWER_NUMBER_FILE);
	    if (followerNumberFile.exists()) {
		System.out
			.println("Read follower number distribution from cached file");
		followerNumberDistribution
			.putAll(AnalyzeUtils.readFromFile(followerNumberFile));
	    } else {
		List<UserProfiler> allProfiles = confReader
			.getAllUserProfilers(DBProvider.getInstance()
				.getCityStateMap());
		System.out
			.println("Start to process follower number distribution");
		followerNumberDistribution
			.putAll(calcFollowerNumberDistribution(allProfiles));
		AnalyzeUtils.saveToFile(followerNumberDistribution, followerNumberFile);
	    }

	    // TODO
	    // Map<Integer, Integer> followerRegionMap =
	    // calcFollowerRegions(allProfiles);
	} catch (Exception ex) {
	    System.err.println("Error on " + this.getClass().getSimpleName());
	    ex.printStackTrace();
	}
    }

    @Override
    public void drawResult() {
	System.out.println("Drawing follower number distribution...");
	ChartUtils.drawChart("", "Follower number distribution",
		"Number of followers", "Number of users",
		"follower_number_distribution", AnalyzeUtils.simplefilter(-1,
			-1, -1, 1, followerNumberDistribution));
	// Filter to remove the follower number is bigger than 1999 and the user
	// number is smaller than 2.
	ChartUtils.drawChart("", "Follower number distribution",
		"Number of followers", "Number of users",
		"follower_number_distribution_0_2000", AnalyzeUtils.simplefilter(2000,
			-1, -1, 1, followerNumberDistribution));
	System.out.println("Done drawing follower number distribution");
    }

    /**
     * Calculate follower number distribution which will return [Number of
     * followers, number of user has that number of followers]
     */
    private Map<Integer, Integer> calcFollowerNumberDistribution(
	    List<UserProfiler> allProfiles) {
	Map<Integer, Integer> followersDistributionMap = Maps.newHashMap();
	for (UserProfiler user : allProfiles) {
	    if (followersDistributionMap.get(user.followers) == null) {
		followersDistributionMap.put(user.followers, 1);
	    } else {
		int numUser = followersDistributionMap.get(user.followers) + 1;
		followersDistributionMap.put(user.followers, numUser);
	    }
	}
	return followersDistributionMap;
    }

    /**
     * Calculate follower region distribution which will return [Number
     * followers, Number follower from another region]
     */
    private Map<Integer, Integer> calcFollowerRegions(
	    List<UserProfiler> allProfiles) {
	return Maps.newHashMap();
    }
}
