package dataset.twitter.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import dataset.chart.ChartUtils;
import dataset.db.DBProvider;
import dataset.utils.ConfReader;

/**
 * Analyze locality and Number of followers.
 * 
 * @author qinjin.wang
 *
 */
public class FollowerDistributionAnalyze implements IAnalyze {
	public static final String CACHED_FOLLOWER_NUMBER_FILE = "output/follower_number_distribution.txt";
	public static final boolean USE_CACHE = true;

	private final Map<String, String> allCityStateMap;

	public FollowerDistributionAnalyze() {
		allCityStateMap = DBProvider.getInstance().getCityStateMap();
	}

	public void executeAnalyze() {
		ConfReader confReader = new ConfReader();
		try {
			final Map<Integer, Integer> followerNumberDistribution = Maps
					.newHashMap();
			final File followerNumberFile = new File(
					CACHED_FOLLOWER_NUMBER_FILE);
			if (USE_CACHE && followerNumberFile.exists()) {
				System.out.println("Read follower number distribution from cached file");
				followerNumberDistribution
						.putAll(readFromFile(followerNumberFile));
			} else {
				File profileFile = confReader.getProfilerFile();
				System.out.println("Start to read profile file at: " + new Date());
				List<String> allLines = Files.readLines(profileFile,
						Charset.defaultCharset());
				System.out.println("Done read profile file at: " + new Date());
				List<UserProfiler> allProfiles = parseAllUserProfilesToState(
						allCityStateMap, allLines);
				System.out.println("Analyzed " + allProfiles.size()
						+ " user profilers from " + allLines.size() + " records.");

				System.out.println("Start to process follower number distribution");
				followerNumberDistribution
						.putAll(calcFollowerNumberDistribution(allProfiles));
				Runnable saveFileThread = new Runnable() {
					@Override
					public void run() {
						saveToFile(followerNumberDistribution,
								followerNumberFile);
					}
				};

				saveFileThread.run();
			}
			System.out.println("Drawing chart...");
			ChartUtils.drawChart("", "Follower number distribution",
					"Number of followers", "Number of users",
					"follower_number_distribution", filter(followerNumberDistribution));
			System.out.println("Done process follower number distribution");

			// TODO
//			Map<Integer, Integer> followerRegionMap = calcFollowerRegions(allProfiles);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//Filter to remove the user number is smaller than 2.
	private Map<Integer, Integer> filter(Map<Integer, Integer> dataSet) {
		Map<Integer, Integer> filtered = Maps.newHashMap();
		for (Map.Entry<Integer, Integer> entry : dataSet.entrySet()) {
			if(entry.getKey()<2000 && entry.getValue() > 1 ){
				filtered.put(entry.getKey(), entry.getValue());
			}
		}
		System.out.println("The result is filtered before plot.");
		return filtered;
	}

	private Map<Integer, Integer> readFromFile(File file) throws IOException {
		List<String> lines = Files.readLines(file, Charset.defaultCharset());
		Map<Integer, Integer> dataSet = Maps.newHashMap();
		for (String line : lines) {
			String[] splited = line.split("|");
			for (String str : splited) {
				String[] pair = str.split("=");
				if (pair.length == 2) {
					dataSet.put(Integer.valueOf(pair[0]),
							Integer.valueOf(pair[1]));
				}
			}
		}
		return dataSet;
	}

	private void saveToFile(Map<Integer, Integer> dataSet, File file) {
		try {
			boolean first = true;
			for (Map.Entry<Integer, Integer> entry : dataSet.entrySet()) {
				if (first) {
					Files.append(entry.getKey() + "=" + entry.getValue(), file,
							Charset.defaultCharset());
					first = false;
				} else {
					Files.append("|" + entry.getKey() + "=" + entry.getValue(),
							file, Charset.defaultCharset());
				}
			}
			System.out.println("Saved to file "+file);
		} catch (Exception ex) {
			System.err.println("Can not save data to file: " + file.toString());
			ex.printStackTrace();
		}
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
