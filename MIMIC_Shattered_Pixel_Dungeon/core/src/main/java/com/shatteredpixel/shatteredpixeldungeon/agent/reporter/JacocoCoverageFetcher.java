package com.shatteredpixel.shatteredpixeldungeon.agent.reporter;


import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;


public class JacocoCoverageFetcher {

    private static final String[] EXCLUSIONS = {
            "/com.shatteredpixel.shatteredpixeldungeon.agent/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.APIs/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.journal/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.messages/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.services.news/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.services.updates/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.scenes/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.ui/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.windows/index.html",
            "/com.shatteredpixel.shatteredpixeldungeon.utils/index.html",

            "/com.shatteredpixel.shatteredpixeldungeon/Challenges.html",
            "/com.shatteredpixel.shatteredpixeldungeon/Chrome.html",
            "/com.shatteredpixel.shatteredpixeldungeon/Dungeon.html",
            "/com.shatteredpixel.shatteredpixeldungeon/GamesInProgress.html",
            "/com.shatteredpixel.shatteredpixeldungeon/QuickSlot.html",
            "/com.shatteredpixel.shatteredpixeldungeon/Rankings.html",
            "/com.shatteredpixel.shatteredpixeldungeon/SPDAction.html",
            "/com.shatteredpixel.shatteredpixeldungeon/SPDSettings.html",
            "/com.shatteredpixel.shatteredpixeldungeon/Statistics.html",
    };
    private static final List<String> EXCLUDED_PACKAGES = Arrays.asList(
            "com/shatteredpixel/shatteredpixeldungeon/agent/",
            "com/shatteredpixel/shatteredpixeldungeon/APIs/",
            "com/shatteredpixel/shatteredpixeldungeon/journal/",
            "com/shatteredpixel/shatteredpixeldungeon/messages/",
            "com/shatteredpixel/shatteredpixeldungeon/services/news/",
            "com/shatteredpixel/shatteredpixeldungeon/services/updates/",
            "com/shatteredpixel/shatteredpixeldungeon/scenes/",
            "com/shatteredpixel/shatteredpixeldungeon/ui/",
            "com/shatteredpixel/shatteredpixeldungeon/windows/",
            "com/shatteredpixel/shatteredpixeldungeon/utils/"
            );

    private static final List<String> EXCLUDED_METHODS = Arrays.asList(
            "com/shatteredpixel/shatteredpixeldungeon/Challenges",
            "com/shatteredpixel/shatteredpixeldungeon/Chrome",
            "com/shatteredpixel/shatteredpixeldungeon/Dungeon",
            "com/shatteredpixel/shatteredpixeldungeon/GamesInProgress",
            "com/shatteredpixel/shatteredpixeldungeon/actors/hero/HeroAction",
            "com/shatteredpixel/shatteredpixeldungeon/QuickSlot",
            "com/shatteredpixel/shatteredpixeldungeon/Rankings",
            "com/shatteredpixel/shatteredpixeldungeon/SPDAction",
            "com/shatteredpixel/shatteredpixeldungeon/SPDSettings",
            "com/shatteredpixel/shatteredpixeldungeon/Statistics",
            "com/shatteredpixel/shatteredpixeldungeon/items/wands/Wand",
            "com/shatteredpixel/shatteredpixeldungeon/items/weapon/melee/MagesStaff"
    );

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

        for (String excludedFile : EXCLUSIONS) {
            pureCoverageData(coverage, packageName + excludedFile);
        }

        System.out.println(coverage);

        return coverage;
    }

    private static void pureCoverageData(JSONObject coverage, String excludedFile) throws IOException {
        ArrayList<String> tags = new ArrayList<>();
        tags.add("html");
        tags.add("body");
        tags.add("table");
        tags.add("tfoot");
        tags.add("tr");

        Element elem = fetchDataUnderLabels(excludedFile, tags);
        assert elem != null;
        String[] cov = elem.text().replaceAll(",", "").replaceAll("[^0-9]", " ").replaceAll(" +", " ").trim().split(" ");

        Double[] coverageData = new Double[cov.length];

        for (int i = 0; i < cov.length; i++) {
            coverageData[i] = Double.parseDouble(cov[i]);
        }

        coverage.put("Covered Instructions", (Double) coverage.get("Covered Instructions") - (coverageData[1] - coverageData[0]));
        coverage.put("Total Instructions", (Double) coverage.get("Total Instructions") - coverageData[1]);
        coverage.put("Instructions Coverage", (Double) coverage.get("Covered Instructions") / (Double) coverage.get("Total Instructions") * 100);

        coverage.put("Covered Branches", (Double) coverage.get("Covered Branches") - (coverageData[4] - coverageData[3]));
        coverage.put("Total Branches", (Double) coverage.get("Total Branches") - coverageData[4]);
        coverage.put("Branches Coverage", (Double) coverage.get("Covered Branches") / (Double) coverage.get("Total Branches") * 100);

        coverage.put("Covered Cxty", (Double) coverage.get("Covered Cxty") - (coverageData[7] - coverageData[6]));
        coverage.put("Total Cxty", (Double) coverage.get("Total Cxty") - coverageData[7]);
        coverage.put("Cxty Coverage", (Double) coverage.get("Covered Cxty") / (Double) coverage.get("Total Cxty") * 100);

        coverage.put("Covered Lines", (Double) coverage.get("Covered Lines") - (coverageData[9] - coverageData[8]));
        coverage.put("Total Lines", (Double) coverage.get("Total Lines") - coverageData[9]);
        coverage.put("Lines Coverage", (Double) coverage.get("Covered Lines") / (Double) coverage.get("Total Lines") * 100);

        coverage.put("Covered Methods", (Double) coverage.get("Covered Methods") - (coverageData[11] - coverageData[10]));
        coverage.put("Total Methods", (Double) coverage.get("Total Methods") - coverageData[11]);
        coverage.put("Methods Coverage", (Double) coverage.get("Covered Methods") / (Double) coverage.get("Total Methods") * 100);

        // If the excluded file is not an index.html file, then it is a class file; so, decrement the class coverage
        if (excludedFile.endsWith("/index.html")) {
            coverage.put("Covered Classes", (Double) coverage.get("Covered Classes") - (coverageData[13] - coverageData[12]));
            coverage.put("Total Classes", (Double) coverage.get("Total Classes") - coverageData[13]);
            coverage.put("Classes Coverage", (Double) coverage.get("Covered Classes") / (Double) coverage.get("Total Classes") * 100);

        } else {
            coverage.put("Covered Classes", (Double) coverage.get("Covered Classes") - 1);
            coverage.put("Total Classes", (Double) coverage.get("Total Classes") - 1);
            coverage.put("Classes Coverage", (Double) coverage.get("Covered Classes") / (Double) coverage.get("Total Classes") * 100);
        }
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

        pureCoverageDataFromXML(coverage, xmlFilePath, EXCLUDED_PACKAGES, EXCLUDED_METHODS);

        System.out.println(coverage);

        return coverage;
    }

    private static int counter = 0;

    /**
     * Fetches the pure coverage data from the JaCoCo XML report
     * @param coverage the coverage data
     * @param xmlFilePath the path to the XML file
     * @param excludedPackages the list of excluded packages
     * @param excludedMethods the list of excluded methods
     * @throws Exception if an error occurs
     */
    public static void pureCoverageDataFromXML(JSONObject coverage, String xmlFilePath, List<String> excludedPackages, List<String> excludedMethods) throws Exception {
        counter = 0;

        System.out.println("Purging coverage data from " + xmlFilePath + "...");

        // Parse the XML file with DTD validation disabled
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> {
            return new InputSource(new StringReader("")); // Ignore DTD
        });

        Document doc = builder.parse(new File(xmlFilePath));
        doc.getDocumentElement().normalize();

        // Iterate over all the <class> elements in the XML
        NodeList classNodes = doc.getElementsByTagName("class");

        for (int i = 0; i < classNodes.getLength(); i++) {
            Node classNode = classNodes.item(i);
            if (classNode.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element classElement = (org.w3c.dom.Element) classNode;
                String className = classElement.getAttribute("name");

                // If the class is in the excluded list, subtract its coverage
                if (isExcluded(excludedPackages, className) || excludedPackages.contains(className)) {
                    adjustCoverageForElement(classElement, coverage);
                }
            }
        }

        // Recalculate coverage percentages after exclusion
        recalculateCoveragePercentages(coverage);

        System.out.println("Pure coverage data from " + xmlFilePath + " finished with " + counter + " counters adjusted.");
    }

    private static boolean isExcluded(List<String> excludedPackages, String className) {
        for (String packageName : excludedPackages) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static void adjustCoverageForElement(org.w3c.dom.Element element, JSONObject coverage) {
        NodeList counterNodes = element.getElementsByTagName("counter");

        // The last 6 counter nodes in the XML are the coverage data for the whole class
        for (int i = counterNodes.getLength() - 6; i < counterNodes.getLength(); i++) {
            counter++;
            org.w3c.dom.Element counterElement = (org.w3c.dom.Element) counterNodes.item(i);
            String type = counterElement.getAttribute("type");
            int missed = Integer.parseInt(counterElement.getAttribute("missed"));
            int covered = Integer.parseInt(counterElement.getAttribute("covered"));
            int total = missed + covered;

            switch (type) {
                case "INSTRUCTION":
                    adjustCoverageData(coverage, "Instructions", missed, covered, total);
                    break;
                case "BRANCH":
                    adjustCoverageData(coverage, "Branches", missed, covered, total);
                    break;
                case "COMPLEXITY":
                    adjustCoverageData(coverage, "Cxty", missed, covered, total);
                    break;
                case "LINE":
                    adjustCoverageData(coverage, "Lines", missed, covered, total);
                    break;
                case "METHOD":
                    adjustCoverageData(coverage, "Methods", missed, covered, total);
                    break;
                case "CLASS":
                    adjustCoverageData(coverage, "Classes", missed, covered, total);
                    break;
            }
        }
    }

    private static void adjustCoverageData(JSONObject coverage, String keyPrefix, int missed, int covered, int total) {
        coverage.put("Covered " + keyPrefix, (Integer) coverage.get("Covered " + keyPrefix) - covered);
        coverage.put("Total " + keyPrefix, (Integer) coverage.get("Total " + keyPrefix) - total);

    }

    private static void recalculateCoveragePercentages(JSONObject coverage) {
        String[] keys = {"Instructions", "Branches", "Cxty", "Lines", "Methods", "Classes"};
        for (String key : keys) {
            double covered = (Integer) coverage.get("Covered " + key);
            double total = (Integer) coverage.get("Total " + key);
            if (total != 0) {
                coverage.put(key + " Coverage", (covered / total) * 100);
            } else {
                coverage.put(key + " Coverage", 0.0); // Avoid division by zero
            }
        }
    }
}