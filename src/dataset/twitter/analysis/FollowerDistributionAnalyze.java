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
	
	private final List<Zips> allZips;
	
	public FollowerDistributionAnalyze(){
		allZips = DBProvider.getInstance().getAllZips();;
	}

	public void executeAnalyze() {
		ConfReader confReader = new ConfReader();

		try{
			File profileFile = confReader.getProfilerFile();
			System.out.println("Start to read profile file at: "+new Date());
			List<String> allLines = Files.readLines(profileFile, Charset.defaultCharset());
			System.out.println("Done read profile file at: "+new Date());
			List<UserProfiler> allProfiles = parseAllUserProfilesToState(allZips, allLines);

			//[num followers, num follower from another region]
			Map<Integer, Integer> followerRegionMap = calculateFollowerRegions(allProfiles);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	private Map<Integer, Integer> calculateFollowerRegions(
			List<UserProfiler> allProfiles) {
		return Maps.newHashMap();
	}

	private List<UserProfiler> parseAllUserProfilesToState(List<Zips> allZips,
			List<String> allLines) {
		//TODO
		List<UserProfiler> allProfilers = Lists.newArrayList();
		return allProfilers;
	}
}
