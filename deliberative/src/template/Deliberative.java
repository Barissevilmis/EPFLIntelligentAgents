package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue; 

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Plan;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class Deliberative implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;
	int costPerKm;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		capacity = agent.vehicles().get(0).capacity();
		costPerKm = agent.vehicles().get(0).costPerKm();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		
		System.out.println("Planning for vehicle " + vehicle + " and tasks " + tasks);
		
		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			plan = makePlan(vehicle, tasks, true);
			break;
		case BFS:
			plan = makePlan(vehicle, tasks, false);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan makePlan(Vehicle vehicle, TaskSet tasks, boolean useASTAR) {
		// Initial State
		City initCity = vehicle.getCurrentCity();
		TaskSet initTasks = vehicle.getCurrentTasks();
		State initState = new State(initCity, initTasks, TaskSet.intersectComplement(tasks, initTasks));
		
		// Initial cost and actions
		double initCost = 0.0;
		ArrayList<Action> initActions = new ArrayList<Action>();
		SCA initSCA = new SCA(initState, initCost, initActions);
		
		// Minimum cost to get to a goal state, and the actions to get there
		double bestCost = Double.MAX_VALUE;
		ArrayList<Action> bestActions = new ArrayList<Action>();
		
		Queue<SCA> q;
		
		if (useASTAR) {
			// Priority queue keeps the states sorted according to their cost + heuristic cost
			q = new PriorityQueue<SCA>(new SCAComparator(costPerKm));
		}
		else {			
			// Queue holds state, cost to get to that state, and actions to get to that state
			q = new LinkedList<SCA>();
		}
		
		// Visited holds the visited states and their costs
		HashMap<State, Double> visited = new HashMap<State, Double>();
		
		q.add(initSCA);
		
		while (!q.isEmpty()) {
			SCA currSCA = q.poll();
			
			// Unpack SCA
			State currState = currSCA.state;
			double currCost = currSCA.cost;
			ArrayList<Action> currActions = currSCA.actions;
			
			// Unpack state
			City currCity = currState.vehicleCity;
			TaskSet carriedTasks = currState.carriedTasks;
			TaskSet remainingTasks = currState.remainingTasks;
			
			// Measure remaining capacity
			int currCapacity = capacity;
			for (Task carriedTask : carriedTasks) {
				currCapacity -= carriedTask.weight;
			}
			
			// Is this a goal state?
			if (carriedTasks.isEmpty() && remainingTasks.isEmpty()) {
				// Is it the best goal state we've seen so far?
				if (currCost < bestCost) {
					bestCost = currCost;
					bestActions = currActions;
				}
			}
			
			// No need to explore if we've already visited this state with a lower cost before
			if (!visited.containsKey(currState) || currCost < visited.get(currState)) {
				visited.put(currState, currCost);
				
				// Delivery successors
				for (Task carriedTask : carriedTasks) {
					City newCity = carriedTask.deliveryCity;
					
					TaskSet newCarriedTasks = carriedTasks.clone();
					newCarriedTasks.remove(carriedTask);
					
					TaskSet newRemainingTasks = remainingTasks.clone();
					
					State newState = new State(newCity, newCarriedTasks, newRemainingTasks);
					
					double newCost = currCost + currCity.distanceTo(newCity);
					
					ArrayList<Action> newActions = new ArrayList<Action>(currActions);
					for (City city : currCity.pathTo(newCity)) {
						newActions.add(new Action.Move(city));
					}
					newActions.add(new Action.Delivery(carriedTask));
					
					// This state is not worth exploring if we can already reach a goal state with lower cost.
					if (newCost < bestCost) {
						SCA newSCA = new SCA(newState, newCost, newActions);
						q.add(newSCA);
					}
				}
				
				// Pickup successors
				for (Task remainingTask : remainingTasks) {
					// Do we have enough capacity to pickup the task?
					if (currCapacity >= remainingTask.weight) {
						City newCity = remainingTask.pickupCity;
						
						TaskSet newCarriedTasks = carriedTasks.clone();
						newCarriedTasks.add(remainingTask);
						
						TaskSet newRemainingTasks = remainingTasks.clone();
						newRemainingTasks.remove(remainingTask);
						
						State newState = new State(newCity, newCarriedTasks, newRemainingTasks);
						
						double newCost = currCost + currCity.distanceTo(newCity);
						
						ArrayList<Action> newActions = new ArrayList<Action>(currActions);
						for (City city : currCity.pathTo(newCity)) {
							newActions.add(new Action.Move(city));
						}
						newActions.add(new Action.Pickup(remainingTask));
						
						// This state is not worth exploring if we can already reach a goal state with lower cost.
						if (newCost < bestCost) {
							SCA newSCA = new SCA(newState, newCost, newActions);
							q.add(newSCA);
						}
					}	
				}
			}
		}
		
		System.out.println(bestCost);
		System.out.println(bestActions);
		return new Plan(initCity, bestActions);
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {

		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}

class SCAComparator implements Comparator<SCA> {
	// Only used in A* to keep the priority queue sorted according to real cost + heuristic cost
	
	int costPerKm;
	
	public SCAComparator(int costPerKm) {
		this.costPerKm = costPerKm;
	}

	@Override
	public int compare(SCA sca1, SCA sca2) {
		return Double.compare(sca1.cost + h(sca1.state, costPerKm), sca2.cost + h(sca2.state, costPerKm));
	}
	
	// To get from state s to a goal state, the agent will at least have to deliver all tasks carried at s 
	// and pickup and deliver all tasks remaining at state s. We know that this will at least include the cost 
	// of delivering the furthest carried task or the cost of picking up and delivering the furthest remaining 
	// task. This will never overestimate the true cost.
	private double h(State s, int costPerKm) {
		City currCity = s.vehicleCity;
		TaskSet carriedTasks = s.carriedTasks;
		TaskSet remainingTasks = s.remainingTasks;
		
		double largestDistance = 0.0;
	
		for (Task carriedTask : carriedTasks) {
			double distToDeliver = currCity.distanceTo(carriedTask.deliveryCity);
			largestDistance = Math.max(largestDistance, distToDeliver);
		}
		
		for (Task remainingTask : remainingTasks) {
			double distToDeliver = currCity.distanceTo(remainingTask.pickupCity) + remainingTask.pickupCity.distanceTo(remainingTask.deliveryCity);
			largestDistance = Math.max(largestDistance, distToDeliver);
		}
		
		return largestDistance * costPerKm;
	}
}
