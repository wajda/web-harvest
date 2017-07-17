package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;

/**
 * Created by rbala on 13.07.2017.
 */
// TODO rbala Missing JavaDoc
public interface ProcessorFactory {

    ProcessorFactory INSTANCE = new DefaultProcessorFactory();

    // TODO rbala Missing JavaDoc
    <TDef extends IElementDef> Processor<TDef> createProcessor(DynamicScopeContext context, TDef elementDef);

}
