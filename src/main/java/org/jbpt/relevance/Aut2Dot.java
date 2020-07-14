package org.jbpt.relevance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Aut2Dot {
    public static void main(String[] args) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
        Gson gson = new GsonBuilder().create();

        List<AutTransition> transitions = new ArrayList<>();
        Set<Integer> states = new HashSet<>();

        reader.beginArray();
        while (reader.hasNext()) {
            AutTransition stTransition = gson.fromJson(reader, AutTransition.class);
            transitions.add(stTransition);
            states.add(stTransition.getFrom());
            states.add(stTransition.getTo());
        }
        reader.close();

        System.out.println("digraph G {");
        for (int state : states)
            System.out.printf("\tn%d [label=\"%d\"];\n", state, state);
        for (AutTransition t: transitions)
            System.out.printf("\tn%d -> n%d [label=\"%s (%5.4f)\"];\n", t.getFrom(), t.getTo(), t.getLabel(), t.getProb());
        System.out.println("}");
    }
}
