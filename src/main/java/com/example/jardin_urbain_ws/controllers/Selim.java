package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.JenaEngine;
import com.example.jardin_urbain_ws.Quiz;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "quiz",produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class Selim {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf"; // Adjust path as needed

    public Selim() {
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
    public String getQuizzs() {

        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "\n" +
                "SELECT ?quiz ?question ?answer\n" +
                "WHERE {\n" +
                "    ?quiz a ont:Quiz .\n" +
                "    ?quiz ont:question ?question .\n" +
                "    ?quiz ont:answer ?answer .\n" +
                "}";
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

    @PostMapping
    public ResponseEntity<String> addQuiz(@RequestBody Quiz quiz) {

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual postIndividual = ontModel.createIndividual(NAMESPACE+"Quiz_" + System.currentTimeMillis(), ontModel.getOntClass(NAMESPACE+"Quiz"));

        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"question"), quiz.getQuestion());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"answer"), quiz.getAnswer());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the quiz.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Quiz added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteQuiz(@RequestParam ("URI") String postURI) {

        // Create an OntModel that performs inference
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Find the post individual based on the provided URI
        Individual postIndividual = ontModel.getIndividual(postURI);

        if (postIndividual != null) {
            // Delete the post individual
            postIndividual.remove();

            // Save the updated RDF data to your file or database
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the quiz.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Quiz deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quiz not found.");
        }
    }

}
