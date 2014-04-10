package dataset.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfReader {
	private final Properties configuration;

	public ConfReader() {
		configuration = new Properties();
		String confStr = "conf/dataset.conf";
		try {
			configuration.load(new FileInputStream(confStr));
		} catch (IOException e) {
			System.err.println("Failed to load configuration " + confStr);
			e.printStackTrace();
		}
	}

	public File getProfilerFile() {
		return new File(
				configuration.getProperty("dataset.twitter.profiles.file"));
	}

	public File getNetworkFile() {
		return new File(
				configuration.getProperty("dataset.twitter.network.file"));
	}

	public File getTweetsDir() {
		return new File(configuration.getProperty("dataset.twitter.tweets.dir"));
	}

}
