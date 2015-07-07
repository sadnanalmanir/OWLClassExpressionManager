/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.unbsj.cbakerlab.owlexprmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

/**
 *
 * @author sadnana
 */
class SADIServiceCodeGenerator {

    static String vocabString = "public static final class Vocab {"
            + "\n\nprivate static Model m_model = ModelFactory.createDefaultModel(); \n";
    static String codeString = "";

    static String rootNode = "";

    // jena code starts with processInput(Resource input, Resource output), hence we start with the root Resource input
    static String currentResourceName = "input";
    // counter for the variables to be used and added to vocabulary
    static String statementVarName = "valueStateVar";
    static int statementVarCounter = 0;

    static String ontSubClassVarName = "ontSubClass";
    static int ontSubClassVarCounter = 0;

    static String attributeResourceVarName = "attributeResVar";
    static int attributeResourceVarCounter = 0;

    static String dataPropertyGenericVarName = "";

    static String dataPropertyByteValueVarName = "valByte";
    static int dataPropertyByteValueVarCounter = 0;
    static String dataPropertyShortValueVarName = "valShort";
    static int dataPropertyShortValueVarCounter = 0;
    static String dataPropertyIntValueVarName = "valInt";
    static int dataPropertyIntValueVarCounter = 0;
    static String dataPropertyLongValueVarName = "valLong";
    static int dataPropertyLongValueVarCounter = 0;

    static String dataPropertyFloatValueVarName = "valFloat";
    static int dataPropertyFloatValueVarCounter = 0;
    static String dataPropertyDoubleValueVarName = "valDouble";
    static int dataPropertyDoubleValueVarCounter = 0;

    static String dataPropertyCharValueVarName = "valChar";
    static int dataPropertyCharValueVarCounter = 0;
    static String dataPropertyStringValueVarName = "valString";
    static int dataPropertyStringValueVarCounter = 0;

    static String dataPropertyBooleanValueVarName = "valBoolean";
    static int dataPropertyBooleanValueVarCounter = 0;

    static ManchesterOWLSyntaxOWLObjectRendererImpl r = new ManchesterOWLSyntaxOWLObjectRendererImpl();

    /**
     * Currently the code handles code generation for OWLDataSomeValuesFrom but
     * NOT OWLDataHasValue must be added in the future for data value property
     * in case needed, examples found in Output classes in
     * util-sadi-services-ontology.owl
     *
     *
     *
     *
     *
     * @param ontology
     * @param df
     * @param parser
     * @param id
     * @param subject
     * @param property
     * @param object
     * @throws ParserException
     */
    void generateServiceCode(OWLOntology ontology, OWLDataFactory df, ManchesterOWLSyntaxClassExpressionParser parser, Object id, Object subject, String property, Object object) throws ParserException {

        if (id.equals("0")) {
            rootNode = (String) subject;
            //System.out.println("once only");
        }

        if (rootNode.equals((String) subject)) {
            currentResourceName = "input";
            //System.out.println("setting current resource only if root is input");
        }

        //System.out.println("--Current resource " + currentResourceName + "for edge == " + property);

        if (!(property.equals("objectIntersectionOfEdge") || property.equals("rdf:type") || property.equals("objectIntersectionOfEdge_objHasValueEdge") || property.equals("objectIntersectionOfEdge_dataHasValueEdge") || property.equals("objectIntersectionOfEdge_dataSomeValuesFromEdge"))) {

            /*
            boolean isSubjectResource = checkSubjectOfProperty(ontology, subject);
            if (isSubjectResource == true) {
                System.out.println("class " + subject + " is defined");
            } else {
                System.out.println("class " + subject + " is NOT defined");
            }
            */
            

            // check the property type
            boolean isPropertyData = checkPropertyType(ontology, property);
            // if the edge i.e. is a data property

            if (isPropertyData == true) {
                System.out.println("edge " + property + " is a data type property");
                // if the data property edge branches out from the root node (denoted by id "0") input
                // this does not work if there is more than one branch from the root node
                //if (id.toString().equals("0")) {
                //System.out.println("root is the input");
                // we know for sure that the the statement is a direct branch of the root node Resource input
                OWLDataSomeValuesFrom dsvf = (OWLDataSomeValuesFrom) parser.parse((String) subject);
                OWLClassExpression dsvfClassExpr = parser.parse((String) subject);

                if (dsvfClassExpr instanceof OWLDataSomeValuesFrom) {
                    generateCodeForDataSomeValueProperty(ontology, df, dsvf, property, object);
                } else if (dsvfClassExpr instanceof OWLDataHasValue) {
                    throw new Error("OWLDataHasValue is not implemented yet.");
                }
                //}
            } else {
                System.out.println("edge " + property + " is an object property");
                OWLClassExpression objectPropertyClassExpr = parser.parse((String) subject);

                if (objectPropertyClassExpr instanceof OWLObjectSomeValuesFrom) {
                    System.out.println("instanceof OWLObjectSomeValuesFrom");
                    generateCodeForObjectSomeValueProperty(ontology, df, objectPropertyClassExpr, property, object);

                } else if (objectPropertyClassExpr instanceof OWLObjectHasValue) {
                    System.out.println("instanceof OWLObjectHasValue");
                    generateCodeForObjectHasValueProperty(ontology, df, objectPropertyClassExpr, property, object);
                }
            }
        }
    }

    private void generateCodeForDataSomeValueProperty(OWLOntology ontology, OWLDataFactory df, OWLDataSomeValuesFrom dsvf, String property, Object object) {

        String servOntNS = ontology.getOntologyID().getOntologyIRI().getNamespace() + "#";

        codeString += "\n\n" + "Statement " + statementVarName + String.valueOf(++statementVarCounter) + " = " + currentResourceName + "." + "getProperty(" + "Vocab" + "." + property + ")" + ";";

        //add the error checking for statement
        codeString += "\n" + "if" + "(" + statementVarName + String.valueOf(statementVarCounter) + " == " + "null" + ")" + "\n"
                + "{" + "\n"
                + "\tlog.fatal" + "(" + "\"" + currentResourceName + " does not have " + property + "\"" + ")" + ";" + "\n"
                + "\tthrow new IllegalArgumentException" + "(" + "\"" + "No " + property + " with the " + currentResourceName + "\"" + ")" + ";" + "\n"
                + "}";
        //add the new variable to the vocabulary
        vocabString += "\n" + "public static final Property " + property + " = m_model.createProperty(\"" + getNSForProperty(ontology, property) + property + "\")" + ";";

        //String propertyValuetype = getDataPropertyType(ontology, df, dsvf, object);
        if (object.equals("string")) {
            codeString += "\n\n" + "String " + dataPropertyStringValueVarName + String.valueOf(++dataPropertyStringValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getString(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyStringValueVarName + String.valueOf(dataPropertyStringValueVarCounter);
        } else if (object.equals("char")) {
            codeString += "\n\n" + "Char " + dataPropertyCharValueVarName + String.valueOf(++dataPropertyCharValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getChar(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyCharValueVarName + String.valueOf(dataPropertyCharValueVarCounter);
        } else if (object.equals("byte")) {
            codeString += "\n\n" + "Byte " + dataPropertyByteValueVarName + String.valueOf(++dataPropertyByteValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getByte(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyByteValueVarName + String.valueOf(dataPropertyByteValueVarCounter);
        } else if (object.equals("short")) {
            codeString += "\n\n" + "Short " + dataPropertyShortValueVarName + String.valueOf(++dataPropertyShortValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getShort(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyShortValueVarName + String.valueOf(dataPropertyShortValueVarCounter);
        } else if (object.equals("int")) {
            codeString += "\n\n" + "int " + dataPropertyIntValueVarName + String.valueOf(++dataPropertyIntValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getInt(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyIntValueVarName + String.valueOf(dataPropertyIntValueVarCounter);
        } else if (object.equals("long")) {
            codeString += "\n\n" + "long " + dataPropertyLongValueVarName + String.valueOf(++dataPropertyLongValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getLong(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyLongValueVarName + String.valueOf(dataPropertyLongValueVarCounter);
        } else if (object.equals("float")) {
            codeString += "\n\n" + "float " + dataPropertyFloatValueVarName + String.valueOf(++dataPropertyFloatValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getFloat(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyFloatValueVarName + String.valueOf(dataPropertyFloatValueVarCounter);
        } else if (object.equals("double")) {
            codeString += "\n\n" + "double " + dataPropertyDoubleValueVarName + String.valueOf(++dataPropertyDoubleValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getChar(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyDoubleValueVarName + String.valueOf(dataPropertyDoubleValueVarCounter);
        } else if (object.equals("boolean")) {
            codeString += "\n\n" + "boolean " + dataPropertyBooleanValueVarName + String.valueOf(++dataPropertyBooleanValueVarCounter) + " = " + statementVarName + String.valueOf(statementVarCounter) + "." + "getChar(" + ")" + ";";
            dataPropertyGenericVarName = dataPropertyBooleanValueVarName + String.valueOf(dataPropertyBooleanValueVarCounter);
        } else {
            System.out.println("Unexpected datatype for the OWLDataSomeValuesFrom filler : " + object);
        }

        //add the error checking for datatype
        codeString += "\n" + "if" + "(" + dataPropertyGenericVarName + " == " + "null" + ")" + "\n"
                + "{" + "\n"
                + "\tlog.fatal" + "(" + "\"" + "No " + object.toString() + " found in the input RDF for " + property + "\"" + ")" + ";" + "\n"
                + "\tthrow new IllegalArgumentException" + "(" + "\"" + "Cannot extract " + object.toString() + " data value from the input RDF attached to " + property + "\"" + ")" + ";" + "\n"
                + "}";

    }

    /**
     * Returns true if the edge (property) is a data property, false otherwise
     *
     * @param ontology the ontology for which the tree (class expression)
     * defined
     * @param property edge of class expression the tree to be checked
     * @return ture if the edge/property is based on OWLDataSomeValuesFrom
     */
    private boolean checkPropertyType(OWLOntology ontology, Object property) {
        Set<OWLDataProperty> dataProperties = ontology.getDataPropertiesInSignature();
        Set<OWLObjectProperty> objectProperties = ontology.getObjectPropertiesInSignature();
        boolean result = false;

        for (OWLDataProperty dp : dataProperties) {
            if (dp.getIRI().getFragment().equals(property)) {
                result = true;
            }
        }
        for (OWLObjectProperty op : objectProperties) {
            if (op.getIRI().getFragment().equals(property)) {
                result = false;
            }
        }
        return result;
    }

    private boolean checkSubjectOfProperty(OWLOntology ontology, Object subject) {
        Set<OWLClass> classes = ontology.getClassesInSignature();
        boolean result = false;

        for (OWLClass cls : classes) {
            if (cls.getIRI().getFragment().equals(subject)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return codeString;
    }

    /**
     *
     * @param ontology
     * @param property
     * @return NS for the object or the data property
     */
    private String getNSForProperty(OWLOntology ontology, String property) {
        Set<OWLDataProperty> dataProperties = ontology.getDataPropertiesInSignature();
        Set<OWLObjectProperty> objectProperties = ontology.getObjectPropertiesInSignature();

        String propertyNS = "";

        for (OWLDataProperty dp : dataProperties) {
            if (dp.getIRI().getFragment().equals(property)) {
                propertyNS = dp.getIRI().getNamespace();
            }
        }

        if (propertyNS.equals("")) {
            for (OWLObjectProperty op : objectProperties) {
                if (op.getIRI().getFragment().equals(property)) {
                    propertyNS = op.getIRI().getNamespace();
                }
            }
        }

        return propertyNS;
    }

    private void generateCodeForObjectHasValueProperty(OWLOntology ontology, OWLDataFactory df, OWLClassExpression objectPropertyClassExpr, String property, Object object) {

        if (property.equals("subClassOf")) {
            //subclassof cannot be found normally
            vocabString += "\n" + "public static final Property " + property + " = m_model.createProperty(\"" + getNSForProperty(ontology, property) + property + "\")" + ";";

            codeString += "\n" + "Iterator " + attributeResourceVarName + String.valueOf(++attributeResourceVarCounter) + " = " + "model" + "." + "getOntClass" + "(" + currentResourceName + "." + "getURI" + "(" + ")" + ")" + "." + "listSubclasses" + "(" + "false" + ")" + ";";

            codeString += "\n" + "if" + "(" + attributeResourceVarName + String.valueOf(attributeResourceVarCounter) + "." + "hasNext" + "(" + ")" + ")" + "\n"
                    + "{" + "\n"
                    + "\tlog.info" + "(" + "\"" + "List of subclasses found for " + "\"" + "+" + currentResourceName + ")" + ";" + "\n"
                    + "}"
                    + "\n" + "else"
                    + "{" + "\n"
                    + "\tlog.info" + "(" + "\"" + "No subclasses found for " + "\"" + "+" + currentResourceName + ")" + ";" + "\n"
                    + "\treturn" + ";" + "\n"
                    + "}";
            codeString += "\n" + "while" + "(" + attributeResourceVarName + String.valueOf(attributeResourceVarCounter) + "." + "hasNext" + "(" + ")" + ")"
                    + "\n" + "{"
                    + "\n\t" + "OntClass " + ontSubClassVarName + String.valueOf(++ontSubClassVarCounter) + " = " + "(" + "OntClass" + ")" + attributeResourceVarName + String.valueOf(attributeResourceVarCounter) + "." + "next" + "(" + ")" + ";";

        } else {
            //add the new variable to the vocabulary
            vocabString += "\n" + "public static final Property " + property + " = m_model.createProperty(\"" + getNSForProperty(ontology, property) + property + "\")" + ";";

            codeString += "\n" + "Resource " + attributeResourceVarName + String.valueOf(++attributeResourceVarCounter) + " = " + currentResourceName + "." + "getPropertyResourceValue(" + "Vocab" + "." + property + ")" + ";";

            //add the error checking for statement
            codeString += "\n" + "if" + "(" + attributeResourceVarName + String.valueOf(attributeResourceVarCounter) + " == " + "null" + ")" + "\n"
                    + "{" + "\n"
                    + "\tlog.fatal" + "(" + "\"" + "No " + property + " on the " + currentResourceName + " node" + "\"" + ")" + ";" + "\n"
                    + "\tthrow new IllegalArgumentException" + "(" + "\"" + "No " + property + " on the " + currentResourceName + " node" + "\"" + ")" + ";" + "\n"
                    + "}";

        }
    }

    private void generateCodeForObjectSomeValueProperty(OWLOntology ontology, OWLDataFactory df, OWLClassExpression objectPropertyClassExpr, String property, Object object) {

        //add the new variable to the vocabulary
        vocabString += "\n" + "public static final Property " + property + " = m_model.createProperty(\"" + getNSForProperty(ontology, property) + property + "\")" + ";";

        codeString += "\n" + "Resource " + attributeResourceVarName + String.valueOf(++attributeResourceVarCounter) + " = " + currentResourceName + "." + "getPropertyResourceValue(" + "Vocab" + "." + property + ")" + ";";

        //update the current resource variable
        currentResourceName = attributeResourceVarName + String.valueOf(attributeResourceVarCounter);

        //add the error checking for statement
        codeString += "\n" + "if" + "(" + attributeResourceVarName + String.valueOf(attributeResourceVarCounter) + " == " + "null" + ")" + "\n"
                + "{" + "\n"
                + "\tlog.fatal" + "(" + "\"" + "No " + property + " on the " + currentResourceName + " node" + "\"" + ")" + ";" + "\n"
                + "\tthrow new IllegalArgumentException" + "(" + "\"" + "No " + property + " on the " + currentResourceName + " node" + "\"" + ")" + ";" + "\n"
                + "}";
    }

}
