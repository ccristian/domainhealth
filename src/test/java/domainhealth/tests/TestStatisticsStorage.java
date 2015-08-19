package domainhealth.tests;

import domainhealth.core.statistics.StatisticsStorage;
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

    StatisticsStorage statisticsStorage;


    @Before
    public void setUp() {
        URL url = this.getClass().getResource("/statistics-huge");
        File file = new File(url.getFile());
        statisticsStorage= new StatisticsStorage(file.getAbsolutePath());
    }

    public void testRootDirectoryPath(){
        URL url = this.getClass().getResource("/statistics-huge");
        File file = new File(url.getFile());
        assertEquals(statisticsStorage.getRootDirectoryPath(), file.getAbsolutePath());
    }

    public void testDatasourceNames(){
        DateTime start = new DateTime(2001, 12, 25, 0, 0, 0, 0);
        DateTime end = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        Interval interval = new Interval(start, end);
        try {
            Set<String> resourceNameList = statisticsStorage.getResourceNamesFromPropsListForInterval(interval,"datasource");
            assertEquals(1,resourceNameList.size());
            assertEquals(resourceNameList.contains("edaResolverEjbDataSource"),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testXXX(){
        DateTime start = new DateTime(2001, 12, 25, 0, 0, 0, 0);
        DateTime end = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        Interval interval = new Interval(start, end);
        try {

            DateAmountDataSet dateAmountDataSet1 = statisticsStorage.getPropertyData("core", null, "OpenSocketsCurrentCount", interval, "EFP7-OSB_TESTserver");
            DateAmountDataSet dateAmountDataSet2 = statisticsStorage.getPropertyData("core", null, "OpenSocketsCurrentCount", interval, "EFP7-OSB_TESTosb11");
            DateAmountDataSet dateAmountDataSet3 = statisticsStorage.getPropertyData("core", null, "OpenSocketsCurrentCount", interval, "EFP7-OSB_TESTosb12");
            System.out.println(dateAmountDataSet3.getData());
            System.out.println(dateAmountDataSet1.getData().size());
            System.out.println(dateAmountDataSet2.getData().size());
            System.out.println(dateAmountDataSet3.getData().size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
