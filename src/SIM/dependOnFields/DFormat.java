package SIM.dependOnFields;

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import splitstree4.core.SplitsException;


public class DFormat extends ObjectProperty<DFormat>{

    private String triangle;
    private boolean labels;
    private boolean diagonal;
    private String varType = "ols";

    private boolean valid = true;
    private ExpressionHelper<DFormat> helper = null;

    /**
     * the Constructor
     */
    public DFormat() {
        triangle = "both";
        labels = true;
        diagonal = true;
        varType = "ols";
    }

    public String getTriangle() {
        return triangle;
    }
    public void setTriangle(String triangle) throws SplitsException {
        if (!triangle.equals("both") && !triangle.equals("lower") && !triangle.equals("upper"))
            throw new SplitsException("Illegal triangle:" + triangle);
        this.triangle = triangle;
        markInvalid();
    }

    public boolean getLabels() {
        return labels;
    }
    public void setLabels(boolean labels) {
        this.labels = labels;
        markInvalid();
    }

    public boolean getDiagonal() {
        return diagonal;
    }
    public void setDiagonal(boolean diagonal) {
        this.diagonal = diagonal;
        markInvalid();
    }

    public String getVarType() {
        return varType;
    }
    public void setVarType(String val) {
        this.varType = val;
        markInvalid();
    }

    @Override
    public void bind(ObservableValue<? extends DFormat> observable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unbind() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isBound() {
        return false;
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public DFormat get() {
        return this;
    }

    @Override
    public void set(DFormat value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addListener(ChangeListener<? super DFormat> listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeListener(ChangeListener<? super DFormat> listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addListener(InvalidationListener listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(helper);
    }

    private void markInvalid() {
        if (valid) {
            valid = false;
            invalidated();
            fireValueChangedEvent();
        }
    }
    /**
     * The method {@code invalidated()} can be overridden to receive
     * invalidation notifications. This is the preferred option in
     * {@code Objects} defining the property, because it requires less memory.
     *
     * The default implementation is empty.
     */
    protected void invalidated() {
    }
}
