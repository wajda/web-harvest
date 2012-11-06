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
package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.Config;
import org.webharvest.definition.ConfigFactory;
import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.FileConfigSource;
import org.webharvest.definition.IncludeDef;
import org.webharvest.definition.URLConfigSource;
import org.webharvest.exception.FileException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.NestedContextFactory;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import com.google.inject.Inject;

/**
 * Include processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "include", validAttributes = { "id", "path" },
        requiredAttributes = "path", definitionClass = IncludeDef.class)
public class IncludeProcessor extends AbstractProcessor<IncludeDef> {

    @Inject
    private ConfigFactory configFactory;

    public Variable execute(DynamicScopeContext context) throws InterruptedException {
        boolean isUrl = false;

        String path = BaseTemplater.evaluateToString(elementDef.getPath(), null, context);

        this.setProperty("Path", path);

        path = CommonUtil.adaptFilename(path);
        String fullPath = path;

        File originalFile = context.getSourceFile();
        String originalUrl = context.getUrl();
        if (originalFile != null) {
            String originalPath = CommonUtil.adaptFilename(originalFile.getAbsolutePath());
            int index = originalPath.lastIndexOf('/');
            if (index > 0) {
                String workingPath = originalPath.substring(0, index);
                fullPath = CommonUtil.getAbsoluteFilename(workingPath, path);
            }
        } else if (originalUrl != null) {
            fullPath = CommonUtil.fullUrl(originalUrl, path);
            isUrl = true;
        }

        try {
            // TODO rbala Use factory with polymorfic methods!
            final ConfigSource source = isUrl ? new URLConfigSource(new URL(fullPath)) : new FileConfigSource(new File(fullPath));

            final Config config = configFactory.create(source);
            config.reload();

            ProcessorResolver.createProcessor(config.getElementDef()).run(
                            NestedContextFactory.create(context));

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            return EmptyVariable.INSTANCE;
        } catch (IOException e) {
            throw new FileException("Cannot include configuration file " + fullPath, e);
        }
    }

}