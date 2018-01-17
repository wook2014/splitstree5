package nexus.characters;

/**
 * Created by Daria on 06.10.2016.
 */
public class Properties implements Cloneable {
    private boolean hasGamma;
    private boolean hasPinvar;
    private double gammaParam = -1;
    private double pInvar = -1;


    /* Options for the program only */

    public boolean hasGamma() {
        return hasGamma;
    }

    public boolean hasPinvar() {
        return hasPinvar;
    }

    /* Options that can be read in and are written */

    public double getGammaParam() {
        return gammaParam;
    }

    public void setGammaParam(double val) {
        if (val > 0.0) {
            hasGamma = true;
            gammaParam = val;
        }
    }

    public double getpInvar() {
        return pInvar;
    }

    public void setpInvar(double pInvar) {
        if (pInvar >= 0.0 && pInvar <= 1.0) {
            hasPinvar = true;
            this.pInvar = pInvar;
        }
    }


    /**
     * Constructor
     */
    public Properties() {
        hasGamma = hasPinvar = false;
    }
}
