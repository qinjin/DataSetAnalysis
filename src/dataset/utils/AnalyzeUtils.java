package dataset.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class AnalyzeUtils {
    /**
     * Filter the result before drawing the plot.
     * 
     * @param keyMax
     * @param keyMin
     * @param valueMax
     * @param valueMin
     * @param dataSet
     * @return
     */
    public static Map<Integer, Integer> simplefilter(int keyMax, int keyMin,
	    int valueMax, int valueMin, Map<Integer, Integer> dataSet) {
	Map<Integer, Integer> filtered = Maps.newHashMap();
	for (Map.Entry<Integer, Integer> entry : dataSet.entrySet()) {
	    boolean isKeyNotFiltered = (keyMax == -1 || keyMax != -1
		    && entry.getKey() < keyMax)
		    && (keyMin == -1 || keyMin != -1 && entry.getKey() > keyMin);
	    boolean isValueNotFiltered = (valueMax == -1 || valueMax != -1
		    && entry.getValue() < valueMax)
		    && (valueMin == -1 || valueMin != -1
			    && entry.getValue() > valueMin);
	    if (isKeyNotFiltered && isValueNotFiltered) {
		filtered.put(entry.getKey(), entry.getValue());
	    }
	}

	System.out.println("The result is filtered before plot.");
	return filtered;
    }

    /**
     * Read from the file to the result map.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static Map<Integer, Integer> readFromFile(File file)
	    throws IOException {
	List<String> lines = Files.readLines(file, Charset.defaultCharset());
	Map<Integer, Integer> dataSet = Maps.newHashMap();
	for (String line : lines) {
	    String[] splited = line.split(";");
	    System.out.println("Splited length: " + splited.length);
	    for (String str : splited) {
		String[] pair = str.split("=");
		if (pair.length == 2) {
		    dataSet.put(Integer.valueOf(pair[0]),
			    Integer.valueOf(pair[1]));
		}
	    }
	}
	return dataSet;
    }

    /**
     * Save to file from another thread.
     * 
     * @param dataSet
     * @param file
     */
    public static void saveToFile(final Map<Integer, Integer> dataSet,
	    final File file) {
	Runnable saveFileThread = new Runnable() {
	    @Override
	    public void run() {
		doSaveToFile(dataSet, file);
	    }

	};

	saveFileThread.run();
    }

    private static void doSaveToFile(Map<Integer, Integer> dataSet, File file) {
	try {
	    boolean first = true;
	    for (Map.Entry<Integer, Integer> entry : dataSet.entrySet()) {
		if (first) {
		    Files.append(entry.getKey() + "=" + entry.getValue(), file,
			    Charset.defaultCharset());
		    first = false;
		} else {
		    Files.append(";" + entry.getKey() + "=" + entry.getValue(),
			    file, Charset.defaultCharset());
		}
	    }
	    System.out.println("Saved to file " + file);
	} catch (Exception ex) {
	    System.err.println("Can not save data to file: " + file.toString());
	    ex.printStackTrace();
	}
    }

    public static Map<? extends Integer, ? extends List<Integer>> readAggratedFromFile(
	    File file, String splitRegex) throws IOException {
	Map<Integer, List<Integer>> map = Maps.newHashMap();
	List<String> lines = Files.readLines(file, Charset.defaultCharset());
	for (String line : lines) {
	    String[] splited = line.split(splitRegex);
	    if (splited.length == 2) {
		int id = Integer.valueOf(splited[0]);
		int followerId = Integer.valueOf(splited[1]);
		if (map.get(id) == null) {
		    List<Integer> followers = Lists.newArrayList();
		    followers.add(followerId);
		    map.put(id, followers);
		} else {
		    map.get(id).add(followerId);
		}
	    }
	}
	System.out.println("Read "+map.size()+" id-followers from file");
	return map;
    }

    // public static Map<BigDecimal, BigDecimal> simplefilter(int keyMax, int
    // keyMin, int valueMax, int valueMin, Map<BigDecimal, BigDecimal>
    // resultMap) {
    // return null;
    // }
}
