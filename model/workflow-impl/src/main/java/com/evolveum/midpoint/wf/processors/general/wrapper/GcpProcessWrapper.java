/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.wf.processors.general.wrapper;

import com.evolveum.midpoint.model.api.context.ModelContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.GeneralChangeProcessorScenarioType;
import com.evolveum.midpoint.xml.ns.model.workflow.common_forms_2.WorkItemContents;
import com.evolveum.midpoint.xml.ns.model.workflow.process_instance_state_2.ProcessInstanceState;

import javax.xml.bind.JAXBException;
import java.util.Map;

/**
 * @author mederly
 */
public interface GcpProcessWrapper {

    /**
     * Determines whether the process should be run in a given situation.
     * (Is applied only if activation condition is null or evaluates to TRUE.)
     *
     * @param scenarioType
     * @param context
     * @param taskFromModel
     * @param result
     * @return
     */
    boolean determineActivation(GeneralChangeProcessorScenarioType scenarioType, ModelContext context, Task taskFromModel, OperationResult result);

    PrismObject<? extends WorkItemContents> externalizeWorkItemContents(org.activiti.engine.task.Task task, Map<String, Object> processInstanceVariables, OperationResult result) throws JAXBException, ObjectNotFoundException, SchemaException;

    PrismObject<? extends ProcessInstanceState> externalizeInstanceState(Map<String, Object> variables) throws SchemaException;

}