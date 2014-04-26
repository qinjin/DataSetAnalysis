DataSetAnalysis
===============

Twitter data set analysis for user behaviours and their locations.


The data set is from: https://wiki.engr.illinois.edu/display/forward/Dataset-UDI-TwitterCrawl-Aug2012

Three assumptions are analyzed and proved:
1. Locality and num of followers: If there are more followers for a user, the followers will be more distributed.
2. Num of tweets and num of followers: If there are more followers for a user, there will be more tweets created by the user.
3. Users are always tweet from the same data center.

In order to run the first analyze, we group the cities in US into time zones. The us_cities database (http://simplemaps.com/cities-data) must be created and the database/us_cities.sql must be imported to the databaese. 
	
Run the analyze from:
AnalysisMain.java
