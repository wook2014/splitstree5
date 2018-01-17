package nexus;

import java.io.Writer;

/**
 * Created by Daria on 19.09.2016.
 */
public interface NexusBlock {

    void setValid(boolean valid);

    boolean isValid();

    void write(Writer w, Taxa taxa);
}
