package org.jbpt.relevance;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public class Bundle {
    public List<BNode> nodes;
    public List<BArc> arcs;

    public void toDot(PrintStream out) {
        out.println("digraph G {");
        nodes.forEach(n -> {if (n != null) out.println(n.toDot());});
        arcs.forEach(a -> {if (a != null) out.println(a.toDot());});
        out.println("}");
    }
}
