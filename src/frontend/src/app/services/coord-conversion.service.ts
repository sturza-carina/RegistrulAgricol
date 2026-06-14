import { Injectable } from '@angular/core';

/**
 * Stereo70 (EPSG:31700) to WGS84 (EPSG:4326) conversion.
 *
 * Uses proper Oblique Stereographic inverse on Krassowski 1940 ellipsoid,
 * followed by a Helmert 3-parameter datum shift to WGS84.
 *
 * Convention for Romanian Stereo70:
 *   X = Northing  (labeled "Nord" in the UI)
 *   Y = Easting   (labeled "Est"  in the UI)
 */
@Injectable({ providedIn: 'root' })
export class CoordConversionService {

  // Krassowski 1940 ellipsoid
  private readonly a   = 6378245.0;
  private readonly f   = 1.0 / 298.3;
  private readonly e2  = 2 * (1.0 / 298.3) - (1.0 / 298.3) ** 2;
  private readonly e   = Math.sqrt(2 * (1.0 / 298.3) - (1.0 / 298.3) ** 2);

  // Stereo70 projection parameters
  private readonly phi0 = 46.0 * Math.PI / 180;   // latitude of origin
  private readonly lam0 = 25.0 * Math.PI / 180;   // longitude of origin
  private readonly k0   = 0.99975;                 // scale factor
  private readonly FE   = 500000.0;               // false easting
  private readonly FN   = 500000.0;               // false northing

  // Cached derived constants (computed once)
  private readonly R:    number;
  private readonly chi0: number;

  // WGS84 ellipsoid
  private readonly a_wgs  = 6378137.0;
  private readonly e2_wgs = 2 / 298.257223563 - (1 / 298.257223563) ** 2;

  // Helmert 3-parameter shift  Krassowski → WGS84 for Romania (metres)
  // Standard EPSG:1838 transformation (±5 m accuracy)
  private readonly dx = 33.4;
  private readonly dy = -146.6;
  private readonly dz = -76.3;

  constructor() {
    const { e2, e, phi0, k0 } = this;
    const sinPhi0 = Math.sin(phi0);
    const N0   = this.a / Math.sqrt(1 - e2 * sinPhi0 ** 2);
    const rho0 = this.a * (1 - e2) / (1 - e2 * sinPhi0 ** 2) ** 1.5;
    this.R = Math.sqrt(N0 * rho0);

    this.chi0 = 2 * Math.atan(
      Math.tan(Math.PI / 4 + phi0 / 2) *
      ((1 - e * sinPhi0) / (1 + e * sinPhi0)) ** (e / 2)
    ) - Math.PI / 2;
  }

  /**
   * Convert a single Stereo70 coordinate pair to WGS84 [lat, lng].
   * @param xNord  Stereo70 X (Northing)
   * @param yEst   Stereo70 Y (Easting)
   * @returns [latitude, longitude] in WGS84 degrees
   */
  stereo70ToWgs84(xNord: number, yEst: number): [number, number] {
    const { a, e2, e, phi0, lam0, k0, FE, FN, R, chi0 } = this;

    // 1. Reduced grid coordinates
    const Ep = yEst - FE;   // easting offset
    const Np = xNord - FN;  // northing offset

    const rho = Math.sqrt(Ep ** 2 + Np ** 2);

    if (rho < 1e-6) {
      // At origin
      return [phi0 * 180 / Math.PI, lam0 * 180 / Math.PI];
    }

    // 2. Inverse oblique stereographic → conformal sphere
    const c    = 2 * Math.atan(rho / (2 * R * k0));
    const cosc = Math.cos(c);
    const sinc = Math.sin(c);

    const chi = Math.asin(
      cosc * Math.sin(chi0) + Np * sinc * Math.cos(chi0) / rho
    );
    const lamK = lam0 + Math.atan2(
      Ep * sinc,
      rho * Math.cos(chi0) * cosc - Np * Math.sin(chi0) * sinc
    );

    // 3. Conformal latitude → geodetic latitude on Krassowski (iterate)
    let phiK = chi;
    for (let i = 0; i < 15; i++) {
      const sp = Math.sin(phiK);
      phiK = 2 * Math.atan(
        Math.tan(Math.PI / 4 + chi / 2) *
        ((1 + e * sp) / (1 - e * sp)) ** (e / 2)
      ) - Math.PI / 2;
    }

    // 4. Krassowski geodetic → Cartesian ECEF
    const NK  = a / Math.sqrt(1 - e2 * Math.sin(phiK) ** 2);
    const Xk  = NK * Math.cos(phiK) * Math.cos(lamK);
    const Yk  = NK * Math.cos(phiK) * Math.sin(lamK);
    const Zk  = NK * (1 - e2) * Math.sin(phiK);

    // 5. Helmert shift Krassowski → WGS84
    const Xw = Xk + this.dx;
    const Yw = Yk + this.dy;
    const Zw = Zk + this.dz;

    // 6. WGS84 ECEF → geographic
    const lamW = Math.atan2(Yw, Xw);
    const p    = Math.sqrt(Xw ** 2 + Yw ** 2);
    let phiW   = Math.atan2(Zw, p * (1 - this.e2_wgs));
    for (let i = 0; i < 15; i++) {
      const sp  = Math.sin(phiW);
      const Nw  = this.a_wgs / Math.sqrt(1 - this.e2_wgs * sp ** 2);
      phiW = Math.atan2(Zw + this.e2_wgs * Nw * sp, p);
    }

    return [phiW * 180 / Math.PI, lamW * 180 / Math.PI];
  }

  /**
   * Build a closed GeoJSON polygon from Stereo70 points.
   * Returns null if fewer than 3 valid points.
   */
  buildGeoJsonPolygon(points: { x: string; y: string }[]): any | null {
    const valid = points
      .map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) }))
      .filter(p => !isNaN(p.x) && !isNaN(p.y));
    if (valid.length < 3) return null;

    const coords = valid.map(p => {
      const [lat, lon] = this.stereo70ToWgs84(p.x, p.y);
      return [lon, lat];
    });
    coords.push(coords[0]); // close ring
    return {
      type: 'Feature',
      geometry: { type: 'Polygon', coordinates: [coords] },
      properties: {}
    };
  }

  /**
   * Calculate area in hectares using the Shoelace formula on the Stereo70
   * plane (X/Y in metres → m² → ha). This is accurate because Stereo70
   * is a conformal projection with a uniform scale factor.
   */
  calculateAreaHa(points: { x: string; y: string }[]): number | null {
    const valid = points
      .map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) }))
      .filter(p => !isNaN(p.x) && !isNaN(p.y));
    if (valid.length < 3) return null;

    let area = 0;
    for (let i = 0; i < valid.length; i++) {
      const j = (i + 1) % valid.length;
      area += valid[i].x * valid[j].y - valid[j].x * valid[i].y;
    }
    // Correct for scale factor k0² since Stereo70 distances are scaled
    const areaSqM = Math.abs(area) / 2.0 / (this.k0 * this.k0);
    return parseFloat((areaSqM / 10000.0).toFixed(4));
  }
}
