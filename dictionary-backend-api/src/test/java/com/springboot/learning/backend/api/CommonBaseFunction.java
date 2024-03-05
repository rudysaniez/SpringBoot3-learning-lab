package com.springboot.learning.backend.api;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class CommonBaseFunction {

    /**
     *
     * @param filename : the filename
     * @return {@link String}
     */
    protected static String getBodyByFileName(String filename) throws Exception {
        InputStream rawFile = CommonBaseFunction.class.getClassLoader().getResourceAsStream(filename);
        return IOUtils.toString(rawFile, Charsets.UTF_8);
    }
}
