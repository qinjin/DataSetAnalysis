package dataset.twitter.analysis;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public static final String FOLLOWER_NUMBER = "1_follower_number_distribution";
    public static final String FOLLOWER_REGION = "1_follower_region_distribution";
    public static final String CACHED_FOLLOWER_NUMBER_FILE = "output/"
	    + FOLLOWER_NUMBER + ".txt";
    public static final String CACHED_FOLLOWER_REGION_FILE = "output/"
	    + FOLLOWER_REGION + ".txt";
    // [Num followers, Num users]
    private final Map<Integer, Integer> followerNumberDistribution;
    // [Num followers, Num regions]
    Map<Integer, Integer> followerRegionDistribution;

    public FollowerDistributionAnalyze() {
	followerNumberDistribution = Maps.newHashMap();
	followerRegionDistribution = Maps.newHashMap();
    }

    @Override
    public void executeAnalyze() {
	ConfReader confReader = new ConfReader();
	try {
	    processFollowerNumbers(confReader);
	    processFollowerRegions(confReader);
	} catch (Exception ex) {
	    System.err.println("Error on " + this.getClass().getSimpleName());
	    ex.printStackTrace();
	}
    }

    private void processFollowerNumbers(ConfReader confReader) throws Exception {
	final File followerNumberFile = new File(CACHED_FOLLOWER_NUMBER_FILE);
	if (followerNumberFile.exists()) {
	    System.out
		    .println("Read follower number distribution from cached file");
	    followerNumberDistribution.putAll(AnalyzeUtils
		    .readFromFile(followerNumberFile));
	} else {
	    System.out
		    .println("Start to process follower number distribution...");
	    List<UserProfiler> allProfiles = confReader
		    .getAllUserProfilers(DBProvider.getInstance()
			    .getCityStateMap());
	    followerNumberDistribution
		    .putAll(calcFollowerNumberDistribution(allProfiles));
	    AnalyzeUtils.saveToFile(followerNumberDistribution,
		    followerNumberFile);
	}
    }

    private void processFollowerRegions(ConfReader confReader) throws Exception {
	final File followerRegionFile = new File(CACHED_FOLLOWER_REGION_FILE);
	if (followerRegionFile.exists()) {
	    System.out
		    .println("Read follower region distribution from cached file");
	    followerRegionDistribution.putAll(AnalyzeUtils
		    .readFromFile(followerRegionFile));
	} else {
	    System.out
		    .println("Start to process follower region distribution...");
	    List<UserProfiler> allProfiles = confReader
		    .getAllUserProfilers(DBProvider.getInstance()
			    .getCityStateMap());
	    File networkDir = confReader.getNetworkDir();
	    if (networkDir.exists() && networkDir.isDirectory()) {
		final Map<Integer, List<Integer>> followersNetwork = Maps
			.newConcurrentMap();
		processFollowerNetwork(networkDir, followersNetwork);
		followerRegionDistribution.putAll(calcFollowerRegions(
			allProfiles, followersNetwork));
		AnalyzeUtils.saveToFile(followerRegionDistribution,
			followerRegionFile);
	    } else {
		throw new Exception("Network dir not found: "
			+ confReader.getNetworkDir());
	    }
	}
    }

    private void processFollowerNetwork(File networkDir,
	    final Map<Integer, List<Integer>> followersNetwork)
	    throws InterruptedException {

	final ExecutorService executor = Executors.newCachedThreadPool();
	final CountDownLatch latch = new CountDownLatch(
		networkDir.list().length);
	for (final File file : networkDir.listFiles()) {
	    System.out.println("Start to process follower network file:"
		    + file.getName());
	    Runnable task = new Runnable() {

		@Override
		public void run() {
		    try {
			followersNetwork.putAll(AnalyzeUtils
				.readAggratedFromFile(file, "\t"));
		    } catch (IOException e) {
			System.err
				.println("Error on read aggragated id-followers");
			e.printStackTrace();
		    }

		    latch.countDown();
		}
	    };

	    executor.execute(task);
	}

	latch.await();
	executor.shutdown();
	System.out.println("Done to process all follower network files");
    }

    @Override
    public void drawResult() {
	drawFollowerNumberDistribution();
	drawFollowerRegionDistribution();
    }

    private void drawFollowerNumberDistribution() {
	System.out.println("Drawing follower number distribution...");
	ChartUtils.drawChart("", "Follower number distribution",
		"Number of followers", "Number of users",
		FOLLOWER_NUMBER, AnalyzeUtils.simplefilter(-1,
			-1, -1, 1, followerNumberDistribution));
	// Filter to remove the follower number is bigger than 1999 and the user
	// number is smaller than 2.
	ChartUtils.drawChart("", "Follower number distribution",
		"Number of followers", "Number of users",
		FOLLOWER_NUMBER+"_0_2000", AnalyzeUtils
			.simplefilter(2000, -1, -1, 1,
				followerNumberDistribution));
	System.out.println("Done drawing follower number distribution");
    }

    private void drawFollowerRegionDistribution() {
	System.out.println("Drawing follower region distribution...");
	ChartUtils.drawChart("", "Follower region distribution",
		"Number of followers", "Number of regions",
		FOLLOWER_REGION, AnalyzeUtils.simplefilter(-1,
			-1, -1, 1, followerRegionDistribution));
	// Filter to remove the follower number is bigger than 1999 and the user
	// number is smaller than 2.
	ChartUtils.drawChart("", "Follower region distribution",
		"Number of followers", "Number of regions",
		FOLLOWER_REGION+"_0_2000", AnalyzeUtils
			.simplefilter(2000, -1, -1, 1,
				followerRegionDistribution));
	System.out.println("Done drawing follower region distribution");
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
	    List<UserProfiler> allProfiles,
	    Map<Integer, List<Integer>> followersNetwork) {
	Map<Integer, Integer> resultMap = Maps.newHashMap();
	Map<Integer, Integer> followersRegionMap = Maps.newHashMap();
	Map<Integer, Integer> counterMap = Maps.newHashMap();
	Map<Integer, String> idRegionMap = createIDRegionMap(allProfiles);

	for (Map.Entry<Integer, List<Integer>> entry : followersNetwork
		.entrySet()) {
	    Integer id = entry.getKey();
	    List<Integer> followersID = entry.getValue();
	    int numFollowers = followersID.size();
	    if (followersRegionMap.get(followersID.size()) == null) {
		counterMap.put(numFollowers, 1);
		followersRegionMap.put(numFollowers,
			calcNumDifferentRegion(id, followersID, idRegionMap));
	    } else {
		int newCounter = counterMap.get(numFollowers) + 1;
		counterMap.put(numFollowers, newCounter);
		int numAvgFromOtherRegions = followersRegionMap.get(followersID
			.size())
			+ calcNumDifferentRegion(id, followersID, idRegionMap);
		followersRegionMap.put(numFollowers, numAvgFromOtherRegions);
	    }
	}

	for (Map.Entry<Integer, Integer> entry : followersRegionMap.entrySet()) {
	    resultMap.put(entry.getKey(),
		    entry.getValue() / counterMap.get(entry.getKey()));
	}
	return resultMap;
    }

    private Map<Integer, String> createIDRegionMap(
	    List<UserProfiler> allProfiles) {
	Map<Integer, String> idRegionMap = Maps.newHashMap();
	for (UserProfiler user : allProfiles) {
	    idRegionMap.put(user.id, user.location);
	}
	return idRegionMap;
    }

    private Integer calcNumDifferentRegion(Integer id,
	    List<Integer> followersID, Map<Integer, String> idRegionMap) {
	String userRegion = idRegionMap.get(id);
	Map<String, Integer> otherRegionsMap = Maps.newHashMap();
	for (Integer followerId : followersID) {
	    String followerRegion = idRegionMap.get(followerId);
	    if (userRegion != null && !userRegion.equals(followerRegion)) {
		if (otherRegionsMap.get(followerRegion) == null) {
		    otherRegionsMap.put(followerRegion, 1);
		}
	    }
	}
	return otherRegionsMap.size();
    }
}
