package template;

import logist.topology.Topology.City;

public class State 
{
	public final City from;
	public final City to;

	public State(City from, City to) 
	{
		this.from = from;
		this.to = to;
	}
}
