package dataset.twitter.analysis;

import java.util.List;
import java.util.Map;

import dataset.db.Zips;

public class UserProfiler {

	public final Integer id;
	public final String name;
	public final Integer followed;
	public final Integer followers;
	public final Integer status;
	public final Integer favorites;
	public final Integer age;
	public final String location;

	public UserProfiler(String str, Map<String, String> allCityStateMap)
			throws Exception {
		if (str == null || str.isEmpty()) {
			throw new Exception("Exception on parsing profiler: null or empty");
		}

		String[] splited = str.split("\\t");
		if (splited == null || splited.length == 0) {
			throw new Exception(
					"Exception on parsing profiler: null or empty after splited. String: "
							+ str);
		}

		if (splited.length != 8) {
			throw new Exception(
					"Exception on parsing profiler: the splited length is "
							+ splited.length + "!=8. String: " + str);
		}

		// Id
		try {
			id = Integer.valueOf(splited[0]);
		} catch (NumberFormatException ex) {
			throw new Exception(
					"Exception on parsing profiler: id can not be parsed. String: "
							+ str);
		}

		// Name
		name = splited[1];

		// Followed (optional)
		int temp;
		try {
			temp = Integer.valueOf(splited[2]);
		} catch (NumberFormatException ex) {
			temp = -1;
		}
		followed = temp;

		// Followers
		try {
			followers = Integer.valueOf(splited[3]);
		} catch (NumberFormatException ex) {
			throw new Exception(
					"Exception on parsing profiler: followers can not be parsed. String: "
							+ str);
		}

		// Status (optional)
		try {
			temp = Integer.valueOf(splited[4]);
		} catch (NumberFormatException ex) {
			temp = -1;
		}
		status = temp;

		// Favorites (optional)
		try {
			temp = Integer.valueOf(splited[5]);
		} catch (NumberFormatException ex) {
			temp = -1;
		}
		favorites = temp;

		// Age (optional)
		try {
			temp = Integer.valueOf(splited[6]);
		} catch (NumberFormatException ex) {
			temp = -1;
		}
		age = temp;

		// Location
		location = allCityStateMap.get(splited[7]);
		// if(location ==null){
		// throw new
		// Exception("Exception on parsing profiler: Unknown location "+str);
		// }
	}
}
