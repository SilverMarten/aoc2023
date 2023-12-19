package aoc;

public class LongCoordinate {

    private final long row;
    private final long column;
    private final int hashCode;

    private LongCoordinate(long row, long column) {
        this.row = row;
        this.column = column;
        this.hashCode = computeHashCode();
    }

    public long getRow() {
        return row;
    }

    public long getColumn() {
        return column;
    }

    public static LongCoordinate of(long row, long column) {
        return new LongCoordinate(row, column);
    }

    /**
     * Return the pre-generated hashCode for this Coordinate.
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * Compute the hashCode for this Coordinate so it can be cached by the
     * constructor.
     * 
     * @return the computed hashCode for this Coordinate.
     */
    private int computeHashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (column ^ (column >>> 32));
        result = prime * result + (int) (row ^ (row >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LongCoordinate other = (LongCoordinate) obj;
        if (column != other.column)
            return false;
        if (row != other.row)
            return false;
        return true;
    }

}
