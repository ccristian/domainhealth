package domainhealth.tests;

import domainhealth.core.jmx.WebLogicMBeanPropConstants;
import domainhealth.core.statistics.StatisticsStorage;
import domainhealth.frontend.data.DateAmountDataItem;
import domainhealth.frontend.data.DateAmountDataSet;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Created by chiovcr on 19/08/2015.
 */
public class TestStatisticsStorage extends TestCase {


    public static String ADMIN_SERVER = "EFP7-OSB_TESTserver";
    public static String MANAGED_SERVER1 = "EFP7-OSB_TESTosb11";
    public static String MANAGED_SERVER2 = "EFP7-OSB_TESTosb12";

    StatisticsStorage statisticsStorage;


    @Before
    public void setUp() {
        URL url = this.getClass().getResource("/statistics-huge");
        File file = new File(url.getFile());
        statisticsStorage = new StatisticsStorage(file.getAbsolutePath());
    }

    public void testRootDirectoryPath() {
        URL url = this.getClass().getResource("/statistics-huge");
        File file = new File(url.getFile());
        assertEquals(statisticsStorage.getRootDirectoryPath(), file.getAbsolutePath());
    }

    public void testDatasourceNames() throws IOException {
        DateTime start = new DateTime(2001, 12, 25, 0, 0, 0, 0);
        DateTime end = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        Interval interval = new Interval(start, end);
        Set<String> resourceNameList = statisticsStorage.getResourceNamesFromPropsListForInterval(interval, "datasource");
        assertEquals(1, resourceNameList.size());
        assertEquals(resourceNameList.contains("edaResolverEjbDataSource"), true);

    }

    public void testDatasourceSize() throws IOException {
        DateTime start = new DateTime(2015, 8, 18, 12, 0, 0, 0);
        DateTime end = new DateTime(2015, 8, 18, 12, 10, 0, 0);
        Interval interval = new Interval(start, end);
        DateAmountDataSet dateAmountDataSet1 = statisticsStorage.getPropertyData("datasource", "edaResolverEjbDataSource", WebLogicMBeanPropConstants.NUM_AVAILABLE, interval, ADMIN_SERVER);
        DateAmountDataSet dateAmountDataSet2 = statisticsStorage.getPropertyData("datasource", "edaResolverEjbDataSource", WebLogicMBeanPropConstants.NUM_AVAILABLE, interval, MANAGED_SERVER1);
        DateAmountDataSet dateAmountDataSet3 = statisticsStorage.getPropertyData("datasource", "edaResolverEjbDataSource", WebLogicMBeanPropConstants.NUM_AVAILABLE, interval, MANAGED_SERVER2);
        //the datasource is not targeted to admin server so it should return 0 items
        assertEquals(dateAmountDataSet1.getData().size(), 0);
        assertEquals(dateAmountDataSet2.getData().size(), 20);
        assertEquals(dateAmountDataSet3.getData().size(), 19);

    }

    public void testDatasourceValue() throws IOException {
        DateTime start = new DateTime(2010, 8, 18, 12, 0, 0, 0);
        DateTime end = new DateTime(2016, 8, 18, 12, 10, 0, 0);
        Interval interval = new Interval(start, end);
        DateAmountDataSet dateAmountDataSet1 = statisticsStorage.getPropertyData("datasource", "edaResolverEjbDataSource", WebLogicMBeanPropConstants.NUM_AVAILABLE, interval, ADMIN_SERVER);
        DateAmountDataSet dateAmountDataSet2 = statisticsStorage.getPropertyData("datasource", "edaResolverEjbDataSource", WebLogicMBeanPropConstants.NUM_AVAILABLE, interval, MANAGED_SERVER1);
        DateAmountDataSet dateAmountDataSet3 = statisticsStorage.getPropertyData("datasource", "edaResolverEjbDataSource", WebLogicMBeanPropConstants.NUM_AVAILABLE, interval, MANAGED_SERVER2);
        //the datasource is not targeted to admin server so it should return 0 items
        assertEquals(dateAmountDataSet1.getData().size(), 0);
        for (DateAmountDataItem dataItem : dateAmountDataSet2.getData()) {
            assertEquals(dataItem.getAmount() >= 0, true);
        }
        for (DateAmountDataItem dataItem : dateAmountDataSet3.getData()) {
            assertEquals(dataItem.getAmount() >= 0, true);
        }

    }


    public void testDestinationNames() throws IOException {
        DateTime start = new DateTime(2001, 12, 25, 0, 0, 0, 0);
        DateTime end = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        Interval interval = new Interval(start, end);
        Set<String> resourceNameList = statisticsStorage.getResourceNamesFromPropsListForInterval(interval, "destination");
        assertEquals(true, resourceNameList.size()>0);
        assertEquals(resourceNameList.contains("eda_inbound1"), true);

    }

}
