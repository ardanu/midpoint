/**
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.common.valueconstruction;

import static org.testng.AssertJUnit.assertEquals;
import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.common.expression.ExpressionFactory;
import com.evolveum.midpoint.common.expression.xpath.XPathExpressionEvaluator;
import com.evolveum.midpoint.schema.SchemaRegistry;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.schema.exception.ObjectNotFoundException;
import com.evolveum.midpoint.schema.exception.SchemaException;
import com.evolveum.midpoint.schema.processor.Property;
import com.evolveum.midpoint.schema.processor.PropertyContainerDefinition;
import com.evolveum.midpoint.schema.processor.PropertyDefinition;
import com.evolveum.midpoint.schema.util.JAXBUtil;
import com.evolveum.midpoint.schema.util.ObjectResolver;
import com.evolveum.midpoint.test.util.DirectoryFileObjectResolver;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ExpressionType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ValueConstructionXType;

/**
 * @author Radovan Semancik
 *
 */
public class TestValueConstruction {

	private static File TEST_DIR = new File("src/test/resources/valueconstruction");
	
	private ValueConstructionFactory factory;
	SchemaRegistry schemaRegistry;
	
	@BeforeClass
	public void setupFactory() throws SAXException, IOException, SchemaException {
		ExpressionFactory expressionFactory = new ExpressionFactory();
		XPathExpressionEvaluator xpathEvaluator = new XPathExpressionEvaluator();
		expressionFactory.registerEvaluator(XPathExpressionEvaluator.XPATH_LANGUAGE_URL, xpathEvaluator);
		ObjectResolver resolver = new DirectoryFileObjectResolver(new File(TEST_DIR, "objects"));
		expressionFactory.setObjectResolver(resolver);
		
		factory = new ValueConstructionFactory();
		factory.initialize();
		
		schemaRegistry = new SchemaRegistry();
		schemaRegistry.initialize();
	}
	
	@Test
	public void testConstructionLiteral() throws JAXBException, ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
		// GIVEN
		JAXBElement<ValueConstructionXType> valueConstructionTypeElement = (JAXBElement<ValueConstructionXType>) JAXBUtil.unmarshal(
				new File(TEST_DIR, "construction-literal.xml"));
		ValueConstructionXType valueConstructionType = valueConstructionTypeElement.getValue();
		
		PropertyContainerDefinition userContainer = schemaRegistry.getCommonSchema().findContainerDefinitionByType(SchemaConstants.I_USER_TYPE);
		PropertyDefinition givenNameDef = userContainer.findPropertyDefinition(new QName(SchemaConstants.NS_C,"givenName"));
		
		// WHEN
		ValueConstruction construction = factory.createValueConstruction(valueConstructionType, givenNameDef, "literal construction");
		construction.evaluate();
		Property result = construction.getOutput();
		
		// THEN
		assertEquals("foobar",result.getValue(String.class));
	}

	

}
