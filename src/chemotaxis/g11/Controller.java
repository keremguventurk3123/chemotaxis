package chemotaxis.g11;

import java.awt.Point;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;


import java.util.Queue;
import java.util.HashMap;
import java.util.Collections;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.DirectionType;

public class Controller extends chemotaxis.sim.Controller {
    DirectionType[][] directionMap;
    int[][] steps;
    //HashMap<Point, DirectionType> agents;
    HashMap<Point, DirectionType> onConveyerAgents;
    Point start;
    Point target;
    Point nextPosition ;
    int needToFinishPath ;
    int RoundFirstPlaced;
    ChemicalCell.ChemicalType lastChemical;
    int chemicalsPerAgent;
    int currentIndex;
    int refreshRate;
    int greenChemicalBudget;
    int greenChemicalsPut;
    Point greenTarget;
    int trackingErrorEpsilon;
    int goalInAgents;
    ArrayList<Point> shortest_path ;

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
    public Controller(Point start, Point target, Integer size, ChemicalCell[][] grid, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter, Integer agentGoal, Integer spawnFreq) {
        super(start, target, size, grid, simTime, budget, seed, simPrinter, agentGoal, spawnFreq);
        onConveyerAgents = new HashMap<>();
        this.start = start;
        this.target = target;
        this.currentIndex =0;
        this.RoundFirstPlaced =0;
        this.nextPosition = start;
        this.needToFinishPath = 0;
        this.lastChemical = ChemicalCell.ChemicalType.RED;
        int endX = target.x - 1;
        int endY = target.y - 1;
        boolean[][] visited = new boolean[size][size];
        steps = new int[size][size];
        directionMap = new DirectionType[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                visited[r][c] = false;
                directionMap[r][c] = DirectionType.CURRENT;
                steps[r][c] = 0;
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
            helper(x + 1, y, 1, 0, DirectionType.NORTH, steps[x][y] + 1, queue, visited);
            helper(x - 1, y, -1, 0, DirectionType.SOUTH, steps[x][y] + 1, queue, visited);
            helper(x, y + 1, 0, 1, DirectionType.WEST, steps[x][y] + 1, queue, visited);
            helper(x, y - 1, 0, -1, DirectionType.EAST, steps[x][y] + 1, queue, visited);
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
        System.out.println("Steps Map");
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print(steps[r][c]);
            }
            System.out.println();
        }
        */
        trackingErrorEpsilon = 2;
        goalInAgents = 0;
        refreshRate = simTime / agentGoal;
        greenChemicalBudget = agentGoal;
        greenChemicalsPut = 0;
        chemicalsPerAgent = (budget - greenChemicalBudget) / agentGoal;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                visited[r][c] = false;
            }
        }
        Queue<Point> queue2 = new LinkedList<>();
        queue2.add(new Point(start.x - 1, start.y - 1));
        greenTarget = null;
        while (!queue2.isEmpty()) {
            Point curr = queue2.remove();
            int x = curr.x;
            int y = curr.y;
            if (x >= 0 && x < size && y >= 0 && y < size && grid[x][y].isOpen() && !visited[x][y]) {
                queue2.add(new Point(x + 1 , y));
                queue2.add(new Point(x - 1 , y));
                queue2.add(new Point(x, y + 1));
                queue2.add(new Point(x, y - 1));
                visited[x][y] = true;
                if (steps[x][y] <= chemicalsPerAgent) {
                    greenTarget = new Point(x + 1, y + 1);
                    System.out.println(greenTarget);
                    System.out.println("Green target");
                    break;
                }
            }
        }
        HashMap<Point, Point> Parents = new HashMap<Point, Point>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                visited[r][c] = false;
            }
        }
        Queue<Point> queue3 = new LinkedList<>();
        Queue<Point> parents = new LinkedList<>();
        queue3.add(new Point(start.x - 1, start.y - 1));
        parents.add(new Point(start.x - 1, start.y - 1));
        Point curr = null ;
        Point dad = null ;
        while (!queue3.isEmpty()) {
            curr = queue3.remove();
            dad = parents.remove();
            int x = curr.x;
            int y = curr.y;






            if (x >= 0 && x < size && y >= 0 && y < size && grid[x][y].isOpen() && !visited[x][y]) {
                queue3.add(new Point(x + 1 , y));
                queue3.add(new Point(x - 1 , y));
                queue3.add(new Point(x, y + 1));
                queue3.add(new Point(x, y - 1));
                parents.add(curr);
                parents.add(curr);
                parents.add(curr);
                parents.add(curr);
                Parents.put(curr,dad);



                visited[x][y] = true;
                //System.out.println(Parents);


            }
            if (x == target.x && y ==target.y) {

                System.out.println("arrived");
                break;}

        }
        System.out.println("hereee");
        ArrayList<Point> path= new ArrayList<Point>() ;
        path.add(curr) ;
        while (!(curr.x == start.x-1 && curr.y ==start.y-1)) {
            path.add(Parents.get(curr));
            curr = Parents.get(curr) ;

        }
        int redundant = path.size() % 7 ;
        for (int i=0; i<redundant; i++) {
            path.remove(path.size()-1 -i) ;
        }
        Collections.reverse(path) ;
        this.shortest_path = path ;

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
        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();
        System.out.println(shortest_path);
        if(chemicalsPerAgent>1) {
            HashMap<Point, DirectionType> newAgents = new HashMap<Point, DirectionType>();

            if (locations.contains(target)) {
                onConveyerAgents.remove(target);
                locations.remove(target);
                goalInAgents++;
            }

            boolean placeChemical = false;

            int minConveyer = Integer.MAX_VALUE;

            for (Point p : locations) {
                if (!onConveyerAgents.containsKey(p) && steps[p.x - 1][p.y - 1] <= chemicalsPerAgent && onConveyerAgents.size() <= (agentGoal + trackingErrorEpsilon - goalInAgents)) {
                    onConveyerAgents.put(p, DirectionType.CURRENT);
                }
                if (onConveyerAgents.containsKey(p)) {
                    if (onConveyerAgents.get(p) != directionMap[p.x - 1][p.y - 1] && steps[p.x - 1][p.y - 1] <= minConveyer) {
                        Point placement = getChemicalPlacement(p.x - 1, p.y - 1, p);
                        if (checkIfAgentExists(placement, p, locations)) {
                            continue;
                        }
                        chemicalPlacement.location = placement;
                        onConveyerAgents.replace(p, directionMap[p.x - 1][p.y - 1]);
                        placeChemical = true;
                        minConveyer = steps[p.x - 1][p.y - 1];
                        //break;
                    }
                }
            }

            for (Point p : onConveyerAgents.keySet()) {
                DirectionType currentDirection = onConveyerAgents.get(p);
                if (currentDirection == DirectionType.NORTH) {
                    newAgents.put(new Point(p.x - 1, p.y), DirectionType.NORTH);
                } else if (currentDirection == DirectionType.SOUTH) {
                    newAgents.put(new Point(p.x + 1, p.y), DirectionType.SOUTH);
                } else if (currentDirection == DirectionType.WEST) {
                    newAgents.put(new Point(p.x, p.y - 1), DirectionType.WEST);
                } else if (currentDirection == DirectionType.EAST) {
                    newAgents.put(new Point(p.x, p.y + 1), DirectionType.EAST);
                } else {
                    newAgents.put(new Point(p.x, p.y), DirectionType.CURRENT);
                }
            }

            onConveyerAgents = newAgents;

            if (placeChemical) {
                chemicals.add(ChemicalCell.ChemicalType.BLUE);
            } else if ((currentTurn - 1) % refreshRate == 0) {
                if (greenChemicalsPut < greenChemicalBudget) {
                    if (!greenTarget.equals(start)) {
                        Point placement = this.greenTarget;
                        chemicalPlacement.location = placement;
                        chemicals.add(ChemicalCell.ChemicalType.GREEN);
                        greenChemicalsPut++;
                    }
                }
            }



        chemicalPlacement.chemicals = chemicals;}
        else {
            ArrayList<Point> shortest_path = this.shortest_path ;
            int chemicalsForone = shortest_path.size() / 7;
            int refreshChems = spawnFreq * (agentGoal/chemicalsForone) ;

            this.nextPosition =shortest_path.get(currentIndex+7);

            if (needToFinishPath==1) {
                        Point placement = nextPosition;
                        chemicalPlacement.location = placement;
                        if (this.lastChemical.equals(ChemicalCell.ChemicalType.GREEN)) {

                            chemicals.add(ChemicalCell.ChemicalType.RED);
                            this.lastChemical = ChemicalCell.ChemicalType.RED;
                        }
                        else {
                            chemicals.add(ChemicalCell.ChemicalType.GREEN);
                            this.lastChemical = ChemicalCell.ChemicalType.GREEN;
                        }
                        this.currentIndex =currentIndex+7;
                        this.nextPosition =shortest_path.get(currentIndex);
                    }
            else{
                System.out.println(currentTurn);
                if ( ((currentTurn - 1) % refreshChems == 0) | currentTurn==1) {
                    Point placement = shortest_path.get(7);
                    System.out.println(placement);

                    chemicalPlacement.location = placement;
                    chemicals.add(ChemicalCell.ChemicalType.GREEN);
                    this.lastChemical = ChemicalCell.ChemicalType.GREEN;
                    this.needToFinishPath =1 ;


                }
                }

            System.out.println(chemicalPlacement.location);



        }
        chemicalPlacement.chemicals = chemicals;
        System.out.println(chemicalPlacement.location);
        System.out.println(chemicals);
        return chemicalPlacement;
    }

    private void helper(int x, int y, int xDiff, int yDiff, DirectionType d, int count, Queue<Point> queue, boolean[][] visited) {
        while (x >= 0 && x < size && y >= 0 && y < size && grid[x][y].isOpen() && !visited[x][y]) {
            queue.add(new Point(x , y));
            visited[x][y] = true;
            directionMap[x][y] = d;
            steps[x][y] = count;
            x += xDiff;
            y += yDiff;
        }
    }

    private Point getChemicalPlacement(int currX, int currY, Point agentLocation) {
        if(directionMap[currX][currY] == DirectionType.NORTH) {
            return new Point(agentLocation.x - 1, agentLocation.y);
        }
        else if(directionMap[currX][currY] == DirectionType.SOUTH) {
            return new Point(agentLocation.x + 1, agentLocation.y);
        }
        else if(directionMap[currX][currY] == DirectionType.EAST) {
            return new Point(agentLocation.x, agentLocation.y + 1);
        }
        else if(directionMap[currX][currY] == DirectionType.WEST) {
            return new Point(agentLocation.x, agentLocation.y - 1);
        }
        return null;
    }

    private boolean checkIfAgentExists(Point placement, Point targetAgent, ArrayList<Point> locations) {
        ArrayList<Point> pointsToCheck = new ArrayList<>();
        pointsToCheck.add(placement);
        if (targetAgent.x + 1 == placement.x) {
            pointsToCheck.add(new Point(placement.x, placement.y - 1));
            pointsToCheck.add(new Point(placement.x, placement.y + 1));
            pointsToCheck.add(new Point(placement.x + 1, placement.y));
        }
        else if (targetAgent.x - 1 == placement.x) {
            pointsToCheck.add(new Point(placement.x, placement.y - 1));
            pointsToCheck.add(new Point(placement.x, placement.y + 1));
            pointsToCheck.add(new Point(placement.x - 1, placement.y));
        }
        else if (targetAgent.y - 1 == placement.y) {
            pointsToCheck.add(new Point(placement.x, placement.y - 1));
            pointsToCheck.add(new Point(placement.x + 1, placement.y));
            pointsToCheck.add(new Point(placement.x - 1, placement.y));
        }
        else if (targetAgent.y + 1 == placement.y) {
            pointsToCheck.add(new Point(placement.x, placement.y + 1));
            pointsToCheck.add(new Point(placement.x + 1, placement.y));
            pointsToCheck.add(new Point(placement.x - 1, placement.y));
        }

        for(Point p : pointsToCheck)  {
            if(locations.contains(p)) {
                return true;
            }
        }
        return false;
    }
}