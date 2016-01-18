package fr.rsommerard.privacyaware.data;

import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import fr.rsommerard.privacyaware.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DataTest {

    private Data mData;

    @Before
    public void setup() {
        mData = createData1();
    }

    @Test
    public void testData() {
        assertNotNull("should not be null", mData);
    }

    @Test
    public void testGetContent() {
        assertEquals("should be equals", "Réfléchir, c'est fléchir deux fois.", mData.getContent());
    }

    @Test
    public void testEquals() {
        assertEquals("should be equals", mData, mData);

        Data data1 = createData1();
        assertEquals("should be equals", mData, data1);

        Data data2 = createData2();
        assertNotEquals("should be not equals", mData, data2);
    }

    @Test
    public void testToString() {
        assertEquals("should be equals", "Réfléchir, c'est fléchir deux fois.", mData.toString());
    }

    private Data createData1() {
        return new Data("Réfléchir, c'est fléchir deux fois.");
    }

    private Data createData2() {
        return new Data("This is a Data");
    }
}
