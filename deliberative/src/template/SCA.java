package template;

import java.util.ArrayList;

import logist.plan.Action;

public class SCA {
	
	public State state;
	public double cost;
	public ArrayList<Action> actions;
	
	public SCA(State state, double cost, ArrayList<Action> actions) {
		this.state = state;
		this.cost = cost;
		this.actions = actions;
	}
}
