package template;

<<<<<<< HEAD
public class State implements Comparable<State> 
{
	public int cityId;
	public int taskCityId;
	
	public State(int cityId, int taskCityId) 
	{
=======
public class State implements Comparable<State> {
	public int cityId;
	public int taskCityId;
	
	public State(int cityId, int taskCityId) {
>>>>>>> dogatest
		this.cityId = cityId;
		this.taskCityId = taskCityId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cityId;
		result = prime * result + taskCityId;
		return result;
	}

	@Override
<<<<<<< HEAD
	public boolean equals(Object obj) 
	{
		if (this == obj)
		{	
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		State other = (State) obj;
		if (cityId != other.cityId)
		{
			return false;
		}
		if (taskCityId != other.taskCityId)
		{
			return false;
		}
=======
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (cityId != other.cityId)
			return false;
		if (taskCityId != other.taskCityId)
			return false;
>>>>>>> dogatest
		return true;
	}
	
	@Override
<<<<<<< HEAD
	public String toString() 
	{
=======
	public String toString() {
>>>>>>> dogatest
		return "(" + cityId + "," + taskCityId + ")";
	}
	
	@Override
<<<<<<< HEAD
	public int compareTo(State other) 
	{
		if (cityId == other.cityId)
		{
			return taskCityId - other.taskCityId;
		}
		else
		{
			return cityId - other.cityId;
		}
=======
	public int compareTo(State other) {
		if (cityId == other.cityId)
			return taskCityId - other.taskCityId;
		else
			return cityId - other.cityId;
>>>>>>> dogatest
	}
}
