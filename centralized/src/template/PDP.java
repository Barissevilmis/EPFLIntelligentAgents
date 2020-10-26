package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import logist.plan.Action;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class PDP {

	private List<Vehicle> vehicles;
	private TaskSet tasks;
	
	private X x; 

	public PDP(List<Vehicle> vehicles, TaskSet tasks) 
	{
		this.vehicles = vehicles;
		this.tasks = tasks;
	}
	
	public Assignment SLSAlgorithm()
	{
		List<Assignment> N;
		Assignment A = SelectInitialSolution();
		Assignment A_old;
		for(int i = 0; i < 10000; i++)
		{
			A_old = A;
			N = ChooseNeigbours();
			A = LocalChoice();
		}
		return A;
	}
	
	public Assignment SelectInitialSolution()
	{
		Vehicle maxVehicle = vehicles.get(0);
		int maxW = maxVehicle.capacity();
		HashMap<Vehicle, List<Action>> vla = new HashMap<Vehicle, List<Action>>();
		List<Action> act = new ArrayList<Action>();
		for(Vehicle v : vehicles)
		{
			vla.put(v, null);
			if(v.capacity() > maxVehicle.capacity())
			{	
				maxW = v.capacity();
				maxVehicle = v;
			}
		}
		for(Task t : tasks)
		{
			act.add(new Action.Pickup(t));
			act.add(new Action.Delivery(t));
			maxW -= t.weight;
		}
		
		if(maxW < 0)
		{
			return null;
		}
		else
		{
			vla.replace(maxVehicle, act);
			Assignment A = new Assignment(vla);
			return A;
		}
	}
	
	public Assignment LocalChoice()
	{
		return null;
	}
	
	public List<Assignment> ChooseNeigbours()
	{
		return null;
	}
	
	public List<Assignment> ChangingVehicle()
	{
		return null;
	}
	
	public List<Assignment> ChangingTaskOrder()
	{
		return null;
	}
	
}
