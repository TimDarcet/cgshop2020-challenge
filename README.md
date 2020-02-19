Minimum Convex Partition solver
===============================

This is a simple solver to the minimum convex partition problem. It uses a randomized heuristic with absolutely no theoretical guarantees. On the CG:SHOP 2020 challenge, it reaches a score of about 0.4.
This project was done in the context of the INF562 - algorithmic geometry course at Ecole polytechnique.

## The algorithm

The strategy is extremely simple: compute a Delaunay triangulation, then repeatedly choose an edge randomly in the list of edges, and if it is eligible for removal, remove it. The algorithm keeps doing that until no more edges are eligible for removal.

## Project structure

Directory `lib` holds the necessary libraries for running the code.
Directory `clouds` holds the input data sample (point clouds).
Directory `results` holds the output convex partitions.
Directory `class` holds the .class compiled files.
Directory `src` holds the source code for the project.

## Code structure

The code is divided into:
- The source files `Algorithms.java`, `GraphViewer.java` and `IO.java` are as provided in the handout: they contain the classes and functions for the project.
- `ConveHull.java` is an additional file to compute the convex hull of a mesh, it is only useful for the score function.
- `Makefile` is the standard driver for the project. Using  `make TARGET=\<filename\>` compiles the sources into .class files then executes the main function in GrapViewer.
- `compute_all.sh` is a small bash driver to compute partitions for all files in a folder. It is useful to compute solutions to the challenge.
- `compute_distributed.py` is an alternative python driver to compute all partitions of files in a folder, but distribute them over several computers accessible with ssh, provided they share the directory where this project is.

## Usage

To compute partitions for all files in `./clouds`:
```
cd src
./compute_all.sh
```
