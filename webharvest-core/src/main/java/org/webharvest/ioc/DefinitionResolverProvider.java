package org.webharvest.ioc;

import org.webharvest.definition.AnnotatedPluginsPostProcessor;
import org.webharvest.definition.ConfigurableResolver;
import org.webharvest.definition.DefinitionResolver;
import org.webharvest.definition.ResolverPostProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.List;

/**
 * Created by rba on 22.07.2017.
 */
// TODO rbala Missing javadoc
// TODO rbala Missing unit tests
public class DefinitionResolverProvider implements Provider<ConfigurableResolver> {

    private final List<? extends ResolverPostProcessor> postProcessors;

    @Inject
    public DefinitionResolverProvider(@Named("resolverPostProcessors") List<? extends ResolverPostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurableResolver get() {
        ConfigurableResolver resolver = new DefinitionResolver();
        for (ResolverPostProcessor postProcessor : postProcessors) {
            resolver.addPostProcessor(postProcessor);
        }
        resolver.refresh();

        return resolver;
    }

}
