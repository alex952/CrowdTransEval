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
package es.ua.alex952.cf_helpers.result_helpers;

import java.util.ArrayList;

/**
 *
 * @author alex952
 */
public class KappaRaters {
	
	private static Integer N_RATES = 6;
	
	private Integer raterA;
	private Integer raterB;

	public Integer getRaterA() {
		return raterA;
	}

	public Integer getRaterB() {
		return raterB;
	}
	
	private int[][] flucencyRates;
	private int[][] adecuacyRates;

	/**
	 * Main constructor.
	 * 
	 * @param raterA First part of the couple of raters (order matters)
	 * @param raterB Last part of the couple of raters (order matters) 
	 */
	public KappaRaters(Integer raterA, Integer raterB) {
		this.raterA = raterA;
		this.raterB = raterB;
		
		this.flucencyRates = new int[N_RATES][N_RATES];
		this.adecuacyRates = new int[N_RATES][N_RATES];
	}
	
	
	/**
	 * Add one more judgment to fluency rate table.
	 * 
	 * @param rateA What first has rated.
	 * @param rateB What last has rated.
	 */
	public void addFluency(Integer rateA, Integer rateB) {
		this.flucencyRates[rateA][rateB]++;
	}
	
	/**
	 * Add one more judgment to fluency rate table.
	 * 
	 * @param rateA rateA What first has rated.
	 * @param rateB  rateB What last has rated.
	 */
	public void addAdequacy(Integer rateA, Integer rateB) {
		this.adecuacyRates[rateA][rateB]++;
	}
	
	/**
	 * Get the numer of rates of a given combination.
	 * 
	 * @param rateA 
	 * @param rateB
	 * @return 
	 */
	public Integer getFluencyRate(Integer rateA, Integer rateB) {
		return this.flucencyRates[rateA][rateB];
	}
	
	/**
	 * Get the numer of rates of a given combination.
	 * 
	 * @param rateA
	 * @param rateB
	 * @return 
	 */
	public Integer getAdequacyRate(Integer rateA, Integer rateB) {
		return this.adecuacyRates[rateA][rateB];
	}
	
	/**
	 * Gets the kappa factor from the raters' matrix
	 * 
	 * @param ratesTable The rater's matrix
	 * @return Kappa factor derived from the table
	 */
	private Double getKappa(int [][] ratesTable) {
		//Total sum of table
		Integer total = 0;
		
		//Stores the rows' sums
		ArrayList<Double> pe1 = new ArrayList<Double>();
		//Stores the cols' sums
		ArrayList<Double> pe2 = new ArrayList<Double>();
		
		//Used to collect total sum and row sum
		for (int i = 0; i < ratesTable.length; i++) {
			int row = 0;
			
			for (int j = 0; j < ratesTable[i].length; j++) {
				total += ratesTable[i][j];
				
				row += ratesTable[i][j];
			}
			
			pe1.add(new Double(row));
			row = 0;
		}
		
		//Calculus of P(a)
		Double pa = 0.0;
		for(int i = 0; i < ratesTable.length; i++) {
			pa += ratesTable[i][i];
		}
		pa /= total;
		
		//Used to collect columns' sum
		for (int i = 0; i < ratesTable.length; i++) {
			int col = 0;
			
			for (int j = 0; j < ratesTable[i].length; j++) {
				col += ratesTable[j][i];
			}
			
			pe2.add(new Double(col));
			col = 0;
		}
		
		//Calculus of P(e)
		Double pe = 0.0;
		for (int i = 0; i < pe1.size(); i++) {
			pe += pe1.get(i) / total * pe2.get(i) / total;
		}
		
		//Return the real Kappa P(a) - P(e) / 1.0 - P(e)
		return (pa - pe) / (1.0 - pe);
	}
	
	/**
	 * Returns the Kappa factor for the Fluency feature.
	 * 
	 * @return Fluency's Kappa factor
	 */
	public Double getFluencyKappa() {
		return this.getKappa(this.flucencyRates);
	}
	
	/**
	 * Returns the Kappa factor for the Adequacy feature.
	 * 
	 * @return Fluency's Kappa factor
	 */
	public Double getAdequacyKappa() {
		return this.getKappa(this.flucencyRates);
	}
	
	private Integer getRatings(int[] ratings) {
		Integer res = 0;
		
		for (Integer rate: ratings) {
			res += rate;
		}
		
		return res;
	}
	
	public Integer getRatingsFluencyA(int score) {
		return this.getRatings(this.flucencyRates[score]);
	}
	
	public Integer getRatingsAdequacyA(int score) {
		
		
		return this.getRatings(this.adecuacyRates[score]);
	}
	
	public Integer getRatingsFluencyB(int score) {
		int[] column = new int[this.flucencyRates.length];
		
		for (int i = 0; i < flucencyRates.length; i++) {
			column[i] = this.flucencyRates[i][score];
		}
		
		return this.getRatings(column);
	}
	
	public Integer getRatingsAdequacyB(int score) {
		int[] column = new int[this.adecuacyRates.length];
		
		for (int i = 0; i < adecuacyRates.length; i++) {
			column[i] = this.adecuacyRates[i][score];
		}
		
		return this.getRatings(column);
	}
}
