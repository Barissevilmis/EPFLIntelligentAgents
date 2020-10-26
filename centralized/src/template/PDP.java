package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class PDP 
{

	private List<Vehicle> vehicles;
	private TaskSet tasks;
	private static Assignment ABest;
	private static double costBest = Double.MAX_VALUE;

	public PDP(List<Vehicle> vehicles, TaskSet tasks) 
	{
		this.vehicles = vehicles;
		this.tasks = tasks;
	}
	
	public Assignment SLSAlgorithm()
	{
		List<Assignment> N;
		Assignment A = selectInitialSolutionMaxCap();
		Assignment AOld;
		for(int i = 0; i < 10000; i++)
		{
			AOld = A.clone();
			N = chooseNeighbours(AOld);
			A = localChoice(N, AOld, 0.4);
		}
		return A;
	}
	
	public Assignment getBestAssignment()
	{
		return ABest;
	}
	
	public Assignment selectInitialSolutionMaxCap()
	{
		Vehicle maxVehicle = vehicles.get(0);
		int maxW = maxVehicle.capacity();
		HashMap<Vehicle, List<TaskAction>> vla = new HashMap<Vehicle, List<TaskAction>>();
		for(Vehicle v : vehicles)
		{
			vla.put(v, new ArrayList<TaskAction>());
			if(v.capacity() > maxW)
			{	
				maxW = v.capacity();
				maxVehicle = v;
			}
		}
		for(Task t : tasks)
		{
			if(maxW >= t.weight)
			{
				TaskAction ta1 = new TaskAction(t, true);
				TaskAction ta2 = new TaskAction(t, false);
				vla.get(maxVehicle).add(ta1);
				vla.get(maxVehicle).add(ta2);
			}
			else
			{
				return null;
			}
		}	
		Assignment A = new Assignment(vla);		
		ABest = new Assignment(vla);
		return A;
	}
	
	public Assignment selectInitialSolutionRandom()
	{
		HashMap<Vehicle, List<TaskAction>> vla = new HashMap<Vehicle, List<TaskAction>>();
		Random rand = new Random();
		
		for(Vehicle v : vehicles)
		{
			vla.put(v, new ArrayList<TaskAction>());
		}

		for(Task t : tasks)
		{
			Vehicle randVeh = vehicles.get(rand.nextInt(vehicles.size()));
			if(randVeh.capacity() >= t.weight)
			{
				TaskAction ta1 = new TaskAction(t, true);
				TaskAction ta2 = new TaskAction(t, false);
				vla.get(randVeh).add(ta1);
				vla.get(randVeh).add(ta2);
			}
			else
			{
				return null;
			}
		}	
		Assignment A = new Assignment(vla);		
		ABest = new Assignment(vla);
		return A;
	}
	
	public Assignment localChoice(List<Assignment> neighbourhood, Assignment AOld, double pr)
	{	
		double minCost = Double.MAX_VALUE;
		List<Assignment> minA = new ArrayList<Assignment>();
		Random rand = new Random();
		
		for(Assignment A : neighbourhood)
		{
			double costA = objective(A);
			if(costA < minCost)
			{
				minCost = costA;
			}
		}
		
		for(Assignment A : neighbourhood)
		{
			double costA = objective(A);
			if(costA == minCost)
			{
				minA.add(A);
			}
		}
		Assignment Anew = minA.get(rand.nextInt(minA.size()));
		double p = rand.nextDouble();
		
		//TO SAVE THE OVERALL BEST SOLUTION
		if(costBest > minCost)
		{
			ABest = Anew;
			costBest = minCost;
		}
		
		
		if(p <= pr)
		{
			System.out.println("Returning Anew with cost " + minCost);
			return Anew;
		}	
		System.out.println("Returning Aold ");
		return AOld;
	}
	

	
	public List<Assignment> chooseNeighbours(Assignment A)
	{
		List<Vehicle> vehicleList = A.getVehiclesWithTask();
		List<Assignment> N = new ArrayList<Assignment>();
		Random rand = new Random();
		
		Vehicle randomVehicle = vehicleList.get(rand.nextInt(vehicleList.size()));
		

		N.addAll(changingVehicle(A, randomVehicle));

		N.addAll(changingTaskOrder(A, randomVehicle));
			
		return N;
	}
	
	public List<Assignment> changingVehicle(Assignment A, Vehicle veh1)
	{
		List<Assignment> neighbours = new ArrayList<Assignment>();
		
		for(Vehicle veh2: A.getVehicles())
		{
			if(!veh2.equals(veh1))
			{
		
				List<TaskAction> veh1ta = new ArrayList<TaskAction>(A.getTaskActions(veh1));
				List<TaskAction> veh2ta = new ArrayList<TaskAction>(A.getTaskActions(veh2));
								
				TaskAction ta1Pickup = veh1ta.remove(0);
				TaskAction ta1Deliver = null;
								
				for(int i = 0; i < veh1ta.size(); i++)
				{
					if(veh1ta.get(i).task.id == ta1Pickup.task.id)
					{
						ta1Deliver = veh1ta.remove(i);
						break;
					}
				}
				
				// Add task to the end of vehicle 2
				veh2ta.add(ta1Pickup);
				veh2ta.add(ta1Deliver);
				
				Assignment neighbour = A.clone();
				
				neighbour.updateTaskActions(veh1, veh1ta);
				neighbour.updateTaskActions(veh2, veh2ta);
				
				neighbours.add(neighbour);
			}
		}
		
		return neighbours;
	}
	
	public List<Assignment> changingTaskOrder(Assignment A, Vehicle vehicle)
	{
		List<TaskAction> taskActions = A.getTaskActions(vehicle);
		List<Assignment> neighbours = new ArrayList<Assignment>();

		if(taskActions.size() >= 4)
		{			
			// Try to change the place of each task action
			for (int i = 0; i < taskActions.size(); i++) 
			{
				TaskAction taskAction = taskActions.get(i);
				
				for (int j = 0; j < taskActions.size(); j++) 
				{
					if (i != j) 
					{
						List<TaskAction> candidateTaskActions = new ArrayList<TaskAction>(taskActions);
						candidateTaskActions.remove(i);
						candidateTaskActions.add(j, taskAction);
						
						// Check if it's a valid neighbor
						if (checkConstraints(vehicle, candidateTaskActions)) 
						{
							Assignment neighbour = A.clone();
							neighbour.updateTaskActions(vehicle, candidateTaskActions);
							neighbours.add(neighbour);
						}
					}
				}
			}	
		}
		
		return neighbours;	
	}
	
	public boolean checkConstraints(Vehicle v, List<TaskAction> lta)
	{
		double w = 0.0;
		for(int i = 0; i < lta.size(); i++)
		{
			if(lta.get(i).isPickup)
			{
				w += lta.get(i).task.weight;
			}
			else
			{
				w -= lta.get(i).task.weight;
			}
			
			for(int j = 0; j < lta.size(); j++)
			{
				if(lta.get(i) != lta.get(j))
				{
					if(lta.get(i).isPickup && !lta.get(j).isPickup && lta.get(i).task.id == lta.get(j).task.id && i > j)
					{
						return false;
					}
				}
			}
			if(w > v.capacity())
			{
				return false;
			}
		}
		return true;
	}
	
	public Double objective(Assignment A)
	{
		double C = 0.0;
		
		for (Vehicle vehicle : A.getVehicles()) 
		{
			double dist = 0.0;
			double costPerKm = vehicle.costPerKm();
			City currentCity = vehicle.homeCity();
			for(TaskAction ta : A.getTaskActions(vehicle)) 
			{
				Task task = ta.task;
				boolean isPickup = ta.isPickup;
				
				if (isPickup) 
				{
					dist += currentCity.distanceTo(task.pickupCity);
					currentCity = task.pickupCity;
				}
				else
				{
					dist += currentCity.distanceTo(task.deliveryCity);
					currentCity = task.deliveryCity;
				}
			}
			
			C += dist*costPerKm;
		}
		
		return C;
	}
	
}
