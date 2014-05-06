package dataset.chart;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This is a utilities class for drawing analyzed results.
 * 
 * @author qinjin.wang
 *
 */
public class ChartUtils {
    private static final Logger logger = LogManager
	    .getLogger(ChartUtils.class);

    /**
     * Draw a chart with specified name, title, x-axis, y-axis, exported, and
     * dataSet. The data set should be a map with <x-axis value, y-axis value>
     * 
     * @param chartName
     * @param title
     * @param xAxisName
     * @param yAxisName
     * @param exportedFileName
     * @param dataSet
     */
    public static void drawChart(String chartName, String title,
	    String xAxisName, String yAxisName, String exportedFileName,
	    Map<Integer, Integer> dataSet) {
	XYSeries series = new XYSeries(chartName);
	for (Map.Entry<Integer, Integer> entry : dataSet.entrySet()) {
	    series.add(entry.getKey(), entry.getValue());
	}

	doDrawChat(title, xAxisName, yAxisName, exportedFileName, series);
    }

    /**
     * Draw a chart with specified name, title, x-axis, y-axis, exported, and
     * dataSet.
     * 
     * @param chartName
     * @param title
     * @param xAxisName
     * @param yAxisName
     * @param exportedFileName
     * @param dataSet
     */
    public static void drawDecimalChart(String chartName, String title,
	    String xAxisName, String yAxisName, String exportedFileName,
	    Map<Integer, BigDecimal> dataSet) {

	XYSeries series = new XYSeries(chartName);
	for (Map.Entry<Integer, BigDecimal> entry : dataSet.entrySet()) {
	    series.add(entry.getKey().doubleValue(), entry.getValue()
		    .doubleValue());
	}

	doDrawChat(title, xAxisName, yAxisName, exportedFileName, series);
    }

    /**
     * Draw a chart with specified name, title, x-axis, y-axis, exported, and
     * dataSet.
     * 
     * @param chartName
     * @param title
     * @param xAxisName
     * @param yAxisName
     * @param exportedFileName
     * @param dataSet
     */
    public static void drawDoubleChart(String chartName, String title,
	    String xAxisName, String yAxisName, String exportedFileName,
	    Map<Integer, Double> dataSet) {

	XYSeries series = new XYSeries(chartName);
	for (Map.Entry<Integer, Double> entry : dataSet.entrySet()) {
	    series.add(entry.getKey().doubleValue(), entry.getValue()
		    .doubleValue());
	}

	doDrawChat(title, xAxisName, yAxisName, exportedFileName, series);
    }

    private static void doDrawChat(String title, String xAxisName,
	    String yAxisName, String exportedFileName, XYSeries series) {
	XYSeriesCollection dataset = new XYSeriesCollection();
	dataset.addSeries(series);

	JFreeChart chart = ChartFactory.createXYLineChart(title, // Title
		xAxisName, // x-axis Label
		yAxisName, // y-axis Label
		dataset, // Data set
		PlotOrientation.VERTICAL, // Plot Orientation
		true, // Show Legend
		false, // Use tool tips
		false // Configure chart to generate URLs?
		);
	try {
	    ChartUtilities.saveChartAsPNG(new File("output" + File.separator
		    + exportedFileName + ".png"), chart, 1000, 600);
	} catch (IOException e) {
	    logger.fatal("Problem occurred creating chart: " + e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * Draw a bar chart with specified name, title, x-axis, y-axis, exported,
     * and dataSet. The data set should be a map with <x-axis value, y-axis
     * value>.
     * 
     * @param chartName
     * @param title
     * @param xAxisName
     * @param yAxisName
     * @param exportedFileName
     * @param dataSet
     */
    public static void drawBarChart(String chartName, String title,
	    String xAxisName, String yAxisName, String exportedFileName,
	    Map<Integer, Integer> dataSet, int interval, int max) {
	DefaultCategoryDataset dcd = new DefaultCategoryDataset();

	for (int i = 0; i < max; i += interval) {
	    int value = dataSet.get(i) == null ? 0 : dataSet.get(i);
	    dcd.setValue(value, yAxisName, String.valueOf(i));
	}

	doDrawBarChart(chartName, title, xAxisName, yAxisName,
		exportedFileName, dcd);

    }
    

    public static void drawBarChart(String chartName, String title,
	    String xAxisName, String yAxisName, String exportedFileName,
	    Map<Integer, Integer> dataSet) {
	DefaultCategoryDataset dcd = new DefaultCategoryDataset();
	for (Map.Entry<Integer, Integer> entry: dataSet.entrySet()) {
	    dcd.setValue(entry.getValue(), yAxisName,  entry.getKey());
	}

	doDrawBarChart(chartName, title, xAxisName, yAxisName,
		exportedFileName, dcd);
    }

    private static void doDrawBarChart(String chartName, String title,
	    String xAxisName, String yAxisName, String exportedFileName,
	    DefaultCategoryDataset dcd) {
	JFreeChart chart = ChartFactory.createBarChart3D(title, xAxisName,
		yAxisName, dcd, PlotOrientation.VERTICAL, false, true, false);
	try {
	    ChartUtilities.saveChartAsPNG(new File("output" + File.separator
		    + exportedFileName + ".png"), chart, 1000, 600);
	} catch (IOException e) {
	    logger.fatal("Problem occurred creating chart: " + e.getMessage());
	    e.printStackTrace();
	}
    }
}
