package org.webharvest.ioc;

import org.webharvest.definition.*;

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

    private final Provider<ElementsRegistry> registryProvider;

    private final List<? extends ResolverPostProcessor> postProcessors;

    @Inject
    public DefinitionResolverProvider(@Named("resolverPostProcessors") List<? extends ResolverPostProcessor> postProcessors, Provider<ElementsRegistry> registryProvider) {
        this.registryProvider = registryProvider;
        this.postProcessors = postProcessors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurableResolver get() {
        ConfigurableResolver resolver = new DefinitionResolver(registryProvider);
        for (ResolverPostProcessor postProcessor : postProcessors) {
            resolver.addPostProcessor(postProcessor);
        }
        // TODO rbala Possibly move it out from provider
        resolver.refresh();

        return resolver;
    }

}
