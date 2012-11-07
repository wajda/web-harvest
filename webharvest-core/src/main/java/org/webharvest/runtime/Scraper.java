/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.events.ScraperExecutionContinuedEvent;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.events.ScraperExecutionExitEvent;
import org.webharvest.events.ScraperExecutionPausedEvent;
import org.webharvest.events.ScraperExecutionStartEvent;
import org.webharvest.events.ScraperExecutionStoppedEvent;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.processors.ProcessorResolver;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Basic runtime class.
 */
public class Scraper implements WebScraper {

    private static final Logger LOG = LoggerFactory.getLogger(Scraper.class);

    @Inject
    private EventBus eventBus;

    private DynamicScopeContext context;

    private volatile ScraperState status = ScraperState.READY;
    private String message = null;

    public void execute(final DynamicScopeContext context) {
        long startTime = System.currentTimeMillis();

        this.setStatus(ScraperState.RUNNING);

        // inform all listeners that execution is just about to start
        eventBus.post(new ScraperExecutionStartEvent(this));

        try {
            final Processor processor = ProcessorResolver.createProcessor(
                    context.getConfig().getElementDef());
            if (processor != null) {
                processor.run(context);
            }
        } catch (InterruptedException e) {
            setStatus(ScraperState.STOPPED);
            Thread.currentThread().interrupt();
        }

        if (this.status == ScraperState.RUNNING) {
            this.setStatus(ScraperState.FINISHED);
        }

        // TODO rbala Remove along with deprecated method getContext()
        this.context = context;

        // inform all listeners that execution is finished
        eventBus.post(new ScraperExecutionEndEvent(this));

        if (LOG.isInfoEnabled()) {
            if (this.status == ScraperState.FINISHED) {
                LOG.info("Configuration executed in {} ms.",
                        (System.currentTimeMillis() - startTime));
            } else if (this.status == ScraperState.STOPPED) {
                LOG.info("Configuration stopped!");
            }
        }
    }

    public DynamicScopeContext getContext() {
        return context;
    }

    public synchronized ScraperState getStatus() {
        return status;
    }

    private synchronized void setStatus(ScraperState status) {
        this.status = status;
    }

    @Subscribe
    public void stopExecution(final ScraperExecutionStoppedEvent event) {
        setStatus(ScraperState.STOPPED);
    }

    @Subscribe
    public void exitExecution(final ScraperExecutionExitEvent event) {
        setStatus(ScraperState.EXIT);
        this.message = event.getMessage();
    }

    public String getMessage() {
        return message;
    }

    @Subscribe
    public void pauseExecution(final ScraperExecutionPausedEvent event) {
        if (this.status == ScraperState.RUNNING) {
            setStatus(ScraperState.PAUSED);

            // inform al listeners that execution is paused
           // eventBus.post(new ScraperExecutionPausedEvent(this));
        }
    }

    @Subscribe
    public void continueExecution(final ScraperExecutionContinuedEvent event) {
        if (this.status == ScraperState.PAUSED) {
            setStatus(ScraperState.RUNNING);

            // inform al listeners that execution is continued
            // eventBus.post(new ScraperExecutionContinuedEvent(this));
        }
    }

    /**
     * Inform all scraper listeners that an error has occured during scraper execution.
     */
    public void informListenersAboutError(Exception e) {
        setStatus(ScraperState.ERROR);

        // inform al listeners that execution is continued
        eventBus.post(new ScraperExecutionErrorEvent(this, e));
    }

}
