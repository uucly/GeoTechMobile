/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
 * This file was semi-automatically converted from the public-domain USGS PROJ source.
 */
package com.jhlabs.map.proj;

import com.jhlabs.map.*;
import com.vividsolutions.jts.geom.Coordinate;

public class AlbersProjection extends Projection {

	private final static double EPS10 = 1.e-10;
	private final static double TOL7 = 1.e-7;
	private double ec;
	private double n;
	private double c;
	private double dd;
	private double n2;
	private double rho0;
	private double phi1;
	private double phi2;
	private double[] en;

	private final static int N_ITER = 15;
	private final static double EPSILON = 1.0e-7;
	private final static double TOL = 1.0e-10;

	public AlbersProjection() {
		minLatitude = Math.toRadians(0);
		maxLatitude = Math.toRadians(80);
    projectionLatitude1 = MapMath.degToRad(45.5);
    projectionLatitude2 = MapMath.degToRad(29.5);
		initialize();
	}

	private static double phi1_(double qs, double Te, double Tone_es) {
		int i;
		double Phi, sinpi, cospi, con, com, dphi;

		Phi = Math.asin (.5 * qs);
		if (Te < EPSILON)
			return( Phi );
		i = N_ITER;
		do {
			sinpi = Math.sin (Phi);
			cospi = Math.cos (Phi);
			con = Te * sinpi;
			com = 1. - con * con;
			dphi = .5 * com * com / cospi * (qs / Tone_es -
			   sinpi / com + .5 / Te * Math.log ((1. - con) /
			   (1. + con)));
			Phi += dphi;
		} while (Math.abs(dphi) > TOL && --i != 0);
		return( i != 0 ? Phi : Double.MAX_VALUE );
	}

	public Coordinate project(double lplam, double lpphi, Coordinate out) {
		double rho;
		if ((rho = c - (!spherical ? n * MapMath.qsfn(Math.sin(lpphi), e, one_es) : n2 * Math.sin(lpphi))) < 0.)
			throw new ProjectionException("F");
		rho = dd * Math.sqrt(rho);
		out.x = rho * Math.sin( lplam *= n );
		out.y = rho0 - rho * Math.cos(lplam);
		return out;
	}

	public Coordinate projectInverse(double xyx, double xyy, Coordinate out) {
		double rho;
		if ((rho = MapMath.distance(xyx, xyy = rho0 - xyy)) != 0) {
			double lpphi, lplam;
			if (n < 0.) {
				rho = -rho;
				xyx = -xyx;
				xyy = -xyy;
			}
			lpphi =  rho / dd;
			if (!spherical) {
				lpphi = (c - lpphi * lpphi) / n;
				if (Math.abs(ec - Math.abs(lpphi)) > TOL7) {
					if ((lpphi = phi1_(lpphi, e, one_es)) == Double.MAX_VALUE)
						throw new ProjectionException("I");
				} else
					lpphi = lpphi < 0. ? -MapMath.HALFPI : MapMath.HALFPI;
			} else if (Math.abs(lpphi = (c - lpphi * lpphi) / n2) <= 1.)
				lpphi = Math.asin(lpphi);
			else
				lpphi = lpphi < 0. ? -MapMath.HALFPI : MapMath.HALFPI;
			lplam = Math.atan2(xyx, xyy) / n;
			out.x = lplam;
			out.y = lpphi;
		} else {
			out.x = 0.;
			out.y = n > 0. ? MapMath.HALFPI : - MapMath.HALFPI;
		}
		return out;
	}

	public void initialize() {
		super.initialize();
		double cosphi, sinphi;
		boolean secant;

		phi1 = projectionLatitude1;
		phi2 = projectionLatitude2;

		if (Math.abs(phi1 + phi2) < EPS10)
			throw new IllegalArgumentException("-21");
		n = sinphi = Math.sin(phi1);
		cosphi = Math.cos(phi1);
		secant = Math.abs(phi1 - phi2) >= EPS10;
		spherical = es <= 0.0;
		if (!spherical) {
			double ml1, m1;

			if ((en = MapMath.enfn(es)) == null)
				throw new IllegalArgumentException("0");
			m1 = MapMath.msfn(sinphi, cosphi, es);
			ml1 = MapMath.qsfn(sinphi, e, one_es);
			if (secant) { /* secant cone */
				double ml2, m2;

				sinphi = Math.sin(phi2);
				cosphi = Math.cos(phi2);
				m2 = MapMath.msfn(sinphi, cosphi, es);
				ml2 = MapMath.qsfn(sinphi, e, one_es);
				n = (m1 * m1 - m2 * m2) / (ml2 - ml1);
			}
			ec = 1. - .5 * one_es * Math.log((1. - e) /
				(1. + e)) / e;
			c = m1 * m1 + n * ml1;
			dd = 1. / n;
			rho0 = dd * Math.sqrt(c - n * MapMath.qsfn(Math.sin(projectionLatitude),
				e, one_es));
		} else {
			if (secant) n = .5 * (n + Math.sin(phi2));
			n2 = n + n;
			c = cosphi * cosphi + n2 * sinphi;
			dd = 1. / n;
			rho0 = dd * Math.sqrt(c - n2 * Math.sin(projectionLatitude));
		}
	}

	/**
	 * Returns true if this projection is equal area
	 */
	public boolean isEqualArea() {
		return true;
	}

	public boolean hasInverse() {
		return true;
	}

	/**
	 * Returns the ESPG code for this projection, or 0 if unknown.
	 */
	public int getEPSGCode() {
		return 9822;
	}

	public String toString() {
		return "Albers Equal Area";
	}

/*
	public String toString() {
		return "Lambert Equal Area Conic";
	}
*/
}

