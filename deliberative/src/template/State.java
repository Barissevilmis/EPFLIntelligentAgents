package template;

import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State 
{
	public City vehicleCity;
	public TaskSet carriedTasks;
	public TaskSet remainingTasks;

	public State(City vehicleCity, TaskSet carriedTasks, TaskSet remainingTasks) {
		this.vehicleCity = vehicleCity;
		this.carriedTasks = carriedTasks;
		this.remainingTasks = remainingTasks;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((carriedTasks == null) ? 0 : carriedTasks.hashCode());
		result = prime * result + ((remainingTasks == null) ? 0 : remainingTasks.hashCode());
		result = prime * result + ((vehicleCity == null) ? 0 : vehicleCity.hashCode());
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
		State other = (State) obj;
		if (carriedTasks == null) {
			if (other.carriedTasks != null)
				return false;
		} else if (!carriedTasks.equals(other.carriedTasks))
			return false;
		if (remainingTasks == null) {
			if (other.remainingTasks != null)
				return false;
		} else if (!remainingTasks.equals(other.remainingTasks))
			return false;
		if (vehicleCity == null) {
			if (other.vehicleCity != null)
				return false;
		} else if (!vehicleCity.equals(other.vehicleCity))
			return false;
		return true;
	}
}
