/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
package net.sf.ij_plugins.quilting;


public class MinPathFinder {
    /**
     * costs[i][j] gives the cumulative cost of going from spot i,j to the destination.
     */
    private final double[][] costs;

    /**
     * destinations[i][j] gives the spot to go next from spot i,j when heading toward the
     * destination.
     */
    private final TwoDLoc[][] path;

    /**
     * This sets up a path finder to find the min path from last row to the first row given the cost
     * of being at each position.
     *
     * @param dists           This gives the cost of each position where dists[0] is the row of
     *                        destination locations and dists[dists.length-1] is the row of starting
     *                        positions. This must be a rectangular array of non-zero dimensions.
     * @param allowHorizontal This says whether or not to allow the path to follow along a row if it
     *                        wants to.
     */
    public MinPathFinder(final double[][] dists, final boolean allowHorizontal) {

        final int rowcnt = dists.length, colcnt = dists[0].length;
        path = new TwoDLoc[rowcnt][colcnt];
        costs = new double[rowcnt][colcnt];

        // set up the destination row
        for (int c = 0; c < colcnt; c++) {
            costs[0][c] = dists[0][c];
            path[0][c] = null;
        }

        // finish up if the path is already determined
        if (colcnt == 1) {

            for (int r = 1; r < rowcnt; r++) {
                costs[r][0] = costs[r - 1][0] + dists[r][0];
                path[r][0] = new TwoDLoc(r - 1, 0);
            }
            return;
        }

        // loop over the rows, getting progressively closer to the source
        // each iteration calculates stuff for row r+1 based on row r
        for (int r = 0; r < rowcnt - 1; r++) {

            // handle the left column
            int choice = (costs[r][0] < costs[r][1] ? 0 : 1);
            costs[r + 1][0] = dists[r + 1][0] + costs[r][choice];
            path[r + 1][0] = new TwoDLoc(r, choice);

            // handle the middle columns
            for (int c = 1; c < colcnt - 1; c++) {

                choice = (costs[r][c - 1] < costs[r][c] ? c - 1 : c);
                choice = (costs[r][c + 1] < costs[r][choice] ? c + 1 : choice);
                costs[r + 1][c] = dists[r + 1][c] + costs[r][choice];
                path[r + 1][c] = new TwoDLoc(r, choice);
            }

            // handle the right column
            final int c = colcnt - 1;
            choice = (costs[r][c] < costs[r][c - 1] ? c : c - 1);
            costs[r + 1][c] = dists[r + 1][c] + costs[r][choice];
            path[r + 1][c] = new TwoDLoc(r, choice);

            // check for horizontal movement along a row
            if (allowHorizontal) {
                handleHorizontalMovement(dists[r + 1], r + 1);
            }

        } // end row loop
    }

    /**
     * Given a row and column number, this says where to go next to head toward the destination.
     */
    public TwoDLoc follow(final TwoDLoc currentLoc) {
        return path[currentLoc.getRow()][currentLoc.getCol()];
    }

    /**
     * This returns the loc of the best source column.
     */
    public TwoDLoc bestSourceLoc() {
        int best = 0;
        for (int i = 1; i < costs[0].length; i++) {
            if (costs[costs.length - 1][i] < costs[costs.length - 1][best]) {
                best = i;
            }
        }
        return new TwoDLoc(costs.length - 1, best);
    }

    public double costOf(final int row, final int col) {
        return costs[row][col];
    }

    public TwoDLoc[][] getPaths() {
        return path;
    }

    public double[][] getCosts() {
        return costs;
    }

    /**
     * This updates costs and path for the given row such that the path can travel horizontally
     * along the row if it improves the costs of the paths.
     */
    private void handleHorizontalMovement(final double[] dists, final int row) {

        boolean changed;

        do {
            changed = false;

            // handle the left spot
            int c = 0;
            int choice = c + 1;
            double newcost = costs[row][choice] + dists[c];
            if (costs[row][c] > newcost) {
                changed = true;
                costs[row][c] = newcost;
                path[row][c] = new TwoDLoc(row, choice);
            }

            // handle the middle spots
            for (c = 1; c < dists.length - 1; c++) {

                choice = (costs[row][c - 1] > costs[row][c + 1] ? c + 1 : c - 1);
                newcost = costs[row][choice] + dists[c];
                if (costs[row][c] > newcost) {
                    changed = true;
                    costs[row][c] = newcost;
                    path[row][c] = new TwoDLoc(row, choice);
                }
            }

            // handle the right column
            c = dists.length - 1;
            choice = c - 1;
            newcost = costs[row][choice] + dists[c];
            if (costs[row][c] > newcost) {
                changed = true;
                costs[row][c] = newcost;
                path[row][c] = new TwoDLoc(row, choice);

            }

        } while (changed);
    }
}
