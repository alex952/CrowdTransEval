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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.Map;

/**
 * An utility class to serve to the web resource creation purposes.
 * 
 * @author alex952
 */
public class ConnectionHelper {

	/**
	 * Main creator and the simpler of all of them.
	 * 
	 * @param url Url that points to the web service
	 * @return An object of the class {@link WebResource} able to invoke 
	 * any kind of REST operation with the given url.
	 */
	public static WebResource getResource(String url) {
		Client c;
		WebResource wr;

		c = Client.create();
		wr = c.resource(url);
		
		return wr;
	}

	/**
	 * Creator that receives an url, a number of paths and a type for the 
	 * response.
	 * 
	 * @param url Url that points to the web service.
	 * @param paths The collection of paths to append to the base url.
	 * @param type The type of the response.
	 * @return An object of the class {@link WebResource} able to invoke.
	 * any kind of REST operation with the given url.
	 */
	public static WebResource getResource(String url, ArrayList<String> paths, String type) {
		WebResource wr = ConnectionHelper.getResource(url);

		for (int i = 0; i < paths.size(); i++) {
			String path = paths.get(i);

			if (i == paths.size() - 1) {
				wr = wr.path(path + "." + type);
			} else {
				wr = wr.path(path);
			}
		}
		
		return wr;
	}

	/**
	 * Creator that receives an url, a number of paths, a type for the 
	 * response and the query parameters.
	 * 
	 * @param url Url that points to the web service.
	 * @param paths The collection of paths to append to the base url.
	 * @param type The type of the response.
	 * @param queryParams The query parameters of the web resource.
	 * @return An object of the class {@link WebResource} able to invoke.
	 * any kind of REST operation with the given url.
	 */
	public static WebResource getResource(String url, ArrayList<String> paths, String type, Map<String, String> queryParams) {
		WebResource wr = ConnectionHelper.getResource(url, paths, type);

		for(String key : queryParams.keySet()) {
			wr = wr.queryParam(key, queryParams.get(key));
		}
		
		return wr;
	}
}
