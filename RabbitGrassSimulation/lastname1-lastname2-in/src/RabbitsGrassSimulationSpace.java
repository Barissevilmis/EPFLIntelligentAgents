import uchicago.src.sim.space.Object2DGrid;
/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace 
{
	private Object2DGrid grassSpace;
	private Object2DGrid rabbitSpace;
	
	public RabbitsGrassSimulationSpace(int gridSize)
	{
	    grassSpace = new Object2DGrid(gridSize, gridSize);
	    rabbitSpace = new Object2DGrid(gridSize, gridSize);
	    
	    for(int i = 0; i < gridSize; i++)
	    {
	      for(int j = 0; j < gridSize; j++)
	      {
	        grassSpace.putObjectAt(i,j,new Integer(0));
	      }
	    }
	  }
	
	 public void grassGrowth(int grow)
	 {
		 // Randomly place money in moneySpace
		 for(int i = 0; i < grow; i++)
		 {

			 int x = (int)(Math.random()*(grassSpace.getSizeX()));
			 int y = (int)(Math.random()*(grassSpace.getSizeY()));
			 
			 //Increase
		     int currVal = getGrass(x,y);
		     grassSpace.putObjectAt(x,y,new Integer(currVal + 1));

		 }
	
	 }
	 public int getGrass(int xx, int yy)
	 {
		 int currVal = 0;
		 if(grassSpace.getObjectAt(xx,yy)!= null)
	     {
			 currVal = ((Integer)grassSpace.getObjectAt(xx,yy)).intValue();
	    	 
	     }
		 return currVal;

	 }
	 
	 public int getTotalGrass()
	 {
		 int totalVal = 0;
		 for(int i = 0; i < grassSpace.getSizeX(); i++)
		 {
			for(int j = 0; j < grassSpace.getSizeY(); j++)
			{
				 if(grassSpace.getObjectAt(i,j)!= null)
			     {
					 totalVal += getGrass(i,j);
			    	 
			     }
			}
		 }
		 return totalVal;
	 }
	 
	 public Object2DGrid getCurrentGrassSpace()
	 {
		 return grassSpace;
	 }
	 
	  public Object2DGrid getCurrentRabbitSpace()
	  {
		    return rabbitSpace;
		  }

	 public boolean isBlockFull(int x, int y)
	 {	 
		 if(rabbitSpace.getObjectAt(x, y) == null)
		 {
			 return false;
		 }	 
		 else
		 {
			 return true;
		 }
		 
	 }
	 
	 public int eatGrass(int xx, int yy)
	 {
		 int grass = getGrass(xx, yy);
		 grassSpace.putObjectAt(xx, yy, new Integer(0));
		 return grass;
	 }
	 
	 public boolean addRabbit(RabbitsGrassSimulationAgent rbt, int passOverGridLimit)
	 {
		 int countLimit = passOverGridLimit * rabbitSpace.getSizeX() * rabbitSpace.getSizeY();
		 
		 for(int count = 0; count < countLimit; count++)
		 {
			 int xx = (int)(Math.random()*(rabbitSpace.getSizeX()));
		     int yy = (int)(Math.random()*(rabbitSpace.getSizeY()));
		     	     
		     if(isBlockFull(xx,yy) == false)
		     {
		        rabbitSpace.putObjectAt(xx,yy,rbt);
		        rbt.setX(xx);
		        rbt.setY(yy);
		        rbt.setRabbitsGrassSimulationSpace(this);
		        return true;
		      }
		     
		 }
		 
		 return false;
	 }
	 public void removeRabbit(int x, int y)
	 {
		 rabbitSpace.putObjectAt(x, y, null);
	 }
	 
	 public boolean moveRabbit(int x, int y, int xx, int yy)
	 {
		 boolean retVal = false;
		 if(isBlockFull(xx, yy) == false)
		 {
			 RabbitsGrassSimulationAgent rbt = (RabbitsGrassSimulationAgent)rabbitSpace.getObjectAt(x, y);
		     removeRabbit(x,y);
		     rbt.setX(xx);
		     rbt.setY(yy);
		     rabbitSpace.putObjectAt(xx, yy, rbt);
		     retVal = true;
		 }
		 return retVal;
	}
}
