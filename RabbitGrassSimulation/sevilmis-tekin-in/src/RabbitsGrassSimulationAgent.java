import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable 
{
	private static int IDStat = 0;
	
	private int x;
	private int y;
	private int dirX;
	private int dirY;
	private int energy;  
	private int IDInd;
	private int gridSize;
	
	//private static Image rabbit = Toolkit.getDefaultToolkit().createImage("rabbit.png");
	private static Image rabbit = Toolkit.getDefaultToolkit().createImage(RabbitsGrassSimulationAgent.class.getResource("/rabbit.png"));
	
	private RabbitsGrassSimulationSpace rgsSpace;
	
	public RabbitsGrassSimulationAgent(int initEnergy, int gridS)
	{
		//RANDOM LOCATION INIT.
		energy = initEnergy;
		gridSize = gridS;
		
		IDStat += 1;
		IDInd = IDStat;
	}

	public void draw(SimGraphics arg0) 
	{	
		//COLOR OF AGENTS TURN FROM RED TO GREEN IN CASE OF LOW ENERGY
		arg0.drawFastRect(Color.yellow);
		arg0.drawImageToFit(rabbit);
		
	}

	public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgs)
	{
		rgsSpace = rgs;
	}
	
	public void report()
	{
		System.out.println(getID() + " at (" + x + ", " + y + ") with energy of " + getRemainingEnergy());
	}
	
	public int getRemainingEnergy()
	{
		return energy;
	}

	
	public void alterEnergy()
	{
		//int attempt = 0;
		//int maxAttempt = 6;
		int dir = (int) (Math.random() * 4) + 1;
		
		if(dir % 4 == 0)
		{
			dirX = -1;
			dirY = 0;
		}
		else if(dir % 4 == 1)
		{
			dirX = 0;
			dirY = -1;
		}
		else if(dir % 4 == 2)
		{
			dirX = 1;
			dirY = 0;
		}
		else
		{
			dirX = 0;
			dirY = 1;
		}
		
		int xx = (x + dirX + gridSize) % gridSize;
		int yy = (y + dirY + gridSize) % gridSize;
		
		tryMove(xx,yy);
		energy += rgsSpace.eatGrass(x,y);
		energy -= 1;
	}
	
	public void decreaseEnergy(int amount)
	{
		energy -= amount;
	}
	
	private boolean tryMove(int xx, int yy)
	{
		return rgsSpace.moveRabbit(x, y, xx, yy);
	}
	
	public String getID()
	{
		return "Rabbit-" + IDInd;
	}


	public int getX() 
	{
		return x;
	}

	public int getY() 
	{
		return y;
	}
	
	
	public void setX(int newX)
	{
		x = newX;
	}
	
	public void setY(int newY)
	{
		y = newY;
	}
	

}
