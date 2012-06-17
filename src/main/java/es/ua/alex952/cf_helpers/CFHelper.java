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
import es.ua.alex952.exceptions.KeyNotConfigured;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Class that represents the base class of all Crowdflower resources
 * 
 * @author alex952
 */
public abstract class CFHelper {
	/**
	 * A string representing the kind of resource of the inherited class. 
	 * Must be prepended on each post request with parameters.
	 */
	protected String						postPrepend="";
	
	/**
	 * Base url for the Crowdflower API.
	 */
	protected final String					baseUrl = "https://api.crowdflower.com/v1/";
	
	/**
	 * Desired format of the returning data from the server.
	 */
	protected final String					type="json";
	
	/**
	 * Paths to append to the base url for the service requests.
	 */
	protected ArrayList<String>				paths = new ArrayList<String>();
	
	/**
	 * Request query parameters (mainly used for the key parameter).
	 */
	protected HashMap<String, String>		queryParams;
	
	/**
	 * API key configured via configuration file config.properties.
	 */
	protected String						key;
	
	protected String configFile;
	
	protected JSONParams					dataParams;

	public JSONParams getDataParams() {
		return dataParams;
	}

	/**
	 * Main constructor of the class. It retrieves the key from the configuration
	 * file and stores it in the ${@link CFHelper#queryParams} variable.
	 * 
	 * @throws KeyNotConfigured 
	 */
	public CFHelper(String configFile) throws KeyNotConfigured {
		queryParams = new HashMap<String, String>();
		this.configFile = configFile;

		if ((this.key = this.getKeyFromProperties()) == null) {
			throw new KeyNotConfigured();
		}

		queryParams.put("key", this.key);
	}

	/**
	 * Utility method for retrieving the API key from the configuration file.
	 * 
	 * @return The key itself or null in case of error.
	 */
	private String getKeyFromProperties() {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(this.configFile));
		} catch (IOException e) {
			return null;
		}

		return p.getProperty("CrowdFlowerKey");
	}

	/**
	 * Gets all the resources of an inherited class of {@link CFHelper}.
	 * 
	 * @return All the resources of the class in the server.
	 * @throws IOException 
	 */
	public JSONParams getAll() throws IOException {
		WebResource wr = ConnectionHelper.getResource(this.baseUrl, this.paths, this.type, this.queryParams);

		String res = wr.get(String.class);
		
		return new JSONParams(res);
	}

	/**
	 * Get the specific information about one resource, identified by an id.
	 * 
	 * @param id The resource identifier.
	 * @return The information about the resource.
	 * @throws IOException 
	 */
	public JSONParams getInfo(String id) throws IOException {
		ArrayList<String> paths2 = (ArrayList<String>)this.paths.clone();
		paths2.add(id);
		
		WebResource wr = ConnectionHelper.getResource(this.baseUrl, paths2, this.type, this.queryParams);

		String res = wr.get(String.class);
		
		return new JSONParams(res);
		
	}

	/**
	 * Creates a resource with the class parameters.
	 * 
	 * @return The service response wrapped in a {@link JSONParams} object.
	 * @throws IOException 
	 */
	public JSONParams create() throws IOException {
		WebResource wr = ConnectionHelper.getResource(this.baseUrl, this.paths, this.type, this.queryParams);
		Form f = new Form();
		
		HashMap<String, String> hashParams = this.dataParams.formatAsHash();

		for (String key: hashParams.keySet()) {
			String value = hashParams.get(key);

			f.add(postPrepend + "[" + key + "]", value);
		}

		String response = wr.type("application/x-www-form-urlencoded").post(String.class, f);

		return new JSONParams(response);
	}
	
	/**
	 * Adds a parameter to the {@link JSONParams} object of the class.
	 * 
	 * @param key The key for the new parameter.
	 * @param value The value of the new parameter.
	 */
	public void addParameter(String key, String value) {
		this.dataParams.addProperty(key, value);
	}
	
	/**
	 * Gets a parameter that already exists in the {@link JSONParams} object
	 * of the class.
	 * 
	 * @param key The key for the desired parameter.
	 * @return The value corresponding to the key.
	 */
	public String getParameter(String key) {
		return this.dataParams.getProperty(key);
	}
	
	/**
	 * Gets all the parameters from the class.
	 * 
	 * @return A {@link JSONParams} object containing all the parameters
	 */
	public JSONParams getParameters() {
		return this.dataParams;
	}
}
