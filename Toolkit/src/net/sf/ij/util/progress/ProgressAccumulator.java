/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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
package net.sf.ij.util.progress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Ultility for aggregating progress events from multiple {@link ProgressReporter}'s
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ProgressAccumulator
        extends DefaultProgressReporter
        implements ProgressListener {
    final private Map reporters = new HashMap();
    private double minimumChange = 0.01;
    private double lastReportedProgress = -1;

    public double getMinimumChange() {
        return minimumChange;
    }

    public void setMinimumChange(final double minimumChange) {
        if (minimumChange < 0 || minimumChange > 1) {
            throw new IllegalArgumentException(
                    "Argument 'minimumChange' cannot be less than 0 or more than 1 [" + minimumChange + "].");
        }
        this.minimumChange = minimumChange;
    }

    /**
     * Add progress reporter with default weight of <code>1</code>.
     * If reporter already exists its weight will be changed to <code>1</code>.
     *
     * @param reporter
     */
    public void addProgressReporter(final ProgressReporter reporter) {
        addProgressReporter(reporter, 1);
    }

    /**
     * Add progress <code>reporter</code> with given <code>weight</code>.
     * If reporter already exists its <code>weight</code> will be updated.
     *
     * @param reporter
     * @param weight
     */
    public void addProgressReporter(final ProgressReporter reporter, final double weight) {
        if (reporter == null) {
            return;
        }
        if (!reporters.containsKey(reporter)) {
            reporter.addProgressListener(this);
        }
        reporters.put(reporter, new Double(weight));
    }


    public void progressNotification(ProgressEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("Progress event argument cannot be null.");
        }

        final Object o = e.getSource();
        if (o == null) {
            throw new IllegalArgumentException("Event source cannot be null.");
        }

        if (!(o instanceof ProgressReporter)) {
            throw new RuntimeException("Received notification from reporter of unsupported type: " + o.getClass());
        }

        final ProgressReporter reporter = (ProgressReporter) o;
        if (!reporters.containsKey(reporter)) {
            throw new RuntimeException("Received notification from unregistered reporter: " + reporter);
        }

        // Sum all weight
        double weightSum = 0;
        double progressSum = 0;
        final Set entries = reporters.entrySet();
        for (final Iterator i = entries.iterator(); i.hasNext();) {
            final Map.Entry entry = (Map.Entry) i.next();
            final double weight = ((Double) entry.getValue()).doubleValue();
            weightSum += weight;
            double progress = ((ProgressReporter) entry.getKey()).getCurrentProgress();
            progressSum += progress * weight;
        }

        assert progressSum >= 0;
        assert weightSum > 0;

        final double progress = progressSum / weightSum;

        setCurrentProgress(progress);

        if ((progress - lastReportedProgress) > minimumChange) {
            lastReportedProgress = progress;
            notifyProgressListeners();
        }
    }
}
