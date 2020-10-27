package template;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

public class ReactiveRLABackup implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		
		// Learn the optimal policy here
		int numCities = topology.size();
//		double[][] Q = new double[numCities * numCities][numCities];
		
		for (City c : topology) {
			System.out.println(c.id + " " + c);
		}
		
//		for (City city1 : topology) {
//			for (City city2 : topology) {
//				System.out.println(city1 + " " + city2 + " " + td.probability(city1, city2) + " " +  td.reward(city1, city2));
//			}
//		}
		
//		System.out.println();
//		City c = topology.parseCity("Paris");
//		double sum = 0.0;
//		double p = td.probability(c, null);
//		sum += p;
//		System.out.println(p);			
//		for (City to : topology) {
//			p = td.probability(c, to);
//			System.out.println(p);	
//			sum += p;
//		}
//		System.out.println(sum);
		
		// Initialize Q
//		HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> Q = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
//		for (City city : topology) {
//			for (City task: topology) {
//				for (City neighbor : city.neighbors()) {
//					setQ(Q, city.id, task.id, neighbor.id, 0);
//				}
//				if (city.id != task.id) {
//					// If task available, set pickup action as well
//					setQ(Q, city.id, task.id, -1, 0);
//				}
//			}
//		}
		
		HashMap<State, Double> V = new HashMap<State, Double>();
		HashMap<State, HashMap<Integer, Double>> Q = new HashMap<State, HashMap<Integer,Double>>();
		HashMap<State, HashMap<Integer, Double>> R = new HashMap<State, HashMap<Integer,Double>>();
		HashMap<State, HashMap<Integer, HashMap<State, Double>>> T = new HashMap<State, HashMap<Integer,HashMap<State,Double>>>();
		
		for (City city : topology) {
			for (City task: topology) {
				State s = new State(city.id, task.id);
				
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
				
				for (City neighbor : city.neighbors()) {
					actionValues.put(neighbor.id, 0.0);
					rewardValues.put(neighbor.id, -city.distanceTo(neighbor));
					
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
					rewardValues.put(-1, td.reward(city, task) - city.distanceTo(task));
					
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
		
//		Map<State, Double> sortedV = V.entrySet().stream()
//                .sorted((e1,e2) -> e1.getKey().compareTo(e2.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
//		System.out.println(sortedV);
//		
//		Map<State, HashMap<Integer, Double>> sortedQ = Q.entrySet().stream()
//                .sorted((e1,e2) -> e1.getKey().compareTo(e2.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
//		System.out.println(sortedQ);
//		
//		Map<State, HashMap<Integer, Double>> sortedR = R.entrySet().stream()
//                .sorted((e1,e2) -> e1.getKey().compareTo(e2.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
//		System.out.println(sortedR);
//		
//		
//		HashMap<State, HashMap<Integer, HashMap<State, Double>>> sortedT = T.entrySet().stream()
//                .sorted((e1,e2) -> e1.getKey().compareTo(e2.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
//		System.out.println(sortedT);
//		
//		for (State key : sortedT.keySet()) {
//			System.out.println(key);
//			System.out.println(sortedT.get(key));
//		}
	}
	
//	public double getQ(HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> Q, int city, int task, int action) {
//		return Q.get(city).get(task).get(action);
//	}
//	
//	public void setQ(HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> Q, int city, int task, int action, double val) {
//		if (Q.get(city) == null) 
//			Q.put(city, new HashMap<Integer, HashMap<Integer, Double>>());
//		
//		if (Q.get(city).get(task) == null) 
//			Q.get(city).put(task, new HashMap<Integer, Double>());
//		
//		Q.get(city).get(task).put(action, val);
//	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
}
