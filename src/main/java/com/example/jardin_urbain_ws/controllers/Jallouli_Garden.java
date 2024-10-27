package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Garden;
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
@RequestMapping(path = "garden", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class Jallouli_Garden {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf"; // Adjust path as needed

    public Jallouli_Garden() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping()
    public String getGardens() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "SELECT ?garden ?name ?size ?location\n" +
                "WHERE {\n" +
                "    ?garden a ont:Garden .\n" +
                "    ?garden ont:name ?name .\n" +
                "    ?garden ont:size ?size .\n" +
                "    ?garden ont:location ?location .\n" +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);

        return new String(outputStream.toByteArray());
    }

    @PostMapping
    public ResponseEntity<String> addGarden(@RequestBody Garden garden) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual gardenIndividual = ontModel.createIndividual(NAMESPACE + "Garden_" + System.currentTimeMillis(),
                ontModel.getOntClass(NAMESPACE + "Garden"));
        gardenIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "name"), garden.getName());
        gardenIndividual.addLiteral(ontModel.getDatatypeProperty(NAMESPACE + "size"), garden.getSize());
        gardenIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "location"), garden.getLocation());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the garden.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Garden added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteGarden(@RequestParam("URI") String gardenURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual gardenIndividual = ontModel.getIndividual(gardenURI);

        if (gardenIndividual != null) {
            gardenIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the garden.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Garden deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Garden not found.");
        }
    }
}
