package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.Mock;
import org.junit.*;

import static org.easymock.EasyMock.*;
import fllog.Log;
import android.util.Log.*;

import org.junit.runner.RunWith;
import org.powermock.api.easymock.*;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * Created by Jan Urbansky on 19.01.2016.
 */
@PrepareForTest(Log.class)
public class BluetoothModelTest {

    private BluetoothModel btModel;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Log.class);
        PowerMock.mockStatic(android.util.Log.class);
        android.util.Log.d(anyString(), anyString());

        Log.init(false, false);
        btModel = new BluetoothModel();
    }

    @Test
    public void testSetMyBT_ADDR() {
        btModel.setMyBT_ADDR("A1");

        String res = btModel.getMyBT_ADDR();
        Assert.assertEquals("A1", res);
    }

}
