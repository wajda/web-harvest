package org.webharvest.definition;

import java.util.Map;

import org.webharvest.exception.PluginException;
import org.webharvest.ioc.InjectorHelper;
import org.webharvest.runtime.processors.AbstractProcessor;
import org.webharvest.runtime.processors.Processor;

/**
 * Definition of all plugin processors.
 */
public class WebHarvestPluginDef extends AbstractElementDef {


    private Class<? extends AbstractProcessor> pluginClass;

    public WebHarvestPluginDef(final XmlNode xmlNode,
            Class<? extends AbstractProcessor> pluginClass) {
        super(xmlNode);
        this.pluginClass = pluginClass;
    }

    public String getUri() {
        return xmlNode.getUri();
    }

    public Map<String, String> getAttributes() {
        return getAttributes(xmlNode.getUri());
    }

    public Map<String, String> getAttributes(String uri) {
        return xmlNode.getAttributes(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Processor createPlugin() {
        if (pluginClass != null) {
            try {
                AbstractProcessor plugin = pluginClass.newInstance();
                plugin.setElementDef(this);

                //FIXME: This a temporary solution which is not neat, but
                //helps with plugin's dependency injection (using Guice).
                InjectorHelper.getInjector().injectMembers(plugin);

                return plugin;
            } catch (InstantiationException e) {
                throw new PluginException(e);
            } catch (IllegalAccessException e) {
                throw new PluginException(e);
            }
        }

        throw new PluginException("Cannot create plugin!");
    }

}