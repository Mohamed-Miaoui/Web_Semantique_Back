package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Tutorial;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "tuto", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class TutorialController {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf"; // Adjust path as needed

    public TutorialController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    public Model getModel() {
        return model;
    }

    @GetMapping()
    public String gteTutos() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" + "\n" + "SELECT ?tutorial ?title ?content ?estimated_time\n" + "WHERE {\n" + "    ?tutorial a ont:Tutorial .\n" + "    ?tutorial ont:title ?title .\n" + "    ?tutorial ont:content ?content .\n" + "    ?tutorial ont:estimated_time ?estimated_time .\n" + "}";
        String qexec = queryString;

        QueryExecution qe = QueryExecutionFactory.create(qexec, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ResultSetFormatter.outputAsJSON(outputStream, results);

        String json = new String(outputStream.toByteArray());
        JSONObject j = new JSONObject(json);

        JSONArray res = j.getJSONObject("results").getJSONArray("bindings");

        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @PostMapping()
    public ResponseEntity<String> addTuto(@RequestBody Tutorial tutorial) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual postIndividual = ontModel.createIndividual(NAMESPACE + "Tutorial" + System.currentTimeMillis(), ontModel.getOntClass(NAMESPACE + "Tutorial"));

        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "title"), tutorial.getTitle());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "content"), tutorial.getContent());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "estimated_time"), tutorial.getEstimated_time().toString());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the tutorial.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Tutorial added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteTuto(@RequestParam("URI") String uri) {
        loadModel();

        // Create an OntModel that performs inference
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Find the post individual based on the provided URI
        Individual postIndividual = ontModel.getIndividual(uri);

        if (postIndividual != null) {
            // Collect the properties to delete first
            List<Statement> statementsToDelete = new ArrayList<>();
            StmtIterator iter = postIndividual.listProperties();

            while (iter.hasNext()) {
                Statement stmt = iter.nextStatement();
                statementsToDelete.add(stmt);
            }

            // Now remove the collected statements
            for (Statement stmt : statementsToDelete) {
                ontModel.remove(stmt);
            }

            // Delete the post individual itself
            postIndividual.remove();

            // Save the updated RDF data to your file or database
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
                loadModel();
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the tutorial.");
            }
            loadModel();
            return ResponseEntity.status(HttpStatus.OK).body("Tutorial deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tutorial not found.");
        }
    }


    @PutMapping
    public ResponseEntity<String> addTutoToQuiz(@RequestParam("tutorialURI") String tutorialURI, @RequestParam("quizURI") String quizURI) {

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Resource quizResource = model.getResource(quizURI);
        Property followsProperty = model.createProperty(NAMESPACE + "follows");

        // Remove any existing follows property to avoid duplication
        quizResource.removeAll(followsProperty);

        // Add the follows property as a reference to the tutorial URI
        quizResource.addProperty(followsProperty, model.createResource(tutorialURI));

        // Save the updated model back to RDF
        try (FileOutputStream out = new FileOutputStream(RDF_FILE)) {
            ontModel.write(out, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving updated RDF model.");
        }

        return ResponseEntity.ok("Tutorial added to quiz successfully.");

    }
}
