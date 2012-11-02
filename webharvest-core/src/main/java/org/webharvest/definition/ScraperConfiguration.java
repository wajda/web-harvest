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
package org.webharvest.definition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.webharvest.ioc.ConfigSource;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.xml.sax.InputSource;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Basic configuration.
 */
public class ScraperConfiguration implements RestorableConfiguration {

    private ElementDefProxy rootElementDef;

    private ScriptingLanguage scriptingLanguage;
    private File sourceFile;
    private String url;

    /**
     * Creates configuration instance loaded from the specified input stream.
     *
     * @param in
     */
    @Inject
    public ScraperConfiguration(@ConfigSource InputSource in) {
        createFromInputStream(in);

    }

    /**
     * Creates configuration instance loaded from the specified File.
     *
     * @param sourceFile
     * @throws FileNotFoundException
     */
    @Deprecated
    public ScraperConfiguration(@Assisted File sourceFile)
            throws FileNotFoundException {
        this.sourceFile = sourceFile;
        createFromInputStream(new InputSource(new FileReader(sourceFile)));
    }

    /**
     * Creates configuration instance loaded from the file specified by filename.
     *
     * @param sourceFilePath
     */
    @Deprecated
    public ScraperConfiguration(@Assisted String sourceFilePath)
            throws FileNotFoundException {
        this(new File(sourceFilePath));
    }

    /**
     * Creates configuration instance loaded from specified URL.
     *
     * @param sourceUrl
     * @throws IOException
     */
    @Deprecated
    public ScraperConfiguration(@Assisted URL sourceUrl)
            throws IOException {
        this.url = sourceUrl.toString();
        createFromInputStream(new InputSource(
                new InputStreamReader(sourceUrl.openStream())));
    }

    private void createFromInputStream(InputSource in) {
        // loads configuration from input stream to the internal structure
        this.rootElementDef = XmlParser.parse(in);
    }

    public List<IElementDef> getOperations() {
        return Arrays.asList(rootElementDef.getOperationDefs());
    }

    public IElementDef getRootElementDef() {
        return rootElementDef;
    }

    /**
     * Returns default configuration's {@link ScriptingLanguage}.
     *
     * @return default configuration's {@link ScriptingLanguage}.
     */
    public ScriptingLanguage getScriptingLanguage() {
        return scriptingLanguage;
    }

    /**
     * Sets default configuration's {@link ScriptingLanguage}.
     *
     * @param language
     *            new default configuration's {@link ScriptingLanguage}
     */
    public void setScriptingLanguage(final ScriptingLanguage language) {
        this.scriptingLanguage = language;
    }

    public File getSourceFile() {
        return this.sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNamespaceURI() {
        return rootElementDef.getNode().getUri();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreState(final ConfigurationSnapshot state) {
        if (state == null) {
            throw new IllegalArgumentException(
                    "Snapshot of configuration must not be null.");
        }
        this.scriptingLanguage = state.getScriptingLanguage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationSnapshot captureState() {
        return new ConfigurationSnapshot(this.scriptingLanguage);
    }
}