package dataset;

import java.util.List;

import dataset.db.DBProvider;
import dataset.db.Zips;

public class AnalysisMain {
	public static void main(String[] args){
		List<Zips> allZips = DBProvider.getInstance().getAllZips();
	}
}
