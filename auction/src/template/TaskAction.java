package template;

import logist.task.Task;

public class TaskAction
{
	public Task task;
	public boolean isPickup;
	
	public TaskAction(Task task, boolean isPickup)
	{
		this.task = task;
		this.isPickup = isPickup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isPickup ? 1231 : 1237);
		result = prime * result + ((task == null) ? 0 : task.hashCode());
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
		TaskAction other = (TaskAction) obj;
		if (isPickup != other.isPickup)
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "" + (isPickup ? "Pickup" : "Deliver") + task;
	}
}
