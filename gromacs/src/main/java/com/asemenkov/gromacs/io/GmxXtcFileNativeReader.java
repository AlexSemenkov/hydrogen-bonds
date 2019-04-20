package com.asemenkov.gromacs.io;

import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.utils.io.FileUtils;
import com.asemenkov.utils.io.Logger;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public class GmxXtcFileNativeReader {

    private static boolean isBusy;

    static {
        String lib = Paths.get(FileUtils.getProjectHome().toString(), //
                "gromacs", "lib", "xtc-native-reader").toString();

        if (SystemUtils.IS_OS_LINUX) lib += ".so";
        else if (SystemUtils.IS_OS_MAC) lib += ".dylib";
        else if (SystemUtils.IS_OS_WINDOWS) lib += ".dll";
        else throw new GmxIoException("Unsupported OS: " + SystemUtils.OS_NAME);

        Logger.log("Loading xtc-native-reader library: " + lib);
        System.load(lib);
    }

    private native boolean openXtcFileC(String xtcFile);

    private native Object readNextFrameC();

    private native void closeXtcFileC();

    private native int getNumberOfAtomsC();

    public void openXtcFile(Path xtcFilePath) {
        if (isBusy) throw new GmxIoException("The previous .xtc file is still opened.");
        else isBusy = openXtcFileC(xtcFilePath.toAbsolutePath().toString());
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
