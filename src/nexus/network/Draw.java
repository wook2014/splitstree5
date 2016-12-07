package nexus.network;

import jloda.util.parse.NexusStreamParser;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Created by Daria on 10.10.2016.
 */

/**
 * drawing switches
 * todo: these belong in assumptions
 */
public class Draw {

    private boolean toScale = true;
    private float hoffset = 0;
    private float voffset = 0;
    private int hflip = 0;
    private int vflip = 0;
    private double rotate = 0;
    private float zoom = -1;

    boolean modifyShowNodeNames = false;
    boolean modifyShowNodeIds = false;
    boolean modifyShowEdgeWeights = false;
    boolean modifyShowEdgeIds = false;
    boolean modifyShowEdgeConfidences = false;
    boolean modifyShowEdgeIntervals = false;
    public boolean modifyConfidenceEdgeWidth = false;
    public boolean modifyConfidenceEdgeShading = false;


    public boolean isToScale() {
        return toScale;
    }

    public void setToScale(boolean toScale) {
        this.toScale = toScale;
    }

    public float getHoffset() {
        return hoffset;
    }

    public void setHoffset(float hoffset) {
        this.hoffset = hoffset;
    }

    public float getVoffset() {
        return voffset;
    }

    public void setVoffset(float voffset) {
        this.voffset = voffset;
    }

    public int getHFlip() {
        return hflip;
    }

    public void setHflip(int hflip) {
        this.hflip = hflip;
    }

    public int getVFlip() {
        return vflip;
    }

    public void setVflip(int vflip) {
        this.vflip = vflip;
    }

    public double getRotate() {
        return rotate;
    }

    public void setRotate(double rotate) {
        this.rotate = rotate;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void read(NexusStreamParser np) throws IOException {
        List<String> tokens = np.getTokensLowerCase("draw", ";");

        toScale = np.findIgnoreCase(tokens, "to_scale", true, toScale);
        toScale = np.findIgnoreCase(tokens, "equal_edges", false, toScale);

        String taxlabels = np.findIgnoreCase(tokens, "taxlabels=", "name id both none", null);
        if (taxlabels != null)   // instruct syncNetwork2PhyloView to modify node labels
        {
            modifyShowNodeNames = (taxlabels.equalsIgnoreCase("name")
                    || taxlabels.equalsIgnoreCase("both"));
            modifyShowNodeIds = (taxlabels.equalsIgnoreCase("id")
                    || taxlabels.equalsIgnoreCase("both"));
        }
        String splitlabels = np.findIgnoreCase(tokens, "splitlabels=", "id weight confidence interval none", null);
        if (splitlabels != null)   // instruct syncNetwork2PhyloView to modify edge labels
        {
            modifyShowEdgeWeights = splitlabels.equalsIgnoreCase("weight");
            modifyShowEdgeIds = splitlabels.equalsIgnoreCase("id");
            modifyShowEdgeConfidences = splitlabels.equalsIgnoreCase("confidence");
            modifyShowEdgeIntervals = splitlabels.equalsIgnoreCase("interval");
        }

        String confidenceRendering = np.findIgnoreCase(tokens, "showconfidence=",
                "none edgewidth edgeshading", null);
        if (confidenceRendering != null) {
            modifyConfidenceEdgeWidth = confidenceRendering.equalsIgnoreCase("edgewidth");
            modifyConfidenceEdgeShading = confidenceRendering.equalsIgnoreCase("edgeshading");
        }


        hoffset = (float) np.findIgnoreCase(tokens, "hoffset=", -1000000, 1000000, hoffset);
        voffset = (float) np.findIgnoreCase(tokens, "voffset=", -1000000, 1000000, voffset);
        hflip = np.findIgnoreCase(tokens, "hflip=", 0, 1, hflip);
        vflip = np.findIgnoreCase(tokens, "vflip=", 0, 1, vflip);
        rotate = np.findIgnoreCase(tokens, "rotateAbout=", -1000, 1000, rotate);
        //  if(np.find(f,"'zoom=auto'"))
        //    zoom= -1;
        zoom = (int) np.findIgnoreCase(tokens, "zoom=", -1000000, 1000000, zoom);

        if (tokens.size() != 0)
            throw new IOException("line " + np.lineno() + ": `" + tokens +
                    "' unexpected in DRAW");
    }

    public void write(Writer w) throws IOException {
        w.write("DRAW");
        w.write(isToScale() ? " to_scale" : " equal_edges");
        if (getHoffset() != 0)
            w.write(" hoffset=" + getHoffset());
        if (getVoffset() != 0)
            w.write(" voffset=" + getVoffset());
        if (getHFlip() != 0)
            w.write(" hflip=1");
        if (getVFlip() != 0)
            w.write(" vflip=1");
        if (getRotate() != 0)
            w.write(" rotateAbout=" + (float) getRotate());
        if (getZoom() != -1)
            w.write(" zoom=" + getZoom());
        w.write(";\n");
    }
}
