package org.webharvest.runtime.processors.plugins.db;

import org.webharvest.exception.PluginException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * DB param plugin - can be used only inside database plugin.
 */
public class DbParamPlugin extends WebHarvestPlugin {

    public String getName() {
        return "db-param";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) throws InterruptedException {
        BaseProcessor processor = scraper.getRunningProcessorOfType(DatabasePlugin.class);
        if (processor != null) {
            DatabasePlugin databasePlugin = (DatabasePlugin) processor;
            String type = evaluateAttribute("type", scraper);
            Variable body = executeBody(scraper, context);
            if (CommonUtil.isEmptyString(type)) {
                type = "text";
                if ( body.getWrappedObject() instanceof byte[] ) {
                    type = "binary";
                } else if (body instanceof ListVariable) {
                    ListVariable list = (ListVariable) body;
                    if (list.toList().size() == 1 && list.get(0).getWrappedObject() instanceof byte[]) {
                        type = "binary";
                    }
                }
            }
            databasePlugin.addDbParam(body, type);
            return new NodeVariable("?");
        } else {
            throw new PluginException("Cannot use db-param attach plugin out of database plugin context!");
        }
    }

    public String[] getValidAttributes() {
        return new String[] {"type"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("type".equalsIgnoreCase(attributeName)) {
            return new String[] {"int", "long", "double", "text", "binary"};
        }
        return null;
    }


}