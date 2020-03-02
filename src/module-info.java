module splitstreefive {
    requires transitive jloda;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive java.sql;
    requires transitive java.desktop;

    requires junit;
    requires Jama;
    requires richtextfx.fat;
    requires fx.platform.utils;
    requires com.install4j.runtime;

    exports splitstree5.resources.css;
    exports splitstree5.resources.icons;
    exports splitstree5.resources.images;

    opens splitstree5.resources.css;
    opens splitstree5.resources.icons;
    opens splitstree5.resources.images;

    exports splitstree5.main;
    exports splitstree5.tools;
    exports splitstree5.utils;
    exports splitstree5.xtra;
    exports splitstree5.xtra.align;

    opens splitstree5.core.algorithms;
    opens splitstree5.core.algorithms.genomes2distances;
    opens splitstree5.core.algorithms.characters2distances;
    opens splitstree5.core.algorithms.characters2network;
    opens splitstree5.core.algorithms.characters2splits;
    opens splitstree5.core.algorithms.distances2network;
    opens splitstree5.core.algorithms.distances2splits;
    opens splitstree5.core.algorithms.distances2trees;
    opens splitstree5.core.algorithms.filters;
    opens splitstree5.core.algorithms.interfaces;
    opens splitstree5.core.algorithms.splits2splits;
    opens splitstree5.core.algorithms.splits2trees;
    opens splitstree5.core.algorithms.trees2distances;
    opens splitstree5.core.algorithms.trees2splits;
    opens splitstree5.core.algorithms.trees2trees;
    opens splitstree5.core.algorithms.views;

    exports splitstree5.core.datablocks;
    exports splitstree5.core.misc;

    opens splitstree5.dialogs.exporter;
    opens splitstree5.dialogs.importer;
    opens splitstree5.dialogs.message;
    opens splitstree5.dialogs.importgenomes;


    opens splitstree5.gui.algorithmtab;
    opens splitstree5.gui.algorithmtab.rootbyoutgroup;
    opens splitstree5.gui.algorithmtab.taxafilterview;
    opens splitstree5.gui.algorithmtab.treefilterview;
    opens splitstree5.gui.algorithmtab.treeselector;
    opens splitstree5.gui.formattab;
    opens splitstree5.gui.formattab.fontselector;
    opens splitstree5.gui.graphlabels;

    opens splitstree5.gui.workflowtab;
    exports splitstree5.gui.workflowtab;

    opens splitstree5.main;
    opens splitstree5.menu;
    opens splitstree5.toolbar;
    opens splitstree5.xtra.align;

    opens splitstree5.io.imports;
    opens splitstree5.io.imports.interfaces;
    opens splitstree5.io.imports.nexus;
    opens splitstree5.io.imports.NeXML;
    opens splitstree5.io.exports;
    opens splitstree5.io.exports.interfaces;

    exports splitstree5.gui.utils;
    exports splitstree5.io.nexus;
    exports splitstree5.treebased;

}