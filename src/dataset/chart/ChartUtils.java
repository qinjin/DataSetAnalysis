package dataset.chart;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This is a utilities class for drawing analyzed results.
 * 
 * @author qinjin.wang
 *
 */
public class ChartUtils {
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
		
		doDrawChat(title, xAxisName, yAxisName, exportedFileName,
			series);
	}
	
	/**
	 * Draw a chart with specified name, title, x-axis, y-axis, exported, and
	 * dataSet. The data set should be a map with <y-axis value, x-axis value>
	 * @param chartName
	 * @param title
	 * @param xAxisName
	 * @param yAxisName
	 * @param exportedFileName
	 * @param dataSet
	 */
	public static void drawDecimalChart(String chartName, String title,
		String xAxisName, String yAxisName, String exportedFileName,
		Map<BigDecimal, BigDecimal> dataSet) {
	    
	    XYSeries series = new XYSeries(chartName);
		for (Map.Entry<BigDecimal, BigDecimal> entry : dataSet.entrySet()) {
			series.add(entry.getValue().doubleValue(), entry.getKey().doubleValue());
		}
		
		doDrawChat(title, xAxisName, yAxisName, exportedFileName,
			series);
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
	    	System.err.println("Problem occurred creating chart: "
	    			+ e.getMessage());
	    	e.printStackTrace();
	    }
	}
}
