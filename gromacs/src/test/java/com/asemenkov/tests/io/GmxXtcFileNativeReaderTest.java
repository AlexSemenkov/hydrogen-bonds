package com.asemenkov.tests.io;

import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.tests.config.GmxAbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
@Test
public class GmxXtcFileNativeReaderTest extends GmxAbstractTest {

    @Test
    public void testXtcPartialReading() {
        Assert.assertFalse(xtcFileNativeReader.isBusy());
        xtcFileNativeReader.openXtcFile(XTC_WATER_IN_ARGON_PATH);
        GmxFrameCoordinates coordinates = xtcFileNativeReader.readNextFrame();

        Assert.assertTrue(xtcFileNativeReader.isBusy());
        Assert.assertEquals(xtcFileNativeReader.getNumberOfAtoms(), 872, "Wrong number of atoms.");
        verifyFrameCoordinates871(coordinates, 1, new float[] { 4.268f, 4.789f, 4.520f });

        xtcFileNativeReader.closeXtcFile();
        Assert.assertFalse(xtcFileNativeReader.isBusy());
    }

    @Test
    public void testXtcWholeFileReading() {
        Assert.assertFalse(xtcFileNativeReader.isBusy());
        xtcFileNativeReader.openXtcFile(XTC_WATER_IN_ARGON_PATH);

        GmxFrameCoordinates tmp, coordinates = null;
        while ((tmp = xtcFileNativeReader.readNextFrame()) != null) coordinates = tmp;

        Assert.assertFalse(xtcFileNativeReader.isBusy());
        verifyFrameCoordinates871(coordinates, 201, new float[] { 3.8720002f, 4.4720000f, 4.6850004f });
    }

    @Test(expectedExceptions = GmxIoException.class)
    public void testXtcOpeningDuringReading() {
        Assert.assertFalse(xtcFileNativeReader.isBusy());
        xtcFileNativeReader.openXtcFile(XTC_WATER_IN_ARGON_PATH);
        try {
            xtcFileNativeReader.openXtcFile(XTC_WATER_IN_ARGON_PATH);
        } catch (GmxIoException ioException) {
            xtcFileNativeReader.closeXtcFile();
            throw ioException;
        }
    }

    @Test(expectedExceptions = GmxIoException.class)
    public void testXtcReadingWhenNoFileOpened() {
        Assert.assertFalse(xtcFileNativeReader.isBusy());
        xtcFileNativeReader.readNextFrame();
    }

    @Test(expectedExceptions = GmxIoException.class)
    public void testXtcReadingWhenFileClosed() {
        Assert.assertFalse(xtcFileNativeReader.isBusy());
        xtcFileNativeReader.openXtcFile(XTC_WATER_IN_ARGON_PATH);
        xtcFileNativeReader.closeXtcFile();
        xtcFileNativeReader.readNextFrame();
    }

    private void verifyFrameCoordinates871(GmxFrameCoordinates frameCoordinates, int frameNo, float[] coordinates) {
        Assert.assertNotNull(frameCoordinates);
        Assert.assertEquals(frameCoordinates.getFrameNo(), frameNo, "Wrong frame No.");
        Assert.assertEquals(frameCoordinates.getCoordinates().length, 872, "Wrong coordinates length.");
        Assert.assertEquals(frameCoordinates.getCoordinates()[871][0], coordinates[0], "Wrong X coordinate.");
        Assert.assertEquals(frameCoordinates.getCoordinates()[871][1], coordinates[1], "Wrong Y coordinate.");
        Assert.assertEquals(frameCoordinates.getCoordinates()[871][2], coordinates[2], "Wrong Z coordinate.");
    }

}
