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
package es.ua.alex952.cf_helpers.params;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Wrapper class of the JsonNode from the library Jackson
 * 
 * @author alex952
 */
public class JSONParams {

	JsonNode rootNode = null;
	
	/**
	 * Main constructor. Initializes a blank node
	 */
	public JSONParams() {
		ObjectMapper mapper = new ObjectMapper();
		rootNode = mapper.createObjectNode();
	}
	
	/**
	 * Constructor that builds up a node out of a 
	 * json string specification
	 * 
	 * @param json JSON in String format
	 * @throws IOException If the structure is not correct
	 */
	public JSONParams(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		rootNode = mapper.readValue(json, JsonNode.class);
	}
	
	private JSONParams(JsonNode node) {
		this.rootNode = node;
	}
	
	/**
	 * Returns a property of an object node
	 * 
	 * @param key key of the wanted property
	 * @return the property
	 */
	public String getProperty(String key) {
		JsonNode node = rootNode.get(key);
		String ret = node.asText();
		
		return ret;
	}
	
	/**
	 * Gets a JSON object from an array by its index
	 * 
	 * @param index index of the desired object
	 * @return the object
	 */
	public JSONParams getObjectAt(Integer index) {
		JsonNode node = rootNode.get(index);
		
		return new JSONParams(node);
	}
	
	/**
	 * Gets an object as it was a property, but
	 * in the form of a JSON object
	 * 
	 * @param key the key of the property
	 * @return a JSON object
	 */
	public JSONParams getObject(String key) {
		JsonNode node = rootNode.get(key);
		
		return new JSONParams(node);
	}
	
	/**
	 * Size of a JSON array
	 * 
	 * @return the size of the JSON array
	 */
	public int size() {
		if (this.rootNode instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode)this.rootNode;
			
			return arrayNode.size();
		}
		
		return 1;
	}
	
	/**
	 * Returns the JSON structure in a String format
	 * 
	 * @return the JSON in String
	 */
	public String toString() {
		return this.rootNode.toString();
	}
	
	/**
	 * Gets the JSON formatted as a Hashmap of properties
	 * 
	 * @return the Hashmap containing all tht properties
	 */
	public HashMap<String, String> formatAsHash() {
		
		HashMap<String, String> ret = new HashMap<String, String>();
		Iterator<Map.Entry<String, JsonNode>> it = this.rootNode.getFields();
		
		while(it.hasNext()) {
			Map.Entry<String, JsonNode> element = it.next();
			
			if (!(element.getValue() instanceof ObjectNode || element.getValue() instanceof ArrayNode)) {
				ret.put(element.getKey(), element.getValue().asText());
			}
		}
		
		return ret;
	}
	
	/**
	 * Returns an iterator to the keyset of the JSON object
	 */
	public Iterator<String> getKeySet() {
		return this.rootNode.getFieldNames();
	}
	
	/**
	 * Adds a property to the JSON object with the given key
	 * 
	 * @param key key for the new attribute
	 * @param value value of the attribute
	 */
	public void addProperty(String key, String value) {
		if (this.rootNode instanceof ObjectNode) {
			ObjectNode obj = (ObjectNode)this.rootNode;
			obj.put(key, value);
		}
	}
}
