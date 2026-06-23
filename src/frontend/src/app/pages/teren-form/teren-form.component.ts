import { Component, OnInit, OnDestroy, NgZone, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TerenService } from '../../services/teren.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { CoordConversionService } from '../../services/coord-conversion.service';
import { GoogleMapsLoaderService } from '../../services/google-maps-loader.service';
import { Teren } from '../../models/teren.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';

const DASH_ICON: google.maps.IconSequence = {
  icon: { path: 'M 0,-1 0,1', strokeOpacity: 1, scale: 3 },
  offset: '0',
  repeat: '12px'
};

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
  map!: google.maps.Map;
  mapInitialized = false;

  // Map overlays
  maskPolygon: google.maps.Polygon | null = null;
  uatOutlinePolygons: google.maps.Polyline[] = [];
  markers: google.maps.Marker[] = [];
  polygonOverlay: google.maps.Polygon | null = null;
  polylineOverlay: google.maps.Polyline | null = null;
  infoWindow: google.maps.InfoWindow | null = null;

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
    private zone: NgZone,
    private googleMapsLoader: GoogleMapsLoaderService
  ) { }

  ngOnInit() {
    if (this.isModal && this.inputGospodarieId) {
      this.gospodarieId = this.inputGospodarieId;
      this.googleMapsLoader.load().then(() => {
        setTimeout(() => {
          this.initMap();
          this.loadUatBoundary();
        }, 300);
      });
    } else {
      this.route.queryParams.subscribe(params => {
        if (params['gospodarieId']) {
          this.gospodarieId = +params['gospodarieId'];
        }
        this.updateBreadcrumbs();
        this.googleMapsLoader.load().then(() => {
          setTimeout(() => {
            this.initMap();
            this.loadUatBoundary();
          }, 150);
        });
      });
    }
  }

  ngOnDestroy() {
    this.markers.forEach(m => m.setMap(null));
    if (this.maskPolygon) this.maskPolygon.setMap(null);
    this.uatOutlinePolygons.forEach(p => p.setMap(null));
    if (this.polygonOverlay) this.polygonOverlay.setMap(null);
    if (this.polylineOverlay) this.polylineOverlay.setMap(null);
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
    this.map = new google.maps.Map(el, {
      center: { lat: 45.9432, lng: 24.9668 },
      zoom: 7,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: false
    });
    this.infoWindow = new google.maps.InfoWindow();
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
    if (this.maskPolygon) { this.maskPolygon.setMap(null); this.maskPolygon = null; }
    this.uatOutlinePolygons.forEach(p => p.setMap(null));
    this.uatOutlinePolygons = [];

    const worldBox: google.maps.LatLngLiteral[] = [
      { lat: -85, lng: -180 }, { lat: 85, lng: -180 }, { lat: 85, lng: 180 }, { lat: -85, lng: 180 }
    ];
    const holes: google.maps.LatLngLiteral[][] = [];

    if (geometry.type === 'Polygon') {
      holes.push(geometry.coordinates[0].map((c: any) => ({ lat: c[1], lng: c[0] })));
    } else if (geometry.type === 'MultiPolygon') {
      geometry.coordinates.forEach((poly: any) =>
        holes.push(poly[0].map((c: any) => ({ lat: c[1], lng: c[0] })))
      );
    }

    this.maskPolygon = new google.maps.Polygon({
      paths: [worldBox, ...holes],
      strokeWeight: 0,
      fillColor: '#1e293b',
      fillOpacity: 0.55,
      clickable: false,
      map: this.map
    });

    holes.forEach(ring => {
      const outline = new google.maps.Polyline({
        path: [...ring, ring[0]],
        strokeColor: '#3b82f6',
        strokeWeight: 2.5,
        strokeOpacity: 0,
        icons: [DASH_ICON],
        clickable: false,
        map: this.map
      });
      this.uatOutlinePolygons.push(outline);
    });

    const bounds = new google.maps.LatLngBounds();
    holes.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
    if (!bounds.isEmpty()) {
      this.map.fitBounds(bounds, 20);
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
    this.markers.forEach(m => m.setMap(null));
    this.markers = [];

    const latlngs: google.maps.LatLngLiteral[] = valid.map((p, idx) => {
      const [lat, lng] = this.conv.stereo70ToWgs84(p.x, p.y);
      const position = { lat, lng };
      const marker = new google.maps.Marker({
        position,
        map: this.map,
        icon: {
          path: google.maps.SymbolPath.CIRCLE,
          scale: 6,
          fillColor: '#3b82f6',
          fillOpacity: 1,
          strokeColor: '#1e40af',
          strokeWeight: 2
        }
      });
      marker.addListener('mouseover', () => {
        this.infoWindow?.setContent(`Punct ${idx + 1}<br>X: ${p.x}<br>Y: ${p.y}`);
        this.infoWindow?.open({ map: this.map, anchor: marker });
      });
      marker.addListener('mouseout', () => this.infoWindow?.close());
      this.markers.push(marker);
      return position;
    });

    // Remove old polygon/polyline
    if (this.polygonOverlay) { this.polygonOverlay.setMap(null); this.polygonOverlay = null; }
    if (this.polylineOverlay) { this.polylineOverlay.setMap(null); this.polylineOverlay = null; }

    if (latlngs.length === 0) return;

    if (latlngs.length === 1) {
      // Just a marker — already drawn above
      this.map.setCenter(latlngs[0]);
      this.map.setZoom(Math.max(this.map.getZoom() ?? 7, 14));
    } else if (latlngs.length === 2) {
      // Draw a line segment
      this.polylineOverlay = new google.maps.Polyline({
        path: latlngs,
        strokeColor: '#3b82f6',
        strokeWeight: 2,
        strokeOpacity: 0,
        icons: [{ icon: { path: 'M 0,-1 0,1', strokeOpacity: 1, scale: 2 }, offset: '0', repeat: '10px' }],
        map: this.map
      });
      const bounds = new google.maps.LatLngBounds();
      latlngs.forEach(p => bounds.extend(p));
      this.map.fitBounds(bounds, 40);
    } else {
      // 3+ points — draw a filled polygon
      this.polygonOverlay = new google.maps.Polygon({
        paths: latlngs,
        strokeColor: '#1e40af',
        strokeWeight: 2.5,
        fillColor: '#86efac',
        fillOpacity: 0.45,
        map: this.map
      });

      // Markers render in their own pane above polygons by default — no z-order fix needed.

      const bounds = new google.maps.LatLngBounds();
      latlngs.forEach(p => bounds.extend(p));
      this.map.fitBounds(bounds, 30);
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
