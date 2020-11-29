package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class SLS 
{
	//To change iteration amount or probability: Change iter and pr
	private List<Vehicle> vehicles;
	private List<Task> taskList;
	private static Assignment ABest;
	private static double costBest = Double.MAX_VALUE;
	private double pr = 0.4;
	private int iter = 10000;
	private Random rand;

	public SLS(List<Vehicle> vehicles, List<Task> taskList) 
	{
		this.vehicles = vehicles;
		this.taskList = taskList;
		this.rand = new Random();
		//Line below for tests
		//this.rand = new Random(0);
	}
	
	public SLS(List<Vehicle> vehicles, TaskSet taskSet) 
	{
		this.vehicles = vehicles;
		this.rand = new Random();
		
		this.taskList = new ArrayList<Task>();
		for(Task t : taskSet)
		{
			this.taskList.add(t);
		}
		//Line below for tests
		//this.rand = new Random(0);
	}
	
	public Assignment addTaskToSolution(Assignment A, Task t)
	{
		Assignment newA = A.clone();
		for(Vehicle veh : this.vehicles)
		{
			if(veh.capacity() >= t.weight)
			{
				List<TaskAction> lsta = new ArrayList<TaskAction>(A.getTaskActions(veh));
				lsta.add(new TaskAction(t,true));
				lsta.add(new TaskAction(t,false));
				newA.updateTaskActions(veh, lsta);
				break;
			}
		}
		return newA;
	}
	public City prevCity(Task t)
	{
		return this.ABest.findCity(t);
	}
	
	public Assignment SLSAlgorithm(Assignment init, long timeStart, long timeout)
	{
		ABest = init.clone();
		costBest = objective(init);
		
		System.out.println("Calling SLS with " + this.taskList);
		List<Assignment> N;
		Assignment A = init.clone();
		Assignment AOld;
		for(int i = 0; i < iter && System.currentTimeMillis() - timeStart <= timeout - 1000; i++)
		{
			AOld = A.clone();
			N = chooseNeighbours(AOld);
			A = localChoice(N, AOld, pr);
		}
		return ABest;
	}
	
	public Assignment SLSAlgorithm(long timeStart, long timeout)
	{
		System.out.println("Calling SLS with " + this.taskList);
		List<Assignment> N;
		Assignment A = selectInitialSolutionMaxCap(timeStart, timeout);
		Assignment AOld;
		for(int i = 0; i < iter && System.currentTimeMillis() - timeStart <= timeout - 1000; i++)
		{
			AOld = A.clone();
			N = chooseNeighbours(AOld);
			A = localChoice(N, AOld, pr);
		}
		return ABest;
	}
	
	public Assignment getBestAssignment()
	{
		return ABest;
	}
	
	public Assignment selectInitialSolutionMaxCap(long timeStart,long timeout)
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
		for(Task t : taskList)
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
				System.out.println("ERROR: there is a task that no vehicle can carry!");
				return null;
			}
		}	
		Assignment initA = new Assignment(vla);
		if(System.currentTimeMillis() - timeStart <= timeout - 1000)
		{
			List<Assignment> Alist = changingTaskOrder(initA, maxVehicle);
			Assignment A = localChoice(Alist, initA, pr);	
			ABest = A.clone();
			costBest = objective(ABest);
			return A;
		}
		else
		{
			ABest = initA.clone();
			costBest = objective(ABest);
			return initA;
		}
	}
	
	
	public Assignment localChoice(List<Assignment> neighbourhood, Assignment AOld, double pr)
	{	
		if (neighbourhood.isEmpty()) 
		{
			return AOld;
		}
		
		double minCost = Double.MAX_VALUE;
		List<Assignment> minA = new ArrayList<Assignment>();
		
		HashMap<Assignment, Double> costs = new HashMap<Assignment, Double>();
				
		for(Assignment A : neighbourhood)
		{
			double costA = objective(A);
			if(costA < minCost)
			{
				minCost = costA;
			}
			costs.put(A, costA);
		}
		
		// Find all the assignments with minimum cost
		for(Assignment A : neighbourhood)
		{
			double costA = costs.get(A);
			if(costA == minCost)
			{
				minA.add(A);
			}
		}
		Assignment Anew = minA.get(rand.nextInt(minA.size()));
		double p = rand.nextDouble();
		
		//To save the overall best solution
		if(costBest > minCost)
		{
			ABest = Anew.clone();
			costBest = minCost;
		}
		
		
		if(p <= pr)
		{
			return Anew;
		}	
		return AOld;
	}
	

	
	public List<Assignment> chooseNeighbours(Assignment A)
	{
		List<Vehicle> vehicleList = A.getVehiclesWithTask();
		List<Assignment> N = new ArrayList<Assignment>();
		
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
				for(int j = 0; j < A.getTaskActions(veh1).size(); j++ )
				{
					if(A.getTaskActions(veh1).get(j).isPickup)
					{
						int taskWeight = A.getTaskActions(veh1).get(j).task.weight;
						
						if (veh2.capacity() >= taskWeight)
						{
							List<TaskAction> veh1ta = new ArrayList<TaskAction>(A.getTaskActions(veh1));
							List<TaskAction> veh2ta = new ArrayList<TaskAction>(A.getTaskActions(veh2));
							
							TaskAction ta1Pickup = veh1ta.remove(j);
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
				}
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
				if(i != j)
				{
					// Check if pickup comes before delivery, otherwise return false
					if(lta.get(i).isPickup && !lta.get(j).isPickup && lta.get(i).task.id == lta.get(j).task.id && i > j)
					{
						return false;
					}
				}
			}
			// Check if carried weight exceeds vehicle capacity at any point
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
				double pd = currentCity.distanceTo(task.pickupCity);
				double dd = currentCity.distanceTo(task.deliveryCity);
				
				if (isPickup) 
				{
					dist += pd;
					currentCity = task.pickupCity;
				}
				else
				{
					dist += dd;
					currentCity = task.deliveryCity;
				}
			}
			
			C += dist*costPerKm;
		}
		
		return C;
	}
	
}
