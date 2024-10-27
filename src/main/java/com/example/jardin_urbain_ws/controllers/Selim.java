package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.JenaEngine;
import com.example.jardin_urbain_ws.Quiz;
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
@RequestMapping(path = "quiz", produces = "application/json")
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

        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" + "\n" + "SELECT ?quiz ?question ?answer\n" + "WHERE {\n" + "    ?quiz a ont:Quiz .\n" + "    ?quiz ont:question ?question .\n" + "    ?quiz ont:answer ?answer .\n" + "}";
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

    @GetMapping("tutorialByQuiz")
    public String getTutoByQuiz(@RequestParam("URI") String uri) {
        // Define the SPARQL query
        String queryString = String.format("PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#> " + "SELECT ?tutorial ?title ?content ?estimated_time " + "WHERE { " + "    BIND(<%s> AS ?quiz) . " + "    ?quiz ont:follows ?follows . " + // Get the follows element
                "    ?follows a ont:Tutorial . " + // Ensure follows is of type Tutorial
                "    ?follows ont:title ?title . " + // Retrieve the title of the tutorial
                "    ?follows ont:content ?content . " + // Retrieve the content of the tutorial
                "    ?follows ont:estimated_time ?estimated_time . " + // Retrieve estimated time
                "}", uri);

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ResultSetFormatter.outputAsJSON(outputStream, results);

        String json = new String(outputStream.toByteArray());
        JSONObject j = new JSONObject(json);

        JSONArray res = j.getJSONObject("results").getJSONArray("bindings");

        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @GetMapping("{search}")
    public String searchQuizs(@PathVariable String search) {

        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" + "\n" + "SELECT ?quiz ?question ?answer\n" + "WHERE {\n" + "    ?quiz a ont:Quiz .\n" + "    ?quiz ont:question ?question .\n" + "    ?quiz ont:answer ?answer .\n" + "    FILTER(CONTAINS(LCASE(?question), LCASE(\"" + search + "\")))\n" + "}";
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

        Individual postIndividual = ontModel.createIndividual(NAMESPACE + "Quiz_" + System.currentTimeMillis(), ontModel.getOntClass(NAMESPACE + "Quiz"));

        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "question"), quiz.getQuestion());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "answer"), quiz.getAnswer());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the quiz.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Quiz added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteQuiz(@RequestParam("URI") String uri) {

        // Create an OntModel that performs inference
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Find the post individual based on the provided URI
        Individual postIndividual = ontModel.getIndividual(uri);

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

    @GetMapping("tuto")
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

    @PostMapping("tuto")
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

    @DeleteMapping("tuto")
    public ResponseEntity<String> deleteTuto(@RequestParam("URI") String uri) {

        // Create an OntModel that performs inference
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Find the post individual based on the provided URI
        Individual postIndividual = ontModel.getIndividual(uri);

        if (postIndividual != null) {
            // Delete the post individual
            postIndividual.remove();

            // Save the updated RDF data to your file or database
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the tutorial.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Tutorial deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tutorial not found.");
        }
    }

    @PutMapping
    public ResponseEntity<String> addTutoToQuiz(@RequestParam("tutorialURI") String tutorialURI, @RequestParam("quizURI") String quizURI) {
        Resource quizResource = model.getResource(quizURI);
        Property followsProperty = model.createProperty(NAMESPACE + "follows");

        // Remove any existing follows property to avoid duplication
        quizResource.removeAll(followsProperty);

        // Add the follows property as a reference to the tutorial URI
        quizResource.addProperty(followsProperty, model.createResource(tutorialURI));

        // Save the updated model back to RDF
        try (FileOutputStream out = new FileOutputStream(RDF_FILE)) { // Replace with your output path
            model.write(out, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving updated RDF model.");
        }

        return ResponseEntity.ok("Tutorial added to quiz successfully.");

    }


}
