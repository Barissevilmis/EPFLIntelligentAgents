package template;

import java.util.List;

import logist.task.Task;

public class X 
{

	public List<Task> nextTaskArr;
	public List<Integer> timeArr;
	public List<Task> vehicleArr;

	
	public X(List<Task> nextTaskArr, List<Integer> timeArr, List<Task> vehicleArr)
	{
		this.nextTaskArr = nextTaskArr;
		this.timeArr = timeArr;
		this.vehicleArr = vehicleArr;
		
	}
	
}
