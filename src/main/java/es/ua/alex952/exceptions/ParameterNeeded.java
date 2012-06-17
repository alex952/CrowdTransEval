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
package es.ua.alex952.exceptions;

/**
 * Exception thrown when an extra parameter was needed and
 * wasn't provided to the method thrower.
 * 
 * @author alex952
 */
public class ParameterNeeded extends Exception {

	public ParameterNeeded() {
		super("An extra paramter was needed and wasn't provided");
	}
	
	public ParameterNeeded(String reason) {
		super("Extra parameter neede: " + reason);
	}
}
