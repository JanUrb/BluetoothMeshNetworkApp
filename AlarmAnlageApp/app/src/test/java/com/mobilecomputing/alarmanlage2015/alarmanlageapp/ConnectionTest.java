package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.bluetooth.BluetoothDevice;

import org.easymock.EasyMock;
import org.easymock.EasyMock.*;
import org.easymock.internal.matchers.Any;
import org.hamcrest.core.AnyOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Jan Urbansky on 20.01.2016.
 */
public class ConnectionTest {

    private Connection connection;
    private ConnectedThread ctMock;
    private BluetoothDevice btDeviceMock;
    @Before
    public void setUp() throws Exception {
        ctMock = EasyMock.createMock(ConnectedThread.class);
        btDeviceMock = EasyMock.createMock(BluetoothDevice.class);
//        Construktor call
        EasyMock.expect(ctMock.getId()).andReturn((long) 1);
    }


    @After
    public void tearDown() throws Exception {
        ctMock = null;
        btDeviceMock = null;
        connection = null;

    }
    @Test
    public void testStart() throws Exception {
        ctMock.start();
        EasyMock.expectLastCall();
        EasyMock.replay(ctMock);
        connection = new Connection(ctMock, btDeviceMock);
        connection.start();
        EasyMock.verify(ctMock);
    }

    @Test
    public void testGetDeviceAddress() throws Exception {
        EasyMock.expect(btDeviceMock.getAddress()).andReturn("aasdas");
        EasyMock.replay(btDeviceMock, ctMock);
        connection = new Connection(ctMock, btDeviceMock);
        connection.getDeviceAddress();
        EasyMock.verify(btDeviceMock, ctMock);
    }

    @Test
    public void testWrite() throws Exception {

    }

    @Test
    public void testGetConnectionID() throws Exception {

    }


}