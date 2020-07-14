package org.jbpt.relevance;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Relevance {
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
    public static double h0(int accumulated_rho, double totalNumberOfTraces) {
        if (accumulated_rho == 0 || accumulated_rho == totalNumberOfTraces)
            return 0;
        else {
            double p = ((double) accumulated_rho) / totalNumberOfTraces;
            return -p * log2(p) - (1 - p) * log2(1 - p);
        }
    }
    public static void main(String[] args) throws Exception {

        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
        Gson gson = new GsonBuilder().create();

        Table<Integer, String, Pair<Integer, Double>> transitions = HashBasedTable.create();
        Set<Integer> states = new HashSet<>();
        Set<Integer> targets = new HashSet<>();

        reader.beginArray();
        while (reader.hasNext()) {
            AutTransition stTransition = gson.fromJson(reader, AutTransition.class);
            transitions.put(stTransition.getFrom(), stTransition.getLabel(), Pair.of(stTransition.getTo(), Math.log(stTransition.getProb())));
            states.add(stTransition.getFrom());
            states.add(stTransition.getTo());
            targets.add(stTransition.getTo());
        }
        reader.close();

        Set<Integer> sources = new HashSet<>(states);
        sources.removeAll(targets);
        Integer initialState = sources.iterator().next(); // There should be only one initial state

        long startTime = System.nanoTime();

        XLog log = XLogReader.openLog(args[1]);

        Map<String, Integer> traceFrequency = new HashMap<>();
        Map<String, Integer> traceSize = new HashMap<>();
        Map<String, Double> log2OfModelProbability = new HashMap<>();

        long totalNumberOfNonFittingTraces = 0;
        long totalNumberOfTraces = 0;

        Set<String> labels = new HashSet<>();

        long bootstrapTime = (System.nanoTime() - startTime);
        System.err.println("Bootstrap time: " + bootstrapTime + ", ");

        startTime = System.nanoTime();
        for (XTrace trace: log) {
            double lprob = 0.0;
            Integer curr = initialState;
            boolean nonfitting = false;
            String largeString = "";
            for (XEvent event: trace) {
                if (event.getAttributes().get("concept:name") == null)
                    continue;
                String label = event.getAttributes().get("concept:name").toString();
                labels.add(label);
                largeString += label;
                if (nonfitting) continue;
                if (transitions.contains(curr, label)) {
                    Pair<Integer, Double> pair = transitions.get(curr, label);
                    lprob += pair.getRight();
                    curr = pair.getLeft();
                } else {
                    nonfitting = true;
                }
            }

            if (!nonfitting && !transitions.contains(curr, "#"))
                nonfitting = true;

            traceSize.put(largeString, trace.size());

            totalNumberOfTraces++;
            if (nonfitting)
                totalNumberOfNonFittingTraces++;
            else
                log2OfModelProbability.put(largeString, lprob / Math.log(2));

            traceFrequency.put(largeString, traceFrequency.getOrDefault(largeString, 0) + 1);
        }
        long step1Time = System.nanoTime() - startTime;

//        System.out.println(traceSize.values().stream().reduce(0, (acc, v) -> acc + v) / totalNumberOfTraces);

        System.err.printf("Total: %d, non-fitting: %d\n", totalNumberOfTraces, totalNumberOfNonFittingTraces);

//        System.out.println(labels.size());

        int accumulated_rho = 0;
        double accumulated_cost_bits = 0;
        double accumulated_temp_cost_bits = 0;
        double accumulated_prob_fitting_traces = 0;

        startTime = System.nanoTime();
        for (String traceString: traceFrequency.keySet()) {
            double traceFreq = (double)traceFrequency.get(traceString);

            double cost_bits = log2OfModelProbability.containsKey(traceString)           // \sigma in \hat(L)_A
                    ? -log2OfModelProbability.get(traceString)                           // - log_2(L_A(\sigma))
                    : (1 + traceSize.get(traceString)) * log2( 1 + labels.size() );   // |\sigma| * log_2(1 + |Alpha|)

            double temp_cost_bits = log2OfModelProbability.containsKey(traceString)      // \sigma in \hat(L)_A
                    ? 0                                                                  // - log_2(L_A(\sigma))
                    : (1 + traceSize.get(traceString)) * log2( 1 + labels.size() );   // |\sigma| * log_2(1 + |Alpha|)

            accumulated_temp_cost_bits += temp_cost_bits * traceFreq;

            accumulated_rho += log2OfModelProbability.containsKey(traceString) ?
                    traceFreq // / totalNumberOfTraces
                    : 0;

            accumulated_cost_bits += (cost_bits * traceFreq) / totalNumberOfTraces;

            if (log2OfModelProbability.containsKey(traceString))
                accumulated_prob_fitting_traces += traceFreq / totalNumberOfTraces;
        }

        System.out.printf("%d,%d,%d,%d,%10.8f,%10.8f,%10.8f,%d,%d,%d\n", states.size(), transitions.size(), totalNumberOfTraces, totalNumberOfNonFittingTraces,
                accumulated_prob_fitting_traces,
                h0(accumulated_rho, totalNumberOfTraces) + accumulated_cost_bits, accumulated_temp_cost_bits,
                bootstrapTime, step1Time, System.nanoTime() - startTime);
    }
}
