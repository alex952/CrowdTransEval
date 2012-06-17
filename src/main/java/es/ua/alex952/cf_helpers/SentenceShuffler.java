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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shuffler class for creating new sentences from another
 *
 * @author alex952
 */
public class SentenceShuffler {

	private int degree;
	private Random r;

	public SentenceShuffler(int degree) throws Exception {
		if(degree < 1 || degree > 5)
			throw new Exception("The degree of shuffling must be an integer between 1 and 5");
		
		this.degree = degree;
	}

	public String shuffle(String sentenceOrig) {
		String sentence = sentenceOrig.toLowerCase();
		
		r = new Random((new Date()).getTime());
		
		String[] nwords = sentence.split("\\s+");
		
		int blocks = (int) Math.floor(nwords.length/15.0);
		if (blocks == 0)
			blocks = 1;
		
		ArrayList<String> words = new ArrayList<String>();
		
		for (int i = 0; i < nwords.length; i++) {
			int index = 0;
			String sblock = "";
			
			while (index < blocks && i < nwords.length) {
				sblock += nwords[i];
				index++;
				
				if (index != blocks) {
					sblock += " ";
					i++;
				}
			}
			
			words.add(sblock);
		}
		
		ArrayList<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < words.size(); i++) {
			positions.add(i);
		}
		
		int count = this.degree;
		int length = words.size();
		
		while (count > 0) {
			
			boolean cont = count*2 < positions.size();
			if (!cont)
				break;
			
			int from = r.nextInt(positions.size());
			int to = r.nextInt(positions.size());
                        while (to == from)
                            to = r.nextInt(positions.size());
			
			Collections.swap(words, positions.get(from), positions.get(to));
			
			int max = Math.max(from, to);
                        positions.remove(max);
                        
                        if (from != to) {
                            int min = Math.min(from, to);
                            positions.remove(min);
                        }
			count--;
		}
		
		String ret = "";
		
		for (int i = 0; i < words.size(); i++) {
			ret += words.get(i);
			
			if (i < words.size() - 1) {
				ret += " ";
			}
		}
		
		ret = Character.toUpperCase(ret.charAt(0)) + ret.substring(1, ret.length());
		
		return ret;
	}
	
	public static void main(String[] args) {
		SentenceShuffler ss;
		try {
			ss = new SentenceShuffler(5);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			
			return;
		}
		
		System.out.println(ss.shuffle("My brand My brand new phrase My brand new phrase brand new phrase My brand new phrase My brand new phrase My brand new phrase My brand new phrase My brand new phrase My brand new phrase"));
	}
}