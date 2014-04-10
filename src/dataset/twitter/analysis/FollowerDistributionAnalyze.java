package dataset.twitter.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import dataset.utils.ConfReader;

/**
 * Analyze locality and Number of followers.
 * 
 * @author qinjin.wang
 *
 */
public class FollowerDistributionAnalyze implements IAnalyze {

	public void executeAnalyze() {
		ConfReader confReader = new ConfReader();

		try{
			File profileFile = confReader.getProfilerFile();
			File networkFile = confReader.getNetworkFile();

			FileInputStream profileInputStream = new FileInputStream(profileFile);
			FileInputStream networkInputStream = new FileInputStream(networkFile);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
//		FileChannel f1 = profileInputStream.getChannel();
//		FileChannel f2 = networkInputStream.getChannel();
//		f1.close();
//		f2.close();
	}
}
