/*
 Copyright (c) 2006-2012 the original author or authors.

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
*/

package org.webharvest.events;

import org.webharvest.runtime.Scraper;

/**
 * Event informing that during the execution of {@link Scraper} some exception
 * has occurred. Event holds a reference to the instance of {@link Scraper}
 * which execution caused exception and reference to the exception raised.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class ScraperExecutionErrorEvent {

    private final Scraper scraper;

    private final Exception exception;

    /**
     * Event constructor accepting reference to {@link Scraper} being executed
     * and {@link Exception} which occurred during the execution.
     *
     * @param scraper
     *            reference to {@link Scraper} being executed
     * @param exception
     *            reference to the {@link Exception} occurred
     */
    public ScraperExecutionErrorEvent(final Scraper scraper,
            final Exception exception) {
        this.scraper = scraper;
        this.exception = exception;
    }

    /**
     * Returns instance of {@link Scraper} which execution caused exception.
     *
     * @return reference to {@link Scraper}
     */
    public Scraper getScraper() {
        return this.scraper;
    }

    /**
     * Return reference to the exception occurred.
     *
     * @return exception occurred
     */
    public Exception getException() {
        return this.exception;
    }
}