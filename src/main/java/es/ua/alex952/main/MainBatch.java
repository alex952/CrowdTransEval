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
package es.ua.alex952.main;

import es.ua.alex952.cf_helpers.JobsCF;
import es.ua.alex952.exceptions.KeyNotConfigured;
import es.ua.alex952.exceptions.ParameterNeeded;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MainBatch implements Runnable {

	private Options options = new Options();
	final private Logger logger = LoggerFactory.getLogger(MainBatch.class);

	private enum Operation {

		QUERY, CREATE, DAEMON, QUIT;
	}
	//Operation casted
	private Operation op = null;
	//File paths
	private String pathLO = null;
	private String pathTR = null;
	private String pathGold = null;
	//Is daemon mode activated?
	private boolean daemon = false;
	//Daemon check frecuency
	private Long frecuency = 10000L;
	//Id for eht job (created or asked for monitorizing)
	private String id = null;
	//Instance for operation
	private JobsCF instance = null;
	//Config file default value
	private String parametersFile = "";
	private String configFile = "";

	/**
	 * Main constructor that parses all arguments from the command line
	 * 
	 * @param args Command line arguments
	 */
	public MainBatch(String[] args) {

		//Operation creation for usage print		
		Option create = OptionBuilder.withLongOpt("create").withDescription("switch for creating a job").create("c");

		Option daemon = OptionBuilder.withArgName("id").withLongOpt("daemon").withDescription("daemon mode for monitorizing the job after its creation").hasOptionalArg().create("d");

		Option configfile = OptionBuilder.withArgName("config.properties").withLongOpt("configfile").withDescription("the properties config file that has all the program specific configurations").hasArg().create("cf");

		Option parametersfile = OptionBuilder.withArgName("parameters.properties").withLongOpt("parametersfile").withDescription("properties paramters file that has all the job specific parameters for its creation").hasArg().create("pf");

		Option sourcelanguage = OptionBuilder.withArgName("sl.txt").withLongOpt("sourcelanguage").withDescription("text file containing all the sentences to be translated").hasArg().create("sl");

		Option referencetranslations = OptionBuilder.withArgName("rt.txt").withLongOpt("referencetranslations").withDescription("text file with a translation of reference for each source language sentence").hasArg().create("rt");

		Option gold = OptionBuilder.withArgName("gold.txt").withLongOpt("gold").withDescription("text file with the gold standards given for the job. It has a three lines format that is composed by one line for the source language sentence, one for the reference translation, and the last one for the correct translation").hasArg().create("g");
		
		Option daemonfrecuency = OptionBuilder.withArgName("daemon frecuency").withLongOpt("daemonfrecuency").withDescription("daemon check frecuency").hasArg().create("df");

		Option help = OptionBuilder.withLongOpt("help").withDescription("shows this help message").create("h");

		options.addOption(create);
		options.addOption(daemon);
		options.addOption(daemonfrecuency);
		options.addOption(configfile);
		options.addOption(parametersfile);
		options.addOption(sourcelanguage);
		options.addOption(referencetranslations);
		options.addOption(gold);
		options.addOption(help);
		
		//Option parsing
		CommandLineParser clp = new BasicParser();
		try {
			CommandLine cl = clp.parse(options, args);
			if (cl.hasOption("help") || cl.getOptions().length == 0) {
				HelpFormatter hf = new HelpFormatter();
				hf.setWidth(100);
				hf.printHelp("CrowdFlowerTasks", options);
				op = Operation.QUIT;

				return;
			}
			
			if (cl.hasOption("daemon") && !cl.hasOption("c")) {
				if (cl.getOptionValue("daemon") == null) {
					logger.error("The daemon option must have a job id if it isn't along with create option");
					op = Operation.QUIT;
					
					return;
				} else if (!cl.hasOption("configfile")) {
					logger.error("The config file is mandatory");
					op = Operation.QUIT;
					
					return;
				}
				
				try {
					Integer.parseInt(cl.getOptionValue("daemon"));
					
					this.id = cl.getOptionValue("daemon");
					this.configFile = cl.getOptionValue("configfile");
					this.op = Operation.DAEMON;
					
					if (cl.hasOption("daemonfrecuency")) {
						try {
                            Long l = Long.parseLong(id);
							this.frecuency  = l;
						} catch (NumberFormatException e) {
							this.logger.info("The frecuency is not a number. Setting to default: 10 sec");
						}
					} else { 
						this.logger.info("Daemon frecuency not set. Setting to default: 10 sec");
					}
				} catch (NumberFormatException e) {
					this.logger.error("The id following daemon option must be an integer");
					this.op = Operation.QUIT;
					
					return;
				}
			} else {
				if (!cl.hasOption("gold")
						|| !cl.hasOption("configfile")
						|| !cl.hasOption("parametersfile")
						|| !cl.hasOption("referencetranslations")
						|| !cl.hasOption("sourcelanguage")) {
					logger.error("The files gold, tr, lo, config.properties and parameters.properties are mandatory for creating jobs");
					this.op = Operation.QUIT;
					
					return;
				} else {
					if (cl.hasOption("daemon"))
						this.daemon = true;
					else {
						if (cl.hasOption("daemonfrecuency"))
							this.logger.info("Daemon frecuency parameter found, ignoring it as there's not a daemon option");
					}
					
					this.configFile = cl.getOptionValue("configfile");
					this.parametersFile = cl.getOptionValue("parametersfile");
					this.pathGold = cl.getOptionValue("gold");
					this.pathLO = cl.getOptionValue("sourcelanguage");
					this.pathTR = cl.getOptionValue("referencetranslations");
					
					this.op = Operation.CREATE;
				}
			}

		} catch (ParseException ex) {
			logger.error("Failed argument parsing", ex);
		}
	}

	/**
	 * Main method of the class that runs the specified options in the
	 * command line arguments parsed in constructor method
	 */
	@Override
	public void run() {

		if (this.op == Operation.QUIT) {
			logger.debug("Showing help and exiting");
			return;
		}

		switch (this.op) {
			case CREATE: {

				
				logger.info("Creating job with {} configuration file, {} parameters file, {} lo file, {} tr file, {} gold file",
							new Object[] {this.configFile,
							this.parametersFile,
							this.pathLO,
							this.pathTR,
							this.pathGold});
				
				logger.info("Entering job creating stage");

				try {
					this.instance = new JobsCF(this.parametersFile, this.configFile);
					this.instance.setPathLO(pathLO);
					this.instance.setPathTR(pathTR);
					this.instance.setPathGold(pathGold);

					this.instance.create();
					this.id = this.instance.getParameter("id");
					logger.info("Job {} created", this.id);
					this.instance.populate();
					logger.info("Job {} populated", this.id);
					this.instance.order();
					logger.info("Job {} ordered", this.id);					
				} catch (ParameterNeeded ex) {
					this.logger.error("A parameter couldn't be found", ex);
				} catch (KeyNotConfigured ex) {
					this.logger.error("The CrowdFlower API key was not correctly configured");
				} catch (IOException ex) {
					this.logger.error("I/O exception ocurred", ex);
				} catch (Exception e) {
					this.logger.error("An error ocurred", e);
				}

				if (!this.daemon) {
					break;
				}
			}
			case DAEMON: {
			try {
				if (this.instance == null) {
					try {
						this.instance = new JobsCF(this.configFile);
					} catch (KeyNotConfigured ex) {
						this.logger.error("The CrowdFlower API key was not correctly configured");
						return;
					} catch (Exception e) {
						this.logger.error("An error ocurred", e);
						return;
					}

					this.instance.addParameter("id", this.id);
				}


				boolean finished = false;

				if (this.op == Operation.CREATE) {
					this.logger.info("Waiting first {} seconds", this.frecuency);
					Thread.sleep(this.frecuency);
				}
				
				do {
					try {
						if ((finished = this.instance.isFinished()) == true) {
							this.logger.info("The job {} has already finished. Preparing results", this.id);
							String graph = this.instance.processResults();

							if (graph == null) {
								System.err.println("Results could not been retrieved due some unexpected error");
							} else {
								FileOutputStream fos = new FileOutputStream(new File("graph.html"));
								fos.write(graph.getBytes());
								fos.close();

								this.logger.info("The html file with the results has been written to the file graph.html");
								this.logger.info("Opening default HTML handler (usually a browser) to show results");
								Desktop.getDesktop().open(new File("graph.html"));

								finished = true;
							}
						} else {
							this.logger.info("The job {} hasn't finished yet. Waiting {} sec. to check again", this.frecuency);
							
							Thread.sleep(this.frecuency);
						}
					} catch (Exception e) {
						this.logger.error("Some error ocurred either checking on the state of the job or gathering its results", e);
						return;
					}
				} while (!finished);

				break;
			} catch (InterruptedException ex) {
				java.util.logging.Logger.getLogger(MainBatch.class.getName()).log(Level.SEVERE, null, ex);
			}
			}
		}
	}

	public static void main(String[] args) {
		MainBatch mb = new MainBatch(args);

		mb.run();
	}
}
