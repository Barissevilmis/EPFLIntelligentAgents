package template;

import java.util.HashMap;
import java.util.List;

import logist.plan.Action;
import logist.simulation.Vehicle;

public class Assignment 
{
	private HashMap<Vehicle, List<Action>> vehicleActionList;
	
	//ADD NEW ACTIONS TO VEHICLES INSTEAD OF STATE LIST
	public Assignment(HashMap<Vehicle, List<Action>> vehicleActionList)
	{
		this.vehicleActionList = vehicleActionList;
		
	}
	
}
