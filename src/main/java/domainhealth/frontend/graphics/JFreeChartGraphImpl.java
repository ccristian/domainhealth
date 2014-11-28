//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the 
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.frontend.graphics;

import domainhealth.core.statistics.MonitorProperties;
import domainhealth.frontend.data.DateAmountDataItem;
import domainhealth.frontend.data.DateAmountDataSet;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DATE_TIME;


/**
 * Encapsulate a Line Chart version of a JFreeChart based graph, based on an X 
 * axis of type Date-Time and a Y axis of type Number-Amount. This should be 
 * the only DomainHealth class which references JFreeChart packages/classes 
 * which may aid migration to a different open source Java graph provider, in 
 * the future, if the need arises.
 */
public class JFreeChartGraphImpl {
	/**
	 * Creates a new JFreeChart based line graph
	 * 
	 * @param chartTitle The name to show above the graph
	 * @param yAxisUnits The units for the Y axis
	 */
	public JFreeChartGraphImpl(String chartTitle, String yAxisUnits) {
		this.chartTitle = chartTitle;
		this.yAxisUnits = yAxisUnits;
	}

	/**
	 * Adds a new line to the chart to contain a series of Data-Time/Amount 
	 * values. If the data-set contains no values, append the text 'N/A' (ie.
	 * Not Available) to the end of the key-name for the line.
	 * 
	 * @param lineName The key of the line in the chart, to display below the chart
	 * @param dateAmountDataSet The series of Data-Time/Amounts to plot on the line
	 */
	public void addDataSeries(String lineName, DateAmountDataSet dateAmountDataSet) {		
		Iterator<DateAmountDataItem> dateAmountIterator = dateAmountDataSet.getByIncreasingDateTime();
		XYSeries dataSeriesLine = new XYSeries(dateAmountIterator.hasNext() ? lineName : (lineName + NOT_AVAILABLE_DESC));

		while (dateAmountIterator.hasNext()) {
			DateAmountDataItem item = dateAmountIterator.next();
			dataSeriesLine.add(item.getDateTime().getTime(), item.getAmount());
		}
		
		xySeriesCollection.addSeries(dataSeriesLine);
	}
	
	/**
	 * Write a PNG image representation of the Graph to the given output 
	 * stream
	 * 
	 * @param out The output stream to write the PNG bytes to
	 * @throws IOException Indicates a problem writing to the output stream
	 */
	public void writeGraphImage(int numServersDisplayed, OutputStream out) throws IOException {
		ValueAxis xAxis = new DateAxis(MonitorProperties.units(DATE_TIME));
		NumberAxis yAxis = new NumberAxis(yAxisUnits);		
		yAxis.setAutoRangeIncludesZero(true);		
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		XYPlot xyPlotLine = new XYPlot(xySeriesCollection, xAxis, yAxis, new StandardXYItemRenderer(StandardXYItemRenderer.LINES));		
		JFreeChart chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, xyPlotLine, true); 
		chart.setBackgroundPaint(java.awt.Color.white);
		// Increase size of graph height to accommodate large legends for when many servers in the domain
		int graphAdditionalHeight = GRAPH_INCREMENT_HEIGHT * ((int) (numServersDisplayed / GRAPH_INCREMENT_SERVER_RATIO)); 
		BufferedImage graphImage = chart.createBufferedImage(GRAPH_WIDTH, INITIAL_GRAPH_HEIGHT + graphAdditionalHeight, new ChartRenderingInfo(new StandardEntityCollection()));
		addNoDataLogoIfEmpty(graphImage);
		ChartUtilities.writeBufferedImageAsPNG(out, graphImage); // Could try extra two PNG related params: encodeAlpha and compression
	}
	
	/**
	 * Check to see if current graph has empty chart lines (ie. no data-series
	 * in any lines), and if so, write the text 'NO DATA' in large letters 
	 * across the front of the graph
	 * 
	 * @param graphImage The current image representation of the generated graph
	 */
	@SuppressWarnings("unchecked")
	private void addNoDataLogoIfEmpty(BufferedImage graphImage) {
		int maxStatCount = 0;
		List<XYSeries> seriesList = xySeriesCollection.getSeries();
		
		for (XYSeries series : seriesList) {
			maxStatCount = Math.max(maxStatCount, series.getItemCount());			
		}
				
		if (maxStatCount <= 0) {
			Graphics2D graphics2D = get2DGraphics(graphImage);
			graphics2D.setFont(new Font(GRAPH_TEXT_FONT, Font.PLAIN,36));
			graphics2D.drawString(NO_DATA_TEXT, 200, 210);
			graphics2D.dispose();
		} else if (maxStatCount <= 1) {
			Graphics2D graphics2D = get2DGraphics(graphImage);
			graphics2D.setFont(new Font(GRAPH_TEXT_FONT, Font.PLAIN, 22));
			graphics2D.drawString(WAITING_FOR_DATA_TEXT_LN1, 152, 205);				
			graphics2D.dispose();
			graphics2D = get2DGraphics(graphImage);
			graphics2D.setFont(new Font(GRAPH_TEXT_FONT, Font.PLAIN, 15));
			graphics2D.drawString(WAITING_FOR_DATA_TEXT_LN2, 81, 225);				
			graphics2D.dispose();
		}		
	}

	/**
	 * Gets handle on the 2D graphics image object for the graph ready to 
	 * change (eg. add text to).
	 * 
	 * @param graphImage The current graph image
	 * @return The 2D object handle
	 */
	private Graphics2D get2DGraphics(BufferedImage graphImage) {
		Graphics2D graphics2D = (Graphics2D) graphImage.getGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.setColor(Color.BLACK);
		return graphics2D;
	}
	
	// Members
	private final String chartTitle;
	private final String yAxisUnits;
	private final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
	
	// Constants
	private final static int GRAPH_WIDTH = 560;
	private final static int INITIAL_GRAPH_HEIGHT = 450;
	private final static int GRAPH_INCREMENT_SERVER_RATIO = 4;
	private final static int GRAPH_INCREMENT_HEIGHT = 22;
	private final static String NOT_AVAILABLE_DESC = "(N/A)";
	private final static String NO_DATA_TEXT = "NO DATA";
	private final static String WAITING_FOR_DATA_TEXT_LN1 = "WAITING FOR INITIAL DATA";
	private final static String WAITING_FOR_DATA_TEXT_LN2 = "(try pressing the 'latest-time' button after a minute or so)";
	private final static String GRAPH_TEXT_FONT = "SansSerif";		
}
