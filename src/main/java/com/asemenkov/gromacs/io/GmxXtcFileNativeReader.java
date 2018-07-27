package com.asemenkov.gromacs.io;

import java.nio.file.Path;

import com.asemenkov.gromacs.exceptions.GmxIoException;
import com.asemenkov.gromacs.frame.GmxFrameCoordinates;
import com.asemenkov.utils.Logger;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public class GmxXtcFileNativeReader {

    private static boolean isBusy;

    static {
        Logger.log("Loading xtc-native-reader library.");
        System.loadLibrary("xtc-native-reader");
    }

    private native boolean openXtcFileC(String xtcFile);

    private native Object readNextFrameC();

    private native void closeXtcFileC();

    private native int getNumberOfAtomsC();

    public boolean openXtcFile(Path xtcFilePath) {
        if (isBusy) throw new GmxIoException("The previous .xtc file is still opened.");
        else isBusy = openXtcFileC(xtcFilePath.toAbsolutePath().toString());
        return isBusy;
    }

    public GmxFrameCoordinates readNextFrame() {
        if (!isBusy) throw new GmxIoException("No .xtc file opened.");
        GmxFrameCoordinates frameCoordinates = (GmxFrameCoordinates) readNextFrameC();
        if (frameCoordinates == null) closeXtcFile();
        return frameCoordinates;
    }

    public void closeXtcFile() {
        closeXtcFileC();
        isBusy = false;
    }

    public int getNumberOfAtoms() {
        if (!isBusy) throw new GmxIoException("No .xtc file opened.");
        return getNumberOfAtomsC();
    }

    public boolean isBusy() {
        return isBusy;
    }
}
