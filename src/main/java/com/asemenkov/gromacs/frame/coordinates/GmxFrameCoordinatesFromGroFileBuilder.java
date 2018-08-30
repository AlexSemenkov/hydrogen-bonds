package com.asemenkov.gromacs.frame.coordinates;

import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.utils.Logger;

import java.util.List;

/**
 * @author asemenkov
 * @since Aug 12, 2018
 */
public class GmxFrameCoordinatesFromGroFileBuilder extends GmxAbstractFrameCoordinatesBuilder<GmxFrameCoordinatesFromGroFileBuilder> {

    private List<GmxGroFileAtomLine> groFileAtomLines;

    // ======== INTERFACE ========

    public GmxFrameCoordinatesFromGroFileBuilder withGroFileAtomLines(List<GmxGroFileAtomLine> groFileAtomLines) {
        this.groFileAtomLines = groFileAtomLines;
        return this;
    }

    @Override
    public GmxFrameCoordinates build() {
        validateGroFileAtomLines();
        float[][] coordinates = groFileAtomLines.parallelStream() //
                .map(GmxGroFileAtomLine::getCoordinates) //
                .toArray(float[][]::new);

        GmxFrameCoordinates toReturn = new GmxFrameCoordinates(coordinates, frameNo);
        Logger.log("Frame coordinates successfully created from .gro file");
        return toReturn;
    }

    // ======== VALIDATION METHODS ========

    private void validateGroFileAtomLines() {
        if (groFileAtomLines == null || groFileAtomLines.size() == 0) //
            throw new GmxIoException("Invalid atom lines from .gro file: " + groFileAtomLines);
    }
}
