import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class Executor implements Callable<Integer> {
	LTC model = null;
	ConcurrentHashMap<Integer, NodeState> influence = null;
	Set<Integer> seed;
	int productId;

	public Executor(LTC model, Set<Integer> seed) {
		this.model = model;
		this.influence = new ConcurrentHashMap<Integer, NodeState>();
		this.seed = seed;
		this.productId = this.model.movieRatings.keySet().iterator().next();
	}

	@Override
	public Integer call() throws Exception {
		int expectedCoverage = 0;
		float rating = 0.0f;
		Queue<Integer> activeNodes = new LinkedList<>();
		Iterator<Integer> itr = model.usersList.keySet().iterator();
		while (itr.hasNext()) {
			int id = itr.next();
			Node tmp = model.usersList.get(id);
			NodeState ns = new NodeState(tmp);
			this.influence.put(id, ns);
			if (seed.contains(id))
				ns.setState(State.ADOPT);
		}

		activeNodes.addAll(seed);
		expectedCoverage += seed.size();
		while (!activeNodes.isEmpty()) {
			int nodeId = activeNodes.poll();
			NodeState u = influence.get(nodeId);
			Iterator<Integer> outLinks = u.node.getOutLinks().iterator();
			while (outLinks.hasNext()) {
				NodeState v = influence.get(outLinks.next());
				if (v.getState() == State.INACTIVE && u.getState() != State.INACTIVE) {
					rating = model.getRating(nodeId, productId, u.getState());
					if (v.updateCurrentThreshold(u, rating) >= v.node.getActivationThreshold()) {
						v.setState(State.ACTIVE);
						activeNodes.add(v.node.getUserId());
						if (v.getState() == State.ADOPT)
							expectedCoverage += 1;
					}
				}
			}
		}
		activeNodes.clear();
		return expectedCoverage;
	}

}
