/**
 * CrowdTransEval, a toolkit for evaluating machine translation
 * system by using crowdsourcing.
 * Copyright (C) 2012 Alejandro Navarro Fulleda <anf5@alu.ua.es>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.ua.alex952.cf_helpers;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import es.ua.alex952.cf_helpers.params.JSONParams;
import es.ua.alex952.cf_helpers.result_helpers.HTMLGenerator;
import es.ua.alex952.cf_helpers.result_helpers.KappaRaters;
import es.ua.alex952.cf_helpers.translation_services.ApertiumTranslator;
import es.ua.alex952.cf_helpers.translation_services.BingTranslator;
import es.ua.alex952.cf_helpers.translation_services.Service;
import es.ua.alex952.exceptions.KeyNotConfigured;
import es.ua.alex952.exceptions.ParameterNeeded;
import java.io.*;
import java.util.*;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A concrete implementation of one resource, namely Jobs, and the concrete
 * methods that it can handle.
 *
 * @author alex952
 */
public class JobsCF extends CFHelper {

	/**
	 * The translation services of the application. See {@link Service}
	 */
	private Service[] services = null;
	private Integer unit_count = 0;
	private String pathLO;
	private String pathTR;
	private String pathGold;
	private SentenceShuffler shuffler;
	
	private String LO;
	private String LM;
	
	
	
	private final Logger logger = LoggerFactory.getLogger(JobsCF.class);
	
	private String[] channels;
	//TODO: remove
	private String pathApertium;
	private String pathGoogle;

	public void setPathApertium(String pathApertium) {
		this.pathApertium = pathApertium;
	}

	public void setPathGoogle(String pathGoogle) {
		this.pathGoogle = pathGoogle;
	}

	public void setPathLO(String pathLO) {
		this.pathLO = pathLO;
	}

	public void setPathTR(String pathTR) {
		this.pathTR = pathTR;
	}

	public void setPathGold(String pathGold) {
		this.pathGold = pathGold;
	}

	public void setServices(Service[] services) {
		this.services = services;
	}
	
	/**
	 * Default constructor of the class. It creates the object and assigns to it
	 * the needed paths for the web services invocation,
	 *
	 * @throws KeyNotConfigured
	 * @throws Exception
	 */
	public JobsCF(String configFile) throws KeyNotConfigured, Exception {
		super(configFile);

		ArrayList<String> urlPaths = new ArrayList<String>();
		urlPaths.add("jobs");

		this.paths = urlPaths;
		this.postPrepend = "job";
		this.configFile = configFile;

		this.dataParams = new JSONParams();
		
		if (this.configFile != null) {
			Properties p = new Properties();
			try{
				p.load(new FileInputStream(new File(this.configFile)));

				String s = p.getProperty("ShuffleGrade", "2");
				Integer grade = Integer.parseInt(s);

				this.shuffler = new SentenceShuffler(grade);

				//Source language and goal language
				this.LO = p.getProperty("SL");
				this.LM = p.getProperty("TL");

				//Initialize translation services
				//Possible dependency injection
				this.services = new Service[] { 
					new ApertiumTranslator(p.getProperty("ApertiumKey")),
					new BingTranslator(p.getProperty("BingClientId"), p.getProperty("BingClientSecret"))
				};
				
				this.channels = p.getProperty("Channels").split(",");
			} catch (IOException e) {
				this.shuffler = new SentenceShuffler(2);
			}
		}
	}

	/**
	 * Constructor that builds up the object with a properties file, which
	 * contains all the fields of the Job itself.
	 *
	 * @param propPath The path of the properties file.
	 * @throws KeyNotConfigured
	 * @throws IOException
	 */
	public JobsCF(String propPath, String configFile) throws KeyNotConfigured, IOException, Exception {
		this(configFile);
		
		Properties p = new Properties();
		p.load(new FileInputStream(propPath));

		for (String key_p : p.stringPropertyNames()) {
			this.addParameter(key_p, p.getProperty(key_p));
		}

		String cml = HTMLGenerator.generateCML(this.services);

		this.addParameter("cml", cml);
	}

	/**
	 * Function that processes the results of an specific job, known by the id
	 * stored in the {@link JSONParams} object of the class, and builds up the
	 * Google graph for the showcasing of them.
	 *
	 * @return The html graph generated with the results of the job.
	 */
	public String processResults() {
		String id = this.getParameter("id");
		HashMap<String, HashMap<String, KappaRaters>> raters = null;
		JSONParams judgmentsJSON = null;

		try {
			raters = this.getRatersJudgements();
			judgmentsJSON = this.getJudgments();
		} catch (Exception e) {
			System.err.println("Some error occured while getting the judgements for the job " + id + ": " + e.getMessage());

			return null;
		}
		
		HTMLGenerator generator = HTMLGenerator.getGenerator();
		generator.generateKappaTable(raters);
		generator.generateKappaAverageCharts(raters);
		generator.generateScoreCharts(judgmentsJSON, this.services);
		
		return generator.toString();
	}

	@Override
	public JSONParams create() throws IOException {
		JSONParams p = super.create();

		this.addParameter("id", p.getProperty("id"));

		return p;
	}

	/**
	 * Populates the, previously created, job with the data passed to it.
	 *
	 * @param data Data to populate the job, in JSON format.
	 * @param id The job id to be populated.
	 * @throws IOException
	 */
	private void populate(String data, String id) throws IOException {
		ArrayList<String> paths2 = (ArrayList<String>) this.paths.clone();
		paths2.add(id);
		paths2.add("upload");
		
		Map<String, String> query2 = (HashMap<String, String>)this.queryParams.clone();
		query2.put("force", "true");

		WebResource wr = ConnectionHelper.getResource(this.baseUrl, paths2, this.type, query2);

		String ret = wr.type(MediaType.APPLICATION_JSON).post(String.class, data);
	}

	/**
	 * Interface for the {@link JobsCF#populate(java.lang.String, java.lang.String)}
	 * using the id of the job already stored in the {@link JSONParams} object
	 * of the class.
	 *
	 * @throws IOException
	 */
	public void populate() throws IOException {
		this.populate(createGoldDataJSON(), this.getParameter("id"));
		this.populate(createDataJSON(), this.getParameter("id"));
		this.markGoldStandars();
	}
	
	/**
	 * Marks the uploaded gold data 
	 * ( created with {@link JobsCF#createGoldDataJSON()} ) 
	 * as gold in the 
	 * CrowdFlower server.
	 * 
	 * @throws IOException 
	 */
	private void markGoldStandars() throws IOException {
		ArrayList<String> paths2 = (ArrayList<String>) this.paths.clone();
		paths2.add(this.getParameter("id"));
		paths2.add("gold");
		
		WebResource wr = ConnectionHelper.getResource(this.baseUrl, paths2, this.type, this.queryParams);
		String ret = wr.put(String.class, "");
	}

	/**
	 * Creates the gold data to be uploaded in JSON format
	 * 
	 * @return String containing the gold data
	 * @throws IOException 
	 */
	private String createGoldDataJSON() throws IOException {
		JSONParams param = new JSONParams();
		String json = "";
		
		Integer nservices = this.services.length;
		Random r = new Random(new Date().getTime());
		
		FileInputStream fis = new FileInputStream(this.pathGold);
		DataInputStream dis = new DataInputStream(fis);

		BufferedReader br1 = new BufferedReader(new InputStreamReader(dis));
		
		while (true) {
			String lo = br1.readLine();
			String tr;
			String tr2;
			
			if (lo == null)
				break;
			
			tr = br1.readLine();
			tr2 = br1.readLine();
			
			param.addProperty("lo", lo);
			param.addProperty("tr", tr);
			
			int correct_position = r.nextInt(nservices);
			
			for (int i = 0; i < nservices; i++) {
				if (i == correct_position) {
					param.addProperty(this.services[i].getName(), tr2);
					param.addProperty("fluency_" + this.services[i].getName() + "_gold", "5");
					param.addProperty("fluency_" + this.services[i].getName() + "_gold_reason", "Some reason");
					param.addProperty("adequacy_" + this.services[i].getName() + "_gold", "5");
					param.addProperty("adequacy_" + this.services[i].getName() + "_gold_reason", "Some reason");
				} else {
					param.addProperty(this.services[i].getName(), this.shuffler.shuffle(tr2));
					param.addProperty("fluency_" + this.services[i].getName() + "_gold", "1");
					param.addProperty("fluency_" + this.services[i].getName() + "_gold_reason", "Some reason");
					param.addProperty("adequacy_" + this.services[i].getName() + "_gold", "1");
					param.addProperty("adequacy_" + this.services[i].getName() + "_gold_reason", "Some reason");
					
				}
			}
			param.addProperty("_golden", "TRUE");
			
			json += "\n" + param.toString();
			param = new JSONParams();
		}
		
		return json;
	}
	
	/**
	 * Creates the job data to be uploaded in JSON format
	 * 
	 * @return String containing the job data
	 * @throws IOException 
	 */
	private String createDataJSON() throws IOException {
		JSONParams param = new JSONParams();
		unit_count = 0;

		String json = "";

		FileInputStream fis = new FileInputStream(this.pathLO);
		DataInputStream dis = new DataInputStream(fis);

		BufferedReader br1 = new BufferedReader(new InputStreamReader(dis));


		FileInputStream fis2 = new FileInputStream(this.pathTR);
		DataInputStream dis2 = new DataInputStream(fis2);

		BufferedReader br2 = new BufferedReader(new InputStreamReader(dis2));

		String line1, line2;

		while ((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
			param = new JSONParams();

			param.addProperty("lo", line1);
			param.addProperty("tr", line2);

			for (Service c : this.services) {
				param.addProperty(c.getName(), c.getTranslation(line1, this.LO, this.LM));
			}

			json += "\n" + param.toString();

			unit_count++;
		}


		return json;
	}

	/**
	 * Queries the server to know whether the job is finished or not.
	 *
	 * @param id The jd of the job to be queried about.
	 * @return The finalization state of the job.
	 * @throws IOException
	 */
	private boolean isFinished(String id) throws IOException {
		JSONParams response = this.getInfo(id);

		String status = response.getProperty("state");

		return status != null && status.equals("finished");
	}

	/**
	 * Interface for the {@link JobsCF#isFinished(java.lang.String) }
	 * using the id of the job already stored in the {@link JSONParams} object
	 * of the class.
	 *
	 * @return The finalization state of the job.
	 * @throws IOException
	 */
	public boolean isFinished() throws IOException, ParameterNeeded {
		String id = null;
		if ((id = this.getParameter("id")) == null) {
			throw new ParameterNeeded("The id parameter was mandatory for the getJudgements method");
		}

		return this.isFinished(this.getParameter("id"));
	}

	/**
	 * Retrieves the results and populates a Hash which contains
	 * information needed by the Kappa calculus
	 *
	 * @return The results in the form of a Hash of services containing the every pair of raters and the 
	 * info about the rates given by them. See {@link KappaRaters}
	 * @throws IOException
	 * @throws ParameterNeeded If the id of the job is missing
	 */
	public HashMap<String, HashMap<String, KappaRaters>> getRatersJudgements() throws IOException, ParameterNeeded {
		//Initialization of all hashes based on service's name
		HashMap<String, HashMap<String, KappaRaters>> ratersHash = new HashMap<String, HashMap<String, KappaRaters>>();
		for(Service s: services) {
			ratersHash.put(s.getName(), new HashMap<String, KappaRaters>());
		}
		
		ArrayList<String> paths2 = (ArrayList<String>) this.paths.clone();

		String id = null;
		if ((id = this.getParameter("id")) == null) {
			throw new ParameterNeeded("The id parameter was mandatory for the getJudgements method");
		}

		paths2.add(id);
		paths2.add("units");
		
		WebResource wr = ConnectionHelper.getResource(this.baseUrl, paths2, this.type, this.queryParams);

		String response = wr.get(String.class);
		JSONParams unitsJson = new JSONParams(response);
		
		Iterator<String> unitsIds = unitsJson.getKeySet();
		
		FileWriter fw = new FileWriter("results.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		
		while(unitsIds.hasNext()) {
			String unitId = unitsIds.next();
			
			populateRaters(unitId, ratersHash, bw);
			bw.newLine();
		}
		
		bw.close();
		this.logger.info("Results written to results.csv file");
		
		return ratersHash;
	}
	
	public JSONParams getJudgments() throws ParameterNeeded, IOException {
		ArrayList<String> paths2 = (ArrayList<String>) this.paths.clone();

		String id = null;
		if ((id = this.getParameter("id")) == null) {
			throw new ParameterNeeded("The id parameter was mandatory for the getJudgements method");
		}

		paths2.add(id);
		paths2.add("judgments");
		
		WebResource wr = ConnectionHelper.getResource(this.baseUrl, paths2, this.type, this.queryParams);
		
		String response = wr.get(String.class);
		JSONParams judgmentsJSON = new JSONParams(response);
		
		return judgmentsJSON;
	}
	
	/**
	 * Used to populate Hashmap with pairs of raters from the
	 * unit's judgments and writes the unit to a csv file.
	 * 
	 * @param unitId The id of the unit that is been analyzed
	 * @param raters An already created hashmap to be populated
	 * @param bos FileWriter with witch to write the unit to the csv file
	 * @throws IOException 
	 */
	private void populateRaters(String unitId, HashMap<String, HashMap<String, KappaRaters>> raters, BufferedWriter bw) throws IOException {
		ArrayList<String> paths2 = (ArrayList<String>) this.paths.clone();
		paths2.add(this.getParameter("id"));
		paths2.add("units");
		paths2.add(unitId);
		
		WebResource wr = ConnectionHelper.getResource(this.baseUrl, paths2, this.type, this.queryParams);

		String response = wr.get(String.class);
		JSONParams completeUnit = new JSONParams(response);
		JSONParams judgments = completeUnit.getObject("results").getObject("judgments");
		
		JSONParams judgmentA = null;
		JSONParams judgmentB = null;
		
		for(int i = 0; i < judgments.size(); i++) {
			judgmentA = judgments.getObjectAt(i);
			
			for(int j = 0; j < judgments.size(); j++) {
				if (i != j) {
					judgmentB = judgments.getObjectAt(j);
					
					int workerA = Integer.parseInt(judgmentA.getProperty("worker_id"));
					int workerB = Integer.parseInt(judgmentB.getProperty("worker_id"));
					
					int leftWorker = Math.min(workerA, workerB);
					int rightWorker = Math.max(workerA, workerB);
					
					for (int k = 0; k < services.length; k++) {
						Service s = services[k];
						
						HashMap<String, KappaRaters> ratersService = raters.get(s.getName());
						KappaRaters kr = null;

						if (ratersService.containsKey(leftWorker + "/" + rightWorker)) {
							kr = ratersService.get(leftWorker + "/" + rightWorker);
						} else {
							kr = new KappaRaters(workerA, workerB);
							ratersService.put(leftWorker + "/" + rightWorker, kr);
						}
						
						kr.addAdequacy(
								Integer.parseInt(judgmentA.getObject("data").getProperty("adequacy_" + s.getName())), 
								Integer.parseInt(judgmentB.getObject("data").getProperty("adequacy_" + s.getName())));
						
						kr.addFluency(
								Integer.parseInt(judgmentA.getObject("data").getProperty("fluency_" + s.getName())), 
								Integer.parseInt(judgmentB.getObject("data").getProperty("fluency_" + s.getName())));
					}
					
				}
			}
		}
		
		writeCSVLine(bw, completeUnit);
	}
	
	/**
	 * Writes a line of wokers judgments to a csv file
	 * 
	 * @param object The complete unit being written
	 * @throws IOException
	 */
	private void writeCSVLine(BufferedWriter bw, JSONParams object) throws IOException {
		ArrayList<String> fields = new ArrayList<String>();	
		
		String lo = object.getObject("results").getObject("judgments").getObjectAt(0).getObject("unit_data").getProperty("lo");
		fields.add(lo);
		
		for (int i = 0; i < services.length; i++) {
			String translation = object.getObject("results").getObject("judgments").getObjectAt(0).getObject("unit_data").getProperty(services[i].getName());
			
			fields.add(translation);
		}
		
		JSONParams judgments = object.getObject("results").getObject("judgments");
		JSONParams judgment = null;
		
		Integer judgmentsSize = judgments.size();
		fields.add(judgmentsSize.toString());
		
		for(int i = 0; i < judgments.size(); i++) {
			judgment = judgments.getObjectAt(i);
			fields.add(judgment.getProperty("worker_id"));
			
			for (int k = 0; k < services.length; k++) {
				Service s = services[k];
				
				fields.add("adequacy_" + s.getName());
				fields.add(judgment.getObject("data").getProperty("adequacy_" + s.getName()));
				fields.add("fluency_" + s.getName());
				fields.add(judgment.getObject("data").getProperty("fluency_" + s.getName()));
			}
		}
		
		bw.write(this.implode(fields.toArray(new String[fields.size()]), ","));
	}
	
	private String implode(String[] fields, String delim) {
		String ret = "";
		
		for (int i = 0; i < fields.length; i++) {
			ret += fields[i].replaceAll("[^A-Za-záéíóú ,\\.0-9\\_]", "");
			
			if (i < fields.length - 1) {
				ret += delim;
			}
		}
		
		return ret;
	}
	

	/**
	 * Orders a job with the units created for it
	 *
	 * @return The reponse of the server
	 * @throws IOException
	 * @throws ParameterNeeded If the id of the job is missing
	 */
	public JSONParams order() throws IOException, ParameterNeeded {
		String id = null;
		if ((id = this.getParameter("id")) == null) {
			throw new ParameterNeeded("The id parameter was mandatory for the getJudgements method");
		}

		ArrayList<String> paths2 = (ArrayList<String>) this.paths.clone();

		paths2.add(id);
		paths2.add("orders");

		WebResource wr = ConnectionHelper.getResource(this.baseUrl, paths2, this.type);

		Form f = new Form();

		f.add("key", this.queryParams.get("key"));
		f.add("debit[units_count]", unit_count);

		for(String channel: channels) {
			f.add("channels[0]", channel);
		}

		String response = wr.type("application/x-www-form-urlencoded").post(String.class, f);

		return new JSONParams(response);
	}

	public static void main(String[] args) {
		try {
			JobsCF helper = new JobsCF(null);

			helper.addParameter("key", null);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}
}
