import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

enum State {
	INACTIVE, ACTIVE, ADOPT, PROMOTE, INHIBIT
}

class NodeState implements java.io.Serializable {

	private static final long serialVersionUID = 42698341359445L;
	private float currentThreshhold;
	private State status;
	Node node;

	public NodeState(Node node) {
		this.node = node;
		this.currentThreshhold = 0.0f;
		this.status = State.INACTIVE;
	}

	// update state of node and the state is changed to Adopt, Promote or
	// inhibit with respective probabilities based on random event.
	public void setState(State state) {
		if (state == State.ACTIVE) {
			double rand = ThreadLocalRandom.current().nextDouble();
			if (rand >= 0 && rand <= node.adoptionProbability) {
				this.status = State.ADOPT;
			} else if (rand > node.adoptionProbability && rand <= 1.0) {
				double rand2 = ThreadLocalRandom.current().nextDouble();
				if (rand2 >= 0 && rand2 <= node.promotionProbability) {
					this.status = State.PROMOTE;
				} else if (rand2 > node.promotionProbability && rand2 <= 1.0) {
					this.status = State.INHIBIT;
				}
			}
		} else {
			this.status = state;
		}

	}

	public State getState() {
		return this.status;
	}

	public double updateCurrentThreshold(NodeState u, float rating) {
		float weight = this.node.inLinks.get(u.node.getUserId());
		this.currentThreshhold += (weight * (rating - LTC.rMin) / (LTC.rMax - LTC.rMin));
		return this.currentThreshhold;
	}

	public void resetCurrentThreshold() {
		this.currentThreshhold = 0.0f;
	}
}

public class Node implements java.io.Serializable {
	private static final long serialVersionUID = -4481942698341359445L;

	int userId;
	float activationThreshold;
	double adoptionProbability;
	double promotionProbability;
	ArrayList<Integer> outLinks;
	ConcurrentHashMap<Integer, Float> inLinks;

	public Node(int userId) {
		this.userId = userId;
		this.outLinks = new ArrayList<>();
		this.inLinks = new ConcurrentHashMap<Integer, Float>();
	}

	public int getUserId() {
		return userId;
	}

	public void setActivationThreshold(float activationThreshold) {
		this.activationThreshold = activationThreshold;
	}

	public void setAdoptionProbability(double adoptionProbability) {
		this.adoptionProbability = adoptionProbability;
	}

	public void setPromotionProbability(double promotionProbability) {
		this.promotionProbability = promotionProbability;
	}

	public void addToOutLinkList(int id) {
		this.outLinks.add(id);
	}

	public void addToInLinkList(int nodeId, float weight) {
		this.inLinks.put(nodeId, weight);
	}

	public ConcurrentHashMap<Integer, Float> getInLinks() {
		return this.inLinks;
	}

	public ArrayList<Integer> getOutLinks() {
		return this.outLinks;
	}

	public float getActivationThreshold() {
		return activationThreshold;
	}
}