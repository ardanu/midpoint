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

package com.evolveum.midpoint.repo.sql;

import java.io.File;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import javax.xml.XMLConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.query.*;
import com.evolveum.midpoint.repo.sql.data.common.*;
import com.evolveum.midpoint.repo.sql.data.common.other.RObjectType;
import com.evolveum.midpoint.repo.sql.data.common.type.RAssignmentExtensionType;
import com.evolveum.midpoint.repo.sql.data.common.type.RObjectExtensionType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.*;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.match.PolyStringNormMatchingRule;
import com.evolveum.midpoint.prism.match.PolyStringOrigMatchingRule;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.repo.sql.data.common.enums.RActivationStatus;
import com.evolveum.midpoint.repo.sql.data.common.enums.RTaskExecutionStatus;
import com.evolveum.midpoint.repo.sql.data.common.other.RAssignmentOwner;
import com.evolveum.midpoint.repo.sql.query.QueryException;
import com.evolveum.midpoint.repo.sql.util.HibernateToSqlTranslator;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * @author lazyman
 */
@ContextConfiguration(locations = {"../../../../../ctx-test.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class QueryInterpreterTest extends BaseSQLRepoTest {

    private static final Trace LOGGER = TraceManager.getTrace(QueryInterpreterTest.class);
    private static final File TEST_DIR = new File("./src/test/resources/query");

    @Test
    public void queryOrganizationNorm() throws Exception {
        Session session = open();

        try {
            ObjectFilter filter = EqualsFilter.createEqual(UserType.F_ORGANIZATION, UserType.class, prismContext,
                    PolyStringNormMatchingRule.NAME, new PolyString("asdf", "asdf"));
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);

            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);


            Criteria o = main.createCriteria("organization", "o");

            o.add(Restrictions.eq("o.norm", "asdf"));

            String expected = HibernateToSqlTranslator.toSql(main);
            String real = getInterpretedQuery(session, UserType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryOrganizationOrig() throws Exception {
        Session session = open();
        try {
            ObjectFilter filter = EqualsFilter.createEqual(UserType.F_ORGANIZATION, UserType.class, prismContext,
                    PolyStringOrigMatchingRule.NAME, new PolyString("asdf", "asdf"));
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);

            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            Criteria o = main.createCriteria("organization", "o");

            o.add(Restrictions.eq("o.orig", "asdf"));

            String expected = HibernateToSqlTranslator.toSql(main);
            String real = getInterpretedQuery(session, UserType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryOrganizationStrict() throws Exception {
        Session session = open();
        try {
            ObjectFilter filter = EqualsFilter.createEqual(UserType.F_ORGANIZATION, UserType.class, prismContext,
                    null, new PolyString("asdf", "asdf"));
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);

            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            Criteria o = main.createCriteria("organization", "o");

            o.add(Restrictions.conjunction()
                    .add(Restrictions.eq("o.orig", "asdf"))
                    .add(Restrictions.eq("o.norm", "asdf")));

            String expected = HibernateToSqlTranslator.toSql(main);
            String real = getInterpretedQuery(session, UserType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryDependent() throws Exception {
        Session session = open();

        try {
            Criteria main = session.createCriteria(RTask.class, "t");
            Criteria d = main.createCriteria("dependent", "d");
            d.add(Restrictions.eq("d.elements", "123456"));
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("t.fullObject"));
            projections.add(Projections.property("t.stringsCount"));
            projections.add(Projections.property("t.longsCount"));
            projections.add(Projections.property("t.datesCount"));
            projections.add(Projections.property("t.referencesCount"));
            projections.add(Projections.property("t.clobsCount"));
            projections.add(Projections.property("t.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);

            ObjectFilter filter = EqualsFilter.createEqual(TaskType.F_DEPENDENT, TaskType.class, prismContext, null, "123456");
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            String real = getInterpretedQuery(session, TaskType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test(expectedExceptions = QueryException.class)
    public void queryClob() throws Exception {
        Session session = open();

        try {
            ObjectFilter filter = EqualsFilter.createEqual(UserType.F_DESCRIPTION, UserType.class, prismContext,
                    null, "aaa");
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);

            //should throw exception, because description is lob and can't be queried
            getInterpretedQuery(session, UserType.class, query);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryEnum() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RTask.class, "t");
            main.add(Restrictions.eq("executionStatus", RTaskExecutionStatus.WAITING));
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("t.fullObject"));
            projections.add(Projections.property("t.stringsCount"));
            projections.add(Projections.property("t.longsCount"));
            projections.add(Projections.property("t.datesCount"));
            projections.add(Projections.property("t.referencesCount"));
            projections.add(Projections.property("t.clobsCount"));
            projections.add(Projections.property("t.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);

            ObjectFilter filter = EqualsFilter.createEqual(TaskType.F_EXECUTION_STATUS, TaskType.class, prismContext,
                    null, TaskExecutionStatusType.WAITING);
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            String real = getInterpretedQuery(session, TaskType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryEnabled() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            main.add(Restrictions.eq("activation.administrativeStatus", RActivationStatus.ENABLED));
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);
            String real = getInterpretedQuery(session, UserType.class,
                    new File(TEST_DIR, "query-user-by-enabled.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }


    @Test
    public void queryGenericLong() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RGenericObject.class, "g");

            Criteria stringExt = main.createCriteria("longs", "l");

            //and
            Criterion c1 = Restrictions.eq("name.norm", "generic object");
            //and
            Conjunction c2 = Restrictions.conjunction();
            c2.add(Restrictions.eq("l.ownerType", RObjectExtensionType.EXTENSION));
            c2.add(Restrictions.eq("l.name", new QName("http://example.com/p", "intType")));
            c2.add(Restrictions.eq("l.value", 123L));

            Conjunction conjunction = Restrictions.conjunction();
            conjunction.add(c1);
            conjunction.add(c2);
            main.add(conjunction);
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("g.fullObject"));
            projections.add(Projections.property("g.stringsCount"));
            projections.add(Projections.property("g.longsCount"));
            projections.add(Projections.property("g.datesCount"));
            projections.add(Projections.property("g.referencesCount"));
            projections.add(Projections.property("g.clobsCount"));
            projections.add(Projections.property("g.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);
            String real = getInterpretedQuery(session, GenericObjectType.class,
                    new File(TEST_DIR, "query-and-generic.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryOrComposite() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RShadow.class, "r");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("r.fullObject"));
            projections.add(Projections.property("r.stringsCount"));
            projections.add(Projections.property("r.longsCount"));
            projections.add(Projections.property("r.datesCount"));
            projections.add(Projections.property("r.referencesCount"));
            projections.add(Projections.property("r.clobsCount"));
            projections.add(Projections.property("r.polysCount"));
            main.setProjection(projections);

            Criteria stringExt = main.createCriteria("strings", "s1");

            //or
            Criterion c1 = Restrictions.eq("intent", "some account type");
            //or
            Conjunction c2 = Restrictions.conjunction();
            c2.add(Restrictions.eq("s1.ownerType", RObjectExtensionType.ATTRIBUTES));
            c2.add(Restrictions.eq("s1.name", new QName("http://midpoint.evolveum.com/blabla", "foo")));
            c2.add(Restrictions.eq("s1.value", "foo value"));
            //or
            Conjunction c3 = Restrictions.conjunction();
            c3.add(Restrictions.eq("s1.ownerType", RObjectExtensionType.EXTENSION));
            c3.add(Restrictions.eq("s1.name", new QName("http://example.com/p", "stringType")));
            c3.add(Restrictions.eq("s1.value", "uid=test,dc=example,dc=com"));
            //or
            Criterion c4 = Restrictions.conjunction().add(
                    Restrictions.eq("r.resourceRef.targetOid", "d0db5be9-cb93-401f-b6c1-86ffffe4cd5e"));

            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(c1);
            disjunction.add(c2);
            disjunction.add(c3);
            disjunction.add(c4);
            main.add(disjunction);

            String expected = HibernateToSqlTranslator.toSql(main);
            String real = getInterpretedQuery(session, ShadowType.class,
                    new File(TEST_DIR, "query-or-composite.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryObjectByName() throws Exception {
        Session session = open();

        try {
            Criteria main = session.createCriteria(RObject.class, "o");
            main.add(Restrictions.and(
                    Restrictions.eq("name.orig", "cpt. Jack Sparrow"),
                    Restrictions.eq("name.norm", "cpt jack sparrow")));
            main.addOrder(Order.asc("name.orig"));
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("o.fullObject"));
            projections.add(Projections.property("o.stringsCount"));
            projections.add(Projections.property("o.longsCount"));
            projections.add(Projections.property("o.datesCount"));
            projections.add(Projections.property("o.referencesCount"));
            projections.add(Projections.property("o.clobsCount"));
            projections.add(Projections.property("o.polysCount"));
            main.setProjection(projections);
            String expected = HibernateToSqlTranslator.toSql(main);

            EqualsFilter filter = EqualsFilter.createEqual(ObjectType.F_NAME, ObjectType.class, prismContext,
                    null, new PolyString("cpt. Jack Sparrow", "cpt jack sparrow"));

            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            query.setPaging(ObjectPaging.createPaging(null, null, ObjectType.F_NAME, OrderDirection.ASCENDING));

            String real = getInterpretedQuery(session, ObjectType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryUserByFullName() throws Exception {
        Session session = open();

        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            main.add(Restrictions.eq("fullName.norm", "cpt jack sparrow"));
            String expected = HibernateToSqlTranslator.toSql(main);

            String real = getInterpretedQuery(session, UserType.class,
                    new File(TEST_DIR, "query-user-by-fullName.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryUserSubstringFullName() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryUserSubstringFullName"});
        Session session = open();

        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            main.add(Restrictions.like("fullName.norm", "%cpt jack sparrow%").ignoreCase());
            String expected = HibernateToSqlTranslator.toSql(main);

            String real = getInterpretedQuery(session, UserType.class,
                    new File(TEST_DIR, "query-user-substring-fullName.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryUserByName() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryUserByName"});
        Session session = open();

        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            main.setProjection(projections);
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));

            main.add(Restrictions.eq("name.norm", "some name identificator"));
            String expected = HibernateToSqlTranslator.toSql(main);

            String real = getInterpretedQuery(session, UserType.class,
                    new File(TEST_DIR, "query-user-by-name.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryConnectorByType() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryConnectorByType"});
        Session session = open();

        try {
            Criteria main = session.createCriteria(RConnector.class, "c");
            Criterion connectorType = Restrictions.conjunction().add(
                    Restrictions.eq("connectorType", "org.identityconnectors.ldap.LdapConnector"));
            main.add(connectorType);
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("c.fullObject"));
            projections.add(Projections.property("c.stringsCount"));
            projections.add(Projections.property("c.longsCount"));
            projections.add(Projections.property("c.datesCount"));
            projections.add(Projections.property("c.referencesCount"));
            projections.add(Projections.property("c.clobsCount"));
            projections.add(Projections.property("c.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);

            String real = getInterpretedQuery(session, ConnectorType.class,
                    new File(TEST_DIR, "query-connector-by-type.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryAccountByAttributesAndResourceRef() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryAccountByAttributesAndResourceRef"});
        Session session = open();
        try {
            Criteria main = session.createCriteria(RShadow.class, "r");

            Criteria stringAttr = main.createCriteria("strings", "s1x");

            //and
            Criterion c1 = Restrictions.conjunction().add(
                    Restrictions.eq("r.resourceRef.targetOid", "aae7be60-df56-11df-8608-0002a5d5c51b"));
            //and
            Conjunction c2 = Restrictions.conjunction();
            c2.add(Restrictions.eq("s1x.ownerType", RObjectExtensionType.ATTRIBUTES));
            c2.add(Restrictions.eq("s1x.name", new QName("http://midpoint.evolveum.com/blabla", "foo")));
            c2.add(Restrictions.eq("s1x.value", "uid=jbond,ou=People,dc=example,dc=com"));

            Conjunction conjunction = Restrictions.conjunction();
            conjunction.add(c1);
            conjunction.add(c2);
            main.add(conjunction);
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("r.fullObject"));
            projections.add(Projections.property("r.stringsCount"));
            projections.add(Projections.property("r.longsCount"));
            projections.add(Projections.property("r.datesCount"));
            projections.add(Projections.property("r.referencesCount"));
            projections.add(Projections.property("r.clobsCount"));
            projections.add(Projections.property("r.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);
            String real = getInterpretedQuery(session, ShadowType.class,
                    new File(TEST_DIR, "query-account-by-attributes-and-resource-ref.xml"));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryUserAccountRef() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryUserAccountRef"});
        Session session = open();
        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            Criteria refs = main.createCriteria("linkRef", "l");
            refs.add(Restrictions.conjunction().add(Restrictions.eq("l.targetOid", "123")));

            String expected = HibernateToSqlTranslator.toSql(main);

            RefFilter filter = RefFilter.createReferenceEqual(UserType.F_LINK_REF, UserType.class, prismContext, "123");
            String real = getInterpretedQuery(session, UserType.class, ObjectQuery.createObjectQuery(filter));

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryTrigger() throws Exception {
        final Date NOW = new Date();

        Session session = open();
        try {
            Criteria main = session.createCriteria(RObject.class, "o");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("o.fullObject"));
            projections.add(Projections.property("o.stringsCount"));
            projections.add(Projections.property("o.longsCount"));
            projections.add(Projections.property("o.datesCount"));
            projections.add(Projections.property("o.referencesCount"));
            projections.add(Projections.property("o.clobsCount"));
            projections.add(Projections.property("o.polysCount"));
            main.setProjection(projections);

            Criteria d = main.createCriteria("trigger", "t");
            d.add(Restrictions.le("t.timestamp", new Timestamp(NOW.getTime())));

            String expected = HibernateToSqlTranslator.toSql(main);

            XMLGregorianCalendar thisScanTimestamp = XmlTypeConverter.createXMLGregorianCalendar(NOW.getTime());

            SchemaRegistry registry = prismContext.getSchemaRegistry();
            PrismObjectDefinition objectDef = registry.findObjectDefinitionByCompileTimeClass(ObjectType.class);
            ItemPath triggerPath = new ItemPath(ObjectType.F_TRIGGER, TriggerType.F_TIMESTAMP);
    //        PrismContainerDefinition triggerContainerDef = objectDef.findContainerDefinition(triggerPath);
            ObjectFilter filter = LessFilter.createLess(triggerPath, objectDef, thisScanTimestamp, true);
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            String real = getInterpretedQuery(session, ObjectType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryAssignmentActivationAdministrativeStatus() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            Criteria a = main.createCriteria("assignments", "a");
            a.add(Restrictions.and(
                    Restrictions.eq("a.assignmentOwner", RAssignmentOwner.FOCUS),
                    Restrictions.eq("a.activation.administrativeStatus", RActivationStatus.ENABLED)
            ));
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);

            SchemaRegistry registry = prismContext.getSchemaRegistry();
            PrismObjectDefinition objectDef = registry.findObjectDefinitionByCompileTimeClass(UserType.class);
            ItemPath activationPath = new ItemPath(UserType.F_ASSIGNMENT, AssignmentType.F_ACTIVATION, ActivationType.F_ADMINISTRATIVE_STATUS);

    //        PrismContainerDefinition activationDef = objectDef.findContainerDefinition(activationPath);

            ObjectFilter filter = EqualsFilter.createEqual(activationPath, objectDef, ActivationStatusType.ENABLED);
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            String real = getInterpretedQuery(session, UserType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryInducementActivationAdministrativeStatus() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RRole.class, "r");
            Criteria a = main.createCriteria("assignments", "a");
            a.add(Restrictions.and(
                    Restrictions.eq("a.assignmentOwner", RAssignmentOwner.ABSTRACT_ROLE),
                    Restrictions.eq("a.activation.administrativeStatus", RActivationStatus.ENABLED)
            ));
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("r.fullObject"));
            projections.add(Projections.property("r.stringsCount"));
            projections.add(Projections.property("r.longsCount"));
            projections.add(Projections.property("r.datesCount"));
            projections.add(Projections.property("r.referencesCount"));
            projections.add(Projections.property("r.clobsCount"));
            projections.add(Projections.property("r.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);

            SchemaRegistry registry = prismContext.getSchemaRegistry();
            PrismObjectDefinition objectDef = registry.findObjectDefinitionByCompileTimeClass(RoleType.class);
            ItemPath activationPath = new ItemPath(RoleType.F_INDUCEMENT, AssignmentType.F_ACTIVATION, ActivationType.F_ADMINISTRATIVE_STATUS);

        //        PrismContainerDefinition activationDef = objectDef.findContainerDefinition(activationPath);

            ObjectFilter filter = EqualsFilter.createEqual(activationPath, objectDef, ActivationStatusType.ENABLED);
            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            String real = getInterpretedQuery(session, RoleType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryInducementAndAssignmentActivationAdministrativeStatus() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RRole.class, "r");
            Criteria a = main.createCriteria("assignments", "a");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("r.fullObject"));
            projections.add(Projections.property("r.stringsCount"));
            projections.add(Projections.property("r.longsCount"));
            projections.add(Projections.property("r.datesCount"));
            projections.add(Projections.property("r.referencesCount"));
            projections.add(Projections.property("r.clobsCount"));
            projections.add(Projections.property("r.polysCount"));
            main.setProjection(projections);

            Criterion and1 = Restrictions.and(
                    Restrictions.eq("a.assignmentOwner", RAssignmentOwner.FOCUS),
                    Restrictions.eq("a.activation.administrativeStatus", RActivationStatus.ENABLED)
            );

            Criterion and2 = Restrictions.and(
                    Restrictions.eq("a.assignmentOwner", RAssignmentOwner.ABSTRACT_ROLE),
                    Restrictions.eq("a.activation.administrativeStatus", RActivationStatus.ENABLED)
            );

            a.add(Restrictions.or(and1, and2));

            String expected = HibernateToSqlTranslator.toSql(main);

            SchemaRegistry registry = prismContext.getSchemaRegistry();
            PrismObjectDefinition objectDef = registry.findObjectDefinitionByCompileTimeClass(RoleType.class);

            //filter1
            ItemPath activationPath1 = new ItemPath(UserType.F_ASSIGNMENT, AssignmentType.F_ACTIVATION, ActivationType.F_ADMINISTRATIVE_STATUS);
    //        PrismContainerDefinition activationDef1 = objectDef.findContainerDefinition(activationPath1);
            ObjectFilter filter1 = EqualsFilter.createEqual(activationPath1, objectDef, ActivationStatusType.ENABLED);

            //filter2
            ItemPath activationPath2 = new ItemPath(RoleType.F_INDUCEMENT, AssignmentType.F_ACTIVATION, ActivationType.F_ADMINISTRATIVE_STATUS);
    //        PrismContainerDefinition activationDef2 = objectDef.findContainerDefinition(activationPath2);
            ObjectFilter filter2 = EqualsFilter.createEqual(activationPath2, objectDef, ActivationStatusType.ENABLED);

            ObjectQuery query = ObjectQuery.createObjectQuery(OrFilter.createOr(filter1, filter2));
            String real = getInterpretedQuery(session, RoleType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryUserByActivationDouble() throws Exception {
        Date NOW = new Date();

        Session session = open();
        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("u.fullObject"));
            projections.add(Projections.property("u.stringsCount"));
            projections.add(Projections.property("u.longsCount"));
            projections.add(Projections.property("u.datesCount"));
            projections.add(Projections.property("u.referencesCount"));
            projections.add(Projections.property("u.clobsCount"));
            projections.add(Projections.property("u.polysCount"));
            main.setProjection(projections);

            main.add(Restrictions.and(
                    Restrictions.eq("u.activation.administrativeStatus", RActivationStatus.ENABLED),
                    Restrictions.eq("u.activation.validFrom", XmlTypeConverter.createXMLGregorianCalendar(NOW.getTime()))));

            String expected = HibernateToSqlTranslator.toSql(main);

            SchemaRegistry registry = prismContext.getSchemaRegistry();
            PrismObjectDefinition objectDef = registry.findObjectDefinitionByCompileTimeClass(UserType.class);
    //        ItemPath triggerPath = new ItemPath(AssignmentType.F_ACTIVATION);

    //        PrismContainerDefinition triggerContainerDef = objectDef.findContainerDefinition(triggerPath);

            ObjectFilter filter1 = EqualsFilter.createEqual(new ItemPath(AssignmentType.F_ACTIVATION, ActivationType.F_ADMINISTRATIVE_STATUS), objectDef,
                    ActivationStatusType.ENABLED);

            ObjectFilter filter2 = EqualsFilter.createEqual(new ItemPath(AssignmentType.F_ACTIVATION, ActivationType.F_VALID_FROM), objectDef,
                    XmlTypeConverter.createXMLGregorianCalendar(NOW.getTime()));

            ObjectQuery query = ObjectQuery.createObjectQuery(AndFilter.createAnd(filter1, filter2));
            String real = getInterpretedQuery(session, UserType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryTriggerTimestampDouble() throws Exception {
        final Date NOW = new Date();

        Session session = open();
        try {
            Criteria main = session.createCriteria(RObject.class, "o");
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("o.fullObject"));
            projections.add(Projections.property("o.stringsCount"));
            projections.add(Projections.property("o.longsCount"));
            projections.add(Projections.property("o.datesCount"));
            projections.add(Projections.property("o.referencesCount"));
            projections.add(Projections.property("o.clobsCount"));
            projections.add(Projections.property("o.polysCount"));
            main.setProjection(projections);

            Criteria d = main.createCriteria("trigger", "t");
            d.add(Restrictions.and(
                    Restrictions.gt("t.timestamp", new Timestamp(NOW.getTime())),
                    Restrictions.lt("t.timestamp", new Timestamp(NOW.getTime()))
            ));

            String expected = HibernateToSqlTranslator.toSql(main);

            XMLGregorianCalendar thisScanTimestamp = XmlTypeConverter.createXMLGregorianCalendar(NOW.getTime());

            SchemaRegistry registry = prismContext.getSchemaRegistry();
            PrismObjectDefinition objectDef = registry.findObjectDefinitionByCompileTimeClass(ObjectType.class);
            ItemPath triggerPath = new ItemPath(ObjectType.F_TRIGGER, TriggerType.F_TIMESTAMP);
    //        PrismContainerDefinition triggerContainerDef = objectDef.findContainerDefinition(triggerPath);
            ObjectFilter greater = GreaterFilter.createGreater(triggerPath, objectDef, thisScanTimestamp, false);
            ObjectFilter lesser = LessFilter.createLess(triggerPath, objectDef, thisScanTimestamp, false);
            AndFilter and = AndFilter.createAnd(greater, lesser);
            LOGGER.info(and.debugDump());

            ObjectQuery query = ObjectQuery.createObjectQuery(and);
            String real = getInterpretedQuery(session, ObjectType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryOrgStructure() throws Exception {
        Session session = open();

        try {
            ProjectionList list = Projections.projectionList();
            list.add(Projections.groupProperty("o.fullObject"));
            list.add(Projections.groupProperty("o.stringsCount"));
            list.add(Projections.groupProperty("o.longsCount"));
            list.add(Projections.groupProperty("o.datesCount"));
            list.add(Projections.groupProperty("o.referencesCount"));
            list.add(Projections.groupProperty("o.clobsCount"));
            list.add(Projections.groupProperty("o.polysCount"));

            list.add(Projections.groupProperty("o.name.orig"));
            list.add(Projections.groupProperty("closure.descendant"));

            list.add(Projections.property("o.fullObject"));
            list.add(Projections.property("o.stringsCount"));
            list.add(Projections.property("o.longsCount"));
            list.add(Projections.property("o.datesCount"));
            list.add(Projections.property("o.referencesCount"));
            list.add(Projections.property("o.clobsCount"));
            list.add(Projections.property("o.polysCount"));

            Criteria main = session.createCriteria(RObject.class, "o");
            main.createCriteria("descendants", "closure").setFetchMode("closure.ancestor", FetchMode.DEFAULT)
                    .createAlias("closure.ancestor", "anc").setProjection(list);
            main.addOrder(Order.asc("o.name.orig"));

            Conjunction conjunction = Restrictions.conjunction();
            conjunction.add(Restrictions.eq("anc.oid", "some oid"));
            conjunction.add(Restrictions.le("closure.depth", 1));
            conjunction.add(Restrictions.gt("closure.depth", 0));
            main.add(conjunction);

            String expected = HibernateToSqlTranslator.toSql(main);

            OrgFilter orgFilter = OrgFilter.createOrg("some oid", null, 1);
            ObjectQuery query = ObjectQuery.createObjectQuery(orgFilter);
            query.setPaging(ObjectPaging.createPaging(null, null, ObjectType.F_NAME, OrderDirection.ASCENDING));

            String real = getInterpretedQuery(session, ObjectType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});

            OperationResult result = new OperationResult("query org structure");
            repositoryService.searchObjects(ObjectType.class, query, null, result);

            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void countObjectOrderByName() throws Exception {
        Session session = open();

        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            main.add(Restrictions.and(
                    Restrictions.eq("u.name.orig", "cpt. Jack Sparrow"),
                    Restrictions.eq("u.name.norm", "cpt jack sparrow")));
            main.setProjection(Projections.rowCount());
            String expected = HibernateToSqlTranslator.toSql(main);

            EqualsFilter filter = EqualsFilter.createEqual(UserType.F_NAME, UserType.class, prismContext,
                    null, new PolyString("cpt. Jack Sparrow", "cpt jack sparrow"));

            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            query.setPaging(ObjectPaging.createPaging(null, null, ObjectType.F_NAME, OrderDirection.ASCENDING));

            String real = getInterpretedQuery(session, UserType.class, query, true);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void countObjectOrderByNameWithoutFilter() throws Exception {
        Session session = open();

        try {
            Criteria main = session.createCriteria(RObject.class, "o");
            main.setProjection(Projections.rowCount());
            String expected = HibernateToSqlTranslator.toSql(main);

            ObjectPaging paging = ObjectPaging.createPaging(null, null, ObjectType.F_NAME, OrderDirection.ASCENDING);
            ObjectQuery query = ObjectQuery.createObjectQuery(null, paging);

            String real = getInterpretedQuery(session, ObjectType.class, query, true);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    /**
     * Q{AND: (EQUALS: parent, PPV(null)),PAGING: O: 0,M: 5,BY: name, D:ASCENDING,
     *
     * @throws Exception
     */
    @Test
    public void countTaskOrderByName() throws Exception {
        Session session = open();

        try {
            Criteria main = session.createCriteria(RTask.class, "t");
            main.add(Restrictions.isNull("t.parent"));

            main.setProjection(Projections.rowCount());
            String expected = HibernateToSqlTranslator.toSql(main);

            EqualsFilter filter = EqualsFilter.createEqual(TaskType.F_PARENT, TaskType.class, prismContext, null);

            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            query.setPaging(ObjectPaging.createPaging(null, null, TaskType.F_NAME, OrderDirection.ASCENDING));

            String real = getInterpretedQuery(session, TaskType.class, query, true);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void inOidTest() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RObject.class, "o");
            main.add(Restrictions.in("oid", Arrays.asList("1", "2")));
            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.property("o.fullObject"));
            projections.add(Projections.property("o.stringsCount"));
            projections.add(Projections.property("o.longsCount"));
            projections.add(Projections.property("o.datesCount"));
            projections.add(Projections.property("o.referencesCount"));
            projections.add(Projections.property("o.clobsCount"));
            projections.add(Projections.property("o.polysCount"));
            main.setProjection(projections);

            String expected = HibernateToSqlTranslator.toSql(main);

            InOidFilter filter = InOidFilter.createInOid(Arrays.asList("1", "2"));

            ObjectQuery query = ObjectQuery.createObjectQuery(filter);
            String real = getInterpretedQuery(session, ObjectType.class, query, false);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void queryOrgTreeFindOrgs() throws Exception {
        Session session = open();

        try {
            ProjectionList list = Projections.projectionList();
            list.add(Projections.groupProperty("o.fullObject"));
            list.add(Projections.groupProperty("o.stringsCount"));
            list.add(Projections.groupProperty("o.longsCount"));
            list.add(Projections.groupProperty("o.datesCount"));
            list.add(Projections.groupProperty("o.referencesCount"));
            list.add(Projections.groupProperty("o.clobsCount"));
            list.add(Projections.groupProperty("o.polysCount"));

            list.add(Projections.groupProperty("o.name.orig"));
            list.add(Projections.groupProperty("closure.descendant"));

            list.add(Projections.property("o.fullObject"));
            list.add(Projections.property("o.stringsCount"));
            list.add(Projections.property("o.longsCount"));
            list.add(Projections.property("o.datesCount"));
            list.add(Projections.property("o.referencesCount"));
            list.add(Projections.property("o.clobsCount"));
            list.add(Projections.property("o.polysCount"));

            Criteria main = session.createCriteria(ROrg.class, "o");
            main.createCriteria("descendants", "closure").setFetchMode("closure.ancestor", FetchMode.DEFAULT)
                    .createAlias("closure.ancestor", "anc").setProjection(list);
            main.addOrder(Order.asc("o.name.orig"));

            Conjunction conjunction = Restrictions.conjunction();
            conjunction.add(Restrictions.eq("anc.oid", "some oid"));
            conjunction.add(Restrictions.le("closure.depth", 1));
            conjunction.add(Restrictions.gt("closure.depth", 0));
            main.add(conjunction);

            String expected = HibernateToSqlTranslator.toSql(main);

            OrgFilter orgFilter = OrgFilter.createOrg("some oid", null, 1);
            ObjectQuery query = ObjectQuery.createObjectQuery(orgFilter);
            query.setPaging(ObjectPaging.createPaging(null, null, ObjectType.F_NAME, OrderDirection.ASCENDING));

            String real = getInterpretedQuery(session, OrgType.class, query);

            LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});

            OperationResult result = new OperationResult("query org structure");
            repositoryService.searchObjects(OrgType.class, query, null, result);

            AssertJUnit.assertEquals(expected, real);
        } finally {
            close(session);
        }
    }

    @Test
    public void asdf() throws Exception {
        Session session = open();
        try {
            Criteria main = session.createCriteria(RUser.class, "u");
            Criteria a = main.createCriteria("assignments", "a");
            a.add(Restrictions.eq("a.assignmentOwner", RAssignmentOwner.FOCUS));
            Criteria e = a.createCriteria("a.extension");

            Criteria s = e.createCriteria("strings", "s");

            Conjunction c2 = Restrictions.conjunction();
            c2.add(Restrictions.eq("s.extensionType", RAssignmentExtensionType.EXTENSION));
            c2.add(Restrictions.eq("s.name", new QName("http://midpoint.evolveum.com/blabla", "foo")));
            c2.add(Restrictions.eq("s.value", "uid=jbond,ou=People,dc=example,dc=com"));

            Conjunction c1 = Restrictions.conjunction();
            c1.add(Restrictions.eq("a.targetRef.targetOid", "1234"));
            c1.add(Restrictions.eq("a.targetRef.type", RObjectType.ORG));

            main.add(Restrictions.and(c1, c2));

            main.setProjection(Projections.property("u.fullObject"));

            String expected = HibernateToSqlTranslator.toSql(main);
            LOGGER.info(">>> >>> {}",expected);
        } finally {
            close(session);
        }
    }
}
