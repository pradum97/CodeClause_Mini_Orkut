package com.mini.orkut;

import java.io.InputStream;

public class FileLoader {
    public InputStream load (String path){

      return   getClass().getResourceAsStream(path);
    }
}
