package com.asemenkov.gromacs.frame.structure;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;

/**
 * @author asemenkov
 * @since Apr 23, 2018
 */
public abstract class GmxAbstractFrameStructureBuilder<T extends GmxAbstractFrameStructureBuilder> {

    protected @Autowired Map<String, Class<? extends GmxAtom>> atomClassesMap;
    protected @Autowired Map<String, Class<? extends GmxResidue>> residueClassesMap;
    protected @Autowired Map<Class<? extends GmxResidue>, Field[]> residueAtomsMap;

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
