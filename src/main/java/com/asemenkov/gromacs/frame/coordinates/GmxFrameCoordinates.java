package com.asemenkov.gromacs.frame.coordinates;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public class GmxFrameCoordinates {

    private final float[][] coordinates;
    private final int frameNo;

    public GmxFrameCoordinates(float[][] coordinates, int frameNo) {
        this.coordinates = coordinates;
        this.frameNo = frameNo;
    }

    public float[][] getCoordinates() {
        return coordinates;
    }

    public int getFrameNo() {
        return frameNo;
    }

}
