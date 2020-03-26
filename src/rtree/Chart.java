package rtree;

import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.ApplicationFrame;

public class Chart extends ApplicationFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Chart(String title, List<Rectangle> skyline, List<Rectangle> others) {
		super(title);
		JFreeChart chart = createChart(skyline, others);
		ChartPanel panel = new ChartPanel(chart, true, true, true, true, true);
		panel.setPreferredSize(new java.awt.Dimension(10000, 10000));
		setContentPane(panel);
	}
	
	private JFreeChart createChart(List<Rectangle> skyline, List<Rectangle> others) {
		DefaultXYDataset dataLine = createDatasetLine(skyline);
		XYItemRenderer rendererLine = new XYLineAndShapeRenderer();
		NumberAxis XAxis = new NumberAxis("X");
		NumberAxis YAxis = new NumberAxis("Y");
		NumberTickUnit unit = new NumberTickUnit(1);
		XAxis.setTickUnit(unit);
		YAxis.setTickUnit(unit);
		
		XYPlot plot = new XYPlot(dataLine, XAxis, YAxis, rendererLine);

		DefaultXYDataset dataScatter = createDatasetScatter(others);
		XYItemRenderer rendererScatter = new XYShapeRenderer();
		plot.setDataset(1, dataScatter);
		plot.setRenderer(1, rendererScatter);

		return new JFreeChart("Skyline", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}
	
	private DefaultXYDataset createDatasetLine(List<Rectangle> skyline) {
		DefaultXYDataset dataset = new DefaultXYDataset();
		double[][] data = new double[2][skyline.size()];
		for (int i = 0; i < skyline.size(); i ++) {
			data[0][i] = skyline.get(i).getLow().getCoordinate(0);
			data[1][i] = skyline.get(i).getLow().getCoordinate(1);
			
			// Magnify the data for better observation
			/*data[0][i] = skyline.get(i).getLow().getCoordinate(0) - 15;
			data[1][i] = skyline.get(i).getLow().getCoordinate(1) - 15;*/
		}
		dataset.addSeries("Skyline Points", data);
		return dataset;
	}

	private DefaultXYDataset createDatasetScatter(List<Rectangle> others) {
		DefaultXYDataset dataset = new DefaultXYDataset();
		double[][] data = new double[2][others.size()];
		for (int i = 0; i < others.size(); i ++) {
			data[0][i] = others.get(i).getLow().getCoordinate(0);
			data[1][i] = others.get(i).getLow().getCoordinate(1);
			
			// Magnify the data for better observation
			/*data[0][i] = others.get(i).getLow().getCoordinate(0) - 15;
			data[1][i] = others.get(i).getLow().getCoordinate(1) - 15;*/
		}
		dataset.addSeries("Other Points", data);
		return dataset;
	}
}