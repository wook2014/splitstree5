/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.xtra.phylotreeview;

public class FloatArray {
    /**
     * dynamic int array
     * Daniel Huson, 10.2017
     */
    private float[] array;

    public FloatArray() {
        this(1024);
    }

    public FloatArray(int initialCapacity) {
        array = new float[initialCapacity];
    }

    public void clear() {
        for (int i = 0; i < array.length; i++)
            array[i] = 0;
    }

    public float get(int index) {
        if (index >= array.length)
            return 0;
        return array[index];
    }

    public void set(int index, float value) {
        if (index >= array.length) {
            float[] tmp = new float[2 * Math.max(array.length, index)];
            System.arraycopy(array, 0, tmp, 0, array.length);
            array = tmp;
        }
        array[index] = value;
    }
}
