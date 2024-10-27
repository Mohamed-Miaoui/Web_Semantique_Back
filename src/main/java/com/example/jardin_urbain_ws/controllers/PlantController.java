package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Plant;
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
@RequestMapping(path = "plant",produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class PlantController {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf";
    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    public PlantController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    public Model getModel() {
        return model;
    }

    @GetMapping()
    public String getPlants() {

        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "\n" +
                "SELECT ?Plant ?name ?type ?water_needs\n" +
                "WHERE {\n" +
                "    ?Plant a ont:Plant .\n" +
                "    ?Plant ont:name ?name .\n" +
                "    ?Plant ont:type ?type .\n" +
                "    ?Plant ont:water_needs ?water_needs .\n" +
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

            transformedItem.put("uri", item.getJSONObject("Plant").getString("value"));
            transformedItem.put("name", item.getJSONObject("name").getString("value"));
            transformedItem.put("type", item.getJSONObject("type").getString("value"));
            transformedItem.put("water_needs", Double.parseDouble(item.getJSONObject("water_needs").getString("value")));

            transformedArray.put(transformedItem);
        }
        return transformedArray.toString();

    }

    @PostMapping
    public ResponseEntity<String> addPlant(@RequestBody Plant Plant) {

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual postIndividual = ontModel.createIndividual(NAMESPACE+"Plant_" + System.currentTimeMillis(), ontModel.getOntClass(NAMESPACE+"Plant"));
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"name"), Plant.getName());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"type"), Plant.getType());
        postIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE+"water_needs"), Plant.getWater_needs().toString());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the Plant.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Plant added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deletePlant(@RequestParam ("URI") String postURI) {

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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the Plant.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Plant deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plant not found.");
        }
    }

}
