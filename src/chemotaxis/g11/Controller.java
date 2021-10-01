package chemotaxis.g11;

import java.awt.Point;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.HashMap;

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

    int chemicalsPerAgent;
    int refreshRate;
    int greenChemicalBudget;
    int greenChemicalsPut;
    Point greenTarget;
    int trackingErrorEpsilon;
    int goalInAgents;

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

        int[][] diagSteps = new int[size][size];
        String[][] diagDirectionMap = new String[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                diagSteps[i][j] = Integer.MAX_VALUE;
                diagDirectionMap[i][j] = "CC";
            }
        }

        diagSteps[endX][endY] = 0;
        diagDirectionMap[endX][endY] = "CC";
        Queue<Point> diagQueue = new LinkedList<>();
        diagQueue.add(new Point(endX, endY));
        while(!diagQueue.isEmpty()) {
            Point curr = diagQueue.remove();
            int x = curr.x;
            int y = curr.y;
            diagHelper(x + 1, y, 1, 0, "NN", diagSteps[x][y] + 1, diagQueue, diagSteps, diagDirectionMap);
            diagHelper(x - 1, y, -1, 0, "SS", diagSteps[x][y] + 1, diagQueue, diagSteps, diagDirectionMap);
            diagHelper(x, y + 1, 0, 1, "WW", diagSteps[x][y] + 1, diagQueue, diagSteps, diagDirectionMap);
            diagHelper(x, y - 1, 0, -1, "EE", diagSteps[x][y] + 1, diagQueue, diagSteps, diagDirectionMap);
            diagHelper(x + 1, y + 1, 1, 1, "NW", diagSteps[x][y] + 2, diagQueue, diagSteps, diagDirectionMap);
            diagHelper(x + 1, y - 1, 1, -1, "NE", diagSteps[x][y] + 2, diagQueue, diagSteps, diagDirectionMap);
            diagHelper(x - 1, y + 1, -1, 1, "SW", diagSteps[x][y] + 2, diagQueue, diagSteps, diagDirectionMap);
            diagHelper(x - 1, y - 1, -1, -1, "SE", diagSteps[x][y] + 2, diagQueue, diagSteps, diagDirectionMap);
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print(diagDirectionMap[r][c] + " ");
            }
            System.out.println();
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (diagSteps[r][c] == Integer.MAX_VALUE) {
                    System.out.print("X");
                }
                else {
                    System.out.print(diagSteps[r][c]);
                }
            }
            System.out.println();
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
                    break;
                }
            }
        }
    }

    private void diagHelper(int x, int y, int xDiff, int yDiff, String direction, int count, Queue<Point> queue, int[][] diagSteps, String[][] diagDirectionMap) {
        while (x >= 0 && x < size && y >= 0 && y < size && grid[x][y].isOpen() && count < diagSteps[x][y]) {
            queue.add(new Point(x , y));
            //visited[x][y] = true;
            diagDirectionMap[x][y] = direction;
            diagSteps[x][y] = count;
            x += xDiff;
            y += yDiff;
        }
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

        HashMap<Point, DirectionType> newAgents = new HashMap<Point, DirectionType>();

        while (locations.contains(target)) {
            onConveyerAgents.remove(target);
            locations.remove(target);
            goalInAgents++;
        }

        boolean placeChemical = false;

        int minConveyer = Integer.MAX_VALUE;

        for(Point p : locations) {
            if(!onConveyerAgents.containsKey(p) && steps[p.x - 1][p.y - 1] <= chemicalsPerAgent && onConveyerAgents.size() <= (agentGoal + trackingErrorEpsilon - goalInAgents)) {
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

        for (Point p: onConveyerAgents.keySet()) {
            DirectionType currentDirection = onConveyerAgents.get(p);
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

        onConveyerAgents = newAgents;
        List<ChemicalCell.ChemicalType> chemicals = new ArrayList<>();

        if(placeChemical) {
            chemicals.add(ChemicalCell.ChemicalType.BLUE);
        }
        else if ((currentTurn - 1) % refreshRate == 0) {
            if (greenChemicalsPut < greenChemicalBudget) {
                if (!greenTarget.equals(start)) {
                    Point placement = this.greenTarget;
                    chemicalPlacement.location = placement;
                    chemicals.add(ChemicalCell.ChemicalType.GREEN);
                    greenChemicalsPut++;
                }
            }
        }


        chemicalPlacement.chemicals = chemicals;
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
        return false;
    }
}