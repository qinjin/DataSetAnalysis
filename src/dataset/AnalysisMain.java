package dataset;

import java.util.List;

import com.google.common.collect.Lists;

import dataset.twitter.analysis.FollowerDistributionAnalyze;
import dataset.twitter.analysis.IAnalyze;
import dataset.twitter.analysis.NumTweetsAnalyze;
import dataset.twitter.analysis.TweetLocationAnalyze;

public class AnalysisMain {
	public static void main(String[] args){
		
		List<IAnalyze> analysis = Lists.newArrayList();
		analysis.add(new FollowerDistributionAnalyze());
//		analysis.add(new NumTweetsAnalyze());
//		analysis.add(new TweetLocationAnalyze());
		
		for(IAnalyze analyze: analysis){
			analyze.executeAnalyze();
			analyze.drawResult();
		}
	}
}
