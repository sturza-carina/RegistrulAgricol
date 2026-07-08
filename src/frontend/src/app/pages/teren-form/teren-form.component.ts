import { Component, OnInit, OnDestroy, NgZone, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TerenService } from '../../services/teren.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { CoordConversionService } from '../../services/coord-conversion.service';
import { Teren } from '../../models/teren.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import * as L from 'leaflet';

@Component({
  selector: 'app-teren-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent, BreadcrumbsComponent],
  templateUrl: './teren-form.component.html',
  styleUrls: ['./teren-form.component.css']
})
export class TerenFormComponent implements OnInit, OnDestroy {
  @Input() isModal = false;
  @Input() inputGospodarieId?: number;
  @Output() closeForm = new EventEmitter<void>();

  gospodarieId!: number;
  map!: L.Map;
  mapInitialized = false;

  // Map overlays
  maskPolygon: L.Polygon | null = null;
  uatOutlinePolygons: L.Polyline[] = [];
  markers: L.Marker[] = [];
  polygonOverlay: L.Polygon | null = null;
  polylineOverlay: L.Polyline | null = null;

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
  breadcrumbItems: BreadcrumbItem[] = [];

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
    if (this.isModal && this.inputGospodarieId) {
      this.gospodarieId = this.inputGospodarieId;
      setTimeout(() => {
        this.initMap();
        this.loadUatBoundary();
      }, 300);
    } else {
      this.route.queryParams.subscribe(params => {
        if (params['gospodarieId']) {
          this.gospodarieId = +params['gospodarieId'];
        }
        this.updateBreadcrumbs();
        setTimeout(() => {
          this.initMap();
          this.loadUatBoundary();
        }, 150);
      });
    }
  }

  ngOnDestroy() {
    this.markers.forEach(m => m.remove());
    if (this.maskPolygon) this.maskPolygon.remove();
    this.uatOutlinePolygons.forEach(p => p.remove());
    if (this.polygonOverlay) this.polygonOverlay.remove();
    if (this.polylineOverlay) this.polylineOverlay.remove();
    if (this.map) this.map.remove();
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' },
      { label: 'Detalii Gospodărie', link: `/gospodarii/${this.gospodarieId}`, queryParams: { tab: 'TERENURI' } },
      { label: 'Adăugare Teren' }
    ];
  }

  private initMap() {
    const el = document.getElementById('teren-map');
    if (!el || this.mapInitialized) return;

    const romaniaBounds = L.latLngBounds(
      [43.6, 20.2], // SouthWest
      [48.3, 29.7]  // NorthEast
    );

    this.map = L.map(el, {
      maxBounds: romaniaBounds,
      maxBoundsViscosity: 1.0,
      minZoom: 6
    }).setView([45.9432, 24.9668], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
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
    const q = `${uatName}, ${county}`;
    const url = `/api/proxy/nominatim?q=${encodeURIComponent(q)}`;
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
    if (this.maskPolygon) { this.maskPolygon.remove(); this.maskPolygon = null; }
    this.uatOutlinePolygons.forEach(p => p.remove());
    this.uatOutlinePolygons = [];

    const worldBox: L.LatLngTuple[] = [
      [-85, -180], [85, -180], [85, 180], [-85, 180]
    ];
    const holes: L.LatLngTuple[][] = [];

    if (geometry.type === 'Polygon') {
      holes.push(geometry.coordinates[0].map((c: any) => [c[1], c[0]] as L.LatLngTuple));
    } else if (geometry.type === 'MultiPolygon') {
      geometry.coordinates.forEach((poly: any) =>
        holes.push(poly[0].map((c: any) => [c[1], c[0]] as L.LatLngTuple))
      );
    }

    this.maskPolygon = L.polygon([worldBox, ...holes] as any, {
      stroke: false,
      fillColor: '#1e293b',
      fillOpacity: 0.55,
      interactive: false
    }).addTo(this.map);

    holes.forEach(ring => {
      const outline = L.polyline([...ring, ring[0]], {
        color: '#3b82f6',
        weight: 2.5,
        opacity: 0.8,
        dashArray: '10, 10',
        interactive: false
      }).addTo(this.map);
      this.uatOutlinePolygons.push(outline);
    });

    const bounds = L.latLngBounds([]);
    holes.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
    if (bounds.isValid()) {
      this.map.fitBounds(bounds, { padding: [20, 20] });
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

  updatePreview() {
    const valid = this.points
      .map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) }))
      .filter(p => !isNaN(p.x) && !isNaN(p.y));

    this.calculatedArea = this.conv.calculateAreaHa(this.points);

    if (!this.mapInitialized) return;

    this.markers.forEach(m => m.remove());
    this.markers = [];

    const latlngs: L.LatLngTuple[] = valid.map((p, idx) => {
      const [lat, lng] = this.conv.stereo70ToWgs84(p.x, p.y);
      const position = [lat, lng] as L.LatLngTuple;
      const marker = L.circleMarker(position, {
        radius: 6,
        fillColor: '#3b82f6',
        fillOpacity: 1,
        color: '#1e40af',
        weight: 2
      }).addTo(this.map);
      marker.bindTooltip(`Punct ${idx + 1}<br>X: ${p.x}<br>Y: ${p.y}`);
      this.markers.push(marker as any);
      return position;
    });

    if (this.polygonOverlay) { this.polygonOverlay.remove(); this.polygonOverlay = null; }
    if (this.polylineOverlay) { this.polylineOverlay.remove(); this.polylineOverlay = null; }

    if (latlngs.length === 0) return;

    if (latlngs.length === 1) {
      this.map.setView(latlngs[0], Math.max(this.map.getZoom() ?? 7, 14));
    } else if (latlngs.length === 2) {
      this.polylineOverlay = L.polyline(latlngs, {
        color: '#3b82f6',
        weight: 2,
        opacity: 0.8,
        dashArray: '10, 10'
      }).addTo(this.map);
      const bounds = L.latLngBounds(latlngs);
      this.map.fitBounds(bounds, { padding: [40, 40] });
    } else {
      this.polygonOverlay = L.polygon(latlngs, {
        color: '#1e40af',
        weight: 2.5,
        fillColor: '#86efac',
        fillOpacity: 0.45
      }).addTo(this.map);

      const bounds = L.latLngBounds(latlngs);
      if (bounds.isValid()) this.map.fitBounds(bounds, { padding: [30, 30] });
    }
  }

  cancel() {
    if (this.isModal) {
      this.closeForm.emit();
    } else {
      this.router.navigate(['/gospodarii', this.gospodarieId]);
    }
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
      next: () => { 
        this.saving = false; 
        if (this.isModal) {
          this.closeForm.emit();
        } else {
          this.router.navigate(['/gospodarii', this.gospodarieId]);
        }
      },
      error: (err) => { this.saving = false; console.error(err); alert('Eroare la salvare teren.'); }
    });
  }
}
