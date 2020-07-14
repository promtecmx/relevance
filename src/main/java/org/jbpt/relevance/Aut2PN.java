package org.jbpt.relevance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.IOUtils;
import org.jbpt.petri.*;
import org.jbpt.petri.io.PNMLSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

public class Aut2PN {
    public static void main(String[] args) throws Exception {
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
        Gson gson = new GsonBuilder().create();

        List<AutTransition> autTransitions = new ArrayList<>();
        Set<Integer> autStates = new HashSet<>();

        reader.beginArray();
        while (reader.hasNext()) {
            AutTransition autTransition = gson.fromJson(reader, AutTransition.class);
            if (autTransition.getLabel().equals("#")) continue;
            autTransitions.add(autTransition);
            autStates.add(autTransition.getFrom());
            autStates.add(autTransition.getTo());
        }
        reader.close();

        PetriNet net = new PetriNet();

        Map<Integer, Place> placeMap = new HashMap<>();
        for (Integer state: autStates) {
            Place place = new Place("s" + state);
            net.addPlace(place);
            placeMap.put(state, place);
        }

        Map<AutTransition, Transition> transMap = new HashMap<>();
        for (AutTransition autTransition: autTransitions) {
            Transition transition = new Transition(autTransition.getLabel());
            net.addTransition(transition);
            net.addFlow(placeMap.get(autTransition.getFrom()), transition);
            net.addFlow(transition, placeMap.get(autTransition.getTo()));
        }

        NetSystem netSystem = new NetSystem(net);
        for (Node n: netSystem.getSourceNodes())
            netSystem.putTokens((Place) n, 1);

        File file = new File(args[0]);
        FileWriter writer = new FileWriter(args[1] + "/" + file.getName().substring(0, file.getName().length() - 4) + "pnml");
        IOUtils.write(PNMLSerializer.serializePetriNet(netSystem), writer);
        writer.flush();
        writer.close();
    }
}
