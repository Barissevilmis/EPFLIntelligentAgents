package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveRLA implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private HashMap<State, Integer> best;
	private List<City> cities;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		cities = topology.cities();

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		int costPerKm = agent.vehicles().get(0).costPerKm();
		
		ArrayList<State> S = new ArrayList<State>();
		HashMap<State, Double> V = new HashMap<State, Double>();
		HashMap<State, HashMap<Integer, Double>> Q = new HashMap<State, HashMap<Integer,Double>>();
		HashMap<State, HashMap<Integer, Double>> R = new HashMap<State, HashMap<Integer,Double>>();
		HashMap<State, HashMap<Integer, HashMap<State, Double>>> T = new HashMap<State, HashMap<Integer,HashMap<State,Double>>>();
		
		setupTables(topology, td, costPerKm, S, V, Q, R, T);

		best = new HashMap<State, Integer>();

		double epsilon = 1e-6;
		double change;
		int iter = 0;
		
		do {
			change = 0;
			
			for (State s : S) {
				double oldV = V.get(s);
				double max = Double.NEGATIVE_INFINITY;
				
				for (Integer a : Q.get(s).keySet()) {
					double newVal = R.get(s).get(a);
					
					for (State s2 : T.get(s).get(a).keySet()) {
						newVal += discount * T.get(s).get(a).get(s2) * V.get(s2);
					}
					
					Q.get(s).put(a, newVal);
					if (newVal > max) {
						max = newVal;
						best.put(s, a);
					}
				}
				
				V.put(s, max);
				change += Math.abs(max - oldV);
			}
			
			System.out.println(change);
			iter++;
		} while (change > epsilon);

		System.out.println("Done in " + iter + " iterations!");
	}
	
	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		State currentState;
		City currentCity = vehicle.getCurrentCity();
		
		if (availableTask == null) {	
			currentState = new State(currentCity.id, currentCity.id);
		}
		else {
			currentState = new State(currentCity.id, availableTask.deliveryCity.id);
		}
		
		int bestAction = best.get(currentState);
		
		if (bestAction == -1) {
			action = new Pickup(availableTask);
		}
		else {
			action = new Move(cities.get(bestAction));
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit of reactive agent after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
	
	public void setupTables(Topology topology, 
							TaskDistribution td, 
							int costPerKm,
							ArrayList<State> S,
							HashMap<State, Double> V, 
							HashMap<State, HashMap<Integer, Double>> Q, 
							HashMap<State, HashMap<Integer, Double>> R, 
							HashMap<State, HashMap<Integer, HashMap<State, Double>>> T) {
		
		for (City city : topology) {
			for (City task: topology) {
				State s = new State(city.id, task.id);
				
				S.add(s);
				
				V.put(s, 0.0);
				
				if (Q.get(s) == null) 
					Q.put(s, new HashMap<Integer, Double>());
				
				if (R.get(s) == null) 
					R.put(s, new HashMap<Integer, Double>());
				
				if (T.get(s) == null) 
					T.put(s, new HashMap<Integer, HashMap<State, Double>>());
				
				HashMap<Integer, Double> actionValues = Q.get(s);
				HashMap<Integer, Double> rewardValues = R.get(s);
				HashMap<Integer, HashMap<State, Double>> stateTransitions = T.get(s);
				
				for (City neighbor : city) {
					actionValues.put(neighbor.id, 0.0);
					rewardValues.put(neighbor.id, -city.distanceTo(neighbor)*costPerKm);
					
					if (stateTransitions.get(neighbor.id) == null)
						stateTransitions.put(neighbor.id, new HashMap<State, Double>());
					
					HashMap<State, Double> stateProbs = stateTransitions.get(neighbor.id);
					
					for (City task2 : topology) {
						State s2 = new State(neighbor.id, task2.id);
						if (neighbor.id != task2.id) {							
							stateProbs.put(s2, td.probability(neighbor, task2));
						}
						else {
							stateProbs.put(s2, td.probability(neighbor, null));
						}
					}
				}
				
				if (city.id != task.id) {
					// If there is a task to pick up, set values for pickup
					actionValues.put(-1, 0.0);
					rewardValues.put(-1, td.reward(city, task) - city.distanceTo(task)*costPerKm);
					
					if (stateTransitions.get(-1) == null)
						stateTransitions.put(-1, new HashMap<State, Double>());
					
					HashMap<State, Double> stateProbs = stateTransitions.get(-1);
					
					for (City task2 : topology) {
						State s2 = new State(task.id, task2.id);
						if (task.id != task2.id) {							
							stateProbs.put(s2, td.probability(task, task2));
						}
						else {
							stateProbs.put(s2, td.probability(task, null));
						}
					}
				}
			}
		}
	}
	
}
