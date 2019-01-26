/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.utils;

import jloda.util.Basic;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * gets all the options of an optionable class
 * Daniel Huson, 1/8/17.
 */
public class OptionsAccessor {
    /**
     * gets all options associated with an optionable.
     * An option is given by a getOption/setOptionValue pair of methods
     *
     * @param optionable
     * @return options
     */
    public static ArrayList<Option> getAllOptions(IOptionable optionable) {
        final Map<String, AnOption> name2AnOption = new HashMap<>();

        Method listMethod = null;

        final ArrayList<Option> options = new ArrayList<>();
        for (Method method : optionable.getClass().getMethods()) {
            String name = method.getName();
            if ((name.startsWith("getOption") || name.startsWith("isOption")) && method.getParameterCount() == 0) {
                name = (name.startsWith("getOption") ? name.substring("getOption".length()) : name.substring("isOption".length()));
                if (name.length() > 0) {
                    final AnOption anOption = name2AnOption.computeIfAbsent(name, k -> new AnOption());
                    anOption.getMethod = (o) -> {
                        try {
                            return method.invoke(o);
                        } catch (Exception e) {
                            Basic.caught(e);
                            return null;
                        }
                    };
                    anOption.returnType = method.getReturnType();
                }
            } else if (name.startsWith("setOption") && method.getParameterCount() == 1) {
                name = name.substring("setOption".length());
                if (name.length() > 0) {
                    final AnOption anOption = name2AnOption.computeIfAbsent(name, k -> new AnOption());
                    anOption.setMethod = (o, v) -> {
                        try {
                            method.invoke(o, v);
                        } catch (Exception e) {
                            Basic.caught(e);
                        }
                    };
                }
            } else if (name.startsWith("getShortDescription") && method.getParameterCount() == 0) {
                name = name.substring("getShortDescription".length());
                if (name.length() > 0) {
                    final AnOption anOption = name2AnOption.computeIfAbsent(name, k -> new AnOption());
                    anOption.infoMethod = (o) -> {
                        try {
                            return method.invoke(o).toString();
                        } catch (Exception e) {
                            Basic.caught(e);
                            return null;
                        }
                    };

                }
            } else if (name.startsWith("getLegalValues") && method.getParameterCount() == 0) {
                name = name.substring("getLegalValues".length());
                if (name.length() > 0) {
                    final AnOption anOption = name2AnOption.computeIfAbsent(name, k -> new AnOption());
                    anOption.legalValuesMethod = (o) -> {
                        try {
                            return (String[]) method.invoke(o);
                        } catch (Exception e) {
                            Basic.caught(e);
                            return null;
                        }
                    };
                }
            } else if (name.equals("listOptions") && method.getParameterCount() == 0)
                listMethod = method;
        }

        // determine the order in which to return options
        final Collection<String> order;
        if (listMethod == null) {
            order = name2AnOption.keySet();
        } else {
            order = new ArrayList<>(name2AnOption.size());
            try {
                final Set<String> set = new HashSet<>();
                final List list = (List) listMethod.invoke(optionable);
                if (list != null) {
                    for (Object a : list) {
                        String name = a.toString();
                        if (name.startsWith("option"))
                            name = name.replaceAll("^option", "");
                        order.add(name);
                        set.add(name);
                    }
                }
                // add other parameters not mentioned in the order
                for (String name : name2AnOption.keySet()) {
                    if (!set.contains(name))
                        order.add(name);
                }

            } catch (Exception e) {
                order.clear();
                order.addAll(name2AnOption.keySet());
            }
        }

        for (String name : order) {
            if (name2AnOption.keySet().contains(name)) {
                AnOption anOption = name2AnOption.get(name);
                if (anOption.getMethod != null && anOption.setMethod != null && anOption.returnType != null) {
                    if (!Arrays.asList("int", "float", "double", "boolean").contains(anOption.returnType.getTypeName())) {
                        try {
                            final Object returnValue = anOption.getMethod.invoke(optionable);
                            if (returnValue.getClass().isEnum()) {
                                final Object[] constants = returnValue.getClass().getEnumConstants();

                                final Option.IGetMethod<IOptionable, String[]> getLegalValues = (o) -> {
                                    final String[] values = new String[constants.length];
                                    for (int i = 0; i < constants.length; i++)
                                        values[i] = constants[i].toString();
                                    return values;
                                };

                                final Option.IGetMethod<IOptionable, Object> theGetMethod = (o) -> {
                                    try {
                                        return anOption.getMethod.invoke(o);
                                    } catch (Exception e) {
                                        Basic.caught(e);
                                        return null;
                                    }
                                };

                                final Option.ISetMethod<IOptionable, Object> theSetMethod = (o, v) -> {
                                    try {
                                        for (Object constant : constants) {
                                            if (constant.toString().equals(v.toString())) {
                                                anOption.setMethod.invoke(o, constant);
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        Basic.caught(e);
                                    }
                                };

                                options.add(new Option(name, optionable, theGetMethod, theSetMethod, anOption.infoMethod, getLegalValues, String.class));
                            }
                        } catch (Exception e) {
                            Basic.caught(e);
                        }
                    } else
                        options.add(new Option(name, optionable, anOption.getMethod, anOption.setMethod, anOption.infoMethod, anOption.legalValuesMethod, anOption.returnType));
                }
            }
        }
        return options;
    }

    /**
     * get updated options values
     *
     * @param options
     */
    public static void getUpdatedOptionValues(List<Option> options) {
        for (Option option : options) {
            option.getValue();
        }
    }

    /**
     * set an option from a string value
     *
     * @param options
     * @param name
     * @param value
     */
    public static void setOptionValue(List<Option> options, String name, String value) {
        for (Option option : options) {
            if (option.getName().equals(name)) {
                switch (option.getType().getTypeName()) {
                    case "boolean":
                        option.setValue(Boolean.parseBoolean(value));
                        break;
                    case "int":
                        option.setValue(Integer.parseInt(value));
                        break;
                    case "double":
                        option.setValue(Double.parseDouble(value));
                        break;
                    case "float":
                        option.setValue(Float.parseFloat(value));
                        break;
                    case "java.lang.String":
                        option.setValue(value);
                        break;
                    default:
                        System.err.println("ERROR: can't set value of type: " + option.getType());
                }
                return;
            }
        }
    }

    /**
     * use this to store options in list until ready to process them
     */
    private static class AnOption {
        Option.IGetMethod<IOptionable, Object> getMethod;
        Option.ISetMethod<IOptionable, Object> setMethod;
        Option.IGetMethod<IOptionable, String[]> legalValuesMethod;
        Option.IGetMethod<IOptionable, String> infoMethod;
        Type returnType;
    }
}
