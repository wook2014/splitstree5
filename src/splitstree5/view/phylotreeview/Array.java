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
package splitstree5.view.phylotreeview;

import java.util.Iterator;

/**
 * dynamic object array
 * Daniel Huson, 10.2017
 */
public class Array<T> implements Iterable<T> {
    private Object[] array;

    public Array() {
        this(1024);
    }

    public Array(int initialCapacity) {
        array = new Object[initialCapacity];
    }

    public void clear() {
        for (int i = 0; i < array.length; i++)
            array[i] = null;
    }

    public T get(int index) {
        if (index >= array.length)
            return null;
        return (T) array[index];
    }

    public void set(int index, T value) {
        if (index >= array.length) {
            Object[] tmp = new Object[2 * Math.max(array.length, index)];
            System.arraycopy(array, 0, tmp, 0, array.length);
            array = tmp;
        }
        array[index] = value;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int pos = -1;

            {
                while (++pos < array.length) {
                    if (array[pos] != null)
                        break;
                }
            }

            @Override
            public boolean hasNext() {
                return pos < array.length;
            }

            @Override
            public T next() {
                final T result = (T) array[pos];
                while (++pos < array.length) {
                    if (array[pos] != null)
                        break;
                }
                return result;
            }
        };
    }
}
