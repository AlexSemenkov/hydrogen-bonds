package com.asemenkov.gromacs.frame.coordinates;

/**
 * @author asemenkov
 * @since Aug 12, 2018
 */
public abstract class GmxAbstractFrameCoordinatesBuilder<T extends GmxAbstractFrameCoordinatesBuilder> {

    protected int frameNo;

    // ======== INTERFACE ========

    public abstract GmxFrameCoordinates build();

    public T withFrameNo(int frameNo) {
        this.frameNo = frameNo;
        return downcastThisToT();
    }

    // ======== SUPPORT METHODS ========

    @SuppressWarnings("unchecked")
    private T downcastThisToT() {
        return (T) this;
    }
}
