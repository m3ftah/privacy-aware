package fr.rsommerard.privacyaware.data;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.List;

import fr.rsommerard.privacyaware.BuildConfig;
import fr.rsommerard.privacyaware.dao.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DataManagerTest {

    private DataManager mDataManager;

    @Before
    public void setup() {
        mDataManager = new DataManager(RuntimeEnvironment.application);

        Data data = new Data();
        data.setContent("This is the initial data.");
        mDataManager.addData(data);
    }

    @After
    public void tearDown() throws Exception {
        Field field = DataManager.class.getDeclaredField("sInstance");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void testSingleton() {
        DataManager dataManager = new DataManager(RuntimeEnvironment.application);

        assertEquals("should be the same reference", mDataManager, dataManager);
    }

    @Test
    public void testHasData() {
        assertTrue("should contain the initial data (at least)", mDataManager.hasData());
    }

    @Test
    public void testAddData() {
        List<Data> dataList = mDataManager.getAllData();
        int nbData = dataList.size();

        Data data = new Data();
        data.setContent("La volution.");

        mDataManager.addData(data);

        dataList = mDataManager.getAllData();

        assertEquals("should have one more data than beginning", nbData + 1, dataList.size());
    }

    @Test
    public void testGetData() {
        assertNotNull("should return a data", mDataManager.getData());
    }

    @Test
    public void testRemoveData() {
        Data data = new Data();
        data.setContent("Captp");

        mDataManager.addData(data);

        List<Data> dataList = mDataManager.getAllData();

        int nbData = dataList.size();

        mDataManager.removeData(data);
        dataList = mDataManager.getAllData();

        assertEquals("should remove the specific data", nbData - 1, dataList.size());
    }
}
