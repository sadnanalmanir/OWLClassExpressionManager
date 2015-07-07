package ca.unbsj.cbakerlab.owlexprmanager;

import static ca.unbsj.cbakerlab.owlexprmanager.ClassExpressionTreeGenerator.setOfGraphs;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.gremlin.java.GremlinPipeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

/**
 * Hello world!
 */

/**
 * Hello world!
 *
 */
public class App {

    static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    static final OWLDataFactory df = OWLManager.getOWLDataFactory();
    static OWLOntology ontology = null;
    static IRI ontology_IRI = null;
    static IRI documentIRI = null;
    static ManchesterOWLSyntaxOWLObjectRendererImpl r = new ManchesterOWLSyntaxOWLObjectRendererImpl();
    static ManchesterOWLSyntaxClassExpressionParser parser;
    //static ClassExpressionToTreeGenerator classExpressionToTreeGenerator = new ClassExpressionToTreeGenerator();
    static OWLClassExpression eca;
    static OWLClass clsSADIInput = null;
    static OWLClass clsSADIOutput = null;
    static Set<OWLClassExpression> eqInpClasses = null;
    static Set<OWLClassExpression> eqOutputClasses = null;
    static ClassExpressionTreeGenerator classExpressionTreeGenerator;
    static DisjunctiveExpressionHandler disjunctiveExpressionHandler;
    static SADIServiceCodeGenerator sadiServiceCodeGenerator;
    static ServiceCodeGenerator serviceCodeGenerator;

    // Last update July 7, 2015
    static CodeGenerator codeGenerator;


    public static Set<Vertex> processedVertices;

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/bmi-demo/bmi-demo-sadi-service-ontology.owl";
    //tested
    //static String classDescription = "(has_height some (Measurement and (has_units value m) and (has_value some string)))";      
    //static String classDescription = "(has_height some (Measurement and (has_units value m) and (has_value some string))) and (has_mass some (Measurement and (has_units value kg) and (has_value some string)))";  

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/kegg/kegg-sadi-service-ontology.owl";
    //tested
    //static String classDescription = "(((type value KEGG_Organism_Identifier) and (SIO_000300 some string)) and (hasContext some (type value Context)))";    
    //getATCClassByKEGGDRUGID_Input
    //static String classDescription = "(type value KEGG_DRUG_Identifier) and (SIO_000300 some string)";
    //getKEGGDRUGIDByATCClass_Input
    //static String classDescription = "subClassOf value SIO_010038";
    //not tested
    //getKEGGOrganismCodeByOrganismName_Input
    //static String classDescription = "((((type value KEGG_Organism_Identifier) and (SIO_000300 some string)) and (hasContext some (type value Context))) or ((type value KEGG_Organism_Identifier) and (SIO_000300 some string)))";
    //static String classDescription = "((((type value SIO_000116) and (SIO_000300 some string)) and (hasContext some (type value Context))) or ((type value SIO_000116) and (SIO_000300 some string)))";
    //getNameByKEGGDISEASEClass_Input
    //static String classDescription = "(((hasContext some (type value Context)) and (SIO_000629 some ((SIO_000008 some ((type value KEGG_DISEASE_Identifier) and (SIO_000300 some string))) and (type value KEGG_DISEASE_Record)))) or (SIO_000629 some ((SIO_000008 some ((type value KEGG_DISEASE_Identifier) and (SIO_000300 some string))) and (type value KEGG_DISEASE_Record))))";

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/util-sadi-services/util-sadi-services-ontology.owl";
    //tested
    //getWikipediaPageByTopic_Input
    //static String classDescription = "SIO_000300 some string";
    //getUniProtIDByUniProtEntryName_Input
    //static String classDescription = "SIO_000116 and (SIO_000300 some string)";
    //static String classDescription = "PMID_Identifier and (SIO_000300 some string)";
    //getGIByUniProtID_Input
    //static String classDescription = "SIO_010015 and (SIO_000011 some (SIO_010043 and (SIO_000212 some UniProt_Record)))";
    //not tested
    //static String classDescription = "";
    //static String classDescription = "";
    //output classes
    //getHTMLByPMCID_Output
    //static String classDescription = SIO_000011 some (Document and (link some string) and (format value \"text/html\"))
    //not tested
    //static String classDescription = "SIO_000133 and (SIO_000011 some (((Document or SIO_000148)) and (link some string) and (format value \"text/html\")))";
    //static String classDescription = "((GO_0003674 or GO_0005575))";
    //static String classDescription = "(link some string) or (SIO_000300 some string)";
    //static String classDescription = "(link some string) or (SIO_000300 some string) or (SIO_000212 some UniProt_Record)";
    //static String classDescription = "SIO_000011 some (subClassOf some ((GO_0003674)))";
    //static String classDescription = "SIO_000011 some (subClassOf some ((GO_0003674 or GO_0005575)))";
    //static String classDescription = "SIO_000011 some (subClassOf some ((GO_0003674 or GO_0005575 or GO_0008150)))";
    //static String classDescription = "(SIO_000011 some (subClassOf some ((GO_0003674 or GO_0005575 or GO_0008150)))) and SIO_000011 some (subClassOf some ((GO_0003674 or GO_0005575)))";
    //working perferctly
    //static String classDescription = "GO_0003674 and (SIO_000011 some (subClassOf some ((GO_0003674 or GO_0005575 or (SIO_000011 some (subClassOf some ((GO_0003674 or GO_0005575))))))))";    

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/icd/icd-sadi-service-ontology.owl";
    //tested
    //getSubclassByICD9DiseaseClass_Input
    //static String classDescription = "subClassOf value SIO_010299";
    //static String classDescription = "(( (hasContext some Context) and (subClassOf value SIO_010299) ) )";    
    //static String classDescription = "((hasContext some Context) and (subClassOf value SIO_010299)) or (subClassOf value SIO_010299)";
    //static String classDescription = "";
    //static String classDescription = "";

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/ddi/ddi-sadi-service-ontology.owl";
    //tested
    //getDrugDrugInteractionsByDrug_Input
    //static String classDescription = "SIO_010038 and (SIO_000008 some (DRUG_BANK_Identifier and (SIO_000300 some string)))";


    // not in this ontology
    //static String classDescription = "GO_0003674 and (SIO_000011 some (subClassOf some (SIO_000011 some (subClassOf some (GO_0005575)))))";
    //static String classDescription = "";

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/temporal-reasoning/temporal-reasoning-sadi-service-ontology.owl";
    //tested
    //getTimeIntervalsByFinishingTimeInterval_Input
    //static String classDescription = "FullyDefinedTimeInterval and (hasContext some Context)";
    //not working
    //static String classDescription = "FullyDefinedTimeInterval";
    //static String classDescription = "FullyDefinedTimeInterval and (hasContext some Context)";
    //not tested
    //static String classDescription = "";
    //static String classDescription = "";

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/uniprot/uniprot-sadi-service-ontology.owl";
    //tested
    //
    //static String classDescription = "SIO_000300 some string";
    //getUniProtIDByPMID_Input
    //static String classDescription = "(type value PMID_Identifier) and (SIO_000300 some string)";
    //getUniProtIDByEC_Input
    //static String classDescription = "(SIO_000629 some ((SIO_000008 some ((type value EC_Hierarchy_Identifier) and (SIO_000300 some string))) and (type value EC_Hierarchy_Record))) and (subClassOf value SIO_010343)";
    //a variation of the above service getUniProtIDByEC_Input
    //static String classDescription = "(SIO_000629 some ((SIO_000008 some ((type value EC_Hierarchy_Identifier))) and (type value EC_Hierarchy_Record))) and (subClassOf value SIO_010343)";

    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/name-search/name-search-sadi-service-ontology.owl";
    //getSimilarICD10DiseaseNames_Input
    //static String classDescription = "(((hasContext some (type value Context)) and (SIO_000300 some string)) or (SIO_000300 some string))";
    //testing the comparator based on length, we want the larger expressions to be processed before we deal with the smaller expressions
    //static String classDescription = "(SIO_000300 some string) or ((hasContext some (type value Context)) and (SIO_000300 some string))";

    /**
     * Test cases for successful code generation from input class
     */


    //OWLDataSomeValuesFrom
    //getWikipediaPageByTopic_Input
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/util-sadi-services/util-sadi-services-ontology.owl";
    //static String classDescription = "SIO_000300 some string";

    //OWLObjectSomeValuesFrom
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/icd/icd-sadi-service-ontology.owl";
    //static String classDescription = "hasContext some Context";

    //OWLObjectHasValue
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/kegg/kegg-sadi-service-ontology.owl";
    //static String classDescription = "type value KEGG_Organism_Identifier";    

    //OWLClass and OWLDataSomeValuesFrom
    //getUniProtIDByUniProtEntryName_Input
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/util-sadi-services/util-sadi-services-ontology.owl";
    //static String classDescription = "SIO_000116 and (SIO_000300 some string)";

    //OWLClass and OWLObjectSomeValuesFrom
    //getTimeIntervalsByFinishingTimeInterval_Input
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/temporal-reasoning/temporal-reasoning-sadi-service-ontology.owl";
    //static String classDescription = "FullyDefinedTimeInterval and (hasContext some Context)";

    //OWLObjectHasValue and OWLDataSomeValuesFrom
    //getATCClassByKEGGDRUGID_Input
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/kegg/kegg-sadi-service-ontology.owl";
    //static String classDescription = "(type value KEGG_DRUG_Identifier) and (SIO_000300 some string)";

    //OWLClass, OWLObjectSomeValuesFrom, OWLObjectHasValue, OWLDataSomeValuesFrom
    //computeBMI_Input
    static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/bmi-demo/bmi-demo-sadi-service-ontology.owl";
    static String classDescription = "(has_height some (Measurement and (has_units value m) and (has_value some string))) and (has_mass some (Measurement and (has_units value kg) and (has_value some string)))";
    //static String classDescription = "(has_height some (Measurement and (has_units value m) and (has_value some string) )) and (has_mass some (Measurement and (has_value some string)))";
    //static String classDescription = "(has_height some (Measurement and (has_units value m) and (has_value some string) )) and (has_mass some (Measurement))";
    // complex disjunction
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/kegg/kegg-sadi-service-ontology.owl";
    //getKEGGOrganismCodeByOrganismName_Input
    //static String classDescription = "((((type value KEGG_Organism_Identifier) and (SIO_000300 some string)) and (hasContext some (type value Context))) or ((type value KEGG_Organism_Identifier) and (SIO_000300 some string)))";
    //static String classDescription = "(((type value KEGG_Organism_Identifier) and (SIO_000300 some string)) and (hasContext some (type value Context)))";
    //getNameByKEGGDISEASEClass_Input
    //static String classDescription = "(((hasContext some (type value Context)) and (SIO_000629 some ((SIO_000008 some ((type value KEGG_DISEASE_Identifier) and (SIO_000300 some string))) and (type value KEGG_DISEASE_Record)))) or (SIO_000629 some ((SIO_000008 some ((type value KEGG_DISEASE_Identifier) and (SIO_000300 some string))) and (type value KEGG_DISEASE_Record))))";


    //tested
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/kegg/kegg-sadi-service-ontology.owl";
    //static String classDescription = "(((type value KEGG_Organism_Identifier) and (SIO_000300 some string)) and (hasContext some (type value Context)))";    

    //OWLObjectHasValue and OWLDataSomeValuesFrom
    //getATCClassByKEGGDRUGID_Input
    //static String onlineOntology = "http://cbakerlab.unbsj.ca:8080/kegg/kegg-sadi-service-ontology.owl";
    //static String classDescription = "(type value KEGG_DRUG_Identifier) and (SIO_000300 some string)";
    //getKEGGDRUGIDByATCClass_Input
    //static String classDescription = "subClassOf value SIO_010038";


    public App() {
        classExpressionTreeGenerator = new ClassExpressionTreeGenerator();
        disjunctiveExpressionHandler = new DisjunctiveExpressionHandler();
        sadiServiceCodeGenerator = new SADIServiceCodeGenerator();
        serviceCodeGenerator = new ServiceCodeGenerator();
        // codeGenerator is the updated class for generating code
        codeGenerator = new CodeGenerator();

        processedVertices = new LinkedHashSet<Vertex>();
    }

    //static String classDescription = "";
    public static void main(String[] args) throws OWLOntologyCreationException, ParserException {

        App app = new App();

        // if from a URI
        documentIRI = IRI.create(onlineOntology);
        // if from online
        ontology = manager.loadOntology(documentIRI);
        //OWLOntology ontology = manager.loadOntologyFromOntologyDocument(documentIRI);

        parser = new ManchesterOWLSyntaxClassExpressionParser(manager.getOWLDataFactory(), new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(manager, Collections.singleton(ontology), new SimpleShortFormProvider())));

        /**
         * check if the class expression expressed as String contains
         * disjunctions
         */
        boolean existDisjunctiveExpression = disjunctiveExpressionHandler.existDisjunctiveExpression(classDescription);
        int classExpressionTreeInstance = 0;
        if (existDisjunctiveExpression == true) {
            System.out.println("The expression IS disjunctive");
            //Stack<String> setOfDisjunctiveExpressions = disjunctiveExpressionHandler.handleDisjunctiveClassExpressions(parser, classDescription);
            Stack<String> setOfDisjunctiveExpressions = sortSetOfDisjunctiveExpressions(disjunctiveExpressionHandler.handleDisjunctiveClassExpressions(parser, classDescription));


            // sort the stack of expressions so that larger expressions are preferred for processing
            //setOfDisjunctiveExpressions = sortSetOfDisjunctiveExpressions(setOfDisjunctiveExpressions);

            int sizeOfsetOfDisjunctiveExpressions = setOfDisjunctiveExpressions.size();
            //ClassExpressionTreeGenerator(sizeOfsetOfDisjunctiveExpressions);
            classExpressionTreeGenerator = new ClassExpressionTreeGenerator(sizeOfsetOfDisjunctiveExpressions);
            while (!setOfDisjunctiveExpressions.isEmpty()) {
                // this is a dynamic tree instance number to be generated before popping expressions, e.g., 0, 1, 3, and so on
                classExpressionTreeInstance = sizeOfsetOfDisjunctiveExpressions - setOfDisjunctiveExpressions.size();
                System.out.println("################### " + classExpressionTreeInstance);

                classDescription = setOfDisjunctiveExpressions.pop();
                System.out.println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
                classExpressionTreeGenerator.generateTreeFromClassExpression(parser, classDescription, classExpressionTreeInstance);
            }
        } else {
            classExpressionTreeGenerator = new ClassExpressionTreeGenerator(1);
            System.out.println("The expression is NOT disjunctive");
            classExpressionTreeGenerator.generateTreeFromClassExpression(parser, classDescription, classExpressionTreeInstance);
        }

        Set<Graph> setOfGeneratedTrees = classExpressionTreeGenerator.getAllTrees();


        classExpressionTreeGenerator.setDegreeOfVertices();
        classExpressionTreeGenerator.assignNodeVariableNames();
        classExpressionTreeGenerator.displayTreeVertices();
        classExpressionTreeGenerator.displayTreeEdges();

        // if trees need to be displayed here then use this
        /*
         for (Graph graph : setOfGraphs) {
         System.out.println();
         //System.out.println("Edges for Tree No: " + (i + 1));
         System.out.println();
         System.out.println("Edges:  " + graph);
         System.out.println();
         for (Edge edge : graph.getEdges()) {
         System.out.println(edge.getVertex(Direction.OUT).getId() + " # " + edge.getVertex(Direction.OUT).getProperty("name") + " -- ** " + edge.getLabel() + " ** --> " + edge.getVertex(Direction.IN).getId() + " # " + edge.getVertex(Direction.IN).getProperty("name"));
         }
         }
         */


        // get each graph with the edge list in DFS order i.e. the order they are created

        // the second List<Edge> is not required as the edges have been sorted using comparator here
        List<Graph> returnedGraphEdgesMap = classExpressionTreeGenerator.getMapOfGraphsAndCorrespondingEdges();

        System.out.println();


        // Generate the code for the input RDF graph based on the input class expression
        int graphCounter = 0;

        for (Graph g : returnedGraphEdgesMap) {
            graphCounter++;
            List<Edge> edgesList = sortEdgesInDFS(g.getEdges());

            codeGenerator.generateServiceCode(ontology, df, parser, edgesList);

        }//end for (Map.Entry










































        
        /*

        int graphCounter = 0; 
        
        for (Graph g : returnedGraphEdgesMap) {
            System.out.println("key,val: ");
            //System.out.println(entry.getKey() + "," + entry.getValue());
            
            graphCounter++;
            
            
            //List<Edge> edgesListSorted = sortEdgesInDFS(g.getEdges()); 
            List<Edge> edgesList = sortEdgesInDFS(g.getEdges());
            System.out.println(g + "," + edgesList);
            //List<Edge> edgesList =  entry.getValue();
              



            Graph gModifiedWithCommonResourceVariable = addCommonResourceVariableKeysInVertices(g, edgesList);
            Graph gModifiedWithResourceVariable = addResourceVariableKeysInVertices(g, edgesList);
            
            
            
            for (Edge edge : edgesList) {

            	
            	
            	
            	//sadiServiceCodeGenerator.generateServiceCode(ontology, df, parser, edge.getVertex(Direction.OUT).getId(), edge.getVertex(Direction.OUT).getProperty("name"), edge.getLabel(), edge.getVertex(Direction.IN).getProperty("name"));
            
            	//make sure the root node has been assigned a value for 
            	if(edge.getVertex(Direction.OUT).getId().equals("0"))
            		if(!checkIfVertexValueAssigned(edge.getVertex(Direction.OUT)))
            			edge.getVertex(Direction.OUT).setProperty("ParentVariableName", "input");
            	
            	
            	if(graphCounter == 1){
            		System.out.println("in first graph");
            		processedVertices.add(edge.getVertex(Direction.OUT));
            		serviceCodeGenerator.generateServiceCode(ontology, df, parser, edge.getVertex(Direction.OUT).getId(), edge.getVertex(Direction.OUT).getProperty("name"), edge.getVertex(Direction.OUT).getProperty("ParentVariableName"), edge.getLabel(), edge.getVertex(Direction.IN).getProperty("name"), edge.getVertex(Direction.IN).getProperty("ParentVariableName"));
            	}
            	else if(graphCounter > 1){
            		System.out.println("afte 1st graph");
            		if(!parentVertexProcessed(processedVertices, edge.getVertex(Direction.OUT))){
            			System.out.println("vertex is new");
            			
            			serviceCodeGenerator.generateServiceCode(ontology, df, parser, edge.getVertex(Direction.OUT).getId(), edge.getVertex(Direction.OUT).getProperty("name"), edge.getVertex(Direction.OUT).getProperty("ParentVariableName"), edge.getLabel(), edge.getVertex(Direction.IN).getProperty("name"), edge.getVertex(Direction.IN).getProperty("ParentVariableName"));
            			// add the vertex after it is processed
            			processedVertices.add(edge.getVertex(Direction.OUT));
            		}
            	}
            	
            	            
            	
            	//if (!(edge.getLabel().equals("objectIntersectionOfEdge") || edge.getLabel().equals("objectIntersectionOfEdge_objHasValueEdge") || edge.getLabel().equals("objectIntersectionOfEdge_dataHasValueEdge") || edge.getLabel().equals("objectIntersectionOfEdge_dataSomeValuesFromEdge")))
            		//System.out.println(" -- "+edge.getVertex(Direction.OUT).getProperty("ParentVariableName") +  " -- "+ edge.getLabel() + " -- " + edge.getVertex(Direction.IN).getProperty("ParentVariableName"));
            	            
            }//end for (Edge edge
            
            
            
            
        }//end for (Map.Entry
        
        */
        
        

        /*
        for (Graph graph : setOfGraphs) {
            for (Edge edge : graph.getEdges()) {
                //System.out.println(edge.getVertex(Direction.OUT).getId() + " # " + edge.getVertex(Direction.OUT).getProperty("name") + " -- ** " + edge.getLabel() + " ** --> " + edge.getVertex(Direction.IN).getId() + " # " + edge.getVertex(Direction.IN).getProperty("name"));
                System.out.println();
                sadiServiceCodeGenerator.generateServiceCode(ontology, df, parser, edge.getVertex(Direction.OUT).getId(), edge.getVertex(Direction.OUT).getProperty("name"), edge.getLabel(), edge.getVertex(Direction.IN).getProperty("name"));
            }
        }
        */

    // print the code
    //System.out.println("codeString:-> \n" + sadiServiceCodeGenerator.toString());
    System.out.println("codeString:-> \n"+serviceCodeGenerator.toString());

}

    private static boolean parentVertexProcessed(Set<Vertex> processedVertices, Vertex vertex) {

        for (Vertex v : processedVertices) {
            if (v.getProperty("name").equals(vertex.getProperty("name"))) {
                System.out.println("returning true");
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param edges
     * @return sort the edges of the graph in ordre of generation i.e. DFS
     */
    private static List<Edge> sortEdgesInDFS(Iterable<Edge> edges) {
        List<Edge> sortedEdges = new ArrayList<Edge>();

        for (Edge e : edges) {
            sortedEdges.add(e);
        }

        Collections.sort(sortedEdges, new Comparator<Edge>() {
            public int compare(Edge e1, Edge e2) {
                //based on number of edge ID
                return Integer.parseInt(e1.getId().toString()) - Integer.parseInt(e2.getId().toString());
            }
        });


        return sortedEdges;
    }

    private static Stack<String> sortSetOfDisjunctiveExpressions(
            Stack<String> setOfDisjunctiveExpressions) {

        List<String> tempExpr = new ArrayList<String>();

        while (!setOfDisjunctiveExpressions.isEmpty()) {
            tempExpr.add(setOfDisjunctiveExpressions.pop());
        }

        Collections.sort(tempExpr, new Comparator<String>() {
            public int compare(String s1, String s2) {
                //based on string length
                //return s1.length() - s2.length();
                //based on number of tokens in the string
                return s1.split(" ").length - s2.split(" ").length;
            }
        });

        for (int i = 0; i < tempExpr.size(); i++) {
            setOfDisjunctiveExpressions.push(tempExpr.get(i));
        }
        return setOfDisjunctiveExpressions;
    }


    private static Graph addResourceVariableKeysInVertices(Graph g,
                                                           List<Edge> edgesList) {

        String resourceVarName = "resourceVar";
        int resourceVarCounter = 0;

        for (Edge e : edgesList) {
            if (!(e.getLabel().equals("objectIntersectionOfEdge") || e.getLabel().equals("objectIntersectionOfEdge_objHasValueEdge") || e.getLabel().equals("objectIntersectionOfEdge_dataHasValueEdge") || e.getLabel().equals("objectIntersectionOfEdge_dataSomeValuesFromEdge"))) {
                // make sure to leave the root vertex, as this will be assigned as Resource input according to SADI convention
                if (!e.getVertex(Direction.OUT).getId().equals("0") && !checkIfVertexValueAssigned(e.getVertex(Direction.OUT))) {
                    e.getVertex(Direction.OUT).setProperty("ParentVariableName", resourceVarName + String.valueOf(++resourceVarCounter));
                }
            }
        }


        return g;
    }

    private static Graph addCommonResourceVariableKeysInVertices(Graph g, List<Edge> edgesList) {

        // copy all the edges into another temporary List
        List<Edge> tempEdgesList = new ArrayList<Edge>();

        // make a copy which refers to the same edgesList
        //List<Edge> tempEdgesList = new ArrayList<Edge>(edgesList);

        for (Edge e : edgesList)
            tempEdgesList.add(e);

        // set a new key to hold the value of the variables for all vertices, initialize them with empty strings

        for (Vertex v : g.getVertices()) {
            v.setProperty("ParentVariableName", "");
            //System.out.println(edge.getVertex(Direction.OUT).getId() + " # " + edge.getVertex(Direction.OUT).getProperty("name") + " -- ** " + edge.getLabel() + " ** --> " + edge.getVertex(Direction.IN).getId() + " # " + edge.getVertex(Direction.IN).getProperty("name"));
        }

        // 1) find edges who have the same root
        // 2)

        String commonResourceVarName = "commonResourceVar";
        int commonResourceVarCounter = 0;

        for (Edge e : edgesList) {
            System.out.println();
            for (Edge eTemp : tempEdgesList) {
                if (!e.getId().equals(eTemp.getId())) {
                    //System.out.println("EDGE not equal edgeID--> "+e.getId() + " vID = " + e.getVertex(Direction.OUT).getId() + "  edgeID-->" + eTemp.getId() + "vID = " + eTemp.getVertex(Direction.OUT).getId());
                    //System.out.println("vertex id      1--> "+e.getVertex(Direction.IN).getId());
                    // if two edges are different and they have the same parent
                    if (e.getVertex(Direction.OUT).getId().equals(eTemp.getVertex(Direction.OUT).getId())) {
                        System.out.println("equality found. edgeID--> " + e.getId() + " vID = " + e.getVertex(Direction.OUT).getId() + "  edgeID-->" + eTemp.getId() + "  vID = " + eTemp.getVertex(Direction.OUT).getId());
                        // if the parent is the root i.e. with an id 0, assign Resource variable input
                        if (e.getVertex(Direction.OUT).getId().equals("0")) {
                            // check if the value for the key ParentVariableName is already assigned
                            if (!checkIfVertexValueAssigned(e.getVertex(Direction.OUT))) {
                                e.getVertex(Direction.OUT).setProperty("ParentVariableName", "input");
                            }
                            if (!checkIfVertexValueAssigned(e.getVertex(Direction.IN))) {
                                e.getVertex(Direction.IN).setProperty("ParentVariableName", "input");
                            }
                            if (!checkIfVertexValueAssigned(eTemp.getVertex(Direction.IN))) {
                                eTemp.getVertex(Direction.IN).setProperty("ParentVariableName", "input");
                            }
                        } else {

                            // CHECK KEGG SADI services, where a commonResource node is required to propagate to its children node
                            // in this case, the common parent resource has value  but their children do NOT have any value assigned to them
                            if (e.getVertex(Direction.IN).getProperty("ParentVariableName").equals("") && eTemp.getVertex(Direction.IN).getProperty("ParentVariableName").equals("") && !(e.getVertex(Direction.OUT).getProperty("ParentVariableName").equals(""))) {
                                String valueInParentNode = e.getVertex(Direction.OUT).getProperty("ParentVariableName").toString();
                                e.getVertex(Direction.IN).setProperty("ParentVariableName", valueInParentNode);
                                eTemp.getVertex(Direction.IN).setProperty("ParentVariableName", valueInParentNode);
                            } else {
                                // check if the value for the key ParentVariableName is already assigned
                                if (!checkIfVertexValueAssigned(e.getVertex(Direction.OUT))) {
                                    e.getVertex(Direction.OUT).setProperty("ParentVariableName", commonResourceVarName + String.valueOf(++commonResourceVarCounter));
                                }
                                if (!checkIfVertexValueAssigned(e.getVertex(Direction.IN))) {
                                    e.getVertex(Direction.IN).setProperty("ParentVariableName", commonResourceVarName + String.valueOf(commonResourceVarCounter));
                                }
                                if (!checkIfVertexValueAssigned(eTemp.getVertex(Direction.IN))) {
                                    eTemp.getVertex(Direction.IN).setProperty("ParentVariableName", commonResourceVarName + String.valueOf(commonResourceVarCounter));
                                }
                            }
                        }


                        // remove the particular temp edge from the tempEdgesList

                    }

                }


            }

        }


        return g;
    }

    private static boolean checkIfVertexValueAssigned(Vertex vertex) {
        if (vertex.getProperty("ParentVariableName").equals(""))
            return false;
        else
            return true;

    }
}
