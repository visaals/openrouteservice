/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.routing;

public class WeightingMethod {
	public static final int UNKNOWN = 0;
	public static final int FASTEST = 1;
	public static final int SHORTEST = 2;
	public static final int RECOMMENDED = 3;
	public static final int RECOMMENDEDR = 4;
	public static final int RECOMMENDEDS = 5;
	public static final int RECOMMENDEDNEW = 6;

	private  WeightingMethod() {}

	public static int getFromString(String method) {
		if ("fastest".equalsIgnoreCase(method)) {
			return WeightingMethod.FASTEST;
		} else if ("shortest".equalsIgnoreCase(method)) {
			return WeightingMethod.SHORTEST;
		} else if ("recommended".equalsIgnoreCase(method)) {
			return WeightingMethod.RECOMMENDED;
		} else if ("recommendedr".equalsIgnoreCase(method)) {
			return WeightingMethod.RECOMMENDEDR;
		} else if ("recommendeds".equalsIgnoreCase(method)) {
			return WeightingMethod.RECOMMENDEDS;
		} else if ("recommendednew".equalsIgnoreCase(method)) {
			return WeightingMethod.RECOMMENDEDNEW;
		}
		return WeightingMethod.UNKNOWN;
	}
	
	public static String getName(int profileType) {
		switch (profileType) {
		case FASTEST:
			return "fastest";
		case SHORTEST:
			return "shortest";
		case RECOMMENDED:
			return "recommended";
		case RECOMMENDEDR:
			return "recommendedr";
		case RECOMMENDEDS:
			return "recommendeds";
		case RECOMMENDEDNEW:
			return "recommendednew";
		default:
			return "";
		}
	}
}
