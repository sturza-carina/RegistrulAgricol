import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';
import { TerenService } from '../../services/teren.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { CoordConversionService } from '../../services/coord-conversion.service';
import { Teren } from '../../models/teren.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-teren-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent],
  templateUrl: './teren-form.component.html',
  styleUrls: ['./teren-form.component.css']
})
export class TerenFormComponent implements OnInit, OnDestroy {
  gospodarieId!: number;
  map!: L.Map;
  mapInitialized = false;

  // Map layers
  maskLayer: L.Polygon | null = null;
  uatOutlineLayer: L.GeoJSON | null = null;
  markerGroup = L.featureGroup();
  polygonLayer: L.Polygon | null = null;
  polylineLayer: L.Polyline | null = null;

  tipTerenOptions = ['Extravilan', 'Intravilan'];

  teren: Partial<Teren> = { denumire: '', tipTeren: '' };

  points: { x: string; y: string }[] = [
    { x: '', y: '' },
    { x: '', y: '' },
    { x: '', y: '' }
  ];

  calculatedArea: number | null = null;
  saving = false;
  uatName = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private terenService: TerenService,
    private gospodarieService: GospodarieService,
    private conv: CoordConversionService,
    private http: HttpClient,
    private zone: NgZone
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['gospodarieId']) {
        this.gospodarieId = +params['gospodarieId'];
      }
      setTimeout(() => {
        this.initMap();
        this.loadUatBoundary();
      }, 150);
    });
  }

  ngOnDestroy() {
    if (this.map) this.map.remove();
  }

  private initMap() {
    const el = document.getElementById('teren-map');
    if (!el || this.mapInitialized) return;
    this.map = L.map('teren-map').setView([45.9432, 24.9668], 7);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
    this.markerGroup.addTo(this.map);
    this.mapInitialized = true;
  }

  private loadUatBoundary() {
    if (!this.gospodarieId) return;
    this.gospodarieService.getGospodarieById(this.gospodarieId).subscribe(g => {
      if (g?.uat) {
        this.uatName = g.uat.denumire || '';
        const county = g.uat.judet || '';
        this.fetchNominatimBoundary(this.uatName, county);
      }
    });
  }

  private fetchNominatimBoundary(uatName: string, county: string) {
    const url = `https://nominatim.openstreetmap.org/search?city=${encodeURIComponent(uatName)}&county=${encodeURIComponent(county)}&country=Romania&format=geojson&polygon_geojson=1&email=admin@registru.ro`;
    this.http.get<any>(url).subscribe(res => {
      if (res?.features?.length > 0) {
        const feature = res.features.find((f: any) =>
          f.geometry && (f.geometry.type === 'Polygon' || f.geometry.type === 'MultiPolygon')
        );
        if (feature) this.zone.run(() => this.applyUatMask(feature.geometry));
      }
    });
  }

  private applyUatMask(geometry: any) {
    if (!this.map) return;
    if (this.maskLayer) { this.map.removeLayer(this.maskLayer); }
    if (this.uatOutlineLayer) { this.map.removeLayer(this.uatOutlineLayer); }

    const worldBox: [number, number][] = [[-90, -180], [90, -180], [90, 180], [-90, 180]];
    const holes: [number, number][][] = [];

    if (geometry.type === 'Polygon') {
      holes.push(geometry.coordinates[0].map((c: any) => [c[1], c[0]] as [number, number]));
    } else if (geometry.type === 'MultiPolygon') {
      geometry.coordinates.forEach((poly: any) =>
        holes.push(poly[0].map((c: any) => [c[1], c[0]] as [number, number]))
      );
    }

    this.maskLayer = L.polygon([worldBox, ...holes] as any, {
      color: '#1e293b', fillColor: '#1e293b', fillOpacity: 0.55,
      weight: 0, stroke: false, interactive: false
    } as any).addTo(this.map);

    this.uatOutlineLayer = L.geoJSON(
      { type: 'Feature', geometry, properties: {} } as any,
      { style: { color: '#3b82f6', weight: 2.5, fill: false, dashArray: '6 4' } }
    ).addTo(this.map);

    const inner = L.polygon(holes as any);
    if (inner.getBounds().isValid()) {
      this.map.fitBounds(inner.getBounds(), { padding: [20, 20] });
    }
  }

  addPoint() {
    this.points.push({ x: '', y: '' });
    this.updatePreview();
  }

  removePoint(i: number) {
    if (this.points.length > 3) {
      this.points.splice(i, 1);
      this.updatePreview();
    }
  }

  /** Called on every keystroke — updates markers, polyline and polygon */
  updatePreview() {
    const valid = this.points
      .map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) }))
      .filter(p => !isNaN(p.x) && !isNaN(p.y));

    // Area from Stereo70 plane (accurate)
    this.calculatedArea = this.conv.calculateAreaHa(this.points);

    if (!this.mapInitialized) return;

    // --- 1. Markers for every valid point ---
    this.markerGroup.clearLayers();
    const latlngs: [number, number][] = valid.map((p, idx) => {
      const ll = this.conv.stereo70ToWgs84(p.x, p.y);
      const marker = L.circleMarker(ll, {
        radius: 6,
        color: '#1e40af',
        fillColor: '#3b82f6',
        fillOpacity: 1,
        weight: 2
      });
      marker.bindTooltip(`Punct ${idx + 1}<br>X: ${p.x}<br>Y: ${p.y}`, { permanent: false });
      this.markerGroup.addLayer(marker);
      return ll;
    });

    // Remove old polygon/polyline
    if (this.polygonLayer) { this.map.removeLayer(this.polygonLayer); this.polygonLayer = null; }
    if (this.polylineLayer) { this.map.removeLayer(this.polylineLayer); this.polylineLayer = null; }

    if (latlngs.length === 0) return;

    if (latlngs.length === 1) {
      // Just a marker — already drawn above
      this.map.setView(latlngs[0], Math.max(this.map.getZoom(), 14));
    } else if (latlngs.length === 2) {
      // Draw a line segment
      this.polylineLayer = L.polyline(latlngs, { color: '#3b82f6', weight: 2, dashArray: '5 4' }).addTo(this.map);
      this.map.fitBounds(this.polylineLayer.getBounds(), { padding: [40, 40] });
    } else {
      // 3+ points — draw a filled polygon
      this.polygonLayer = L.polygon(latlngs, {
        color: '#1e40af',
        weight: 2.5,
        fillColor: '#86efac',
        fillOpacity: 0.45
      }).addTo(this.map);

      // Keep UAT mask on top of polygon but keep labels accessible
      if (this.maskLayer) this.maskLayer.bringToFront();
      if (this.uatOutlineLayer) this.uatOutlineLayer.bringToFront();
      this.markerGroup.bringToFront();

      if (this.polygonLayer.getBounds().isValid()) {
        this.map.fitBounds(this.polygonLayer.getBounds(), { padding: [30, 30] });
      }
    }
  }

  cancel() {
    this.router.navigate(['/gospodarii', this.gospodarieId]);
  }

  save() {
    if (!this.teren.denumire?.trim()) { alert('Introduceți denumirea terenului.'); return; }
    if (!this.teren.tipTeren) { alert('Selectați tipul terenului.'); return; }

    const coordString = this.points
      .filter(p => p.x.trim() !== '' && p.y.trim() !== '')
      .map(p => `${p.x.trim()} ${p.y.trim()}`).join('\n');

    const polygon = this.conv.buildGeoJsonPolygon(this.points);

    const payload: any = {
      denumire: this.teren.denumire,
      tipTeren: this.teren.tipTeren,
      stereo70Coordinates: coordString || null,
      polygon,
      gospodarieId: this.gospodarieId
    };

    this.saving = true;
    this.terenService.createTeren(payload).subscribe({
      next: () => { this.saving = false; this.router.navigate(['/gospodarii', this.gospodarieId]); },
      error: (err) => { this.saving = false; console.error(err); alert('Eroare la salvare teren.'); }
    });
  }
}
