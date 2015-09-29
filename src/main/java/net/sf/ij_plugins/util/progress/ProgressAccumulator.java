/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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

package net.sf.ij_plugins.util.progress;

import net.sf.ij_plugins.util.Validate;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for aggregating progress events from multiple {@link ProgressReporter}'s
 *
 * @author Jarek Sacha
 */
public class ProgressAccumulator
        extends DefaultProgressReporter
        implements ProgressListener {


    private static class Data {
        double weight;
        String message;

        public Data(final double weight, final String message) {
            this.weight = weight;
            this.message = message;
        }
    }


    private final Map<ProgressReporter, Data> reporters = new HashMap<>();
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
     * Add progress reporter with default weight of <code>1</code>. If reporter already exists its
     * weight will be changed to <code>1</code>.
     *
     * @param reporter reporter
     */
    public void addProgressReporter(final ProgressReporter reporter) {
        addProgressReporter(reporter, 1);
    }


    /**
     * Add progress <code>reporter</code> with given <code>weight</code>. If reporter already exists
     * its <code>weight</code> and <code>message</code> will be updated.
     *
     * @param reporter reporter
     * @param weight   weight
     */
    public void addProgressReporter(final ProgressReporter reporter, final double weight) {
        addProgressReporter(reporter, weight, null);
    }


    /**
     * Add progress <code>reporter</code> with given <code>weight</code>. If reporter already exists
     * its <code>weight</code> and <code>message</code> will be updated.
     *
     * @param reporter reporter
     * @param weight   weight
     * @param message  message that will be reported when this reporter send progress event. If
     *                 <code>null</code> the original message send by reporter will be used.
     */
    public void addProgressReporter(final ProgressReporter reporter, final double weight, final String message) {
        if (reporter == null) {
            return;
        }
        if (!reporters.containsKey(reporter)) {
            reporter.addProgressListener(this);
        }
        reporters.put(reporter, new Data(weight, message));
    }


    public void removeProgressReporter(final ProgressReporter reporter) {
        final ProgressReporter r = (ProgressReporter) reporters.remove(reporter);
        if (r != null) {
            r.removeProgressListener(this);
        }
    }

    public void removeAllProgressReporter() {
        for (final ProgressReporter progressReporter : reporters.keySet()) {
            progressReporter.removeProgressListener(this);
        }
        reporters.clear();
    }


    @Override
    public void progressNotification(final ProgressEvent event) {
        Validate.argumentNotNull(event, "event");

        final Object o = event.getSource();
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
        for (final Map.Entry<ProgressReporter, Data> entry : reporters.entrySet()) {
            final Data data = entry.getValue();
            weightSum += data.weight;
            final double progress = entry.getKey().currentProgress();
            progressSum += progress * data.weight;
        }

        assert progressSum >= 0;
        assert weightSum > 0;

        final double progress = progressSum / weightSum;
        final Data data = reporters.get(reporter);

        final String message = data.message != null
                ? data.message
                : event.getMessage();

        if ((progress - lastReportedProgress) > minimumChange) {
            lastReportedProgress = progress;
            notifyProgressListeners(progress, message);
        } else {
            setCurrentProgress(progress);
        }
    }
}
