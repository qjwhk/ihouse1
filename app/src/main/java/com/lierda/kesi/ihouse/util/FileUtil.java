package com.lierda.kesi.ihouse.util;

import java.io.File;

/**
 * Created by qianjiawei on 2018/4/12.
 */

public class FileUtil {

    public static boolean isFileExist(File file){
        if(file.exists()){
           return true;
        }else {
            return false;
        }
    }
}
