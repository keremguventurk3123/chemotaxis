package chemotaxis.g11;

import java.awt.Point;
import java.util.*;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.DirectionType;
public class Controller extends chemotaxis.sim.Controller {
    DirectionType[][] directionMap;
    HashMap<Point, DirectionType> agents;
    Point start;
    Point target;

    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size     	 grid/map size
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, grid, simTime, budget, seed, simPrinter);
        agents = new HashMap<>();
        this.start = start;
        this.target = target;
        int endX = target.x - 1;
        int endY = target.y - 1;
        boolean[][] visited = new boolean[size][size];
        directionMap = new DirectionType[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                visited[r][c] = false;
                directionMap[r][c] = DirectionType.CURRENT;
            }
        }
        Queue<Point> queue = new LinkedList<Point>();
        queue.add(new Point(endX, endY));

        visited[endX][endY] = true;
        directionMap[endX][endY] = DirectionType.CURRENT;

        while (!queue.isEmpty()) {
            Point curr = queue.remove();
            int x = curr.x;
            int y = curr.y;
            if (x - 1 >= 0 && grid[x - 1][y].isOpen() && !visited[x - 1][y]) {
                queue.add(new Point(x - 1, y));
                visited[x-1][y] = true;
                directionMap[x - 1][y] = DirectionType.SOUTH;
            }

            if (y - 1 >= 0 && grid[x][y - 1].isOpen() && !visited[x][y - 1]) {
                queue.add(new Point(x, y - 1));
                visited[x][y-1] = true;
                directionMap[x][y - 1] = DirectionType.EAST;
            }

            if (x + 1 < size && grid[x + 1][y].isOpen() && !visited[x + 1][y]) {
                queue.add(new Point(x + 1, y));
                visited[x+1][y] = true;
                directionMap[x + 1][y] = DirectionType.NORTH;
            }

            if (y + 1 < size && grid[x][y + 1].isOpen() && !visited[x][y + 1]) {
                queue.add(new Point(x, y + 1));
                visited[x][y+1] = true;
                directionMap[x][y + 1] = DirectionType.WEST;
            }
        }

        //Prints the map that is made
        /*
        HashMap<DirectionType, Character> debugging = new HashMap<>();
        debugging.put(DirectionType.NORTH, 'N');
        debugging.put(DirectionType.SOUTH, 'S');
        debugging.put(DirectionType.EAST, 'E');
        debugging.put(DirectionType.WEST, 'W');
        debugging.put(DirectionType.CURRENT, 'C');
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print(debugging.get(directionMap[r][c]));
            }
            System.out.println();
        }
        */
    }

    public int closestToTarget(ArrayList<Point> locations) {
        int closestDistance = 9999999;
        int closestIdx = 0;
        for(int i = 0; i < locations.size(); i++) {
            int x = locations.get(i).x;
            int y = locations.get(i).y;
            int distance = Math.abs(x - this.target.x) + Math.abs(y - this.target.y);
            if(distance > 0 && distance < closestDistance) {
                closestIdx = i;
                closestDistance = distance;
            }
        }
        return closestIdx;
    }



    public DirectionType getHighestConcentrationDirection(ChemicalCell[][] grid , Point p , DirectionType d) {
        HashMap<DirectionType, Integer> bitDirectionMap = new HashMap<DirectionType, Integer>();
        bitDirectionMap.put(DirectionType.NORTH, 0b11);
        bitDirectionMap.put(DirectionType.SOUTH, 0b00);
        bitDirectionMap.put(DirectionType.WEST, 0b10);
        bitDirectionMap.put(DirectionType.EAST, 0b01);

        System.out.print(p);
        List<Point> neighbours =   new ArrayList<>();
        if (p.x!=1) {neighbours.add(new Point(p.x - 2, p.y-1));}
         if (p.y!=1) {neighbours.add(new Point(p.x-1, p.y - 2));}
         if (p.x!=size) {neighbours.add(new Point(p.x, p.y - 1));}
         if (p.y!=size) {neighbours.add(new Point(p.x - 1, p.y));}
        Double maxConcentration = Math.max(Math.max(grid[p.x-1][p.y-1].getConcentration(ChemicalCell.ChemicalType.BLUE), grid[p.x-1][p.y-1].getConcentration(ChemicalCell.ChemicalType.RED)), grid[p.x-1][p.y-1].getConcentration(ChemicalCell.ChemicalType.GREEN));
        DirectionType maxDirection = d;
        Point maxCell = p;
        for (Point it : neighbours) {

            double maximum = Math.max(Math.max(grid[it.x][it.y].getConcentration(ChemicalCell.ChemicalType.BLUE), grid[it.x][it.y].getConcentration(ChemicalCell.ChemicalType.RED)), grid[it.x][it.y].getConcentration(ChemicalCell.ChemicalType.GREEN));
            if (maximum > maxConcentration ) {
                maxConcentration = maximum;
                maxCell = it;
            }}
            if (maxCell.equals(new Point(p.x - 2, p.y - 1))) {
                maxDirection = DirectionType.NORTH;
            } else if (maxCell.equals(new Point(p.x - 1, p.y - 2))) {
                maxDirection = DirectionType.WEST;
            } else if (maxCell.equals(new Point(p.x - 1, p.y))) {
                maxDirection = DirectionType.EAST;
            }
            else if (maxCell.equals(new Point(p.x , p.y-1))) {
                maxDirection = DirectionType.NORTH;
            }
            if (bitDirectionMap.get(maxDirection) + bitDirectionMap.get(d) == 3)
            {
                maxDirection =d ;
            }

            return maxDirection;

        }
    /**
     * Apply chemicals to the map
     *
     * @param currentTurn         current turn in the simulation
     * @param chemicalsRemaining  number of chemicals remaining
     * @param locations           current locations of the agents
     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, ArrayList<Point> locations, ChemicalCell[][] grid) {
        ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
        if (locations.contains(start)) {
            agents.put(start, DirectionType.SOUTH);
        }

        if (locations.contains(target)) {
            agents.remove(target);
            locations.remove(target);
        }

        Point wrongDirectionAgent = null;
        double threshold = 0.2;
        for (Point p: locations) {
            if (!p.equals(target) && agents.get(p) != directionMap[p.x - 1][p.y - 1]) {
                {
                    wrongDirectionAgent = p;
                    System.out.print(p);
                    System.out.print(" ");
                    System.out.print(agents.get(p));

                    System.out.print(directionMap[p.x - 1][p.y - 1]);

                    break;}



            }
        }
        int PlacedChemical =0;
        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();
        if (wrongDirectionAgent != null) {
            DirectionType newDirection = directionMap[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1];
            if (newDirection == DirectionType.NORTH && grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.RED) < threshold ) {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x - 1, wrongDirectionAgent.y);
                agents.put(wrongDirectionAgent, DirectionType.NORTH);
                chemicals.add(ChemicalCell.ChemicalType.RED);
                PlacedChemical =1;

            }
            else if (newDirection == DirectionType.SOUTH  && (
                    grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.BLUE) < threshold ||
                            grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.BLUE) < grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.GREEN)))
                    {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x + 1, wrongDirectionAgent.y);
                agents.put(wrongDirectionAgent, DirectionType.SOUTH);
                chemicals.add(ChemicalCell.ChemicalType.BLUE);
                        PlacedChemical =1;
            }
            else if (newDirection == DirectionType.EAST && (grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.GREEN) < threshold
                    || grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.BLUE) > grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.GREEN)))
            {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x, wrongDirectionAgent.y + 1);
                agents.put(wrongDirectionAgent, DirectionType.EAST);
                chemicals.add(ChemicalCell.ChemicalType.GREEN);
                PlacedChemical =1;
            }
            else if (newDirection == DirectionType.WEST && grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.BLUE) !=1 &&  grid[wrongDirectionAgent.x - 1][wrongDirectionAgent.y - 1].getConcentration(ChemicalCell.ChemicalType.GREEN) !=1 ) {
                chemicalPlacement.location = new Point(wrongDirectionAgent.x, wrongDirectionAgent.y - 1);
                agents.put(wrongDirectionAgent, DirectionType.WEST);
                chemicals.add(ChemicalCell.ChemicalType.BLUE);
                chemicals.add(ChemicalCell.ChemicalType.GREEN);
                PlacedChemical =1;
            }

        }
        for (Point p: locations) {
            if(grid[p.x - 1][p.y - 1].getConcentration(ChemicalCell.ChemicalType.BLUE) >0.001 ||
                grid[p.x - 1][p.y - 1].getConcentration(ChemicalCell.ChemicalType.GREEN) > 0.001 ||
                grid[p.x - 1][p.y - 1].getConcentration(ChemicalCell.ChemicalType.RED) > 0.001)
                if (!((wrongDirectionAgent)!=null && PlacedChemical==1))
                        {   System.out.print(agents.get(p));
                            DirectionType correctDirection =getHighestConcentrationDirection(grid ,  p , agents.get(p))  ;

                            agents.replace(p ,correctDirection ) ;
                        }};

        HashMap<Point, DirectionType> newAgents = new HashMap<Point, DirectionType>();
        for (Point p: agents.keySet()) {
            DirectionType currentDirection = agents.get(p);
            if (currentDirection == DirectionType.NORTH) {
                newAgents.put(new Point(p.x - 1, p.y), DirectionType.NORTH);
            }
            else if (currentDirection == DirectionType.SOUTH) {
                newAgents.put(new Point(p.x + 1, p.y), DirectionType.SOUTH);
            }
            else if (currentDirection == DirectionType.WEST) {
                newAgents.put(new Point(p.x, p.y - 1), DirectionType.WEST);
            }
            else if (currentDirection == DirectionType.EAST) {
                newAgents.put(new Point(p.x, p.y + 1), DirectionType.EAST);
            }
            else {
                newAgents.put(new Point(p.x, p.y), DirectionType.CURRENT);
            }
        }
        this.agents = newAgents;
        /*
        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();
        if (wrongDirectionAgent != null) {
            chemicals.add(ChemicalCell.ChemicalType.BLUE);
        }
         */
        chemicalPlacement.chemicals = chemicals;
        return chemicalPlacement;
    }
}