package org.jbpt.relevance;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;

public class JDFG2Aut {
    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        File file = new File(args[0]);
        Reader reader = new FileReader(file);
        Bundle dfg = gson.fromJson(reader, Bundle.class);

        Map<Integer, Integer> aggregateOutgoingFrequency = new HashMap<>();
        Map<Integer, Pair<String, Integer>> nodeInfo = new HashMap<>();

        for (BNode node: dfg.nodes)
            if (node != null) {
                nodeInfo.put(node.getId(), Pair.of(node.getLabel(), node.getFreq()));
            }

        Set<Integer> sinks = new HashSet<>(nodeInfo.keySet());

        for (BArc arc: dfg.arcs) {
            if (arc != null) {
                aggregateOutgoingFrequency.put(
                        arc.getFrom(),
                        aggregateOutgoingFrequency.getOrDefault(arc.getFrom(), 0) +
                                arc.getFreq()
                );
                sinks.remove(arc.getFrom());
            }
        }

        List<AutTransition> aut2 = new ArrayList<>();

        for (BArc arc: dfg.arcs) {
            String label = sinks.contains(arc.getTo()) ? "#" : nodeInfo.get(arc.getTo()).getLeft();
            aut2.add(new AutTransition(
                    arc.getFrom(), arc.getTo(), label,
                    (double) arc.getFreq() / aggregateOutgoingFrequency.get(arc.getFrom())
            ));
        }

        FileWriter writer = new FileWriter(args[1] + "/" + file.getName());
        IOUtils.write(gson.toJson(aut2), writer);
        writer.flush();
        writer.close();
    }
}
