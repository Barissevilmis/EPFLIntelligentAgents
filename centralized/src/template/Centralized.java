package template;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Action;
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
public class Centralized implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    private PDP pdp;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
        System.out.println(tasks);
        System.out.println();
        
        pdp = new PDP(vehicles, tasks);
        Assignment solution = pdp.SLSAlgorithm();
        System.out.println(solution);
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        return convertSolutionToPlans(vehicles, solution);
    }
    
    public List<Plan> convertSolutionToPlans(List<Vehicle> vehicles, Assignment solution) {
    	ArrayList<Plan> plans = new ArrayList<Plan>();
    	
    	for (Vehicle vehicle : vehicles) {
    		City current = vehicle.getCurrentCity();
    		Plan plan = new Plan(current);
    		
    		for (TaskAction taskAction : solution.getTaskActions(vehicle)) {
    			Task task = taskAction.task;
    			boolean isPickup = taskAction.isPickup;
    			
    			if (isPickup) {
    				// move: current city => pickup location
    	            for (City city : current.pathTo(task.pickupCity)) {
    	                plan.appendMove(city);
    	            }
    	            plan.appendPickup(task);
    	            current = task.pickupCity;
    			}
    			else {
    				// move: pickup location => delivery location
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
