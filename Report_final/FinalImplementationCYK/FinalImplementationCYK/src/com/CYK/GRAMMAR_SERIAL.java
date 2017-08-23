package com.CYK;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by irtazasafi on 16/10/2016.
 */
public class GRAMMAR_SERIAL {
    Multimap<String, String> forward = HashMultimap.create();
    Multimap<String, String> reverse = HashMultimap.create();

    GRAMMAR_SERIAL(String filename) throws IOException { // constructor parses the grammar and loads it into a MultiMap
        Stream<String> stream = Files.lines(Paths.get(filename));
        stream.forEach((String grammarLine)->{
            String LHS = grammarLine.split("-")[0];
            String [] secondSplit = grammarLine.split("-")[1].split(",");
            Arrays.stream(secondSplit).forEach((String RHS)->{
                forward.put(LHS,RHS);
                reverse.put(RHS,LHS);
            });

        });
    }

    Collection<String> getForward(String key){
        return forward.get(key);
    }

    Collection<String> getReverse(String key){
        return reverse.get(key);
    }
}
