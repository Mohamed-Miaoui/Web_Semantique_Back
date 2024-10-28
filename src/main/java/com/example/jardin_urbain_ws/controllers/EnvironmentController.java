package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Environment;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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

@RestController
@RequestMapping(path = "environment",produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class EnvironmentController {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf";
    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    public EnvironmentController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    public Model getModel() {
        return model;
    }

    @GetMapping()
    public String getEnvironments() {

        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "\n" +
                "SELECT ?Environment ?light_conditions ?temperature ?humidity\n" +
                "WHERE {\n" +
                "    ?Environment a ont:Environment .\n" +
                "    ?Environment ont:light_conditions ?light_conditions .\n" +
                "    ?Environment ont:temperature ?temperature .\n" +
                "    ?Environment ont:humidity ?humidity .\n" +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ResultSetFormatter.outputAsJSON(outputStream, results);

        String json = new String(outputStream.toByteArray());
        JSONObject j = new JSONObject(json);

        String jsonString = j.getJSONObject("results").getJSONArray("bindings").toString();
        JSONArray jsonArray = new JSONArray(jsonString);
        JSONArray transformedArray = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            JSONObject transformedItem = new JSONObject();

            transformedItem.put("uri", item.getJSONObject("Environment").getString("value"));
            transformedItem.put("light_conditions", item.getJSONObject("light_conditions").getString("value"));
            transformedItem.put("temperature", Double.parseDouble(item.getJSONObject("temperature").getString("value")));
            transformedItem.put("humidity", Double.parseDouble(item.getJSONObject("humidity").getString("value")));

            transformedArray.put(transformedItem);
        }
        return transformedArray.toString();

    }

    @PostMapping
    public ResponseEntity<String> addEnvironment(@RequestBody Environment Environment) {

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual postIndividual = ontModel.createIndividual(NAMESPACE+"Environment_" + System.currentTimeMillis(), ontModel.getOntClass(NAMESPACE+"Environment"));
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"light_conditions"), Environment.getLight_conditions());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"temperature"), Environment.getTemperature().toString());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"humidity"), Environment.getHumidity().toString());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the Environment.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Environment added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteEnvironment(@RequestParam ("URI") String postURI) {

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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the Environment.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Environment deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Environment not found.");
        }
    }

}
