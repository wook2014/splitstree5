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

package splitstree5.utils;

import javafx.scene.layout.Pane;
import jloda.util.Basic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * extract all properties in an Optionable class by reflection
 * Created by huson on 12/31/16.
 */
public class Option {

    private final String name;
    private final Optionable optionable;
    private final Method infoMethod;
    private final Method getMethod;
    private final Method setMethod;
    private final Method legalValues;
    private final Type type;
    private Object newValue;

    /**
     * constructors an option
     *  @param name
     * @param optionable
     * @param getMethod
     * @param setMethod
     */
    private Option(String name, Optionable optionable, Method getMethod, Method setMethod, Method infoMethod, Method legalValuesMethod, Type type) {
        this.name = name;
        this.optionable = optionable;
        this.infoMethod = infoMethod;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
        this.type = type;
        this.legalValues = legalValuesMethod;
    }

    /**
     * sets the name of the option
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * sets the description of the option
     *
     * @return
     */
    public String getInfo() {
        try {
            return (infoMethod == null ? null : infoMethod.invoke(optionable).toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get list of all legal values, if the values are constrained
     *
     * @return legal values (as strings) or null
     */
    public String[] getLegalValues() {
        try {
            return (legalValues == null ? null : (String[]) legalValues.invoke(optionable));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * gets the value of an option
     *
     * @return value
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object getValue() throws InvocationTargetException, IllegalAccessException {
        return getMethod.invoke(optionable);
    }

    /**
     * sets a new value for the option, but does not write it back. Need to call setValue() for this value to be set
     *
     * @param newValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void holdValue(Object newValue) {
        this.newValue = newValue;
    }

    /**
     * writes back a new value, if it was set
     *
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void setValue() throws InvocationTargetException, IllegalAccessException {
        if (newValue != null)
            setMethod.invoke(optionable, newValue);
    }

    /**
     * writes back the given value
     *
     * @param newValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void setValue(Object newValue) throws InvocationTargetException, IllegalAccessException {
        holdValue(newValue);
        setValue();
    }

    /**
     * gets the type of this option
     *
     * @return type
     */
    public Type getType() {
        return type;
    }

    public String toString() {
        try {
            return "name=" + name + " info=" + infoMethod + " getMethod=" + getMethod + " setMethod=" + setMethod + " type=" + type.getTypeName() + " value=" + getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets all options associated with an optionable.
     * An option is given by a getOption/setOption pair of methods
     *
     * @param optionable
     * @return options
     */
    public static ArrayList<Option> getAllOptions(Optionable optionable) {
        final Map<String, AnOption> name2AnOption = new HashMap<>();

        Method listMethod=null;

        final ArrayList<Option> options = new ArrayList<>();
        for (Method method : optionable.getClass().getMethods()) {
            String name = method.getName();
            if (name.startsWith("getOption") && method.getParameterCount() == 0) {
                name = name.substring("getOption".length());
                if (name.length() > 0) {
                    AnOption anOption = name2AnOption.get(name);
                    if (anOption == null) {
                        anOption = new AnOption();
                        name2AnOption.put(name, anOption);
                    }
                    anOption.getMethod = method;
                    anOption.returnType = method.getReturnType();
                }
            } else if (name.startsWith("isOption") && method.getParameterCount() == 0) {
                name = name.substring("isOption".length());
                if (name.length() > 0) {
                    AnOption anOption = name2AnOption.get(name);
                    if (anOption == null) {
                        anOption = new AnOption();
                        name2AnOption.put(name, anOption);
                    }
                    anOption.setMethod = method;
                    anOption.returnType = method.getReturnType();
                }
            } else if (name.startsWith("setOption") && method.getParameterCount() == 1) {
                name = name.substring("setOption".length());
                if (name.length() > 0) {
                    AnOption anOption = name2AnOption.get(name);
                    if (anOption == null) {
                        anOption = new AnOption();
                        name2AnOption.put(name, anOption);
                    }
                    anOption.setMethod = method;
                }
            } else if (name.startsWith("getShortDescription") && method.getParameterCount() == 0) {
                name = name.substring("getShortDescription".length());
                if (name.length() > 0) {
                    AnOption anOption = name2AnOption.get(name);
                    if (anOption == null) {
                        anOption = new AnOption();
                        name2AnOption.put(name, anOption);
                    }
                    try {
                        anOption.infoMethod = method;
                    } catch (Exception e) {
                        Basic.caught(e);
                    }
                }
            } else if (name.startsWith("getLegalValues") && method.getParameterCount() == 0) {
                name = name.substring("getLegalValues".length());
                if (name.length() > 0) {
                    AnOption anOption = name2AnOption.get(name);
                    if (anOption == null) {
                        anOption = new AnOption();
                        name2AnOption.put(name, anOption);
                    }
                    anOption.legalValuesMethod = method;
                }
            } else if (name.equals("listOptions") && method.getParameterCount() == 0)
                listMethod =method;
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
                for (Object a : list) {
                    String name = a.toString();
                    if (name.startsWith("option"))
                        name = name.replaceAll("^option", "");
                    order.add(name);
                    set.add(name);
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
                                try {
                                    final Object[] constants = returnValue.getClass().getEnumConstants();
                                    final Optionable optionable2 = new Optionable() {
                                        @Override
                                        public String getName() {
                                            return optionable.getName();
                                        }

                                        @Override
                                        public void setName(String name) {
                                            optionable.setName(name);
                                        }

                                        @Override
                                        public String getShortDescription() {
                                            return optionable.getShortDescription();
                                        }

                                        @Override
                                        public void setShortDescription(String shortDescription) {
                                            optionable.setShortDescription(shortDescription);
                                        }

                                        @Override
                                        public List<String> listOptions() {
                                            return optionable.listOptions();
                                        }

                                        @Override
                                        public Pane getPane() {
                                            return optionable.getPane();
                                        }

                                        public Object getOption() {
                                            try {
                                                return anOption.getMethod.invoke(optionable).toString();
                                            } catch (Exception e) {
                                                Basic.caught(e);
                                                return null;
                                            }
                                        }

                                        public void setOption(String param) {
                                            try {
                                                Object constant = null;
                                                for (Object c : constants) {
                                                    if (c.toString().equals(param)) {
                                                        constant = c;
                                                        break;
                                                    }
                                                }
                                                if (constant != null)
                                                    anOption.setMethod.invoke(optionable, constant);
                                            } catch (Exception e) {
                                                Basic.caught(e);
                                            }
                                        }

                                        public String[] getLegalValues() {
                                            final String[] values = new String[constants.length];
                                            for (int i = 0; i < constants.length; i++)
                                                values[i] = constants[i].toString();
                                            return values;
                                        }
                                    };

                                    final AnOption anOption2 = new AnOption();
                                    anOption2.getMethod = optionable2.getClass().getMethod("getOption");
                                    anOption2.setMethod = optionable2.getClass().getMethod("setOption", String.class);
                                    anOption2.legalValuesMethod = optionable2.getClass().getMethod("getLegalValues");
                                    anOption2.returnType = String.class;
                                    options.add(new Option(name, optionable2, anOption2.getMethod, anOption2.setMethod, anOption2.infoMethod, anOption2.legalValuesMethod, anOption2.returnType));
                                } catch (NoSuchMethodException e) {
                                    Basic.caught(e);
                                }
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

    private static class AnOption {
        Method getMethod;
        Method setMethod;
        Method legalValuesMethod;
        Method infoMethod;
        Type returnType;
    }
}
