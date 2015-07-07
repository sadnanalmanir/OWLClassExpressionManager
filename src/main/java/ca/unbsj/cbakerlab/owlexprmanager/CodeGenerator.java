package ca.unbsj.cbakerlab.owlexprmanager;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.model.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Generates the code for processing the input RDG graph according to the SADI input class description
 * Created by sadnana on 07/07/15.
 */
public class CodeGenerator {

    public String codeString;
    public Set<OWLClass> classes;
    public Set<OWLDataProperty> dataProperties;
    public Set<OWLObjectProperty> objectProperties;


    public void generateServiceCode(OWLOntology ontology, OWLDataFactory df, ManchesterOWLSyntaxClassExpressionParser parser, List<Edge> edgesList) {

        classes = ontology.getClassesInSignature();
        dataProperties = ontology.getDataPropertiesInSignature();
        objectProperties = ontology.getObjectPropertiesInSignature();

        for (Edge edge : edgesList) {
            // create code for those edge labels with object/data properties,
            // empty labels are not considered
            if (!edge.getLabel().equals("")) {
                // if the edge is not a data property i.e. is an object property
                if ( !checkPropertyType(objectProperties, dataProperties, edge.getLabel()) ) {
                    System.out.println("object prop");
                    //if(edge.getVertex(Direction.IN).getProperty("name") instanceof OWLClass) {
                    if(isOWLClass(edge.getVertex(Direction.IN).getProperty("name").toString(), classes)){
                        System.out.println(edge.getVertex(Direction.IN).getProperty("name") + " -- is an instance of OWLClass");
                        codeString += createCodeForResourceValue(edge.getVertex(Direction.IN).getProperty("nodeVariableName").toString(), edge.getLabel(), edge.getVertex(Direction.OUT).getProperty("nodeVariableName").toString());
                    }
                    else {
                        codeString += createCodeForResourceStmt(edge.getVertex(Direction.IN).getProperty("nodeVariableName").toString(), edge.getLabel(), edge.getVertex(Direction.OUT).getProperty("nodeVariableName").toString());
                    }
                } else{
                    codeString += createCodeForDataValue(edge.getVertex(Direction.IN).getProperty("name").toString(), edge.getVertex(Direction.IN).getProperty("nodeVariableName").toString(), edge.getLabel(), edge.getVertex(Direction.OUT).getProperty("nodeVariableName").toString());
                }

            }

        }

        System.out.println(codeString);

    }

    private String createCodeForDataValue(String dataType, String objVariableName, String predicateName, String subVariableName) {
        String result = "";

        if (dataType.equals("string")) {
            result += "\n\n" + "String " + objVariableName + " = " + subVariableName + "." + "getString(" + ")" + ";";
            //dataPropertyGenericVarName = dataPropertyStringValueVarName + String.valueOf(dataPropertyStringValueVarCounter);
        }

        return result + "\n";
    }

    private boolean isOWLClass(String className, Set<OWLClass> classes) {
        for(OWLClass cls : classes){
            if(className.equals(cls.getIRI().getFragment()))
                return true;
        }
        return false;
    }

    private String createCodeForResourceStmt(Serializable objVariableName, String predicateName, String subVariableName) {
        String result = "";
        result += "\n" + "\t\t" + "Statement " + objVariableName + " = " + subVariableName + "." + "getProperty" + "(" + "Vocab" + "." + predicateName + ")";

        result += "\n" + "\t\t" + "if" + "(" + objVariableName + " == " + "null" + ")" + "\n"
                + "\t\t" + "{" + "\n"
                + "\t\t" + "\tlog.fatal" + "(" + "\"" + "No " + objVariableName + " found in the input RDF for " + predicateName + "\"" + ")" + ";" + "\n"
                + "\t\t" + "\tthrow new IllegalArgumentException" + "(" + "\"" + "Cannot extract " + objVariableName + " from the input RDF attached to " + predicateName + "\"" + ")" + ";" + "\n"
                + "\t\t" + "}";

        return result + "\n";
    }

    private String createCodeForResourceValue(String objVariableName, String predicateName, String subVariableName) {
        String result = "";
        result += "\n" + "\t\t" + "Resource " + objVariableName + " = " + subVariableName + "." + "getPropertyResourceValue" + "(" + "Vocab" + "." + predicateName + ")";

        result += "\n" + "\t\t" + "if" + "(" + objVariableName + " == " + "null" + ")" + "\n"
                + "\t\t" + "{" + "\n"
                + "\t\t" + "\tlog.fatal" + "(" + "\"" + "No " + objVariableName + " found in the input RDF for " + predicateName + "\"" + ")" + ";" + "\n"
                + "\t\t" + "\tthrow new IllegalArgumentException" + "(" + "\"" + "Cannot extract " + objVariableName + " from the input RDF attached to " + predicateName + "\"" + ")" + ";" + "\n"
                + "\t\t" + "}";


        return result + "\n";
    }


    /**
     * @param objectProperties set of object properties
     * @param dataProperties   set of data properties
     * @param propertyLabel    edge label i.e. property name
     * @return ture if the edge/property is data property
     */
    private boolean checkPropertyType(Set<OWLObjectProperty> objectProperties, Set<OWLDataProperty> dataProperties, String propertyLabel) {
        boolean result = false;

        for (OWLDataProperty dp : dataProperties) {
            if (dp.getIRI().getFragment().equals(propertyLabel)) {
                result = true;
            }
        }
        for (OWLObjectProperty op : objectProperties) {
            if (op.getIRI().getFragment().equals(propertyLabel)) {
                result = false;
            }
        }
        return result;
    }
}