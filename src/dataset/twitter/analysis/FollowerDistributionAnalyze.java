package dataset.twitter.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import dataset.db.DBProvider;
import dataset.db.Zips;
import dataset.utils.ConfReader;

/**
 * Analyze locality and Number of followers.
 * 
 * @author qinjin.wang
 *
 */
public class FollowerDistributionAnalyze implements IAnalyze {
	
	private final Map<String, String> allCityStateMap;
	
	public FollowerDistributionAnalyze(){
		allCityStateMap = DBProvider.getInstance().getCityStateMap();
	}

	public void executeAnalyze() {
		ConfReader confReader = new ConfReader();
		try{
			File profileFile = confReader.getProfilerFile();
			System.out.println("Start to read profile file at: "+new Date());
			List<String> allLines = Files.readLines(profileFile, Charset.defaultCharset());
			System.out.println("Done read profile file at: "+new Date());
			List<UserProfiler> allProfiles = parseAllUserProfilesToState(allCityStateMap, allLines);

			//[num followers, num follower from another region]
			Map<Integer, Integer> followerRegionMap = calculateFollowerRegions(allProfiles);
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private List<UserProfiler> parseAllUserProfilesToState(Map<String, String> allCityStateMap,
			List<String> allLines) {
		List<UserProfiler> allProfilers = Lists.newArrayList();
		for(String line : allLines){
			try{
				UserProfiler profiler = new UserProfiler(line, allCityStateMap);
				System.out.println("User "+ profiler.id +" followers "+profiler.followers+ " location "+profiler.location);
			}catch(Exception ex){
				System.err.println(ex);
			} 
		}
		return allProfilers;
	}

	private Map<Integer, Integer> calculateFollowerRegions(
			List<UserProfiler> allProfiles) {
		return Maps.newHashMap();
	}

	
}
