import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

public class GraphVisualization {
	public static void main(String args[]) throws FileNotFoundException {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new MultiGraph("MovieLens", false, true);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		// graph.setAttribute("layout.stabilization-limit", 0.7);
		String styleSheet = "node { size: 5px; fill-color: #777; text-mode: hidden; z-index: 1;}"
				+ "node.seed { size: 10px; fill-color: red; text-mode: hidden; z-index: 5; }"
				+ "node.adopt { size: 5px; fill-color: green; text-mode: hidden; z-index: 4;}"
				+ "node.promote { size: 5px; fill-color: blue; text-mode: hidden;z-index: 3;}"
				+ "node.inhibit { size: 5px; fill-color: yellow; text-mode: hidden;z-index: 2;}"
				+ "edge { shape: cubic-curve; arrow-shape: none;z-index: 0;}";
		graph.addAttribute("ui.stylesheet", styleSheet);

		LTC app = new LTC();
		app.deSerializeMap("/tmp/graphviz.ser");

		int edgeCount = 0;
		ConcurrentHashMap<Integer, Node> usersList = app.getUsersList();
		Iterator<Integer> it = usersList.keySet().iterator();
		while (it.hasNext()) {
			Node v = usersList.get(it.next());
			int vId = v.getUserId();
			ConcurrentHashMap<Integer, Float> inLinks = v.getInLinks();
			Iterator<Integer> neighbors = inLinks.keySet().iterator();
			while (neighbors.hasNext()) {
				int uId = neighbors.next();
				float weight = inLinks.get(uId);
				String edgeIndex = Integer.toString(edgeCount);
				graph.addEdge(edgeIndex, Integer.toString(uId), Integer.toString(vId), true);
				Edge e = graph.getEdge(edgeIndex);
				e.setAttribute("weight", weight);
				edgeCount++;
			}
		}

		Set<Integer> seedSet = app.getCalculatedSeed();
		it = usersList.keySet().iterator();
		while (it.hasNext()) {
			Node v = usersList.get(it.next());
			org.graphstream.graph.Node graphNode = graph.getNode(Integer.toString(v.getUserId()));
			if (seedSet.contains(v.getUserId()))
				graphNode.setAttribute("ui.class", "seed");
		}
		System.out.println("Total No Of Users : " + usersList.size());
		System.out.println("Total No Of Edges : " + edgeCount);
		System.out.println("Total No Of Seeds : " + seedSet.size());
		System.out.println("Expected Spread for Seeds : " + app.totalCoverage);		

		Viewer viewer = graph.display();
	}
}