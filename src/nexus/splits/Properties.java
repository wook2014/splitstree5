package nexus.splits;

/**
 * Created by Daria on 10.10.2016.
 */
public class Properties implements Cloneable {

    public final static int COMPATIBLE = 1;
    public final static int CYCLIC = 2;
    public final static int WEAKLY_COMPATIBLE = 3;
    public final static int INCOMPATIBLE = 4;
    public final static int UNKNOWN = 5;
    int compatibility = UNKNOWN;
    private double fit = -1.0;
    private double lsfit = -1.0;
    private boolean leastSquares = false;

    /**
     * do edges represent least square estimates?
     *
     * @return least squares
     */
    public boolean isLeastSquares() {
        return leastSquares;
    }

    /**
     * do edges represent least square estimates?
     *
     * @param leastSquares
     */
    public void setLeastSquares(boolean leastSquares) {
        this.leastSquares = leastSquares;
    }

    /**
     * Constructor
     */
    public Properties() {
    }

    /**
     * Gets the fit value
     *
     * @return the fit value
     */
    public double getFit() {
        return fit;
    }

    /**
     * Sets the fit value
     *
     * @param fit the fit value
     */
    public void setFit(double fit) {
        this.fit = fit;
    }

    public double getLSFit() {
        return this.lsfit;
    }

    public void setLSFit(double fit) {
        this.lsfit = fit;
    }

    /**
     * Returns the compatibilty value
     *
     * @return is compatible
     */
    public int getCompatibility() {
        return compatibility;
    }

    /**
     * Set the compatiblity value
     *
     * @param flag the compatibility value
     */
    public void setCompatibility(int flag) {
        compatibility = flag;
    }
}
