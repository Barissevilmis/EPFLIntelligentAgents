package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import logist.simulation.Vehicle;

public class Assignment 
{
	public HashMap<Vehicle, List<TaskAction>> vehicleActionList;
	
	//ADD NEW ACTIONS TO VEHICLES INSTEAD OF STATE LIST
	public Assignment(HashMap<Vehicle, List<TaskAction>> vehicleActionList)
	{
		this.vehicleActionList = vehicleActionList;
		
	}	
	
	public Set<Vehicle> getVehicles()
	{
		return vehicleActionList.keySet();
	}
	
	public List<TaskAction> getTaskActions(Vehicle v)
	{
		if(!vehicleActionList.isEmpty())
		{
			return vehicleActionList.get(v);
		}
		
		return null;
		
	}
	
	public List<Vehicle> getVehiclesWithTask() 
	{
		List<Vehicle> veh = new ArrayList<Vehicle>();
		for(Vehicle v : getVehicles())
		{
			if(!getTaskActions(v).isEmpty())
			{
				veh.add(v);
			}
		}
		return veh;
	}
	
	public void updateTaskActions(Vehicle vehicle, List<TaskAction> newTaskActions) 
	{
		vehicleActionList.put(vehicle, newTaskActions);
	}
	
	public Assignment clone() {
		HashMap<Vehicle, List<TaskAction>> newVehicleActionList = new HashMap<Vehicle, List<TaskAction>>();
		for (Vehicle vehicle : getVehicles()) {
			List<TaskAction> newTaskActions = new ArrayList<TaskAction>(getTaskActions(vehicle));
			newVehicleActionList.put(vehicle, newTaskActions);
		}
		return new Assignment(newVehicleActionList);
	}
	
	@Override
	public String toString() {
		return vehicleActionList.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((vehicleActionList == null) ? 0 : vehicleActionList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Assignment other = (Assignment) obj;
		if (vehicleActionList == null) {
			if (other.vehicleActionList != null)
				return false;
		} else if (!vehicleActionList.equals(other.vehicleActionList))
			return false;
		return true;
	}

}
