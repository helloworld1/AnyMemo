package org.liberty.android.fantastischmemo.converter;

import java.io.File;

import org.apache.mycommons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.utils.AMZipUtils;

public class ZipImporter implements AbstractConverter {
    
    private static final long serialVersionUID = 8597517392515565023L;

    @Override
    public void convert(String src, String dest) throws Exception {
        String pathToExtract = FilenameUtils.getPath(dest);
        AMZipUtils.unZipFile(new File(src), new File(pathToExtract));
    }

    @Override
    public String getSrcExtension() {
        return "zip";
    }
    
    @Override
    public String getDestExtension() {
        return "db";
    }

}
