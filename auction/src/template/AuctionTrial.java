package template;

//the list of imports
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionTrial implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private List<Vehicle> vehicles;
	
	private long timeout_setup;
    private long timeout_plan;
    private long timeout_bid;
	
	private List<Task> tasks;
	private Double cost;
	private Double newCost;
	private Double marginal;
	private Double profitRatio;
	private Double oppRatio;
	private Double finalBid;
	
	private Long income;
	
	private SLS sls;
	private Assignment solution;
	private Assignment init;
	private Assignment oldSolution;
	private Double auctionAmount;

	private HashMap<City, Double> popularityVec;
	private HashMap<HashMap<City,City>, Double> popularityMat;
	private List<Long> minBids;
	
	private final int MIN_BID = 100;
	private HashMap<Integer, Double> historyBid;

	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicles = agent.vehicles();
		
		this.cost = 0.0;
		this.profitRatio = 1.1;
		this.auctionAmount = 0.0;
		this.popularityVec = new HashMap<City,Double>();
		this.popularityMat = new HashMap<HashMap<City,City>,Double>();
		this.historyBid = new HashMap<Integer,Double>();
		this.minBids = new ArrayList<Long>();
		this.tasks = new ArrayList<Task>();
		this.sls = new SLS(this.vehicles, this.tasks);
		
		// this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        System.out.println("This is agent " + agent.id());
        
		// the setup method cannot last more than timeout_setup milliseconds
        this.timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        this.timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        // the bid method cannot execute more than timeout_bid milliseconds
        this.timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);
        
        this.income = 0L;
        
        //TODO: Find popularity distribution for each city
        for (City a : topology) 
        {
        	for (City b : topology) 
        	{
				for(City pt : a.pathTo(b))
				{
					if(this.popularityVec.containsKey(pt))
					{
						this.popularityVec.put(pt, this.popularityVec.get(pt)+this.distribution.probability(a, b));
					}
					else
					{
						this.popularityVec.put(pt, this.distribution.probability(a, b));
					}
					
				}
        	}
        }  
        
        //TODO: Sum the popularity vector terms corresponding to the shortest path cities for each task
        double maxVal = Double.MIN_VALUE;
        double minVal = Double.MAX_VALUE;
        for (City a : topology) 
        {
        	for (City b : topology) 
        	{
        		HashMap<City,City> tmp = new HashMap<City,City>();
        		tmp.put(a, b);
        		this.popularityMat.put(tmp, 0.0);
        		for(City pt : a.pathTo(b))
				{
					this.popularityMat.put(tmp, this.popularityMat.get(tmp)+this.popularityVec.get(pt));
				}
        		if(this.popularityMat.get(tmp) > maxVal)
        		{
        			maxVal = this.popularityMat.get(tmp);
        		}
        		if(this.popularityMat.get(tmp) < minVal)
        		{
        			minVal = this.popularityMat.get(tmp);
        		}
        		
        	}
        }
        //TODO: Normalize popularity matrix
        for (City a : topology) 
        {
        	for (City b : topology) 
        	{
        		HashMap<City,City> tmp = new HashMap<City,City>();
        		tmp.put(a, b);
        		this.popularityMat.put(tmp, (this.popularityMat.get(tmp) - minVal) / (maxVal - minVal));
        	}	
        }
        
	}

	
	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		System.out.println("Bids for previous task:");
		for (int i = 0; i < bids.length; i++) {
			System.out.println("Agent " + i + ": " + bids[i]);
		}
		System.out.println("Task " + previous + " went to " + winner);
		
		if (winner == agent.id()) 
		{
			this.cost = this.newCost;
			this.income += bids[agent.id()];
		}
		else 
		{
			this.solution = this.oldSolution;
			this.tasks.remove(tasks.size()-1);
		}
		
		minBids.add(minBid(bids));
	}
	
	
	@Override
	public Long askPrice(Task task) {
		
		System.out.println("\nAuctioning task: " + task);
		
		this.auctionAmount += 1;
		
		HashMap<City,City> tmp = new HashMap<City,City>();
		
		double expected = 0.0;
		int count = 0;
		
		for(City a : topology)
		{
			tmp.put(a,task.pickupCity);
			if(this.popularityMat.containsKey(tmp))
			{
				expected += ((1-this.popularityMat.get(tmp))* a.distanceTo(task.pickupCity));
			}
			else
			{
				expected += a.distanceTo(task.pickupCity);
			}
			
			count += 1;
		}
		
		expected = (expected / count) + task.pickupCity.distanceTo(task.deliveryCity);
		
		tmp.clear();
		tmp.put(task.pickupCity,task.deliveryCity);
		this.tasks.add(task);
		
		int cnt = 0;
		for(Vehicle veh : this.vehicles)
		{		
			if (veh.capacity() < task.weight)
				cnt++;
		}
	
		if(cnt >= this.vehicles.size())
		{
			return null;
		}
			
		this.oldSolution = this.solution;
		
		
		if (this.solution != null) 
		{
			// Initialize SLS from previous solution
			this.init = this.sls.addTaskToSolution(this.solution, task);		
			this.solution = this.sls.SLSAlgorithm(this.init, System.currentTimeMillis(), this.timeout_bid);			
		}
		else 
		{	
			this.solution = this.sls.SLSAlgorithm(System.currentTimeMillis(), this.timeout_bid);
		}
		
		
		this.newCost = this.sls.objective(this.solution);
		this.marginal = this.newCost - this.cost;
		City prevCity = this.sls.prevCity(task);
		Double ddist = prevCity.distanceTo(task.pickupCity) + task.pickupCity.distanceTo(task.deliveryCity);
	
		if(this.auctionAmount > 1)	
		{
			Double oppRatio;
			if(this.tasks.size()/this.auctionAmount < 0.5)
			{
				oppRatio = (minBids.get(minBids.size()-1) / expected)*0.9;
			}
			else
			{
				oppRatio = (minBids.get(minBids.size()-1) / expected)*1.1;
			}
			this.profitRatio = Math.max(1.01, oppRatio*ddist/this.marginal);
		}
		
		this.finalBid = this.marginal * this.profitRatio;
		
		System.out.println("Tasks: " + this.tasks);
		

		return (long) Math.max(MIN_BID, this.finalBid);
	}
	
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		System.out.println("My tasks: " + this.tasks);
		System.out.println("Total income: " + income);
		
		if (this.solution == null) { 
			List<Plan> plans = new ArrayList<Plan>();
			while (plans.size() < vehicles.size())
				plans.add(Plan.EMPTY);

			return plans;
		}
		
		this.solution = this.sls.SLSAlgorithm(this.solution, System.currentTimeMillis(), this.timeout_plan);
		
		// Replace tasks with the ones given as argument so Logist is happy
		replaceTasks(tasks);
		
		double finalCost = this.sls.objective(this.solution);
		System.out.println("Total cost: " + finalCost);
		System.out.println("Profit: " + (this.income - finalCost));
		
		return convertSolutionToPlans(this.vehicles, this.solution);
	}
	
	public Long minBid(Long [] bids)
	{
		Long res = Long.MAX_VALUE;
		for(Long ll : bids)
		{
			if(res > ll)
			{
				res = ll;
			}
		}
		return res;
	}
	
	public void replaceTasks(TaskSet tasks) {
		HashMap<Integer, Task> idToTask = new HashMap<Integer, Task>();
		for (Task task : tasks) {
			idToTask.put(task.id, task);
		}
		
		for(Vehicle v : vehicles)
		{
			for(TaskAction ta :  this.solution.getTaskActions(v))
			{
				Task correctTask = idToTask.get(ta.task.id);
				ta.task = correctTask;
			}
		}	
	}
	
	
	// Generate proper plan from the pickup and delivery actions
    public List<Plan> convertSolutionToPlans(List<Vehicle> vehicles, Assignment solution) {
    	ArrayList<Plan> plans = new ArrayList<Plan>();
    	
    	for (Vehicle vehicle : vehicles) {
    		City current = vehicle.getCurrentCity();
    		Plan plan = new Plan(current);
    		
    		for (TaskAction taskAction : solution.getTaskActions(vehicle)) {
    			Task task = taskAction.task;
    			boolean isPickup = taskAction.isPickup;
    			
    			if (isPickup) {
    				// move => pickup location
    	            for (City city : current.pathTo(task.pickupCity)) {
    	                plan.appendMove(city);
    	            }
    	            plan.appendPickup(task);
    	            current = task.pickupCity;
    			}
    			else {
    				// move => delivery location
    	            for (City city : current.pathTo(task.deliveryCity)) {
    	                plan.appendMove(city);
    	            }
    	            plan.appendDelivery(task);
    	            current = task.deliveryCity;
    			}
    		}
    		
    		plans.add(plan);
    	}
    	
    	return plans;
    }
}
