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
public class Auction implements AuctionBehavior {

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
	
	private Long income;
	
	private SLS sls;
	private Assignment solution;
	private Assignment init;
	
	private final int MIN_BID = 100;

	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicles = agent.vehicles();

		this.cost = 0.0;
		this.profitRatio = 1.1;
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
        
        income = 0L;
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
			income += bids[agent.id()];
		}
		else 
		{
			this.tasks.remove(tasks.size()-1);
		}
	}
	
	
	@Override
	public Long askPrice(Task task) {
		System.out.println("\nAuctioning task: " + task);
		
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

		this.solution = this.sls.SLSAlgorithm(System.currentTimeMillis(), timeout_bid);
		this.newCost = this.sls.objective(this.solution);
		this.marginal = this.newCost - this.cost;
		
		System.out.println("Tasks: " + this.tasks + "\n");
		
		return (long) Math.max(MIN_BID, this.marginal  * this.profitRatio);
	}
	

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		System.out.println("My tasks: " + this.tasks);
		System.out.println("Total income: " + income);
		
		this.sls = new SLS(this.vehicles, tasks);
		this.solution = this.sls.SLSAlgorithm(System.currentTimeMillis(), this.timeout_plan);
		
		double finalCost = this.sls.objective(this.solution);
		System.out.println("Total cost: " + finalCost);
		System.out.println("Profit: " + (income - finalCost));
		
		return convertSolutionToPlans(this.vehicles, this.solution);
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
