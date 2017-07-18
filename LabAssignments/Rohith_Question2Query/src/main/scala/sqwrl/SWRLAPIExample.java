package sqwrl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import java.io.File;
import java.util.Optional;

public class SWRLAPIExample
{
    public static void main(String[] args)
    {
        if (args.length > 1)
            Usage();

        Optional<@NonNull String> owlFilename = args.length == 0 ? Optional.<@NonNull String>empty() : Optional.of(args[0]);
        Optional<@NonNull File> owlFile = (owlFilename != null && owlFilename.isPresent()) ?
                Optional.of(new File(owlFilename.get())) :
                Optional.<@NonNull File>empty();

        try {
            // Create an OWL ontology using the OWLAPI
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
//
// OWLOntology ontology = owlFile.isPresent() ?
// ontologyManager.loadOntologyFromOntologyDocument(owlFile.get()) :
// ontologyManager.createOntology();

            OWLOntology ontology=ontologyManager.loadOntologyFromOntologyDocument(new File("data/myowlontology.owl"));

            SWRLRuleEngine ruleEngine= SWRLAPIFactory.createSWRLRuleEngine(ontology);
            ruleEngine.infer();

//      // Create SQWRL query engine using the SWRLAPI
            SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);


 //       SWRLAPIRule rule = ruleEngine.createSWRLRule("Languages",
//                    "Country(?p) ^ hasLanguage(?p, ?l) -> Langu(?p)");
//
//      // Create and execute a SQWRL query using the SWRLAPI
//      SQWRLResult result = queryEngine.runSQWRLQuery("q1", "swrlb:add(?p) -> sqwrl:select(?p)");


            String prefix="http://www.semanticweb.org/mayanka/ontologies/2017/6/family#";
            
            // Create and execute a SQWRL query
            SQWRLResult result = queryEngine.runSQWRLQuery("Q1",
                    prefix+"Person(?p) ^"+prefix+"hasGender(?p, ?g) -> sqwrl:select(?p, ?g)");

            // Process the results of the SQWRL query
            while (result.next()) {
                System.out.println("Name: " + result.getLiteral("p").getString());
                System.out.println("Gender: " + result.getLiteral("g").getString());
            }
            // Process the SQWRL result
            //if (result.next())
                //System.out.println("x: " + result.getLiteral("x").getInteger());

        } catch (OWLOntologyCreationException e) {
            System.err.println("Error creating OWL ontology: " + e.getMessage());
            System.exit(-1);
        } catch (SWRLParseException e) {
            System.err.println("Error parsing SWRL rule or SQWRL query: " + e.getMessage());
            System.exit(-1);
        } catch (SQWRLException e) {
            System.err.println("Error running SWRL rule or SQWRL query: " + e.getMessage());
            System.exit(-1);
        } catch (RuntimeException e) {
            System.err.println("Error starting application: " + e.getMessage());
            System.exit(-1);
        } catch (SWRLBuiltInException e) {
            e.printStackTrace();
        }
    }

    private static void Usage()
    {
        System.err.println("Usage: " + SWRLAPIExample.class.getName() + " [ <owlFileName> ]");
        System.exit(1);
    }
}
