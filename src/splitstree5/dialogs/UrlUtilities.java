/*
 * URLUtilities.java Copyright (C) 2021. Daniel H. Huson
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
 *
 */

package splitstree5.dialogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * some URL utilities
 * Daniel Huson, 9.2020
 */
public class UrlUtilities {

    /**
     * if ftpUrl is a directory, gets a file from directory that matches the given reg exp
     */
    public static String getFileForFtpUrl(String ftpUrl, String mustNotMatch, String mustMatch) throws IOException {
        if (!ftpUrl.matches(mustNotMatch) && ftpUrl.matches(mustMatch))
            return ftpUrl;
        else { // assume it is a directory
            URL url = new URL(ftpUrl + ";type=d");
            URLConnection conn = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.matches(mustNotMatch) && line.matches(mustMatch)) {
                        if (line.contains("/"))
                            line = line.substring(line.indexOf("/") + 1);
                        System.err.println("Url: " + ftpUrl);
                        System.err.println("File: " + line);
                        return ftpUrl + "/" + line;
                    }
                }
            }
        }
        return null;
    }
}
