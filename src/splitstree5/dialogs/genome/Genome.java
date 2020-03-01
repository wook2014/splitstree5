/*
 *  Genome.java Copyright (C) 2020 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.dialogs.genome;

import java.util.Iterator;

/**
 * represents data associated with a genome
 * Daniel Huson, 2.2020
 */
public abstract class Genome {
    abstract public String getName();

    /**
     * get the number of parts (e.g. chromosomes or contigs)
     *
     * @return number of parts
     */
    abstract public int getNumberOfParts();

    /**
     * get the name of a part
     *
     * @param i 1-based
     * @return name
     */
    abstract public String getName(int i);

    /**
     * get the length of a part
     *
     * @param i 1-based
     * @return number of letters
     */
    abstract public int length(int i);

    /**
     * get the sequence of a part
     *
     * @param i 1-based
     * @return
     */
    abstract public byte[] getSequence(int i);

    public Iterable<byte[]> parts() {
        return () -> new Iterator<>() {
            int i = 0;
            byte[] next = (i < getNumberOfParts() ? getSequence(i++) : null);

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public byte[] next() {
                final byte[] result = next;
                next = (i < getNumberOfParts() ? getSequence(i++) : null);
                return result;
            }
        };
    }

    public int length() {
        int length = 0;
        for (int i = 0; i < getNumberOfParts(); i++)
            length += length(i);
        return length;
    }
}
