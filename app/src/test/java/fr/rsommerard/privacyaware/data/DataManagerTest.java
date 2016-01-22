package fr.rsommerard.privacyaware.data;

import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import fr.rsommerard.privacyaware.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DataManagerTest {

    private static DataManager mDataManager;

    @Before
    public void setup() {
        mDataManager = DataManager.getInstance();
    }

    @Test
    public void testSingleton() {
        DataManager dataManager = DataManager.getInstance();

        assertEquals("should be equals", mDataManager, dataManager);
    }

    @Test
    public void testHasDatas() {
        Data data = createData1();

        mDataManager.addData(data);

        assertTrue("should be true", mDataManager.hasData());
    }

    @Test
    public void testAddData() {
        Data data1 = createData1();

        mDataManager.addData(data1);
        List<Data> datas = mDataManager.getAllData();

        int nbDatas = datas.size();
        mDataManager.addData(data1);
        datas = mDataManager.getAllData();

        assertEquals("should be the same number of datas", nbDatas, datas.size());

        nbDatas = datas.size();

        Data peer2 = createData2();

        mDataManager.addData(peer2);
        datas = mDataManager.getAllData();

        assertEquals("should be nbDatas + 1", nbDatas + 1, datas.size());
    }

    @Test
    public void testGetData() {
        Data data = createData1();

        mDataManager.addData(data);

        assertNotNull("should return a data", mDataManager.getData());
    }

    @Test
    public void testRemoveData() {
        Data data = createData3();

        mDataManager.addData(data);

        List<Data> datas = mDataManager.getAllData();

        int nbDatas = datas.size();

        mDataManager.removeData(data);
        datas = mDataManager.getAllData();

        assertEquals("should remove a data", nbDatas - 1, datas.size());
    }

    private Data createData1() {
        return new Data("Réfléchir, c'est fléchir deux fois.");
    }

    private Data createData2() {
        return new Data("This is a Data");
    }

    private Data createData3() {
        return new Data("Epic - Winning");
    }
}
