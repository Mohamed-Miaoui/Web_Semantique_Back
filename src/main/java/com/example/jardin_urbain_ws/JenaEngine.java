package com.example.jardin_urbain_ws;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.jena.query.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.util.FileManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class JenaEngine {
    private static final String RDF = "http://www.example.org/ontologies/2022/10/donnateur#";

    public static Model readModel(String owlFilePath) {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = FileManager.get().open(owlFilePath)) {
            model.read(in, "");
        } catch (IOException e) {
            System.out.println("Ontology file: " + owlFilePath + " not found");
            return null;
        }
        return model;
    }

    public static Model applyInferenceRules(Model model, String ruleFilePath) {
        try (InputStream in = FileManager.get().open(ruleFilePath)) {
            List<Rule> rules = Rule.rulesFromURL(ruleFilePath);
            GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
            reasoner.setOWLTranslation(true);
            reasoner.setTransitiveClosureCaching(true);
            return ModelFactory.createInfModel(reasoner, model);
        } catch (IOException e) {
            System.out.println("Rule File: " + ruleFilePath + " not found");
            return null;
        }
    }

    public static OutputStream executeQuery(Model model, String queryString) {
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qe.execSelect();
            OutputStream output = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(output, results);
            return output;
        }
    }

    public static OutputStream executeQueryFromFile(Model model, String queryFilePath) {
        File queryFile = new File(queryFilePath);
        String queryString = FileTool.getContents(queryFile);
        return executeQuery(model, queryString);
    }

    public static String executeQueryAsJSON(Model model, String queryFilePath) {
        try {
            // Load the SPARQL query from the file
            String queryString = new String(Files.readAllBytes(Paths.get(queryFilePath)), StandardCharsets.UTF_8);

            // Create and execute the query
            Query query = QueryFactory.create(queryString);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                ResultSet results = qexec.execSelect();

                // Convert result set to JSON
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsJSON(outputStream, results);
                return outputStream.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (QueryParseException e) {
            e.printStackTrace();
            return "Error parsing SPARQL query: " + e.getMessage();
        }
    }

    public static void executeUpdate(Dataset dataset, String updateString) {
        UpdateRequest updateRequest = UpdateFactory.create(updateString);
        UpdateExecutionFactory.create(updateRequest, dataset).execute();
    }

    public static void writeModelToFile(Model model, String filePath) {
        try (OutputStream out = new FileOutputStream(filePath)) {
            model.write(out, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}