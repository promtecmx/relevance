package org.jbpt.relevance;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class JDFG2Dot {
    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();
        Reader reader = new FileReader(args[0]);
        Bundle dfg = gson.fromJson(reader, Bundle.class);
        dfg.toDot(System.out);
    }
}
