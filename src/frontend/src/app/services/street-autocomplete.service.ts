import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

interface NominatimAddress {
  road?: string;
  pedestrian?: string;
  footway?: string;
}

interface NominatimResult {
  address?: NominatimAddress;
}

@Injectable({ providedIn: 'root' })
export class StreetAutocompleteService {
  constructor(private http: HttpClient) {}

  /** Suggests Romanian street names matching `query`, optionally scoped to a city/county, via Nominatim. */
  search(query: string, city?: string, county?: string): Observable<string[]> {
    if (!query || query.trim().length < 3) return of([]);

    let url = `https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&limit=8&countrycodes=ro&email=admin@registru.ro&street=${encodeURIComponent(query.trim())}`;
    if (city) url += `&city=${encodeURIComponent(city.trim())}`;
    if (county) url += `&county=${encodeURIComponent(county.trim())}`;

    return this.http.get<NominatimResult[]>(url).pipe(
      map(results => {
        const names = results
          .map(r => r.address?.road || r.address?.pedestrian || r.address?.footway)
          .filter((n): n is string => !!n);
        return Array.from(new Set(names));
      }),
      catchError(() => of([]))
    );
  }
}
