/**
 * PluginClassLoader.java
 * Copyright (C) 2018 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.util;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Finds all classes in the named package, of the given type
 *
 * Daniel Huson and others, 2003
 */
public class PluginClassLoader {
    /**
     * get an instance of each class of the given type in the given packages
     *
     * @param clazz
     * @param packageNames
     * @return instances
     */
    public static <T> List<T> getInstances(Class<T> clazz, String... packageNames) {
        return getInstances(clazz, null, packageNames);
    }

    /**
     * get an instance of each class of the given types in the given packages
     *
     * @param clazz1       this must be assignable from the returned class
     * @param clazz2       if non-null, must also be assignable from the returned class
     * @param packageNames
     * @return instances
     */
    public static <T> List<T> getInstances(Class<T> clazz1, Class clazz2, String... packageNames) {
        final List<T> plugins = new LinkedList<>();
        final LinkedList<String> packageNameQueue = new LinkedList<>();
        packageNameQueue.addAll(Arrays.asList(packageNames));
        while (packageNameQueue.size() > 0) {
            try {
                final String packageName = packageNameQueue.removeFirst();
                final String[] resources = ResourcesUtils.fetchResources(packageName);

                for (int i = 0; i != resources.length; ++i) {
                    //System.err.println("Resource: " + resources[i]);
                    if (resources[i].endsWith(".class")) {
                        try {
                            resources[i] = resources[i].substring(0, resources[i].length() - 6);
                            final Class<T> c = ResourcesUtils.classForName(packageName.concat(".").concat(resources[i]));
                            if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && clazz1.isAssignableFrom(c) && (clazz2 == null || clazz2.isAssignableFrom(c))) {
                                try {
                                    plugins.add(c.newInstance());
                                } catch (InstantiationException ex) {
                                    //Basic.caught(ex);
                                }
                            }
                        } catch (Exception ex) {
                            // Basic.caught(ex);
                        }
                    } else {
                        try {
                            final String newPackageName = resources[i];
                            if (!newPackageName.equals(packageName)) {
                                packageNameQueue.addLast(newPackageName);
                                // System.err.println("Adding package name: " + newPackageName);
                            }
                        } catch (Exception ex) {
                            // Basic.caught(ex);
                        }
                    }
                }
            } catch (IOException ex) {
                Basic.caught(ex);
            }
        }
        return plugins;
    }

    /**
     * get an instance of each class of the given type in the given packages
     *
     * @param packageNames
     * @param clazz
     * @return instances
     */
    public static <T> List<T> getInstances(String[] packageNames, Class<T> clazz) {
        return getInstances(clazz, null, packageNames);
    }
}

