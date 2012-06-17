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
 *
 * @author alex952
 */
public class JSONParams {

	JsonNode rootNode = null;
	
	public JSONParams() {
		ObjectMapper mapper = new ObjectMapper();
		rootNode = mapper.createObjectNode();
	}
	
	public JSONParams(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		rootNode = mapper.readValue(json, JsonNode.class);
	}
	
	private JSONParams(JsonNode node) {
		this.rootNode = node;
	}
	
	
	public String getProperty(String key) {
		JsonNode node = rootNode.get(key);
		String ret = node.asText();
		
		return ret;
	}
	
	public JSONParams getObjectAt(Integer index) {
		JsonNode node = rootNode.get(index);
		
		return new JSONParams(node);
	}
	
	public JSONParams getObject(String key) {
		JsonNode node = rootNode.get(key);
		
		return new JSONParams(node);
	}
	
	public int size() {
		if (this.rootNode instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode)this.rootNode;
			
			return arrayNode.size();
		}
		
		return 1;
	}
	
	public String toString() {
		return this.rootNode.toString();
	}
	
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
	
	public Iterator<String> getKeySet() {
		return this.rootNode.getFieldNames();
	}
	
	public void addProperty(String key, String value) {
		if (this.rootNode instanceof ObjectNode) {
			ObjectNode obj = (ObjectNode)this.rootNode;
			obj.put(key, value);
		}
	}
	
	public static void main(String[] args) {
		try {
			JSONParams p = new JSONParams("{\"hola\":\"hola2\",\"adios\":\"adios2\"}");
			
		} catch (IOException ex) {
			Logger.getLogger(JSONParams.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
