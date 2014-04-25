package dataset.twitter.analysis;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

public class UserTweetsLocation {
    private static final Logger logger = LogManager.getLogger(UserTweetsLocation.class);
    public final Integer userId;
    public final Map<String, Integer> timezoneTweetsMap;
    
    public UserTweetsLocation(final Integer userId){
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
    public Double calcTweetsFromOneTimeZonePercentage(){
	Integer sum = Integer.valueOf(0);
	Integer maxValue= Integer.valueOf(-1);
	for(Integer value: timezoneTweetsMap.values()){
	    sum = sum + value;
	    if(value.intValue() > maxValue){
		maxValue = value;
	    }
	}
	
	try{
	    return Double.valueOf(maxValue)/(Double.valueOf(sum));    
	}catch(ArithmeticException ex){
	    logger.fatal("Can not calc BigDecimal value for: "+maxValue+"/"+sum);
	    return Double.NaN;
	}
    }
}
