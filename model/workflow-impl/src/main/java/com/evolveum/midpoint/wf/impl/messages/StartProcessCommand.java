/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.wf.impl.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Command to start process instance.
 */
public class StartProcessCommand extends MidPointToActivitiMessage {

    private Map<String,Object> variables;
    private String processName;
    private String processOwner;
    private String taskOid;
    private boolean sendStartConfirmation;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public boolean isSendStartConfirmation() {
        return sendStartConfirmation;
    }

    public void setSendStartConfirmation(boolean sendStartConfirmation) {
        this.sendStartConfirmation = sendStartConfirmation;
    }

    public String getTaskOid() {
        return taskOid;
    }

    public void setTaskOid(String taskOid) {
        this.taskOid = taskOid;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void setVariablesFrom(Map<String, Object> variables) {
        this.variables = new HashMap<String,Object>(variables);
    }

    public void addVariable(String name, Object value) {
        if (variables == null) {
            variables = new HashMap<String,Object>();
        }
        variables.put(name, value);
    }

    public String getProcessOwner() {
        return processOwner;
    }

    public void setProcessOwner(String processOwner) {
        this.processOwner = processOwner;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[process=" + processName + ", task=" + taskOid + ", variables=" + variables + ", sendStartConfirmation=" + sendStartConfirmation + "]";
    }
}
