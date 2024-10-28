package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Event;
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
@RequestMapping(path = "event", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class Bayoudh_Event {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf"; // Adjust path as needed

    public Bayoudh_Event() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping()
    public ResponseEntity<String> getEvents() {
        // Load the model before querying to ensure it has the latest data
        loadModel();

        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "PREFIX j0: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "SELECT ?event ?name ?date ?location ?sponsor ?sponsorName\n" +
                "WHERE {\n" +
                "    ?event a ont:Event .\n" +
                "    ?event ont:name ?name .\n" +
                "    ?event ont:date ?date .\n" +
                "    ?event ont:location ?location .\n" +
                "    OPTIONAL { \n" +
                "        ?sponsor j0:funds ?event .\n" +
                "        OPTIONAL { \n" +
                "            ?sponsor ont:name ?sponsorName .\n" +
                "        }\n" +
                "    }\n" +
                "}";

        // Execute the query
        try (QueryExecution qe = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qe.execSelect();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);

            // Return the JSON response
            return ResponseEntity.ok(new String(outputStream.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing query: " + e.getMessage());
        }
    }


    @PostMapping
    public ResponseEntity<String> addEvent(@RequestBody Event event) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual eventIndividual = ontModel.createIndividual(NAMESPACE + "Event_" + System.currentTimeMillis(),
                ontModel.getOntClass(NAMESPACE + "Event"));
        eventIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "name"), event.getName());
        eventIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "date"), event.getDate());
        eventIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "location"), event.getLocation());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the event.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Event added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteEvent(@RequestParam("URI") String eventURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual eventIndividual = ontModel.getIndividual(eventURI);

        if (eventIndividual != null) {
            eventIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the event.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Event deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }
    }

    @PutMapping
    public ResponseEntity<String> updateEvent(@RequestParam("URI") String eventURI, @RequestBody Event updatedEvent) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual eventIndividual = ontModel.getIndividual(eventURI);

        if (eventIndividual != null) {
            // Update properties
            eventIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "name"));
            eventIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "name"), updatedEvent.getName());

            eventIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "date"));
            eventIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "date"), updatedEvent.getDate());

            eventIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "location"));
            eventIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "location"), updatedEvent.getLocation());

            // Save changes to RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the event.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Event updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }
    }
    @PostMapping("/assign-sponsor")
    public ResponseEntity<String> assignSponsorToEvent(@RequestParam("eventURI") String eventURI,
                                                       @RequestParam("sponsorURI") String sponsorURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Load existing RDF model
        loadModel();

        // Log the URIs for debugging
        System.out.println("Event URI: " + eventURI);
        System.out.println("Sponsor URI: " + sponsorURI.trim());

        Individual eventIndividual = ontModel.getIndividual(eventURI);
        Individual sponsorIndividual = ontModel.getIndividual(sponsorURI.trim());

        if (eventIndividual == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found: " + eventURI);
        }
        if (sponsorIndividual == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sponsor not found: " + sponsorURI);
        }

        try {
            // Check if the funds relationship already exists
            boolean alreadyAssigned = ontModel.listStatements(sponsorIndividual,
                    ontModel.getObjectProperty(NAMESPACE + "funds"), eventIndividual).hasNext();

            if (alreadyAssigned) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Sponsor already assigned to provide funds for this event.");
            }

            // Assign sponsor to event using the funds property
            sponsorIndividual.addProperty(ontModel.getObjectProperty(NAMESPACE + "funds"), eventIndividual);

            // Save changes to RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Sponsor assigned to event successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save the model: " + e.getMessage());
        }
    }


}
