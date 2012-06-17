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

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alex952
 */
public class BingTranslator extends Service {
	private String clientId;
	private String clientSecret;
	
	public BingTranslator(String clientId, String clientSecret) {
		super("bing");
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	@Override
	public String getTranslation(String text, String lo, String lm) {
		Translate.setClientId(this.clientId);
		Translate.setClientSecret(this.clientSecret);
		String translation = "";
		
		try {
			Language lLO = Language.fromString(lo);
			Language lLM = Language.fromString(lm);
			
			translation = Translate.execute(text, lLO, lLM);
		} catch (Exception ex) {
			return null;
		}
		
		return translation;
	}
	
	public static void main(String[] args) {
		BingTranslator bt = new BingTranslator("54acb00c-d618-4503-8743-f02f7bb52806", "kt6l0gi/bgG5snL9na9+zCHct3NBL81TXJGdusm3Vg4=");
		System.out.println(bt.getTranslation("The wind lashed the trees", "en", "es"));
	}
}
