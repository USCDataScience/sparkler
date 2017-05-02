package edu.usc.irds.sparkler.config;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class creates a wrapper for list when it is directly specified or
 * indirectly specified as file path.
 *
 * Example: File backed indirect list
 *
 *    userAgents: user-agents.txt
 *
 * Example: Direct list
 *    userAgents:
 *     - UA 1
 *     - UA 2
 */
public class FileBackedList extends ArrayList<String> {


    public FileBackedList(){
    }

    public FileBackedList(String contentFile){
        super(readFile(contentFile, FileBackedList.class.getClassLoader()));
    }

    public FileBackedList(Collection<String> contents){
        super(contents);
    }

    public static List<String> readFile(String contentFile, ClassLoader loader){

        try (InputStream stream = loader.getResourceAsStream(contentFile)) {
            assert stream != null;
            return IOUtils.readLines(stream, Charset.defaultCharset()).stream()
                    .map(String::trim)
                    .filter(l -> !(l.startsWith("#") || l.isEmpty()))
                    .collect(Collectors.toList());
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
