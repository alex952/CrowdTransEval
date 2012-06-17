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
package es.ua.alex952.cf_helpers.translation_services;

import com.sun.jersey.api.client.WebResource;
import es.ua.alex952.cf_helpers.ConnectionHelper;
import es.ua.alex952.cf_helpers.params.JSONParams;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author alex952
 */
public class ApertiumTranslator extends Service {

	String key;
	
	public ApertiumTranslator(String key) {
		super("apertium");
		this.key = key;
	}

	@Override
	public String getTranslation(String text, String lo, String lm) {
		String url = "http://api.apertium.org/json/translate";
		String translation = "";
		
		WebResource wr = ConnectionHelper.getResource(url);
		wr = wr.queryParam("q", text).queryParam("langpair", lo+"|"+lm);
		String ret = wr.get(String.class);
		
		try {
			JSONParams response = new JSONParams(ret);
			
			translation = response.getObject("responseData").getProperty("translatedText");
			translation = translation.replaceAll(Pattern.quote("*"), "");
		} catch (IOException ex) {
			translation = null;
		}
		
		return translation;
	}
	
	public static void main(String[] args) {
		ApertiumTranslator at = new ApertiumTranslator("nada");
		System.out.println(at.getTranslation("The wind lashed the trees", "en", "es"));
	}
}
