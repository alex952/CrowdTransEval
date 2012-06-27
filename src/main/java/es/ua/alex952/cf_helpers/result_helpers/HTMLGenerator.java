/**
 * CrowdTransEval, a toolkit for evaluating machine translation system by using
 * crowdsourcing. Copyright (C) 2012 Alejandro Navarro Fulleda <anf5@alu.ua.es>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package es.ua.alex952.cf_helpers.result_helpers;

import es.ua.alex952.cf_helpers.params.JSONParams;
import es.ua.alex952.cf_helpers.translation_services.Service;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <<Singleton>>
 *
 * @author alex952
 */
public class HTMLGenerator {

	private String headTop = "";
	private String head = "";
	private String headBottom = "";
	private String bodyTop = "";
	private String body = "";
	private String bodyBottom = "";
	private String htmlGenerated = "";
	private static HTMLGenerator instance = null;

	/**
	 * Private constructor
	 */
	private HTMLGenerator() {
	}

	/**
	 * Entrance point of the class (Singleton pattern).
	 *
	 * @return Unique instance of the class
	 */
	public static HTMLGenerator getGenerator() {
		if (instance == null) {
			instance = new HTMLGenerator();
		}

		return instance;
	}

	/**
	 * Utility function that generates the CML associated to the Job to put it
	 * into the creation parameters.
	 *
	 * @return The CML generated.
	 */
	public static String generateCML(Service[] services) {
		String cml = "<p><strong>Original sentence: </strong>{{lo}}</p>"
				+ "<p><strong>Reference translation: </strong>{{tr}}</p>";

		cml += "<table>";

		for (Service c : services) {

			cml += "<tr>";

			cml += "<td>{{" + c.getName() + "}}</td>"
					+ "<td>"
					+ "<cml:ratings validates=\"required\" label=\"Adequacy\" name=\"adequacy_" + c.getName() + "\" gold=\"true\">"
					+ "<cml:rating label=\"0\" value=\"0\" />"
					+ "<cml:rating label=\"1\" value=\"1\" />"
					+ "<cml:rating label=\"2\" value=\"2\" />"
					+ "<cml:rating label=\"3\" value=\"3\" />"
					+ "<cml:rating label=\"4\" value=\"4\" />"
					+ "<cml:rating label=\"5\" value=\"5\" />"
					+ "</cml:ratings>"
					+ "</td>"
					+ "<td>"
					+ "<cml:ratings validates=\"required\" label=\"Fluency\" name=\"fluency_" + c.getName() + "\" gold=\"true\">"
					+ "<cml:rating label=\"0\" value=\"0\" />"
					+ "<cml:rating label=\"1\" value=\"1\" />"
					+ "<cml:rating label=\"2\" value=\"2\" />"
					+ "<cml:rating label=\"3\" value=\"3\" />"
					+ "<cml:rating label=\"4\" value=\"4\" />"
					+ "<cml:rating label=\"5\" value=\"5\" />"
					+ "</cml:ratings>"
					+ "</td>";

			cml += "</tr>";
		}

		cml += "</table>";

		return cml;
	}

	/**
	 * Generates charts representing the Kappa average for each service (fluency
	 * and adequacy).
	 *
	 * @param raters Hashmap of pairs of raters
	 */
	public void generateKappaAverageCharts(HashMap<String, HashMap<String, KappaRaters>> raters) {
		String chart = "var adequacy = new google.visualization.DataTable();\n"
				+ "adequacy.addColumn(\"string\", \"Service\");\n"
				+ "adequacy.addColumn(\"number\", \"Adequacy\");\n"
				+ "var fluency = new google.visualization.DataTable();\n"
				+ "fluency.addColumn(\"string\", \"Service\");\n"
				+ "fluency.addColumn(\"number\", \"Fluency\");\n";

		String adata = "";
		String fdata = "";

		Set<String> services = (Set<String>) raters.keySet();
		int scount = 0;

		for (String service : services) {
			HashMap<String, KappaRaters> serviceRatters = raters.get(service);

			double f = 0.0;
			double a = 0.0;

			for (String key : serviceRatters.keySet()) {
				f += serviceRatters.get(key).getFluencyKappa();
				a += serviceRatters.get(key).getAdequacyKappa();
			}

			f /= serviceRatters.size();
			a /= serviceRatters.size();

			adata += "['" + service + "', " + a + "]";
			fdata += "['" + service + "', " + f + "]";

			if (scount++ < services.size() - 1) {
				adata += ",";
				fdata += ",";
			}
		}

		chart += "adequacy.addRows([\n"
				+ adata
				+ "]);\n";

		chart += "fluency.addRows([\n"
				+ fdata
				+ "]);\n";

		chart += "var optionsA = {\n"
				+ "title: 'Translation\\'s Adequacy by translation service',\n"
				+ "hAxis: {title: 'Adequacy', titleTextStyle: {color: 'red'}}};\n";

		chart += "var optionsF = {\n"
				+ "title: 'Translation\\'s Fluency by translation service',\n"
				+ "hAxis: {title: 'Fluency', titleTextStyle: {color: 'red'}}};\n";

		chart += "var chartA = new google.visualization.ColumnChart(document.getElementById('kappa_average_adequacy'));\n";
		chart += "var chartF = new google.visualization.ColumnChart(document.getElementById('kappa_average_fluency'));\n";
		chart += "chartA.draw(adequacy, optionsA);\n";
		chart += "chartF.draw(fluency, optionsF);\n";

		head += chart;
		body += "<h2>Kappa averages by service</h2>\n"
				+ "<div id='kappa_average_adequacy'></div>\n"
				+ "<div id='kappa_average_fluency'></div>\n";
	}

	public void generateScoreCharts(JSONParams judgments, Service[] services) {
		String chart = "var adequacy = new google.visualization.DataTable();\n"
				+ "adequacy.addColumn(\"string\", \"Service\");\n"
				+ "adequacy.addColumn(\"number\", \"Adequacy\");\n"
				+ "var fluency = new google.visualization.DataTable();\n"
				+ "fluency.addColumn(\"string\", \"Service\");\n"
				+ "fluency.addColumn(\"number\", \"Fluency\");\n";

		String adata = "";
		String fdata = "";

		Double[] acumFluencyServices = new Double[services.length];
		Double[] acumAdequacyServices = new Double[services.length];
		Iterator<String> unitsIterator = judgments.getKeySet();
		while (unitsIterator.hasNext()) {
			String u = unitsIterator.next();
			JSONParams unit = judgments.getObject(u);

			for (int i = 0; i < services.length; i++) {
				String service = services[i].getName();
				acumFluencyServices[i] = Double.parseDouble(unit.getObject("fluency_" + service).getProperty("avg"));
				acumAdequacyServices[i] = Double.parseDouble(unit.getObject("adequacy_" + service).getProperty("avg"));
			}
		}

		for (int i = 0; i < services.length; i++) {
			acumFluencyServices[i] /= judgments.size();
			acumAdequacyServices[i] /= judgments.size();
		}

		for (int i = 0; i < services.length; i++) {
			String service = services[i].getName();

			adata += "['" + service + "', " + acumAdequacyServices[i] + "]";
			fdata += "['" + service + "', " + acumFluencyServices[i] + "]";

			if (i < services.length - 1) {
				adata += ",";
				fdata += ",";
			}
		}

		chart += "adequacy.addRows([\n"
				+ adata
				+ "]);\n";

		chart += "fluency.addRows([\n"
				+ fdata
				+ "]);\n";

		chart += "var optionsA = {\n"
				+ "title: 'Translation\\'s Adequacy by translation service',\n"
				+ "hAxis: {title: 'Adequacy', titleTextStyle: {color: 'red'}}};\n";

		chart += "var optionsF = {\n"
				+ "title: 'Translation\\'s Fluency by translation service',\n"
				+ "hAxis: {title: 'Fluency', titleTextStyle: {color: 'red'}}};\n";

		chart += "var chartA = new google.visualization.ColumnChart(document.getElementById('score_average_adequacy'));\n";
		chart += "var chartF = new google.visualization.ColumnChart(document.getElementById('score_average_fluency'));\n";
		chart += "chartA.draw(adequacy, optionsA);\n";
		chart += "chartF.draw(fluency, optionsF);\n";

		head += chart;
		body += "<h2>Adequacy/Fluency averages by service</h2>\n"
				+ "<div id='score_average_adequacy'></div>\n"
				+ "<div id='score_average_fluency'></div>\n";
	}

	/**
	 * Generates a table for each service containing the info about factor Kappa
	 * calculated every pair of raters.
	 *
	 * @param raters Hashmap of pairs of raters
	 */
	public void generateKappaTable(HashMap<String, HashMap<String, KappaRaters>> raters) {

		for (String service : raters.keySet()) {
			HashMap<String, KappaRaters> serviceRatters = raters.get(service);

			body += "<h2>Kappa results for service " + service + "</h2>\n"
					+ "<h3>Fluency kappa</h3>\n"
					+ "<div id=\"kappa_fluency_" + service + "\"></div>\n"
					+ "<h3>Adequacy kappa</h3>\n"
					+ "<div id=\"kappa_adequacy_" + service + "\"></div>\n";

			HashSet<Integer> leftPart = new HashSet<Integer>();
			HashSet<Integer> rightPart = new HashSet<Integer>();
			for (String key : serviceRatters.keySet()) {
				KappaRaters rater = serviceRatters.get(key);
				leftPart.add(rater.getRaterA());
				rightPart.add(rater.getRaterB());
			}

			String dataFluency = "var dataFluency = google.visualization.arrayToDataTable([\n"
					+ "[ ' ', ";

			String dataAdequacy = "var dataAdequacy = google.visualization.arrayToDataTable([\n"
					+ "[ ' ', ";
			int count = 0;

			for (Integer right : rightPart) {
				dataFluency += "'" + right.toString() + "'";
				dataAdequacy += "'" + right.toString() + "'";

				if (count++ < rightPart.size() - 1) {
					dataFluency += ", ";
					dataAdequacy += ", ";
				}
			}

			dataFluency += "],\n";
			dataAdequacy += "],\n";
			int countl = 0;
			int countr = 0;

			for (Integer left : leftPart) {
				dataFluency += "[ '" + left + "', ";
				dataAdequacy += "[ '" + left + "', ";

				for (Integer right : rightPart) {
					if (serviceRatters.containsKey(left + "/" + right)) {
						KappaRaters k = serviceRatters.get(left + "/" + right);

						dataFluency += "'" + k.getFluencyKappa() + "'";
						dataAdequacy += "'" + k.getAdequacyKappa() + "'";
					} else {
						dataFluency += "false";
						dataAdequacy += "false";
					}

					if (countr++ < rightPart.size() - 1) {
						dataFluency += ", ";
						dataAdequacy += ", ";
					}
				}

				dataFluency += "]";
				dataAdequacy += "]";
				if (countl++ < rightPart.size() - 1) {
					dataFluency += ", ";
					dataAdequacy += ", ";
				}
				dataFluency += "\n";
				dataAdequacy += "\n";
			}

			dataFluency += "]);\n";
			dataAdequacy += "]);\n";

			head += dataAdequacy + dataFluency
					+ "visualizationFluency = new google.visualization.Table(document.getElementById('kappa_fluency_" + service + "'));\n"
					+ "visualizationAdequacy = new google.visualization.Table(document.getElementById('kappa_adequacy_" + service + "'));\n"
					+ "visualizationFluency.draw(dataFluency, null);\n"
					+ "visualizationAdequacy.draw(dataAdequacy, null);\n";
		}
	}

	/*
	 * public String generateHTML(HashMap<String, HashMap<String, KappaRaters>>
	 * raters) { this.generateSurrondings(); return this.htmlGenerated;
	}
	 */
	/**
	 * Returns the html generated by the above procedures along with the headers
	 * and extra tags for a complete html output.
	 *
	 * @return Complete HTML file (String) with all the contente generated
	 */
	public String getHTML() {
		String html = "";

		if (this.headTop == null || this.headTop.equals("")) {
			generateSurrondings();
		}

		html = this.headTop + this.head + this.headBottom
				+ this.bodyTop + this.body + this.bodyBottom;

		return html;
	}

	/**
	 * Generate the headers and extra tags of the resultant HTML.
	 */
	private void generateSurrondings() {
		this.headTop = "<html>\n"
				+ "<head>\n"
				+ "<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n"
				+ "<script type=\"text/javascript\">\n"
				+ "google.load(\"visualization\", \"1\", {packages:[\"corechart\", \"table\"]});\n"
				+ "google.setOnLoadCallback(drawCharts);\n"
				+ "function drawCharts() {\n";


		this.headBottom = "}\n"
				+ "</script>\n"
				+ "</head>\n";

		this.bodyTop = "<body>\n";
		this.bodyBottom = "</body>\n</html>";
	}

	/**
	 * See {@link HTMLGenerator#getHTML()}
	 *
	 * @return
	 */
	public String toString() {
		return this.getHTML();
	}

	public static void main(String[] args) {
		HTMLGenerator gen = HTMLGenerator.getGenerator();

		HashMap<String, HashMap<String, KappaRaters>> rater = new HashMap<String, HashMap<String, KappaRaters>>();
		rater.put("google", new HashMap<String, KappaRaters>());
		rater.put("bing", new HashMap<String, KappaRaters>());

		HashMap<String, KappaRaters> googleRaters = rater.get("google");
		KappaRaters g12 = new KappaRaters(1, 2);
		KappaRaters g13 = new KappaRaters(1, 3);

		g12.addAdequacy(2, 1);
		g12.addAdequacy(3, 4);
		g12.addAdequacy(4, 4);
		g12.addAdequacy(4, 4);
		g12.addAdequacy(4, 1);
		g12.addAdequacy(4, 2);
		g12.addAdequacy(4, 2);
		g12.addAdequacy(4, 2);
		g12.addAdequacy(0, 2);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 0);
		g12.addAdequacy(0, 4);
		g12.addAdequacy(0, 2);
		g12.addAdequacy(0, 2);
		g12.addAdequacy(0, 2);
		g12.addAdequacy(0, 2);
		g12.addAdequacy(4, 2);
		g12.addAdequacy(3, 4);
		g12.addAdequacy(1, 2);
		g12.addAdequacy(1, 2);
		g12.addAdequacy(1, 2);
		g12.addAdequacy(1, 2);
		g12.addAdequacy(1, 2);
		g12.addAdequacy(1, 2);
		g12.addAdequacy(2, 4);
		g12.addAdequacy(2, 1);
		g12.addAdequacy(5, 5);
		g12.addAdequacy(5, 5);
		g12.addAdequacy(5, 5);
		g12.addAdequacy(5, 5);
		g12.addAdequacy(5, 5);
		g12.addAdequacy(5, 5);
		g12.addAdequacy(3, 4);
		g12.addAdequacy(3, 4);
		
		g12.addFluency(2, 1);
		g12.addFluency(3, 4);
		g12.addFluency(4, 4);
		g12.addFluency(4, 4);
		g12.addFluency(4, 1);
		g12.addFluency(4, 2);
		g12.addFluency(4, 2);
		g12.addFluency(4, 2);
		g12.addFluency(0, 2);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 0);
		g12.addFluency(0, 4);
		g12.addFluency(0, 2);
		g12.addFluency(0, 2);
		g12.addFluency(0, 2);
		g12.addFluency(0, 2);
		g12.addFluency(4, 2);
		g12.addFluency(3, 4);
		g12.addFluency(1, 2);
		g12.addFluency(1, 2);
		g12.addFluency(1, 2);
		g12.addFluency(1, 2);
		g12.addFluency(1, 2);
		g12.addFluency(1, 2);
		g12.addFluency(2, 4);
		g12.addFluency(2, 1);
		g12.addFluency(5, 5);
		g12.addFluency(5, 5);
		g12.addFluency(5, 5);
		g12.addFluency(5, 5);
		g12.addFluency(5, 5);
		g12.addFluency(5, 5);
		g12.addFluency(3, 4);
		g12.addFluency(3, 4);
		
		g13.addAdequacy(2, 1);
		g13.addAdequacy(3, 4);
		g13.addAdequacy(4, 4);
		g13.addAdequacy(4, 4);
		g13.addAdequacy(4, 1);
		g13.addAdequacy(4, 2);
		g13.addAdequacy(4, 2);
		g13.addAdequacy(4, 2);
		g13.addAdequacy(0, 2);
		g13.addAdequacy(0, 0);
		g13.addAdequacy(0, 0);
		g13.addAdequacy(0, 0);
		g13.addAdequacy(0, 0);
		g13.addAdequacy(0, 0);
		g13.addAdequacy(0, 4);
		g13.addAdequacy(0, 2);
		g13.addAdequacy(0, 2);
		g13.addAdequacy(0, 2);
		g13.addAdequacy(0, 2);
		g13.addAdequacy(4, 2);
		g13.addAdequacy(3, 4);
		g13.addAdequacy(1, 2);
		g13.addAdequacy(1, 2);
		g13.addAdequacy(1, 2);
		g13.addAdequacy(1, 2);
		g13.addAdequacy(1, 2);
		g13.addAdequacy(1, 2);
		g13.addAdequacy(2, 4);
		g13.addAdequacy(2, 1);
		g13.addAdequacy(5, 5);
		g13.addAdequacy(5, 5);
		g13.addAdequacy(5, 5);
		g13.addAdequacy(5, 5);
		g13.addAdequacy(5, 5);
		g13.addAdequacy(5, 5);
		g13.addAdequacy(3, 4);
		g13.addAdequacy(3, 4);
		
		g13.addFluency(2, 1);
		g13.addFluency(3, 4);
		g13.addFluency(4, 4);
		g13.addFluency(4, 4);
		g13.addFluency(4, 1);
		g13.addFluency(4, 2);
		g13.addFluency(4, 2);
		g13.addFluency(4, 2);
		g13.addFluency(0, 2);
		g13.addFluency(0, 0);
		g13.addFluency(0, 0);
		g13.addFluency(0, 0);
		g13.addFluency(0, 0);
		g13.addFluency(0, 0);
		g13.addFluency(0, 4);
		g13.addFluency(0, 2);
		g13.addFluency(0, 2);
		g13.addFluency(0, 2);
		g13.addFluency(0, 2);
		g13.addFluency(4, 2);
		g13.addFluency(3, 4);
		g13.addFluency(1, 2);
		g13.addFluency(1, 2);
		g13.addFluency(1, 2);
		g13.addFluency(1, 2);
		g13.addFluency(1, 2);
		g13.addFluency(1, 2);
		g13.addFluency(2, 4);
		g13.addFluency(2, 1);
		g13.addFluency(5, 5);
		g13.addFluency(5, 5);
		g13.addFluency(5, 5);
		g13.addFluency(5, 5);
		g13.addFluency(5, 5);
		g13.addFluency(5, 5);
		g13.addFluency(3, 4);
		g13.addFluency(3, 4);


		googleRaters.put("1/2", g12);
		//googleRaters.put("1/3", g13);

		HashMap<String, KappaRaters> bingRaters = rater.get("bing");
		//bingRaters.put("1/2", g12);
		bingRaters.put("1/3", g13);

		gen.generateKappaTable(rater);
		gen.generateKappaAverageCharts(rater);

		String html = gen.getHTML();


		try {
			FileOutputStream fos = new FileOutputStream(new File("debug.html"));
			fos.write(html.getBytes());
			fos.close();
			Desktop.getDesktop().open(new File("debug.html"));
		} catch (IOException e) {
		}
	}
}
