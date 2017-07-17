package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;

/**
 * Created by rbala on 13.07.2017.
 */
// TODO rbala Missing JavaDoc
public class DefaultProcessorFactory implements ProcessorFactory {

    /**
     * {@inheritDoc}
     */
    // TODO rbala Missing unit test
    @Override
    public <TDef extends IElementDef> Processor<TDef> createProcessor(DynamicScopeContext context, TDef elementDef) {
        return new StoppedOrExitedProcessor<TDef>(
                new RunningStatusController<TDef>(
                        elementDef.createPlugin()));
    }

}
