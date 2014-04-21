package dataset.twitter.analysis;

import java.math.BigDecimal;
import java.util.Map;

import com.google.common.collect.Maps;

public class UserTweetsLocation {
    public final BigDecimal userId;
    public final Map<String, Integer> timezoneTweetsMap;
    
    public UserTweetsLocation(final BigDecimal userId){
	this.userId = userId;
	this.timezoneTweetsMap = Maps.newHashMap();
    }
    
    public void stepNumTweetsForTimeZone(final String timezone){
	if(timezoneTweetsMap.get(timezone) == null){
	    timezoneTweetsMap.put(timezone, 1);
	} else {
	    int newNum = timezoneTweetsMap.get(timezone)+1;
	    timezoneTweetsMap.put(timezone, newNum);
	}
    }
    
    /**
     * @return The largest number of tweets from a timezone divide all tweets number.
     */
    public BigDecimal calcTweetsFromOneTimeZonePercentage(){
	Integer sum = Integer.valueOf(0);
	Integer maxValue= Integer.valueOf(-1);
	for(Integer value: timezoneTweetsMap.values()){
	    sum = sum + value;
	    if(value.intValue() > maxValue){
		maxValue = value;
	    }
	}
	
	return BigDecimal.valueOf(maxValue).divide(BigDecimal.valueOf(sum));
    }
}
