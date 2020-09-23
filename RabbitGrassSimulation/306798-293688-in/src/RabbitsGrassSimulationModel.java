import java.awt.Color;
import java.util.ArrayList;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;


/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl 
{		
	
		private static final int GRIDSIZE = 20;
		private static final int NUMINITRABBITS = 20;
		private static final int NUMINITGRASS = 100;
		private static final int GRASSGROWTHRATE = 50;
		private static final int BIRTHTHRESHOLD = 30;
			
		private int gridSize = GRIDSIZE;
		private int numInitRabbits = NUMINITRABBITS;
		private int numInitGrass = NUMINITGRASS;
		private int grassGrowthRate = GRASSGROWTHRATE;
		private int birthThreshold = BIRTHTHRESHOLD;
		private int initialRabbitEnergy = 15;
		
		private RabbitsGrassSimulationSpace rgsSpace;
		
		private Schedule schedule;
		
		private DisplaySurface displaySurf;
		
		private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
		
		private OpenSequenceGraph amountOfGrassInSpace;

		class grassInSpace implements DataSource, Sequence 
		{

			public Object execute() 
			{
				return new Double(getSValue());
		    }

		    public double getSValue() 
		    {
		      return (double)rgsSpace.getTotalGrass();
		    }
		}
		
		class rabbitsInSpace implements DataSource, Sequence {
			public Object execute() {
				return new Double(getSValue());
			}
			
			public double getSValue() {
				return (double) 5*countLivingRabbits();
			}
		}

		public static void main(String[] args) 
		{
			
			System.out.println("Rabbit skeleton");

			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			// Do "not" modify the following lines of parsing arguments
			if (args.length == 0) // by default, you don't use parameter file nor batch mode 
				init.loadModel(model, "", false);
			else
				init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));
			
		}
		//MANDATORY 1
		public void begin() 
		{
			buildModel();
		    buildSchedule();
		    buildDisplay();
		    
		    displaySurf.display();
		    amountOfGrassInSpace.display();
		}
		
		//REQ 3
		public String[] getInitParam() 
		{
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			RangePropertyDescriptor rdGridSize = new RangePropertyDescriptor(
					"GridSize", 0, 100, 10);
			descriptors.put("GridSize", rdGridSize);
			
			RangePropertyDescriptor rdNumInitRabbits = new RangePropertyDescriptor(
					"NumInitRabbits", 0, 50, 10);
			descriptors.put("NumInitRabbits", rdNumInitRabbits);
			
			RangePropertyDescriptor rdNumInitGrass = new RangePropertyDescriptor(
					"NumInitGrass", 0, 200, 20);
			descriptors.put("NumInitGrass", rdNumInitGrass);
			
			RangePropertyDescriptor rdGrassGrowthRate = new RangePropertyDescriptor(
					"GrassGrowthRate", 0, 100, 10);
			descriptors.put("GrassGrowthRate", rdGrassGrowthRate);
			
			RangePropertyDescriptor rdBirthThreshold = new RangePropertyDescriptor(
					"BirthThreshold", 0, 100, 10);
			descriptors.put("BirthThreshold", rdBirthThreshold);
			
			
			String[] params = {"GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
			return params;
		}
		
		//MANDOTARY 2
		public String getName() 
		{
			return "GROUP 9(BARIS & DOGA) RABBIT GRASS SIMULATOR";
		}
		
		//REQ 1
		public Schedule getSchedule() 
		{
			return schedule;
		}
		//REQ 2
		public void setup() 
		{
			System.out.println("Running setup");
			
			rgsSpace = null;
			rabbitList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			
			if (displaySurf != null)
			{
				displaySurf.dispose();
			}
		    displaySurf = null;
		    
		    if (amountOfGrassInSpace != null)
		    {
		        amountOfGrassInSpace.dispose();
		    }
		    amountOfGrassInSpace = null;

		    displaySurf = new DisplaySurface(this, "Rabbit Grass Simulation Model Window 1");
		    amountOfGrassInSpace = new OpenSequenceGraph("Rabbit and Grass Populations",this);
	
			registerDisplaySurface("Rabbit Grass Simulation Model Window 1", displaySurf);
			this.registerMediaProducer("Plot", amountOfGrassInSpace);

		}
		
		public void buildModel()
		{
		    System.out.println("Running BuildModel");
		    rgsSpace = new RabbitsGrassSimulationSpace(gridSize);
		    rgsSpace.grassGrowth(numInitGrass);
		    
		    for(int i = 0; i < numInitRabbits; i++)
		    {
		        addNewRabbit();
		    }
		    for(int i = 0; i < rabbitList.size(); i++)
		    {
		    	RabbitsGrassSimulationAgent currRbt = (RabbitsGrassSimulationAgent)rabbitList.get(i);
		        currRbt.report();
		    }
		}

		public void buildSchedule()
		{
		    System.out.println("Running BuildSchedule");
		    
		    //SHUFFLE AND MOVE RABBITS IN RANDOM ORDER
		    class RabbitsGrassSimulationStep extends BasicAction 
		    {
		        public void execute() {
		          rgsSpace.grassGrowth(grassGrowthRate);
		          
		          SimUtilities.shuffle(rabbitList);
		          for(int i = 0; i < rabbitList.size(); i++)
		          {
		        	  RabbitsGrassSimulationAgent rbt = (RabbitsGrassSimulationAgent)rabbitList.get(i);
		        	  rbt.alterEnergy();
		          }
		          
		          removeDeadRabbits();
		          rabbitReproduce();
		          
		          displaySurf.updateDisplay();
		        }
		     }

		    schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());
		    
		    //CHECK ALIVE RABBIT NUMBER EVERY FIVE TIME STEPS: CAN CHANGE TIME STEPS AMOUNT	  
		    class RabbitsGrassSimulationCountLiving extends BasicAction 
		    {
		    	public void execute()
		    	{
		    		countLivingRabbits();
		    	}
		    }

		    schedule.scheduleActionAtInterval(5, new RabbitsGrassSimulationCountLiving());
		    
		    class RabbitsGrassSimulationUpdateGrassInSpace extends BasicAction 
		    {
		        public void execute()
		        {
		          amountOfGrassInSpace.step();
		        }
		    }

		    schedule.scheduleActionAtInterval(5, new RabbitsGrassSimulationUpdateGrassInSpace());
		}

		public void buildDisplay()
		{
			System.out.println("Running BuildDisplay");
			ColorMap colorMap = new ColorMap();

		    for(int i = 1; i < 16; i++)
		    {
		    	colorMap.mapColor(i, new Color(0, 255-i*8, 0));
			}
			colorMap.mapColor(0, Color.white);

		    Value2DDisplay displayGrass = new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), colorMap);
		    Object2DDisplay displayRabbits = new Object2DDisplay(rgsSpace.getCurrentRabbitSpace());
		    displayRabbits.setObjectList(rabbitList);
		    
			displaySurf.addDisplayableProbeable(displayGrass, "Grass");
			displaySurf.addDisplayableProbeable(displayRabbits, "Rabbits");

			amountOfGrassInSpace.addSequence("Grass", new grassInSpace());
			amountOfGrassInSpace.addSequence("Rabbits (5x)", new rabbitsInSpace());
		}
		
		private void addNewRabbit()
		{
			RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(initialRabbitEnergy, gridSize);
			if(rgsSpace.addRabbit(r, 10))
			{
				rabbitList.add(r);
			}
		}
		
		private void removeDeadRabbits()
		{
		    for(int i = rabbitList.size() - 1; i >= 0; i--)
		    {
		    	RabbitsGrassSimulationAgent rbt = (RabbitsGrassSimulationAgent)rabbitList.get(i);
		    	if(rbt.getRemainingEnergy() < 1)
		    	{
			        rgsSpace.removeRabbit(rbt.getX(), rbt.getY());
			        rabbitList.remove(i);
		    	}
		    }
		}
		
		private void rabbitReproduce()
		{
			int babyRabbits = 0;
			// REPRODUCTION RATE & ENERGY DECREASE AMOUNT
			for(int i = 0; i < rabbitList.size() ; i++)
		    {
		    	RabbitsGrassSimulationAgent rbt = (RabbitsGrassSimulationAgent)rabbitList.get(i);
		    	if(rbt.getRemainingEnergy() >= birthThreshold)
		    	{
		    		babyRabbits++;
		    		rbt.decreaseEnergy(rbt.getRemainingEnergy() / 2);
		    	}
		    }
			for (int i = 0; i < babyRabbits; i++) 
			{				
				addNewRabbit();
			}
		}

		
		private int countLivingRabbits()
		{
			int aliveRabbits = 0;
			for(int i = 0; i < rabbitList.size(); i++)
			{
				RabbitsGrassSimulationAgent rbt = (RabbitsGrassSimulationAgent)rabbitList.get(i);
				if(rbt.getRemainingEnergy() > 0) 
				{
					aliveRabbits++;
				}
			}
			System.out.println("Alive rabbit amount is: " + aliveRabbits);

			return aliveRabbits;
		}
			  
		public int getGridSize()
		{
			return gridSize;
		}
		
		public int getNumInitRabbits()
		{
			return numInitRabbits;
		}
		
		public int getNumInitGrass()
		{
			return numInitGrass;
		}
		
		public int getGrassGrowthRate()
		{
			return grassGrowthRate;
		}
		
		public int getBirthThreshold()
		{
			return birthThreshold;
		}

		
		public void setGridSize(int gs)
		{
			gridSize = gs;
		}
		
		public void setNumInitRabbits(int nir)
		{
			numInitRabbits = nir;
		}
		
		public void setNumInitGrass(int nig)
		{
			numInitGrass = nig;
		}
		
		public void setGrassGrowthRate(int ggr)
		{
			grassGrowthRate = ggr;
		}
		
		public void setBirthThreshold(int bt)
		{
			birthThreshold = bt;
		}

}
