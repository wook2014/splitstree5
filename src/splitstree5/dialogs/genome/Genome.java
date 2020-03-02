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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * represents data associated with a genome
 * Daniel Huson, 2.2020
 */
public class Genome {
    private String name;
    private String accession;
    private int length;
    final private ArrayList<GenomePart> parts = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public ArrayList<GenomePart> getParts() {
        return parts;
    }

    /**
     * get the number of parts (e.g. chromosomes or contigs)
     *
     * @return number of parts
     */
    public int getNumberOfParts() {
        return parts.size();
    }

    /**
     * get the name of a part
     *
     * @param i 1-based
     * @return name
     */
    public String getName(int i) {
        return parts.get(i).getName();
    }

    /**
     * get the length of a part
     *
     * @param i 1-based
     * @return number of letters
     */
    public int length(int i) {
        return parts.get(i).getLength();
    }

    /**
     * get the sequence of a part
     *
     * @param i 1-based
     * @return
     */
    public GenomePart getSequence(int i) {
        return parts.get(i);
    }

    public Iterable<byte[]> parts() {
        return () -> new Iterator<>() {
            int i = 0;
            byte[] next = (i < getNumberOfParts() ? getSequence(i++).getSequence() : null);

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public byte[] next() {
                final byte[] result = next;
                next = (i < getNumberOfParts() ? getSequence(i++).getSequence() : null);
                return result;
            }
        };
    }

    public int computeLength() {
        int length = 0;
        for (int i = 0; i < getNumberOfParts(); i++)
            length += length(i);
        return length;
    }

    public static class GenomePart {
        private String name;
        private byte[] sequence;
        private String file;
        private long offset;
        private int length;

        public GenomePart() {
        }

        public GenomePart(String name, byte[] sequence, String file, long offset, int length) {
            this.name = name;
            this.sequence = sequence;
            this.file = file;
            this.offset = offset;
            this.length = length;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getSequence() {
            if (sequence != null)
                return sequence;
            else {
                // todo: read from file
                return null;
            }
        }

        public void setSequence(byte[] sequence, int length) {
            if (sequence != null) {
                file = null;
                offset = 0;
            }
            this.sequence = sequence;
            this.length = length;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file, long offset, int length) {
            if (file != null)
                sequence = null;
            this.file = file;
            this.offset = offset;
            this.length = length;
        }

        public long getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }
    }
}
