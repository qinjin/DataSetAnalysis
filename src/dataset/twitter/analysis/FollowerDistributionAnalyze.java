package dataset.twitter.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
    //Aggregation factor.
    public static final int AGG_FACTOR= 100;
    
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

	    // try {
	    // Map<Integer, List<Integer>> aggResult = AnalyzeUtils
	    // .readAggratedFromFile(file, "\t");
	    // followersNetwork.putAll(aggResult);
	    // } catch (IOException e) {
	    // logger.fatal("Error on read aggragated id-followers");
	    // e.printStackTrace();
	    // }
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
	

	drawMinFollowersBarchart();
	logger.info("Done drawing follower number distribution");
    }

    private void drawMinFollowersBarchart() {
	int usersSum = 0;
	Map<Integer, Integer> minFollowersMap = Maps.newTreeMap();
	int min10FollowerUsers = 0;
	int min100FollowerUsers = 0;
	int min1000FollowerUsers = 0;
	int min10000FollowerUsers = 0;
	int min100000FollowerUsers = 0;
	int min1000000FollowerUsers = 0;
	for (Map.Entry<Integer, Integer> entry : followerNumberDistribution
		.entrySet()) {
	    usersSum += entry.getValue();
	    if (entry.getKey() >= 10) {
		min10FollowerUsers += entry.getValue();
	    }
	    if (entry.getKey() >= 100) {
		min100FollowerUsers += entry.getValue();
	    }
	    if (entry.getKey() >= 1000) {
		min1000FollowerUsers += entry.getValue();
	    }
	    if (entry.getKey() >= 10000) {
		min10000FollowerUsers += entry.getValue();
	    }
	    if (entry.getKey() >= 100000) {
		min100000FollowerUsers += entry.getValue();
	    }
	    if (entry.getKey() >= 1000000) {
		min1000000FollowerUsers += entry.getValue();
	    }
	}

	minFollowersMap.put(10, min10FollowerUsers);
	minFollowersMap.put(100, min100FollowerUsers);
	minFollowersMap.put(1000, min1000FollowerUsers);
	minFollowersMap.put(10000, min10000FollowerUsers);
	minFollowersMap.put(100000, min100000FollowerUsers);
	minFollowersMap.put(1000000, min1000000FollowerUsers);

	logger.info("Num of total users analyzed: " + usersSum
		+ ". Num users at least have 10 followers: "
		+ min10FollowerUsers
		+ ". Num users at least have 100 followers: "
		+ min100FollowerUsers
		+ ". Num users at least have 1000 followers: "
		+ min1000FollowerUsers
		+ ". Num users at least have 10000 followers: "
		+ min10000FollowerUsers
		+ ". Num users at least have 100000 followers: "
		+ min100000FollowerUsers
		+ ". Num users at least have 1000000 followers: "
		+ min1000000FollowerUsers);

	ChartUtils.drawBarChart("", "Follower number distribution",
		"Min number of followers", "Number of users", FOLLOWER_NUMBER
			+ "_bar", minFollowersMap);
    }
    
    private void drawFollowerRegionDistribution() {
	logger.info("Drawing follower region distribution...");
	ChartUtils.drawChart("", "Follower region distribution",
		"Number of followers", "Number of regions", FOLLOWER_REGION,
		AnalyzeUtils.simplefilter(-1, -1, -1, -1,
			followerRegionDistribution));
	// Filter to remove the follower number is bigger than 1999 and the user
	// number is smaller than 2.
	ChartUtils.drawChart("", "Follower region distribution",
		"Number of followers", "Number of regions", FOLLOWER_REGION
			+ "_0_2000", AnalyzeUtils.simplefilter(2000, -1, -1,
			-1, followerRegionDistribution));

	ChartUtils.drawBarChart("", "Follower region distribution",
		"Number of followers", "Number of regions", FOLLOWER_REGION
			+ "_bar_0_5000", AnalyzeUtils.simplefilter(-1, -1, -1,
			-1, followerRegionDistribution), AGG_FACTOR, 5000);

	ChartUtils.drawBarChart("", "Follower region distribution",
		"Number of followers", "Number of regions", FOLLOWER_REGION
			+ "_bar", AnalyzeUtils.simplefilter(-1, -1, -1, -1,
			followerRegionDistribution), AGG_FACTOR, 12000);

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

    private Map<Integer, Integer> calcAggFollowerRegions(
	    List<UserProfiler> allProfiles,
	    Map<Integer, List<Integer>> followersNetwork) {
	Map<Integer, Set<String>> aggFollowerRegionMap = Maps.newHashMap();
	Map<Integer, Set<String>> followersRegionMap = Maps.newHashMap();
	Map<Integer, Integer> counterMap = Maps.newHashMap();
	Map<Integer, Integer> aggCounterMap = Maps.newHashMap();
	Map<Integer, Integer> resultMap = Maps.newHashMap();
	Map<Integer, String> idRegionMap = createIDRegionMap(allProfiles);

	for (Map.Entry<Integer, List<Integer>> entry : followersNetwork
		.entrySet()) {
	    Integer id = entry.getKey();
	    List<Integer> followersID = entry.getValue();
	    int numFollowers = followersID.size();
	    if (followersRegionMap.get(numFollowers) == null) {
		counterMap.put(numFollowers, 1);
		followersRegionMap.put(numFollowers,
			getDifferentRegions(id, followersID, idRegionMap));
	    } else {
		int newCounter = counterMap.get(numFollowers) + 1;
		counterMap.put(numFollowers, newCounter);
		followersRegionMap.get(numFollowers).addAll(
			getDifferentRegions(id, followersID, idRegionMap));
	    }
	}

	for (Map.Entry<Integer, Set<String>> entry : followersRegionMap
		.entrySet()) {
	    int key = entry.getKey();
	    // Round the aggregated key.
	    if ((key % AGG_FACTOR) > (AGG_FACTOR/2)) {
		key = key + (AGG_FACTOR - key % AGG_FACTOR);
	    } else {
		key = key - key % AGG_FACTOR;
	    }

	    if (aggFollowerRegionMap.get(key) == null) {
		aggFollowerRegionMap.put(key, entry.getValue());
	    } else {
		aggFollowerRegionMap.get(key).addAll(entry.getValue());
	    }
	}

	for (Map.Entry<Integer, Integer> entry : counterMap.entrySet()) {
	    int key = entry.getKey();
	    // Round the aggregated key.
	    if ((key % AGG_FACTOR) > (AGG_FACTOR/2)) {
		key = key + (AGG_FACTOR - key % AGG_FACTOR);
	    } else {
		key = key - key % AGG_FACTOR;
	    }

	    if (aggCounterMap.get(key) == null) {
		aggCounterMap.put(key, entry.getValue());
	    } else {
		int newCounter = aggCounterMap.get(key) + entry.getValue();
		aggCounterMap.put(key, newCounter);
	    }
	}

	Iterator<Entry<Integer, Set<String>>> iter = aggFollowerRegionMap
		.entrySet().iterator();
	while (iter.hasNext()) {
	    Entry<Integer, Set<String>> next = iter.next();
	    if (aggCounterMap.get(next.getKey()) != null) {
		int value = next.getValue().size()
			/ aggCounterMap.get(next.getKey()) == 0 ? 1 : next
			.getValue().size() / aggCounterMap.get(next.getKey());
		resultMap.put(next.getKey(), value);
	    } else {
		logger.fatal("A key not found in counter map: " + next.getKey());
	    }

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

    private Set<String> getDifferentRegions(Integer id,
	    List<Integer> followersID, Map<Integer, String> idRegionMap) {
	String userRegion = idRegionMap.get(id);
	Set<String> regions = Sets.newHashSet();
	for (Integer followerId : followersID) {
	    String followerRegion = idRegionMap.get(followerId);
	    if (userRegion != null && !userRegion.equals(followerRegion)) {
		regions.add(followerRegion);
	    }
	}
	return regions;
    }
}
