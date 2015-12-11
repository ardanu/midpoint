/*
 * Copyright (c) 2010-2015 Evolveum
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

package com.evolveum.midpoint.repo.sql.query2.hqm.condition;

import com.evolveum.midpoint.repo.sql.query2.definition.JpaEntityDefinition;
import com.evolveum.midpoint.repo.sql.query2.hqm.HibernateQuery;
import com.evolveum.midpoint.repo.sql.query2.hqm.RootHibernateQuery;
import org.apache.commons.lang.Validate;

/**
 * @author mederly
 */
public class HibernateSubquery extends HibernateQuery {

    private HibernateQuery parentQuery;

    public HibernateSubquery(JpaEntityDefinition primaryEntityDef, HibernateQuery parentQuery) {
        super(primaryEntityDef);
        Validate.notNull(parentQuery);
        this.parentQuery = parentQuery;
    }

    @Override
    public RootHibernateQuery getRootQuery() {
        return parentQuery.getRootQuery();
    }
}
