/*
 * IJ-Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.clustering;

/**
 * Configurable parameters of the k-means algorithm.
 */
public final class KMeansConfig implements Cloneable {

    /**
     * Seed used to initialize random number generator.
     */
    private int randomizationSeed = 48;
    private boolean randomizationSeedEnabled = true;
    private double tolerance = 0.0001;
    private int numberOfClusters = 4;
    private boolean clusterAnimationEnabled;
    private boolean printTraceEnabled;


    public int getRandomizationSeed() {
        return randomizationSeed;
    }


    public void setRandomizationSeed(final int randomizationSeed) {
        this.randomizationSeed = randomizationSeed;
    }


    /**
     * If <code>true</code>, random number generator will be initialized with a
     * <code>randomizationSeed</code>. If <code>false</code> random number generator will be
     * initialized using 'current' time.
     *
     * @return {@code true} when randomization seed is enabled.
     * @see #getRandomizationSeed()
     */
    public boolean isRandomizationSeedEnabled() {
        return randomizationSeedEnabled;
    }


    public void setRandomizationSeedEnabled(final boolean randomizationSeedEnabled) {
        this.randomizationSeedEnabled = randomizationSeedEnabled;
    }


    public int getNumberOfClusters() {
        return numberOfClusters;
    }


    public void setNumberOfClusters(final int numberOfClusters) {
        this.numberOfClusters = numberOfClusters;
    }


    /**
     * Return tolerance used to determine cluster centroid distance. This tolerance is used to
     * determine if a centroid changed location between iterations.
     *
     * @return cluster centroid location tolerance.
     */
    public double getTolerance() {
        return tolerance;
    }


    public void setTolerance(final float tolerance) {
        this.tolerance = tolerance;
    }


    /**
     * Return <code>true</code> if when an animation illustrating cluster optimization is
     * enabled.
     *
     * @return {@code true} when cluster animation is enabled.
     */
    public boolean isClusterAnimationEnabled() {
        return clusterAnimationEnabled;
    }


    public void setClusterAnimationEnabled(final boolean clusterAnimationEnabled) {
        this.clusterAnimationEnabled = clusterAnimationEnabled;
    }


    /**
     * Return <code>true</code> if a trace is printed to the ImageJ's Result window.
     *
     * @return {@code true} when printing of trace is enabled.
     */
    public boolean isPrintTraceEnabled() {
        return printTraceEnabled;
    }


    public void setPrintTraceEnabled(final boolean printTraceEnabled) {
        this.printTraceEnabled = printTraceEnabled;
    }


    /**
     * Make duplicate of this object. This a convenience wrapper for {@link #clone()} method.
     *
     * @return duplicate of this object.
     */
    public KMeansConfig duplicate() {
        try {
            return (KMeansConfig) this.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning object of class " + getClass().getName() + ".", e);
        }
    }
}
