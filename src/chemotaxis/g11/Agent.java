package chemotaxis.g11;

import java.util.Map;
import java.util.HashMap;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;



public class Agent extends chemotaxis.sim.Agent {
    /**
     * Agent constructor
     *
     * @param simPrinter  simulation printer
     *
     */
	public Agent(SimPrinter simPrinter) {
		super(simPrinter);
	}

    public Double getHighestConcentration(Map<ChemicalType, Double> concentrations) {

        return Math.max(Math.max(concentrations.get(ChemicalType.RED), concentrations.get(ChemicalType.BLUE)), concentrations.get(ChemicalType.GREEN));
    }
    public  DirectionType  MaxConcentrationColor ( Map<ChemicalType, Double> m ) {
        ChemicalCell.ChemicalType maxColor = ChemicalCell.ChemicalType.BLUE;
        DirectionType d= DirectionType.NORTH;

        if (m.get(ChemicalType.BLUE) > m.get(ChemicalType.GREEN)
                && m.get(ChemicalType.BLUE) > m.get(ChemicalType.RED)) {
            d=DirectionType.SOUTH;
        }
        else if (m.get(ChemicalType.GREEN) > m.get(ChemicalType.BLUE)
                && m.get(ChemicalType.GREEN) > m.get(ChemicalType.RED)) {
            d=DirectionType.EAST;
        }
        else if (m.get(ChemicalType.RED) > m.get(ChemicalType.BLUE)
                && m.get(ChemicalType.RED) > m.get(ChemicalType.GREEN)) {
            d=DirectionType.NORTH;
        }

return d;}

    /**
     * Move agent
     *
     * @param randomNum        random number available for agents
     * @param previousState    byte of previous state
     * @param currentCell      current cell
     * @param neighborMap      map of cell's neighbors
     * @return                 agent move
     *
     */
    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        /* WE suppose that for the direction we use the last 2 bits
        of the byte and we set the default mapping as stated below:
         11: up
         00: down
         01: right
         10: left
        */
        HashMap<DirectionType, Integer> bitDirectionMap = new HashMap<DirectionType, Integer>();
        bitDirectionMap.put(DirectionType.NORTH, 0b11);
        bitDirectionMap.put(DirectionType.SOUTH, 0b00);
        bitDirectionMap.put(DirectionType.WEST, 0b10);
        bitDirectionMap.put(DirectionType.EAST, 0b01);

        int firstmove = previousState;

        previousState = (byte)( previousState - 128);

        Move move = new Move();
        //move.currentState = previousState;
        Integer previousDirection = previousState & 0b11;
        move.currentState = (byte)(previousState);
        //System.out.println(String.format("%8s", Integer.toBinaryString(move.currentState & 0xFF)).replace(' ', '0'));


        Map<ChemicalType, Double> concentrations = currentCell.getConcentrations();
        double highestConcentration = getHighestConcentration(concentrations);
        double currentConcentration = highestConcentration;
        double highest2 =0.0;

        for (DirectionType directionType : neighborMap.keySet()) {
            Map<ChemicalType, Double> neighborConcentrations = neighborMap.get(directionType).getConcentrations();
            if (highestConcentration < getHighestConcentration(neighborConcentrations))
            {
                highestConcentration = getHighestConcentration(neighborConcentrations);

                if (( firstmove> 4 || firstmove <0))
                { //i am not in the start i cannot allow backtracking



                    if (bitDirectionMap.get(directionType) + previousDirection == 3)
                    {
                        continue;
                    }
                    {
                        move.directionType = directionType;
                        //System.out.println(directionType);

                        move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
                    }
                }
                else //if i am at the start i can allow anything
                    {

                    move.directionType = directionType;
                        //System.out.println(directionType);

                        move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
                    }
            }
        }

        /*
        BLUE is SOUTH
        GREEN is EAST
        RED is NORTH
        GREEN + BLUE is WEST
         */
        System.out.println(highestConcentration);
        System.out.println(currentConcentration);
        System.out.println(move.directionType);
        DirectionType MaxColor = MaxConcentrationColor(currentCell.getConcentrations());

        if (highestConcentration > 0 && currentConcentration == highestConcentration) {
            if (concentrations.get(ChemicalType.BLUE) == 1 && concentrations.get(ChemicalType.GREEN) == 1) {
                move.directionType = DirectionType.WEST;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            }
            else { move.directionType = MaxColor;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);}
            /** else if (concentrations.get(ChemicalType.RED) == MaxColor) {
                move.directionType = DirectionType.NORTH;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            } else if (concentrations.get(ChemicalType.GREEN) == MaxColor) {
                move.directionType = DirectionType.EAST;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            } else if (concentrations.get(ChemicalType.BLUE) == MaxColor) {
                move.directionType = DirectionType.SOUTH;
                move.currentState = (byte) (bitDirectionMap.get(move.directionType) | 0b00);
            }*/
        }

        if ( move.directionType == DirectionType.CURRENT ) {
            if ( previousDirection == 0)
            { move.directionType = DirectionType.SOUTH; }
            else if (previousDirection == 1)
            {move.directionType = DirectionType.EAST; }
            else if (previousDirection == 2)
            {move.directionType = DirectionType.WEST; }
            else { move.directionType = DirectionType.NORTH; }
        }
        System.out.println(move.directionType);


        move.currentState = (byte) (move.currentState + 128) ;
        //System.out.println(String.format("%8s", Integer.toBinaryString(previousState & 0xFF)).replace(' ', '0'));

        return move;
    }
}