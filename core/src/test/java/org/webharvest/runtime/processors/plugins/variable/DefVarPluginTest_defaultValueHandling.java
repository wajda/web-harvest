/*
 Copyright (c) 2006-2007, Vladimir Nikic
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

package org.webharvest.runtime.processors.plugins.variable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.mock.Mock;
import org.unitils.mock.annotation.Dummy;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.NodeVariable;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.webharvest.runtime.processors.plugins.PluginTestUtils.createPlugin;

@SuppressWarnings({"unchecked"})
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class DefVarPluginTest_defaultValueHandling {

    @Dummy
    Logger logger;

    ScraperContext context = new ScraperContext();
    Mock<Scraper> scraperMock;

    @Before
    public void before() {
        scraperMock.returns(logger).getLogger();
        scraperMock.returns(context).getContext();
        scraperMock.returns(new ScriptEngineFactory(ScriptingLanguage.GROOVY)).getScriptEngineFactory();
    }

    @Test
    public void testExecutePlugin_default_notExistingValue() throws Exception {
        createPlugin(
                "<def var='x' value='${notExistingVar}' default='zzz'/>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("zzz"), context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_default_emptyValue() throws Exception {
        context.setLocalVar("empty", EmptyVariable.INSTANCE);
        context.setLocalVar("zzz", new NodeVariable("default value"));
        createPlugin(
                "<def var='x' value='${empty}' default='${zzz}'/>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("default value"), context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_default_emptyValueAttribute() throws Exception {
        context.setLocalVar("zzz", new NodeVariable("default value"));
        createPlugin(
                "<def var='x' value='' default='${zzz}'/>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("default value"), context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_default_syntacticSugar_varNotEmpty() throws Exception {
        context.setLocalVar("zzz", new NodeVariable("default value"));
        context.setLocalVar("x", new NodeVariable("some value"));
        createPlugin(
                "<def var='x' default='${zzz}'/>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("some value"), context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_default_syntacticSugar_varIsEmpty() throws Exception {
        context.setLocalVar("zzz", new NodeVariable("default value"));
        context.setLocalVar("x", EmptyVariable.INSTANCE);
        createPlugin(
                "<def var='x' default='${zzz}'/>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("default value"), context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_default_syntacticSugar_varIsUndefined() throws Exception {
        context.setLocalVar("zzz", new NodeVariable("default value"));
        createPlugin(
                "<def var='x' default='${zzz}'/>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("default value"), context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_default_syntacticSugar_bodyNotEmpty() throws Exception {
        context.setLocalVar("zzz", new NodeVariable("default value"));
        createPlugin(
                "<def var='x' default='${zzz}'>body</def>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("body"), context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_default_syntacticSugar_bodyIsEmpty() throws Exception {
        context.setLocalVar("zzz", new NodeVariable("default value"));
        createPlugin(
                "<def var='x' default='${zzz}'><empty/></def>",
                DefVarPlugin.class).executePlugin(scraperMock.getMock(), context);
        assertReflectionEquals(new NodeVariable("default value"), context.getVar("x"));
    }

}
