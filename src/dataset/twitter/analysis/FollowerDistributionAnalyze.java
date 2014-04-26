package dataset.twitter.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
    private static final Logger logger = LogManager
	    .getLogger(FollowerDistributionAnalyze.class);
    public static final String FOLLOWER_NUMBER = "1_follower_number_distribution";
    public static final String FOLLOWER_REGION = "1_follower_region_distribution";
    public static final String CACHED_FOLLOWER_NUMBER_FILE = "output/"
	    + FOLLOWER_NUMBER + ".txt";
    public static final String CACHED_FOLLOWER_REGION_FILE = "output/"
	    + FOLLOWER_REGION + ".txt";
    // [Num followers, Num users]
    private final Map<Integer, Integer> followerNumberDistribution;
    // [Aggregated num followers, Num regions]
    Map<Integer, Integer> followerRegionDistribution;

    public FollowerDistributionAnalyze() {
	followerNumberDistribution = Maps.newHashMap();
	followerRegionDistribution = Maps.newHashMap();
    }

    @Override
    public void executeAnalyze() {
	logger.info("START ANALYZE " + this.getClass().getSimpleName());
	ConfReader confReader = new ConfReader();
	try {
	    processFollowerNumbers(confReader);
	    processFollowerRegions(confReader);
	} catch (Exception ex) {
	    logger.fatal("Error on " + this.getClass().getSimpleName());
	    ex.printStackTrace();
	}
    }

    private void processFollowerNumbers(ConfReader confReader) throws Exception {
	final File followerNumberFile = new File(CACHED_FOLLOWER_NUMBER_FILE);
	if (followerNumberFile.exists()) {
	    logger.debug("Read follower number distribution from cached file");
	    followerNumberDistribution.putAll(AnalyzeUtils
		    .readFromFile(followerNumberFile));
	} else {
	    logger.debug("Start to process follower number distribution...");
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
	    logger.debug("Read follower region distribution from cached file");
	    followerRegionDistribution.putAll(AnalyzeUtils
		    .readFromFile(followerRegionFile));
	} else {
	    logger.debug("Start to process follower region distribution...");
	    List<UserProfiler> allProfiles = confReader
		    .getAllUserProfilers(DBProvider.getInstance()
			    .getCityStateMap());
	    File networkDir = confReader.getNetworkDir();
	    if (networkDir.exists() && networkDir.isDirectory()) {
		final Map<Integer, List<Integer>> followersNetwork = Maps
			.newConcurrentMap();
		processFollowerNetwork(networkDir, followersNetwork);
		followerRegionDistribution.putAll(calcAggFollowerRegions(
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
	    logger.debug("Start to process follower network file:"
		    + file.getName());
	    Runnable task = new Runnable() {

		@Override
		public void run() {
		    try {
			Map<Integer, List<Integer>> aggResult = AnalyzeUtils
				.readAggratedFromFile(file, "\t");
			followersNetwork.putAll(aggResult);
		    } catch (IOException e) {
			logger.fatal("Error on read aggragated id-followers");
			e.printStackTrace();
		    }

		    latch.countDown();
		}
	    };

	    executor.execute(task);
	    
//	    try {
//		Map<Integer, List<Integer>> aggResult = AnalyzeUtils
//			.readAggratedFromFile(file, "\t");
//		followersNetwork.putAll(aggResult);
//	    } catch (IOException e) {
//		logger.fatal("Error on read aggragated id-followers");
//		e.printStackTrace();
//	    }
	}

	latch.await();
	executor.shutdown();
	logger.info("Done process all follower network files");
    }

    @Override
    public void drawResult() {
	drawFollowerNumberDistribution();
	drawFollowerRegionDistribution();
	logger.info("DONE ANALYZE " + this.getClass().getSimpleName());
    }

    private void drawFollowerNumberDistribution() {
	logger.info("Drawing follower number distribution...");
	ChartUtils.drawChart("", "Follower number distribution",
		"Number of followers", "Number of users", FOLLOWER_NUMBER,
		AnalyzeUtils.simplefilter(-1, -1, -1, 1,
			followerNumberDistribution));
	// Filter to remove the follower number is bigger than 1999 and the user
	// number is smaller than 2.
	ChartUtils.drawChart("", "Follower number distribution",
		"Number of followers", "Number of users", FOLLOWER_NUMBER
			+ "_0_2000", AnalyzeUtils.simplefilter(2000, -1, -1, 1,
			followerNumberDistribution));
	logger.info("Done drawing follower number distribution");
    }

    private void drawFollowerRegionDistribution() {
	logger.info("Drawing follower region distribution...");
	ChartUtils.drawChart("", "Follower region distribution",
		"Number of followers", "Number of regions", FOLLOWER_REGION,
		AnalyzeUtils.simplefilter(-1, -1, -1, 1,
			followerRegionDistribution));
	// Filter to remove the follower number is bigger than 1999 and the user
	// number is smaller than 2.
	ChartUtils.drawChart("", "Follower region distribution",
		"Number of followers", "Number of regions", FOLLOWER_REGION
			+ "_0_2000", AnalyzeUtils.simplefilter(2000, -1, -1, 1,
			followerRegionDistribution));

	ChartUtils.drawBarChart("", "Follower region distribution",
		"Number of followers", "Number of regions", FOLLOWER_REGION
			+ "_bar_0_2000", AnalyzeUtils.simplefilter(2000, -1,
			-1, 1, followerRegionDistribution), 100, 2100);

	logger.info("Done drawing follower region distribution");
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

    private Map<Integer, Integer> calcAggFollowerRegions(
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
	    int key = entry.getKey();
	    // Round the aggregated key.
	    if (key % 100 > 50) {
		key = key + (100 - key % 100);
	    } else {
		key = key - key % 100;
	    }

	    if (resultMap.get(key) == null) {
		resultMap.put(key, entry.getValue());
	    } else {
		int newValue = resultMap.get(key) + entry.getValue();
		resultMap.put(key, newValue);
	    }
	}
	
	Iterator<Entry<Integer, Integer>> iter = resultMap.entrySet().iterator();
	while(iter.hasNext()){
	    Entry<Integer, Integer> next = iter.next();
	    next.setValue(next.getValue() / counterMap.get(next.getKey()));
	}
	
	return resultMap;
    }

    // Aggregate per 100.
    private Map<Integer, Integer> aggregate(Map<Integer, Integer> map) {
	Map<Integer, Integer> aggMap = Maps.newHashMap();
	for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
	    int key = entry.getKey();
	    // Round the aggregated key.
	    if (key % 100 > 50) {
		key = key + (100 - key % 100);
	    } else {
		key = key - key % 100;
	    }

	    if (aggMap.get(key) == null) {
		aggMap.put(key, entry.getValue());
	    } else {
		int newValue = aggMap.get(key) + entry.getValue();
		aggMap.put(key, newValue);
	    }
	}
	return aggMap;
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
