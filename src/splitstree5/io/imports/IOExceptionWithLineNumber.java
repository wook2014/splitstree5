/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.io.imports;

import java.io.IOException;

/**
 * exception with line number
 * Daniel Huson, 1.2018
 */
public class IOExceptionWithLineNumber extends IOException {
    /**
     * constructor
     *
     * @param message
     * @param lineNumber
     */
    public IOExceptionWithLineNumber(String message, int lineNumber) {
        super(message);
        setLineNumber(lineNumber);
    }

    @Override
    public String getMessage() {
        return "Line " + lineNumber + ": " + super.getMessage();
    }

    private int lineNumber = -1;

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
