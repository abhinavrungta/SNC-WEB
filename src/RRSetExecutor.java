import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class RRSetExecutor implements Callable<Integer> {
	LTC model = null;
	int setNo = 0;

	public RRSetExecutor(LTC model, int setNo) {
		this.model = model;
		this.setNo = setNo;
	}

	@Override
	/**
	 * 1. Select a Random Node in the underlying Graph G. 2. Perform BFS by
	 * adding a node of an incoming edge (e) to the queue with probability p(e)
	 * 3. Do until queue is empty.
	 * 
	 * @return
	 */
	public Integer call() throws Exception {
		// get a random node Id.
		int width = 0;
		Object key = model.userArray[ThreadLocalRandom.current().nextInt(model.userArray.length)];
		int currentNode = model.usersList.get(key).userId;
		// create a random RR set using a random BFS.
		Set<Integer> visited = new HashSet<Integer>();
		Queue<Integer> bfs = new LinkedList<Integer>();
		bfs.add(currentNode);
		visited.add(currentNode);
		while (!bfs.isEmpty()) {
			currentNode = bfs.poll();
			this.model.inverseSet.get(currentNode).add(this.setNo);
			Set<Entry<Integer, Float>> inLinks = model.usersList.get(currentNode).getInLinks().entrySet();
			width += inLinks.size();
			Iterator<Entry<Integer, Float>> itr = inLinks.iterator();
			while (itr.hasNext()) {
				Entry<Integer, Float> item = itr.next();
				if (!visited.contains(item.getKey())) {
					visited.add(item.getKey());
					double prob = ThreadLocalRandom.current().nextDouble();
					// TODO: Change probability condition. For now assuming that
					// p(e) is equal to edge weight.
					if (prob < item.getValue()) {
						bfs.add(item.getKey());
					}
				}
			}
		}
		return width;
	}

}
