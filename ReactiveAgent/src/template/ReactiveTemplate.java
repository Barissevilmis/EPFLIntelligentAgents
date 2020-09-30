package template;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ReactiveTemplate implements ReactiveBehavior 
{

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private List<City> cityList;
	private List<State> stateList;
	private List<Vehicle> vehicleList;
	private HashMap<State,HashMap<Integer,Double>> rewardTable;
	private HashMap<State,Double> transitionTable;
	private HashMap<State,Double> vVector;
	private HashMap<State,State> bestVector;
	private HashMap<State,Double> qVector;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) 
	{

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		this.cityList = topology.cities();	
		this.vehicleList = agent.vehicles();
		this.vVector = new HashMap<State,Double>();
		this.bestVector = new HashMap<State,State>();
		this.qVector = new HashMap<State,Double>();
		this.stateList = new ArrayList<State>();
		this.transitionTable = new HashMap<State,Double>();
		this.rewardTable = new HashMap<State,HashMap<Integer, Double>>();
		
		HashMap<Integer,Double> tmp = new HashMap<Integer,Double>();
		
		int currReward = 0;
		double dist = 0;
		
		//TODO: FIX FOR NULL STATE
		for(City from : cityList)
		{	
			for(City to : cityList)
			{
				currReward = td.reward(from, to);
				dist = -from.distanceTo(to);
				State st;


				for(Vehicle veh : vehicleList)
				{
					if(!from.equals(to))
					{
						tmp.put(veh.id(),currReward - dist*veh.costPerKm());
					}
					else
					{
						tmp.put(veh.id(),0.0);
					}
				}
				if(!from.equals(to))
				{
					st = new State(from, to);
				}
				else
				{
					st = new State(from, null);
				}
				
				this.rewardTable.put(st, deepCopy(tmp));
				this.transitionTable.put(st, td.probability(from, to));
				this.stateList.add(st);
				this.vVector.put(st, 0.0);
			
				tmp.clear();
			}
		}
	
		//TODO: ADD BESTVEC AND IMPROVE STATES EXIT
		Double qVal = 0.0;
		Integer x = 0;
		while(x<100)
		{
			for(Vehicle veh : vehicleList)
			{
				for(State fromSt : stateList)
				{	
					qVal = this.rewardTable.get(fromSt).get(veh.id()) ;
					
					for(State toSt : stateList)
					{
						if(toSt.from.equals(fromSt.to))
						{
							qVal += discount * (this.transitionTable.get(toSt) * this.vVector.get(toSt));
						}
					}			
					if(this.qVector.containsKey(fromSt))
					{
						this.qVector.replace(fromSt, qVal);
					}
					else
					{
						this.qVector.put(fromSt, qVal);
					}
					State maxAction = Collections.max(qVector.entrySet(), HashMap.Entry.comparingByValue()).getKey();
					bestVector.replace(fromSt, maxAction);
					vVector.replace(fromSt, qVal);
				}
			}
			x += 1;
		}
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) 
	{
		Action action;
		State st = new State(vehicle.getCurrentCity(), availableTask == null ? null : availableTask.deliveryCity);

		City taskTo = this.bestVector.get(st).from;

		// If the destination and the task's destination match, take the task
		if (taskTo.equals(st.to)) 
		{
			action =  new Pickup(availableTask);
		} 
		else 
		{
			action =  new Move(taskTo);
		}
	
		if (numActions >= 1) 
		{
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
	
	public static HashMap<Integer, Double> deepCopy(
		    HashMap<Integer, Double> original)
		{
		    HashMap<Integer, Double> deepCopy = new HashMap<Integer, Double>();
		    for (Map.Entry<Integer, Double> entry : original.entrySet())
		    {
		        deepCopy.put(entry.getKey(),
		           entry.getValue());
		    }
		    return deepCopy;
		}
}
