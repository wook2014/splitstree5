# splitstree5
A complete rewrite of the SplitsTree program

This was my first JavaFX project and there are so may early design decisions that I have come to regret... 

Nevertheless the program has a number of nice features and is more flexible than SplitsTree4, so it is definitely worth using.

It contains an implementation of our new "phylogenetic outline" algorithm that represents the output of the neighbor-net algorithm using O(n^2) nodes and edges rather than O(n^4), where n is the number of taxa.

It contains an implementation of our new "phylogenetic context" code that allows one to quickly determine the phylogenetic context of a prokaryotic draft genome, using the GTDB taxonomy and database.

These two new approaches are described here:

Bagcı, Bryant, Cetinkaya and Huson, Microbial phylogenetic context using phylogenetic outlines, to appear in: Genome Biology and Evolution.

You can obtain installers for the latest version here: https://software-ab.informatik.uni-tuebingen.de/download/splitstree5
