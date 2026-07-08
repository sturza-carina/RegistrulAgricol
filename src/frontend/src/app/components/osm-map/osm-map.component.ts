import { Component, OnInit, ElementRef, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { UatContextService } from '../../services/uat-context.service';
import { Subscription } from 'rxjs';
import * as L from 'leaflet';

@Component({
  selector: 'app-osm-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './osm-map.component.html',
  styleUrls: ['./osm-map.component.css']
})
export class OsmMapComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapContainer', { static: false }) mapContainer!: ElementRef;
  private map: L.Map | undefined;
  private uatSub!: Subscription;
  private maskLayer: L.Polygon | null = null;

  // Default coordinates (Cluj) fallback
  center: L.LatLngExpression = [46.7712, 23.6236];

  constructor(
    private uatContextService: UatContextService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.initMap();
    this.uatSub = this.uatContextService.activeUat$.subscribe(uat => {
      if (uat) {
        this.loadUatBoundary(uat.denumire, uat.judet);
      } else {
        this.clearMask();
        if (this.map) {
          this.map.setView(this.center, 13);
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.uatSub) {
      this.uatSub.unsubscribe();
    }
    if (this.maskLayer) {
      this.maskLayer.remove();
    }
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap(): void {
    const romaniaBounds = L.latLngBounds(
      [43.6, 20.2], // SouthWest
      [48.3, 29.7]  // NorthEast
    );

    this.map = L.map(this.mapContainer.nativeElement, {
      maxBounds: romaniaBounds,
      maxBoundsViscosity: 1.0,
      minZoom: 6
    }).setView(this.center, 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  private loadUatBoundary(uatName: string, county: string) {
    const q = `${uatName}, ${county}`;
    const url = `/api/proxy/nominatim?q=${encodeURIComponent(q)}`;
    this.http.get(url).subscribe((res: any) => {
      if (res && res.features && res.features.length > 0) {
        const feature = res.features.find((f: any) => f.geometry && (f.geometry.type === 'Polygon' || f.geometry.type === 'MultiPolygon'));
        if (feature) {
          this.applyMask(feature.geometry);
        }
      }
    });
  }

  private clearMask() {
    if (this.maskLayer) {
      this.maskLayer.remove();
      this.maskLayer = null;
    }
  }

  private applyMask(geometry: any) {
    if (!this.map) return;
    this.clearMask();

    const worldBox: L.LatLngTuple[] = [
      [-85, -180], [85, -180], [85, 180], [-85, 180]
    ];

    let holes: L.LatLngTuple[][] = [];
    if (geometry.type === 'Polygon') {
      holes.push(geometry.coordinates[0].map((coord: any) => [coord[1], coord[0]] as L.LatLngTuple));
    } else if (geometry.type === 'MultiPolygon') {
      geometry.coordinates.forEach((poly: any) => {
        holes.push(poly[0].map((coord: any) => [coord[1], coord[0]] as L.LatLngTuple));
      });
    }

    this.maskLayer = L.polygon([worldBox, ...holes] as any, {
      stroke: false,
      fillColor: '#000',
      fillOpacity: 0.6,
      interactive: false
    }).addTo(this.map);

    const bounds = L.latLngBounds([]);
    holes.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
    if (bounds.isValid()) {
      this.map.fitBounds(bounds, { padding: [20, 20] });
    }
  }
}
