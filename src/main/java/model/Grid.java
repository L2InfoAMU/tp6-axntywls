package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * {@link Grid} instances represent the grid in <i>The Game of Life</i>.
 */
public class Grid implements Iterable<Cell> {

    private final int numberOfRows;
    private final int numberOfColumns;
    private final Cell[][] cells;

    /**
     * Creates a new {@code Grid} instance given the number of rows and columns.
     *
     * @param numberOfRows    the number of rows
     * @param numberOfColumns the number of columns
     * @throws IllegalArgumentException if {@code numberOfRows} or {@code numberOfColumns} are
     *                                  less than or equal to 0
     */
    public Grid(int numberOfRows, int numberOfColumns) {
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.cells = createCells();
    }

    /**
     * Returns an iterator over the cells in this {@code Grid}.
     *
     * @return an iterator over the cells in this {@code Grid}
     */

    @Override
    public Iterator<Cell> iterator() {
        return new GridIterator(this);
    }

    private Cell[][] createCells() {
        Cell[][] cells = new Cell[getNumberOfRows()][getNumberOfColumns()];
        for (int rowIndex = 0; rowIndex < getNumberOfRows(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < getNumberOfColumns(); columnIndex++) {
                cells[rowIndex][columnIndex] = new Cell();
            }
        }
        return cells;
    }

    /**
     * Returns the {@link Cell} at the given index.
     *
     * <p>Note that the index is wrapped around so that a {@link Cell} is always returned.
     *
     * @param rowIndex    the row index of the {@link Cell}
     * @param columnIndex the column index of the {@link Cell}
     * @return the {@link Cell} at the given row and column index
     */
    public Cell getCell(int rowIndex, int columnIndex) {
        return cells[getWrappedRowIndex(rowIndex)][getWrappedColumnIndex(columnIndex)];
    }

    private int getWrappedRowIndex(int rowIndex) {
        return (rowIndex + getNumberOfRows()) % getNumberOfRows();
    }

    private int getWrappedColumnIndex(int columnIndex) {
        return (columnIndex + getNumberOfColumns()) % getNumberOfColumns();
    }

    /**
     * Returns the number of rows in this {@code Grid}.
     *
     * @return the number of rows in this {@code Grid}
     */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * Returns the number of columns in this {@code Grid}.
     *
     * @return the number of columns in this {@code Grid}
     */
    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    private List<Cell> getNeighbours(int rowIndex, int columnIndex) {
        List<Cell> neighbours = new ArrayList<>();
        for (int yIndex = rowIndex -1; yIndex <= rowIndex+1; yIndex ++){
            for (int xIndex = columnIndex -1; xIndex <= columnIndex+1; xIndex ++){
                neighbours.add( getCell(yIndex,xIndex) );
            }
        }
        //we have to remove our actual Cell! => [getCell(rowIndex,columnIndex)]
        neighbours.remove(getCell(rowIndex,columnIndex));
        return neighbours;
    }

    private int countAliveNeighbours(int rowIndex, int columnIndex) {
        List<Cell> neighbours = getNeighbours(rowIndex,columnIndex);
        int aliveNeighbours = 0;
        for (Cell cell : neighbours){
            if (cell.isAlive()){aliveNeighbours ++;}
        }
        return aliveNeighbours;
    }

    private CellState calculateNextState(int rowIndex, int columnIndex) {
        int aliveNeighbours = countAliveNeighbours(rowIndex, columnIndex);
        Cell myCell = getCell(rowIndex, columnIndex);
        //if myCell is ALIVE:
        if (myCell.isAlive()) {
            if (aliveNeighbours == 2 || aliveNeighbours == 3) {
                return myCell.getState();
            }
            return CellState.DEAD;
        }
        //if myCell is DEAD:
        if (aliveNeighbours == 3) {
            return chooseColorOnBirth(rowIndex,columnIndex);
        }
        return CellState.DEAD;
    }
    /**
     * Give birth to a Cell with the same color as the majority of it's neighbours.
     * in this method, Our cell has exactly 3 neighbours.
     * @param rowIndex
     * @param columnIndex
     * @return  The state of the majority of the neighbours.
     */
    private CellState chooseColorOnBirth(int rowIndex, int columnIndex){
        List<Cell> neighbours = getNeighbours(rowIndex, columnIndex);
        int nb_of_red_friends = 0;
        for (Cell cell : neighbours) {
            if (cell.getState() == CellState.ALIVE_RED){nb_of_red_friends ++;}
            //2 is the majority of neighbours alive, if we have 2 red, then our cell will born RED.
            if (nb_of_red_friends == 2 ){return CellState.ALIVE_RED;}
        }
        //here there is not: at least 2 red neighbours, so our majority is BLUE.
        return CellState.ALIVE_BLUE;
    }

    private CellState[][] calculateNextStates() {
        CellState[][] nextCellState = new CellState[getNumberOfRows()][getNumberOfColumns()];
        for(int yIndex=0; yIndex < getNumberOfRows();yIndex++){
            for (int xIndex=0; xIndex < getNumberOfColumns(); xIndex++){
                nextCellState[yIndex][xIndex] = calculateNextState(yIndex,xIndex);
            }
        }
        return nextCellState;
    }

    private void updateStates(CellState[][] nextState) {
        Iterator<Cell> iterator = iterator();
        for(int yIndex=0; yIndex < getNumberOfRows();yIndex++){
            for (int xIndex=0; xIndex < getNumberOfColumns(); xIndex++){
                CellState new_state = nextState[yIndex][xIndex];
                iterator.next().setState(new_state);
            }
        }

    }

    /**
     * Transitions all {@link Cell}s in this {@code Grid} to the next generation.
     *
     * <p>The following rules are applied:
     * <ul>
     * <li>Any live {@link Cell} with fewer than two live neighbours dies, i.e. underpopulation.</li>
     * <li>Any live {@link Cell} with two or three live neighbours lives on to the next
     * generation.</li>
     * <li>Any live {@link Cell} with more than three live neighbours dies, i.e. overpopulation.</li>
     * <li>Any dead {@link Cell} with exactly three live neighbours becomes a live cell, i.e.
     * reproduction.</li>
     * </ul>
     */
    void updateToNextGeneration() {
        //Get the grid of new_states
        CellState[][] nextStates = calculateNextStates();
        //Set the new_states of the Cells of "this".
        updateStates(nextStates);
    }

    /**
     * Sets all {@link Cell}s in this {@code Grid} as dead.
     */
    void clear() {
        Iterator<Cell> iterator = iterator();
        //while there is Cells, set their state as DEAD.
        while(iterator.hasNext()){
            iterator.next().setState(CellState.DEAD);
        }
    }

    /**
     * Goes through each {@link Cell} in this {@code Grid} and randomly sets its state as ALIVE or DEAD.
     *
     * @param random {@link Random} instance used to decide if each {@link Cell} is ALIVE or DEAD.
     * @throws NullPointerException if {@code random} is {@code null}.
     */
    void randomGeneration(Random random) {
        Iterator<Cell> iterator = iterator();
        while(iterator.hasNext()){
            // nextBoolean == 1, then our cell is ALIVE
            if (random.nextBoolean()) {
                // if true again, then our CELL is RED *** chance =(1/2) *(1/2) = 1/4
                if (random.nextBoolean()){iterator.next().setState(CellState.ALIVE_RED);}
                //else our Cell is BLUE *** chance =(1/2) *(1/2) = 1/4
                else{iterator.next().setState(CellState.ALIVE_BLUE);}
            }
            // else it is DEAD
            else{
                iterator.next().setState(CellState.DEAD);//chance = (1/2)
            }
        }
    }

}
