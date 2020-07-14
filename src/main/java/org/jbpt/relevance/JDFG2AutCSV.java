package org.jbpt.relevance;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class JDFG2AutCSV {
    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        File file = new File(args[0]);
        Reader reader = new FileReader(file);
        Bundle dfg = gson.fromJson(reader, Bundle.class);

        Map<Integer, String> labels = new TreeMap<>();
        Map<Integer, Integer> stateFreq = new TreeMap<>();

        Integer sourceId = null;

        for (BNode node: dfg.nodes)
            if (node != null) {
                labels.put(node.getId(), node.getLabel());
                if (node.getLabel().equals("__INITIAL__"))
                    sourceId = node.getId();
            }

        Table<Integer, Integer, Integer> transitions = HashBasedTable.create();
        for (BArc arc: dfg.arcs) {
            if (arc != null) {
                transitions.put(arc.getFrom(), arc.getTo(), arc.getFreq());
                stateFreq.put(arc.getTo(), stateFreq.getOrDefault(arc.getTo(), 0) + arc.getFreq());

                if (arc.getFrom().equals(sourceId))
                    stateFreq.put(sourceId, stateFreq.getOrDefault(sourceId, 0) + arc.getFreq());
            }
        }

        for (Integer src : labels.keySet()) {
            System.out.printf("%d,%d", src, stateFreq.get(src));
            String transLabels = "";
            for (Integer tgt : labels.keySet()) {
                if (transitions.contains(src, tgt)) {
                    transLabels += "," + labels.get(tgt);
                    System.out.print("," + stateFreq.get(tgt));
                } else
                    System.out.print(",0");
            }
            System.out.println(transLabels.isEmpty()? "," : transLabels);
        }
    }
}
