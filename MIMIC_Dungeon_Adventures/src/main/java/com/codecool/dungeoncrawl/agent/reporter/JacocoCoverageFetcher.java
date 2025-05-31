package com.codecool.dungeoncrawl.agent.reporter;


import org.json.JSONException;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

import java.io.StringReader;
import java.util.ArrayList;


public class JacocoCoverageFetcher {

    /**
     * Fetches the total coverage data from the JaCoCo HTML report
     * @param packageName the name of the package
     * @return the coverage data as a JSONObject
     * @throws IOException if an I/O error occurs
     * @throws JSONException if a JSON error occurs
     */
    public static JSONObject fetchTotalCoverage(String packageName) throws IOException, JSONException {
        System.out.println("Fetching JaCoCo coverage from " + packageName + "...");
        String htmlFilePath = packageName + "/index.html";

        ArrayList<String> tags = new ArrayList<>();
        tags.add("html");
        tags.add("body");
        tags.add("table");
        tags.add("tfoot");
        tags.add("tr");

        Element elem = fetchDataUnderLabels(htmlFilePath, tags);
        assert elem != null;
        String[] cov = elem.text().replaceAll(",", "").replaceAll("[^0-9]", " ").replaceAll(" +", " ").trim().split(" ");

        Double[] coverageData = new Double[cov.length];

        for (int i = 0; i < cov.length; i++) {
            coverageData[i] = Double.parseDouble(cov[i]);
        }

        JSONObject coverage = new JSONObject();
        coverage.put("Timestamp", packageName.split("_")[1]);

        coverage.put("Covered Instructions", coverageData[1] - coverageData[0]);
        coverage.put("Total Instructions", coverageData[1]);
        coverage.put("Instructions Coverage", (coverageData[1] - coverageData[0]) / coverageData[1] * 100);

        coverage.put("Covered Branches", coverageData[4] - coverageData[3]);
        coverage.put("Total Branches", coverageData[4]);
        coverage.put("Branches Coverage", (coverageData[4] - coverageData[3]) / coverageData[4] * 100);

        coverage.put("Covered Cxty", coverageData[7] - coverageData[6]);
        coverage.put("Total Cxty", coverageData[7]);
        coverage.put("Cxty Coverage", (coverageData[7] - coverageData[6]) / coverageData[7] * 100);

        coverage.put("Covered Lines", coverageData[9] - coverageData[8]);
        coverage.put("Total Lines", coverageData[9]);
        coverage.put("Lines Coverage", (coverageData[9] - coverageData[8]) / coverageData[9] * 100);

        coverage.put("Covered Methods", coverageData[11] - coverageData[10]);
        coverage.put("Total Methods", coverageData[11]);
        coverage.put("Methods Coverage", (coverageData[11] - coverageData[10]) / coverageData[11] * 100);

        coverage.put("Covered Classes", coverageData[13] - coverageData[12]);
        coverage.put("Total Classes", coverageData[13]);
        coverage.put("Classes Coverage", (coverageData[13] - coverageData[12]) / coverageData[13] * 100);

        System.out.println(coverage);

        return coverage;
    }

    /**
     * Fetches data from the HTML file under the specified tags
     * @param htmlFilePath path to the HTML file
     * @param tags list of tags to traverse
     * @return the element found under the specified tags
     * @throws IOException if an I/O error occurs
     */
    public static Element fetchDataUnderLabels(String htmlFilePath, ArrayList<String> tags) throws IOException {
        File input = new File(htmlFilePath);
        Element currentElement = Jsoup.parse(input, "UTF-8");

        for (String tag : tags) {
            assert currentElement != null;
            Elements elements = currentElement.select(tag);
            if (elements.isEmpty()) {
                return null;
            }
            currentElement = elements.first();
        }

        return currentElement;
    }

    /**
     * Fetches the total coverage data from the JaCoCo XML report
     * @param xmlFilePath the path to the XML file
     * @return the coverage data as a JSONObject
     * @throws Exception if an error occurs
     */
    public static JSONObject fetchTotalCoverageFromXML(String xmlFilePath) throws Exception {
        System.out.println("Fetching JaCoCo coverage from " + xmlFilePath + "...");

        // Parse the XML file with DTD validation disabled
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> {
            // Ignore the DTD file and provide an empty input source
            return new InputSource(new StringReader(""));
        });

        Document doc = builder.parse(new File(xmlFilePath));
        doc.getDocumentElement().normalize();

        // Fetch coverage data from the XML structure
        NodeList counters = doc.getElementsByTagName("counter");

        JSONObject coverage = new JSONObject();
        coverage.put("Timestamp", xmlFilePath.split("_")[1].replace(".xml", ""));

        // Get the last 6 counter nodes in the XML as the coverage data
        for (int i = counters.getLength() - 6; i < counters.getLength(); i++) {
            Node counterNode = counters.item(i);
            org.w3c.dom.Element counterElement = (org.w3c.dom.Element) counterNode;

            String type = counterElement.getAttribute("type");
            int missed = Integer.parseInt(counterElement.getAttribute("missed"));
            int covered = Integer.parseInt(counterElement.getAttribute("covered"));
            int total = missed + covered;
            double coveragePercentage = ((double) covered / total) * 100;

            switch (type) {
                case "INSTRUCTION":
                    coverage.put("Covered Instructions", covered);
                    coverage.put("Total Instructions", total);
                    coverage.put("Instructions Coverage", coveragePercentage);
                    break;
                case "BRANCH":
                    coverage.put("Covered Branches", covered);
                    coverage.put("Total Branches", total);
                    coverage.put("Branches Coverage", coveragePercentage);
                    break;
                case "COMPLEXITY":
                    coverage.put("Covered Cxty", covered);
                    coverage.put("Total Cxty", total);
                    coverage.put("Cxty Coverage", coveragePercentage);
                    break;
                case "LINE":
                    coverage.put("Covered Lines", covered);
                    coverage.put("Total Lines", total);
                    coverage.put("Lines Coverage", coveragePercentage);
                    break;
                case "METHOD":
                    coverage.put("Covered Methods", covered);
                    coverage.put("Total Methods", total);
                    coverage.put("Methods Coverage", coveragePercentage);
                    break;
                case "CLASS":
                    coverage.put("Covered Classes", covered);
                    coverage.put("Total Classes", total);
                    coverage.put("Classes Coverage", coveragePercentage);
                    break;
            }
        }

        System.out.println(coverage);

        return coverage;
    }
}