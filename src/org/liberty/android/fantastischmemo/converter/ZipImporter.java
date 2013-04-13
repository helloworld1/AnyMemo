package org.liberty.android.fantastischmemo.converter;

import java.io.File;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.utils.AMZipUtils;


public class ZipImporter implements AbstractConverter {
    
    @Override
    public void convert(String src, String dest) throws Exception {
        AMZipUtils.unZipFile(new File(src), new File(AMEnv.DEFAULT_ROOT_PATH));
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
