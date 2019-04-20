package com.asemenkov.gromacs.frame.structure;

import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.gromacs.particles.utils.GmxAtomReflectionData;
import com.asemenkov.gromacs.particles.utils.GmxResidueReflectionData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * @author asemenkov
 * @since Apr 23, 2018
 */
public abstract class GmxAbstractFrameStructureBuilder<T extends GmxAbstractFrameStructureBuilder> {

    protected @Autowired GmxAtomReflectionData atomReflectionData;
    protected @Autowired GmxResidueReflectionData residueReflectionData;

    protected String description;
    protected float[] box;

    // ======== INTERFACE ========

    public abstract GmxFrameStructure build();

    public T withDescription(String description) {
        this.description = description;
        return downcastThisToT();
    }

    public T withBox(float[] box) {
        this.box = Arrays.copyOf(box, 3);
        return downcastThisToT();
    }

    // ======== VALIDATION METHODS ========

    protected void validateBox() {
        if (box == null || box.length != 3 || box[0] == 0 || box[1] == 0 || box[2] == 0) //
            throw new GmxIoException("Invalid box from .gro file: " + Arrays.toString(box));
    }

    // ======== SUPPORT METHODS ========

    @SuppressWarnings("unchecked")
    private T downcastThisToT() {
        return (T) this;
    }

}
