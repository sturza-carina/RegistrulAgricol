import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import * as L from 'leaflet';
import { TerenService } from '../../services/teren.service';
import { ParcelaService } from '../../services/parcela.service';
import { CoordConversionService } from '../../services/coord-conversion.service';
import { Teren } from '../../models/teren.model';
import { Parcela } from '../../models/parcela.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-teren-parcele',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent],
  templateUrl: './teren-parcele.component.html',
  styleUrls: ['./teren-parcele.component.css']
})
export class TerenParceleComponent implements OnInit, OnDestroy {
  terenId!: number;
  gospodarieId!: number;
  teren: Teren | null = null;
  parcele: Parcela[] = [];

  map!: L.Map;
  mapInitialized = false;

  // Map layers
  terenOutlineLayer: L.GeoJSON | L.Polygon | null = null;
  terenMaskLayer: L.Polygon | null = null;
  parcelaLayerGroup = L.featureGroup();
  markerGroup = L.featureGroup();
  previewPolygon: L.Polygon | null = null;
  previewPolyline: L.Polyline | null = null;

  isAddingParcela = false;
  viewingParcela: Parcela | null = null;

  newParcela: Parcela = {
    denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null, stereo70Coordinates: ''
  };
  points: { x: string; y: string }[] = [
    { x: '', y: '' }, { x: '', y: '' }, { x: '', y: '' }
  ];
  calculatedArea: number | null = null;
  saving = false;

  categoriiFolosinta = ['Arabil', 'Pășune', 'Fânețe', 'Livadă', 'Vii', 'Pădure', 'Ape', 'Alte'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private terenService: TerenService,
    private parcelaService: ParcelaService,
    private conv: CoordConversionService,
    private zone: NgZone
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.terenId = +params['id'];
    });
    this.route.queryParams.subscribe(params => {
      if (params['gospodarieId']) this.gospodarieId = +params['gospodarieId'];
    });

    this.terenService.getTerenById(this.terenId).subscribe(t => {
      this.teren = t;
      console.log('LOADED TEREN:', t);
      setTimeout(() => {
        this.initMap();
        this.loadParcele();
      }, 150);
    });
  }

  ngOnDestroy() {
    if (this.map) this.map.remove();
  }

  private initMap() {
    const el = document.getElementById('teren-parcele-map');
    if (!el || this.mapInitialized) return;
    this.map = L.map('teren-parcele-map').setView([45.9432, 24.9668], 7);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
    this.parcelaLayerGroup.addTo(this.map);
    this.markerGroup.addTo(this.map);
    this.mapInitialized = true;

    // Draw teren boundary
    if (this.teren?.polygon) {
      let geoJson = this.teren.polygon;
      try {
        if (typeof geoJson === 'string') {
          let parsed = JSON.parse(geoJson);
          while(typeof parsed === 'string') { parsed = JSON.parse(parsed); }
          geoJson = parsed;
        }
        
        const geom = geoJson.geometry || geoJson;
        if (geom.type === 'Polygon') {
          const latlngs = geom.coordinates[0].map((c: any[]) => [c[1], c[0]]);
          this.terenOutlineLayer = L.polygon(latlngs, {
            color: '#1e40af', weight: 4, fillColor: '#bfdbfe', fillOpacity: 0.15, dashArray: '8 5'
          }).addTo(this.map);
        } else if (geom.type === 'MultiPolygon') {
          const latlngs = geom.coordinates[0][0].map((c: any[]) => [c[1], c[0]]);
          this.terenOutlineLayer = L.polygon(latlngs, {
            color: '#1e40af', weight: 4, fillColor: '#bfdbfe', fillOpacity: 0.15, dashArray: '8 5'
          }).addTo(this.map);
        } else {
          // Fallback
          this.terenOutlineLayer = L.geoJSON(geoJson as any, {
            style: { color: '#1e40af', weight: 4, fillColor: '#bfdbfe', fillOpacity: 0.15, dashArray: '8 5' }
          }).addTo(this.map) as any;
        }
      } catch (e) {
        console.error('Failed to parse Teren polygon:', e, this.teren.polygon);
      }

      // Ensure the map resizes perfectly and zooms when the layout is established
      const el = document.getElementById('teren-parcele-map');
      if (el) {
        const ro = new ResizeObserver(() => {
          if (!this.map) return;
          this.map.invalidateSize();
          
          let bounds = this.terenOutlineLayer ? this.terenOutlineLayer.getBounds() : null;
          if (!bounds || !bounds.isValid()) {
              bounds = this.parcelaLayerGroup.getBounds();
          }
          if (bounds && bounds.isValid()) {
              this.map.fitBounds(bounds, { padding: [25, 25] });
          }
        });
        ro.observe(el);
      }
    }
  }

  private applyTerenMask() {
    if (!this.map || !this.teren?.polygon) return;
    if (this.terenMaskLayer) { this.map.removeLayer(this.terenMaskLayer); }

    let geoJson = this.teren.polygon;
    try {
      if (typeof geoJson === 'string') {
        let parsed = JSON.parse(geoJson);
        while(typeof parsed === 'string') { parsed = JSON.parse(parsed); }
        geoJson = parsed;
      }
    } catch(e) {}

    const worldBox: [number, number][] = [[-90, -180], [90, -180], [90, 180], [-90, 180]];
    const geom = (geoJson as any).geometry ?? geoJson;
    const holes: [number, number][][] = [];

    if (geom.type === 'Polygon') {
      holes.push(geom.coordinates[0].map((c: any) => [c[1], c[0]] as [number, number]));
    } else if (geom.type === 'MultiPolygon') {
      geom.coordinates.forEach((poly: any) =>
        holes.push(poly[0].map((c: any) => [c[1], c[0]] as [number, number]))
      );
    }

    this.terenMaskLayer = L.polygon([worldBox, ...holes] as any, {
      color: '#1e293b', fillColor: '#1e293b', fillOpacity: 0.55,
      weight: 0, stroke: false, interactive: false
    } as any).addTo(this.map);

    if (this.terenOutlineLayer) this.terenOutlineLayer.bringToFront();
    this.parcelaLayerGroup.bringToFront();
    this.markerGroup.bringToFront();
  }

  private removeTerenMask() {
    if (this.terenMaskLayer && this.map) {
      this.map.removeLayer(this.terenMaskLayer);
      this.terenMaskLayer = null;
    }
  }

  private clearPreviewLayers() {
    if (this.previewPolygon && this.map) { this.map.removeLayer(this.previewPolygon); this.previewPolygon = null; }
    if (this.previewPolyline && this.map) { this.map.removeLayer(this.previewPolyline); this.previewPolyline = null; }
    this.markerGroup.clearLayers();
  }

  // --- Load & render parcels ---
  loadParcele() {
    this.parcelaService.getParcele(this.terenId).subscribe(data => {
      this.parcele = data;
      this.renderParcele();
    });
  }

  renderParcele() {
    if (!this.mapInitialized) return;
    this.parcelaLayerGroup.clearLayers();
    this.parcele.forEach(p => {
      if (p.polygon) {
        let geoJson = p.polygon;
        try {
          if (typeof geoJson === 'string') {
            let parsed = JSON.parse(geoJson);
            while(typeof parsed === 'string') { parsed = JSON.parse(parsed); }
            geoJson = parsed;
          }
        } catch(e) {}
        const layer = L.geoJSON(geoJson as any, {
          style: { color: '#dc2626', weight: 2, fillColor: '#fca5a5', fillOpacity: 0.5 }
        });
        layer.bindTooltip(`<b>${p.denumire}</b><br>${p.suprafata} ha<br>${p.categorieFolosinta}`);
        layer.on('click', () => this.zone.run(() => { this.viewingParcela = p; this.isAddingParcela = false; }));
        layer.addTo(this.parcelaLayerGroup);
      }
    });

    if (this.terenOutlineLayer) {
      this.terenOutlineLayer.bringToFront();
    }
  }

  // --- Point / polygon management ---
  addPoint() {
    this.points.push({ x: '', y: '' });
    this.updatePreview();
  }

  removePoint(i: number) {
    if (this.points.length > 1) {
      this.points.splice(i, 1);
      this.updatePreview();
    }
  }

  /** Live update: markers + line + polygon as the user types */
  updatePreview() {
    const valid = this.points
      .map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) }))
      .filter(p => !isNaN(p.x) && !isNaN(p.y));

    this.calculatedArea = this.conv.calculateAreaHa(this.points);
    if (this.calculatedArea !== null) {
      this.newParcela.suprafata = this.calculatedArea;
    }

    if (!this.mapInitialized) return;

    // 1. Markers for every valid point
    this.markerGroup.clearLayers();
    const latlngs: [number, number][] = valid.map((p, idx) => {
      const ll = this.conv.stereo70ToWgs84(p.x, p.y);
      const marker = L.circleMarker(ll, {
        radius: 6, color: '#1e40af', fillColor: '#3b82f6', fillOpacity: 1, weight: 2
      });
      marker.bindTooltip(`Punct ${idx + 1}<br>X: ${p.x}<br>Y: ${p.y}`, { permanent: false });
      this.markerGroup.addLayer(marker);
      return ll;
    });

    // 2. Remove old preview shapes
    if (this.previewPolygon) { this.map.removeLayer(this.previewPolygon); this.previewPolygon = null; }
    if (this.previewPolyline) { this.map.removeLayer(this.previewPolyline); this.previewPolyline = null; }

    if (latlngs.length === 0) return;

    if (latlngs.length === 1) {
      this.map.setView(latlngs[0], Math.max(this.map.getZoom(), 15));
    } else if (latlngs.length === 2) {
      this.previewPolyline = L.polyline(latlngs, { color: '#3b82f6', weight: 2, dashArray: '5 4' }).addTo(this.map);
      this.map.fitBounds(this.previewPolyline.getBounds(), { padding: [40, 40] });
    } else {
      this.previewPolygon = L.polygon(latlngs, {
        color: '#16a34a', weight: 2.5, fillColor: '#86efac', fillOpacity: 0.45
      }).addTo(this.map);

      // Keep mask + outline on top, markers last
      if (this.terenMaskLayer) this.terenMaskLayer.bringToFront();
      if (this.terenOutlineLayer) this.terenOutlineLayer.bringToFront();
      this.parcelaLayerGroup.bringToFront();
      this.markerGroup.bringToFront();

      if (this.previewPolygon.getBounds().isValid()) {
        this.map.fitBounds(this.previewPolygon.getBounds(), { padding: [25, 25] });
      }
    }
  }

  openAddParcelaForm() {
    this.viewingParcela = null;
    this.newParcela = { denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null, stereo70Coordinates: '' };
    this.points = [{ x: '', y: '' }, { x: '', y: '' }, { x: '', y: '' }];
    this.calculatedArea = null;
    this.isAddingParcela = true;
    this.applyTerenMask();

    // Zoom to teren
    if (this.terenOutlineLayer) {
      const b = this.terenOutlineLayer.getBounds();
      if (b.isValid()) this.map.fitBounds(b, { padding: [20, 20] });
    }
  }

  cancelAdd() {
    this.isAddingParcela = false;
    this.clearPreviewLayers();
    this.calculatedArea = null;
    this.removeTerenMask();
  }

  saveParcela() {
    if (!this.newParcela.denumire?.trim()) { alert('Introduceți denumirea parcelei.'); return; }

    const coordString = this.points
      .filter(p => p.x.trim() !== '' && p.y.trim() !== '')
      .map(p => `${p.x.trim()} ${p.y.trim()}`).join('\n');

    this.newParcela.stereo70Coordinates = coordString || undefined;
    this.newParcela.polygon = this.conv.buildGeoJsonPolygon(this.points);

    this.saving = true;
    this.parcelaService.createParcela(this.terenId, this.newParcela).subscribe({
      next: (saved) => {
        this.saving = false;
        this.parcele.push(saved);
        this.renderParcele();
        this.isAddingParcela = false;
        this.clearPreviewLayers();
        this.removeTerenMask();
        this.calculatedArea = null;
      },
      error: (err) => { this.saving = false; console.error(err); alert('Eroare la salvare parcelă.'); }
    });
  }

  deleteParcela(p: Parcela) {
    if (!p.id) return;
    if (!confirm(`Ștergeți parcela "${p.denumire}"?`)) return;
    this.parcelaService.deleteParcela(p.id).subscribe({
      next: () => {
        this.parcele = this.parcele.filter(x => x.id !== p.id);
        this.renderParcele();
        this.viewingParcela = null;
      },
      error: () => alert('Eroare la ștergere.')
    });
  }

  goBack() {
    this.router.navigate(['/gospodarii', this.gospodarieId]);
  }
}
