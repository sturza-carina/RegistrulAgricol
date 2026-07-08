import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TerenService } from '../../services/teren.service';
import { ParcelaService } from '../../services/parcela.service';
import { CoordConversionService } from '../../services/coord-conversion.service';
import { CategorieFolosintaService } from '../../services/categorie-folosinta.service';
import * as L from 'leaflet';
import { GospodarieService } from '../../services/gospodarie.service';
import { Teren } from '../../models/teren.model';
import { Parcela } from '../../models/parcela.model';
import { CategorieFolosinta } from '../../models/categorie-folosinta.model';
import { CulturaParcela } from '../../models/cultura-parcela.model';
import { CulturaParcelaService } from '../../services/cultura-parcela.service';
import { CicluProductie } from '../../models/ciclu-productie.model';
import { CicluProductieService } from '../../services/ciclu-productie.service';
import { Recoltare } from '../../models/recoltare.model';
import { RecoltareService } from '../../services/recoltare.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { SursaApa } from '../../models/sursa-apa.model';
import { SursaApaService } from '../../services/sursa-apa.service';
import { LookupService } from '../../services/lookup.service';
import { Pom, TipInregistrarePom } from '../../models/pom.model';
import { PomService } from '../../services/pom.service';
import { VitaDeVie, TipInregistrareVita } from '../../models/vita-de-vie.model';
import { VitaDeVieService } from '../../services/vita-de-vie.service';
import { PasuneFaneata, TipFolosintaPasune } from '../../models/pasune-faneata.model';
import { PasuneFaneataService } from '../../services/pasune-faneata.service';
import { SpecieRef } from '../../models/specie-ref.model';
import { Padure } from '../../models/padure.model';
import { PadureService } from '../../services/padure.service';

@Component({
  selector: 'app-teren-parcele',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent, BreadcrumbsComponent],
  templateUrl: './teren-parcele.component.html',
  styleUrls: ['./teren-parcele.component.css']
})
export class TerenParceleComponent implements OnInit, OnDestroy {
  terenId!: number;
  gospodarieId!: number;
  teren: Teren | null = null;
  parcele: Parcela[] = [];
  breadcrumbItems: BreadcrumbItem[] = [];

  map!: L.Map;
  mapInitialized = false;

  // Map layers
  terenOutlineLayer: L.Polygon | null = null;
  terenMaskLayer: L.Polygon | null = null;
  judetOutlineLayer: L.Polygon | null = null;
  parcelaPolygons: L.Polygon[] = [];
  parcelaLayers: { parcela: Parcela; polygon: L.Polygon }[] = [];
  previewMarkers: L.Marker[] = [];
  previewPolygon: L.Polygon | null = null;
  previewPolyline: L.Polyline | null = null;

  isAddingParcela = false;
  viewingParcela: Parcela | null = null;

  newParcela: Parcela = {
    denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null, stereo70Coordinates: '',
    tipMediu: 'CAMP_DESCHIS', suprafataUtilaMp: 0
  };
  points: { x: string; y: string }[] = [
    { x: '', y: '' }, { x: '', y: '' }, { x: '', y: '' }
  ];
  calculatedArea: number | null = null;
  saving = false;

  categorii: CategorieFolosinta[] = [];
  isAddingCategorie = false;
  editingCategorie: CategorieFolosinta | null = null;
  newCategorie: CategorieFolosinta = { denumire: '', descriere: '' };

  culturi: CulturaParcela[] = [];
  isAddingCultura = false;
  editingCultura: CulturaParcela | null = null;
  newCultura: Partial<CulturaParcela> = {};

  surse: SursaApa[] = [];
  isAddingSursa = false;
  editingSursa: SursaApa | null = null;
  newSursa: Partial<SursaApa> = { stareFunctionare: true };

  pomi: Pom[] = [];
  isAddingPom = false;
  editingPom: Pom | null = null;
  newPom: Partial<Pom> = { tipInregistrare: TipInregistrarePom.IZOLAT };
  tipuriInregistrarePom = [TipInregistrarePom.IZOLAT, TipInregistrarePom.PLANTATIE];
  speciiPomiToate: SpecieRef[] = [];

  vitaDeVie: VitaDeVie[] = [];
  isAddingVita = false;
  editingVita: VitaDeVie | null = null;
  newVita: Partial<VitaDeVie> = { tipInregistrare: TipInregistrareVita.IZOLAT };
  tipuriInregistrareVita = [TipInregistrareVita.IZOLAT, TipInregistrareVita.PLANTATIE];

  pasuniFanete: PasuneFaneata[] = [];
  isAddingPasune = false;
  editingPasune: PasuneFaneata | null = null;
  newPasune: Partial<PasuneFaneata> = { tipFolosinta: TipFolosintaPasune.PASUNAT };
  tipuriFolosintaPasune = [TipFolosintaPasune.PASUNAT, TipFolosintaPasune.COSIT, TipFolosintaPasune.MIXT];

  cicluri: CicluProductie[] = [];
  isAddingCiclu = false;
  editingCiclu: CicluProductie | null = null;
  newCiclu: Partial<CicluProductie> = { status: 'ACTIV', programSprijin: false };

  recoltari: Recoltare[] = [];
  isAddingRecoltare = false;
  editingRecoltare: Recoltare | null = null;
  newRecoltare: Partial<Recoltare> = { cantitateKg: 0 };

  paduri: Padure[] = [];
  isAddingPadure = false;
  editingPadure: Padure | null = null;
  newPadure: Partial<Padure> = { tipVegetatie: 'Pădure' };
  tipuriVegetatieForestiera = ['Pădure', 'Perdea forestieră de protecție', 'Pepinieră silvică', 'Răchitărie', 'Alta'];

  categoriiFolosinta: string[] = [];
  tipuriSol: string[] = [];
  tipuriSursa: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private terenService: TerenService,
    private parcelaService: ParcelaService,
    private categorieService: CategorieFolosintaService,
    private culturaService: CulturaParcelaService,
    private sursaApaService: SursaApaService,
    private conv: CoordConversionService,
    private gospodarieService: GospodarieService,
    private lookupService: LookupService,
    private pomiService: PomService,
    private vitaDeVieService: VitaDeVieService,
    private pasuneFaneataService: PasuneFaneataService,
    private cicluService: CicluProductieService,
    private recoltareService: RecoltareService,
    private padureService: PadureService,
    private http: HttpClient,
    private zone: NgZone
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.terenId = +params['id'];
    });
    this.route.queryParams.subscribe(params => {
      if (params['gospodarieId']) {
        this.gospodarieId = +params['gospodarieId'];
        this.gospodarieService.getGospodarieById(this.gospodarieId).subscribe(g => {
          if (g?.uat?.denumire && g?.uat?.judet) {
            this.loadUatBoundary(g.uat.denumire, g.uat.judet);
          }
        });
      }
    });

    this.terenService.getTerenById(this.terenId).subscribe(t => {
      this.teren = t;
      this.updateBreadcrumbs();
      console.log('LOADED TEREN:', t);
      setTimeout(() => {
        this.initMap();
        this.loadParcele();
        this.loadCategorii();
      }, 150);
    });

    this.lookupService.getCategoriiFolosinta().subscribe(v => this.categoriiFolosinta = v);
    this.lookupService.getTipuriSol().subscribe(v => this.tipuriSol = v);
    this.lookupService.getTipuriSursaApa().subscribe(v => this.tipuriSursa = v);
    this.lookupService.getSpeciiPomi().subscribe(v => this.speciiPomiToate = v);
  }

  private uatMaskLayer: L.Polygon | null = null;
  private pendingUatGeometry: any = null;

  /** Fetches the outline of the UAT the gospodărie belongs to and applies a mask. */
  private loadUatBoundary(uatName: string, county: string) {
    const q = `${uatName}, ${county}`;
    const url = `/api/proxy/nominatim?q=${encodeURIComponent(q)}`;
    this.http.get<any>(url).subscribe(res => {
      if (res?.features?.length > 0) {
        const feature = res.features.find((f: any) =>
          f.geometry && (f.geometry.type === 'Polygon' || f.geometry.type === 'MultiPolygon')
        );
        if (feature) {
          if (this.mapInitialized) {
            this.applyUatMask(feature.geometry);
          } else {
            this.pendingUatGeometry = feature.geometry;
          }
        }
      }
    });
  }

  private applyUatMask(geometry: any) {
    if (!this.map) return;
    if (this.uatMaskLayer) {
      this.uatMaskLayer.remove();
      this.uatMaskLayer = null;
    }

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

    this.uatMaskLayer = L.polygon([worldBox, ...holes] as any, {
      stroke: false,
      fillColor: '#000',
      fillOpacity: 0.6,
      interactive: false
    }).addTo(this.map);

    if (!this.teren?.polygon) {
      const bounds = L.latLngBounds([]);
      holes.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
      if (bounds.isValid()) {
        this.map.fitBounds(bounds, { padding: [20, 20] });
      }
    }
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' },
      { label: 'Detalii Gospodărie', link: `/gospodarii/${this.gospodarieId}?tab=TERENURI` },
      { label: `Teren: ${this.teren?.denumire || ''}` }
    ];
  }

  ngOnDestroy() {
    this.parcelaPolygons.forEach(p => p.remove());
    this.previewMarkers.forEach(m => m.remove());
    if (this.terenOutlineLayer) this.terenOutlineLayer.remove();
    if (this.terenMaskLayer) this.terenMaskLayer.remove();
    if (this.judetOutlineLayer) this.judetOutlineLayer.remove();
    if (this.previewPolygon) this.previewPolygon.remove();
    if (this.previewPolyline) this.previewPolyline.remove();
    if (this.map) this.map.remove();
  }

  /** Parses a (possibly doubly-stringified) GeoJSON value and returns the geometry object. */
  private parseGeoJson(raw: any): any | null {
    let geoJson = raw;
    try {
      if (typeof geoJson === 'string') {
        let parsed = JSON.parse(geoJson);
        while (typeof parsed === 'string') { parsed = JSON.parse(parsed); }
        geoJson = parsed;
      }
    } catch (e) {
      console.error('Failed to parse polygon JSON:', e, raw);
      return null;
    }
    return geoJson.geometry ?? geoJson;
  }

  /** Converts a Polygon/MultiPolygon geometry into Google Maps paths (rings, including holes). */
  private extractPaths(geom: any): L.LatLngTuple[][] {
    if (!geom) return [];
    if (geom.type === 'Polygon') {
      return geom.coordinates.map((ring: any[]) => ring.map((c: number[]) => [c[1], c[0]] as L.LatLngTuple));
    }
    if (geom.type === 'MultiPolygon') {
      const paths: L.LatLngTuple[][] = [];
      geom.coordinates.forEach((poly: any[]) => {
        poly.forEach((ring: any[]) => paths.push(ring.map((c: number[]) => [c[1], c[0]] as L.LatLngTuple)));
      });
      return paths;
    }
    return [];
  }

  private initMap() {
    const el = document.getElementById('teren-parcele-map');
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

    if (this.pendingUatGeometry) {
      this.applyUatMask(this.pendingUatGeometry);
      this.pendingUatGeometry = null;
    }

    if (this.teren?.polygon) {
      const geom = this.parseGeoJson(this.teren.polygon);
      const paths = this.extractPaths(geom);
      if (paths.length) {
        this.terenOutlineLayer = L.polygon(paths as any, {
          color: '#1e40af', weight: 4, opacity: 0.9,
          fillColor: '#bfdbfe', fillOpacity: 0.15
        }).addTo(this.map);

        const bounds = L.latLngBounds([]);
        paths.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
        if (bounds.isValid()) this.map.fitBounds(bounds);
      }
    }

    this.renderParcele();
  }

  private applyTerenMask() {
    if (!this.map || !this.teren?.polygon) return;
    if (this.terenMaskLayer) { this.terenMaskLayer.remove(); this.terenMaskLayer = null; }

    const geom = this.parseGeoJson(this.teren.polygon);
    const holes = this.extractPaths(geom);
    if (!holes.length) return;

    const worldBox: L.LatLngTuple[] = [
      [-85, -180], [85, -180], [85, 180], [-85, 180]
    ];

    this.terenMaskLayer = L.polygon([worldBox, ...holes] as any, {
      stroke: false,
      fillColor: '#1e293b',
      fillOpacity: 0.55,
      interactive: false
    }).addTo(this.map);
  }

  private removeTerenMask() {
    if (this.terenMaskLayer) {
      this.terenMaskLayer.remove();
      this.terenMaskLayer = null;
    }
  }

  private clearPreviewLayers() {
    if (this.previewPolygon) { this.previewPolygon.remove(); this.previewPolygon = null; }
    if (this.previewPolyline) { this.previewPolyline.remove(); this.previewPolyline = null; }
    this.previewMarkers.forEach(m => m.remove());
    this.previewMarkers = [];
  }

  // --- Load & render parcels ---
  loadParcele() {
    this.parcelaService.getParcele(this.terenId, 0, 1000).subscribe(response => {
      this.parcele = response.content;
      this.renderParcele();
    });
  }

  renderParcele() {
    if (!this.mapInitialized) return;
    this.parcelaPolygons.forEach(p => p.remove());
    this.parcelaPolygons = [];
    this.parcelaLayers = [];

    this.parcele.forEach(p => {
      if (!p.polygon) return;
      const geom = this.parseGeoJson(p.polygon);
      const paths = this.extractPaths(geom);
      if (!paths.length) return;

      const polygon = L.polygon(paths as any, {
        color: '#dc2626', weight: 2, opacity: 1,
        fillColor: '#fca5a5', fillOpacity: 0.5
      }).addTo(this.map);

      const content = `<b>${p.denumire}</b><br>${p.suprafata} ha<br>${p.categorieFolosinta}`;
      polygon.bindTooltip(content, { sticky: true });
      polygon.on('click', () => {
        this.zone.run(() => this.viewParcelaOnMap(p));
      });

      this.parcelaPolygons.push(polygon);
      this.parcelaLayers.push({ parcela: p, polygon });
    });
  }

  /** Pans/zooms the map to the given parcel and briefly highlights its outline. */
  viewParcelaOnMap(p: Parcela) {
    this.viewingParcela = p;
    this.isAddingParcela = false;

    const layer = this.parcelaLayers.find(l => l.parcela.id === p.id);
    if (!layer || !this.map) return;

    const bounds = layer.polygon.getBounds();
    if (bounds.isValid()) this.map.fitBounds(bounds, { maxZoom: 18 });

    layer.polygon.setStyle({ color: '#2563eb', weight: 4 });
    setTimeout(() => layer.polygon.setStyle({ color: '#dc2626', weight: 2 }), 1500);
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

    this.previewMarkers.forEach(m => m.remove());
    this.previewMarkers = [];

    const latlngs: L.LatLngTuple[] = valid.map((p, idx) => {
      const [lat, lng] = this.conv.stereo70ToWgs84(p.x, p.y);
      const marker = L.circleMarker([lat, lng], {
        radius: 6, fillColor: '#3b82f6', fillOpacity: 1, color: '#1e40af', weight: 2
      }).addTo(this.map);
      marker.bindTooltip(`Punct ${idx + 1}<br>X: ${p.x}<br>Y: ${p.y}`);
      this.previewMarkers.push(marker as any);
      return [lat, lng];
    });

    if (this.previewPolygon) { this.previewPolygon.remove(); this.previewPolygon = null; }
    if (this.previewPolyline) { this.previewPolyline.remove(); this.previewPolyline = null; }

    if (latlngs.length === 0) return;

    if (latlngs.length === 1) {
      this.map.setView(latlngs[0], Math.max(this.map.getZoom() ?? 0, 15));
    } else if (latlngs.length === 2) {
      this.previewPolyline = L.polyline(latlngs, { color: '#3b82f6', weight: 2, opacity: 0.8 }).addTo(this.map);
      const bounds = L.latLngBounds(latlngs);
      this.map.fitBounds(bounds, { padding: [40, 40] });
    } else {
      this.previewPolygon = L.polygon(latlngs, {
        color: '#16a34a', weight: 2.5, fillColor: '#86efac', fillOpacity: 0.45
      }).addTo(this.map);

      const bounds = L.latLngBounds(latlngs);
      if (bounds.isValid()) this.map.fitBounds(bounds, { padding: [25, 25] });
    }
  }

  openEditParcelaForm(p: Parcela) {
    this.viewingParcela = null;
    this.newParcela = { ...p };
    this.points = [{ x: '', y: '' }, { x: '', y: '' }, { x: '', y: '' }];
    if (p.stereo70Coordinates) {
      const lines = p.stereo70Coordinates.split('\n');
      for (let i = 0; i < lines.length; i++) {
        const parts = lines[i].split(' ');
        if (parts.length === 2) {
          if (i < this.points.length) {
            this.points[i] = { x: parts[0], y: parts[1] };
          } else {
            this.points.push({ x: parts[0], y: parts[1] });
          }
        }
      }
    }
    this.calculatedArea = null;
    this.isAddingParcela = true;
    this.applyTerenMask();
    this.updatePreview();

    if (this.terenOutlineLayer) {
      const bounds = this.terenOutlineLayer.getBounds();
      if (bounds.isValid()) this.map.fitBounds(bounds);
    }
  }

  openAddParcelaForm() {
    this.viewingParcela = null;
    this.newParcela = {
      denumire: '',
      suprafata: 0,
      categorieFolosinta: '',
      polygon: null,
      stereo70Coordinates: '',
      tipMediu: 'CAMP_DESCHIS',
      suprafataUtilaMp: 0
    };
    this.points = [{ x: '', y: '' }, { x: '', y: '' }, { x: '', y: '' }];
    this.calculatedArea = null;
    this.isAddingParcela = true;
    this.applyTerenMask();

    // Zoom to teren
    if (this.terenOutlineLayer) {
      const bounds = this.terenOutlineLayer.getBounds();
      if (bounds.isValid()) this.map.fitBounds(bounds);
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
    if (this.newParcela.id) {
      this.parcelaService.updateParcela(this.newParcela.id, this.newParcela).subscribe({
        next: (saved) => {
          this.saving = false;
          const idx = this.parcele.findIndex(p => p.id === saved.id);
          if (idx !== -1) {
            this.parcele[idx] = saved;
          }
          this.renderParcele();
          this.isAddingParcela = false;
          this.clearPreviewLayers();
          this.removeTerenMask();
          this.calculatedArea = null;
        },
        error: (err) => { this.saving = false; console.error(err); alert('Eroare la editare parcelă.'); }
      });
    } else {
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
  }

  loadCategorii() {
    this.categorieService.getCategoriiForTeren(this.terenId, 0, 1000).subscribe({
      next: (response) => {
        this.categorii = response.content;
      },
      error: () => this.categorii = []
    });
  }

  openAddCategorieForm() {
    this.isAddingCategorie = true;
    this.editingCategorie = null;
    this.newCategorie = { denumire: '', descriere: '' };
  }

  openEditCategorie(categorie: CategorieFolosinta) {
    this.isAddingCategorie = true;
    this.editingCategorie = categorie;
    this.newCategorie = { ...categorie };
  }

  cancelAddCategorie() {
    this.isAddingCategorie = false;
    this.editingCategorie = null;
    this.newCategorie = { denumire: '', descriere: '' };
  }

  saveCategorie() {
    if (!this.newCategorie.denumire?.trim()) {
      alert('Introduceți denumirea categoriei.');
      return;
    }

    this.saving = true;
    if (this.editingCategorie && this.editingCategorie.id) {
      this.categorieService.updateCategorie(this.editingCategorie.id, this.newCategorie).subscribe({
        next: (updated) => {
          this.saving = false;
          const idx = this.categorii.findIndex(c => c.id === updated.id);
          if (idx >= 0) this.categorii[idx] = updated;
          this.cancelAddCategorie();
        },
        error: (err) => { this.saving = false; console.error(err); alert('Eroare la actualizare categorie.'); }
      });
    } else {
      this.categorieService.createCategorie(this.terenId, this.newCategorie).subscribe({
        next: (saved) => {
          this.saving = false;
          this.categorii.push(saved);
          this.cancelAddCategorie();
        },
        error: (err) => { this.saving = false; console.error(err); alert('Eroare la salvare categorie.'); }
      });
    }
  }

  deleteCategorie(categorie: CategorieFolosinta) {
    if (!categorie.id) return;
    if (!confirm(`Ștergeți categoria "${categorie.denumire}"?`)) return;
    this.categorieService.deleteCategorie(categorie.id).subscribe({
      next: () => {
        this.categorii = this.categorii.filter(c => c.id !== categorie.id);
      },
      error: () => alert('Eroare la ștergere categorie.')
    });
  }

  selectParcela(p: Parcela) {
    this.viewingParcela = p;
    this.isAddingParcela = false;
    this.isAddingCultura = false;
    this.editingCultura = null;
    this.culturi = [];
    this.pomi = [];
    this.vitaDeVie = [];
    this.pasuniFanete = [];
    this.paduri = [];
    if (p.id) {
      if (this.showCulturi(p)) this.loadCulturi(p.id);
      this.loadSurse(p.id);
      if (this.showPomi(p)) this.loadPomi(p.id);
      if (this.showVita(p)) this.loadVita(p.id);
      if (this.showPasuneFaneata(p)) this.loadPasuneFaneata(p.id);
      if (this.showPadure(p)) this.loadPaduri(p.id);
      if (p.tipMediu !== 'CAMP_DESCHIS') {
        this.loadCicluri(p.id);
        this.loadRecoltari(p.id);
      }
    }
  }

  closeParcela() {
    this.viewingParcela = null;
    this.isAddingCultura = false;
    this.editingCultura = null;
    this.surse = [];
    this.isAddingSursa = false;
    this.editingSursa = null;
    this.pomi = [];
    this.isAddingPom = false;
    this.editingPom = null;
    this.vitaDeVie = [];
    this.isAddingVita = false;
    this.editingVita = null;
    this.pasuniFanete = [];
    this.isAddingPasune = false;
    this.editingPasune = null;
    this.paduri = [];
    this.isAddingPadure = false;
    this.editingPadure = null;
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

  loadCulturi(parcelaId: number) {
    this.culturaService.getCulturi(parcelaId, 0, 1000).subscribe({
      next: (response) => {
        this.culturi = response.content || [];
      },
      error: () => this.culturi = []
    });
  }

  openAddCulturaForm() {
    this.isAddingCultura = true;
    this.editingCultura = null;
    this.newCultura = {
      anAgricol: new Date().getFullYear(),
      specieCultura: '',
      suprafataCultivataHa: this.viewingParcela?.suprafata || 0
    };
  }

  openEditCultura(cultura: CulturaParcela) {
    this.isAddingCultura = true;
    this.editingCultura = cultura;
    this.newCultura = { ...cultura, tipSol: cultura.tipSol || '' };
  }

  cancelAddCultura() {
    this.isAddingCultura = false;
    this.editingCultura = null;
    this.newCultura = {};
  }

  saveCultura() {
    if (!this.newCultura.specieCultura?.trim() || !this.newCultura.anAgricol || !this.newCultura.suprafataCultivataHa) {
      alert('Completați anul agricol, specia și suprafața.');
      return;
    }
    if (!this.viewingParcela?.id) return;

    this.saving = true;
    if (this.editingCultura && this.editingCultura.id) {
      this.culturaService.updateCultura(this.viewingParcela.id, this.editingCultura.id, this.newCultura as CulturaParcela).subscribe({
        next: updated => {
          this.saving = false;
          const idx = this.culturi.findIndex(c => c.id === updated.id);
          if (idx >= 0) this.culturi[idx] = updated;
          this.cancelAddCultura();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare cultura.'); }
      });
    } else {
      this.culturaService.createCultura(this.viewingParcela.id, this.newCultura as CulturaParcela).subscribe({
        next: saved => {
          this.saving = false;
          this.culturi.push(saved);
          this.cancelAddCultura();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare cultura.'); }
      });
    }
  }

  deleteCultura(cultura: CulturaParcela) {
    if (!cultura.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți cultura "${cultura.specieCultura}"?`)) return;
    this.culturaService.deleteCultura(this.viewingParcela.id, cultura.id).subscribe({
      next: () => {
        this.culturi = this.culturi.filter(c => c.id !== cultura.id);
      },
      error: () => alert('Eroare la ștergere cultura.')
    });
  }

  loadSurse(parcelaId: number) {
    this.sursaApaService.getSurse(parcelaId, 0, 1000).subscribe({
      next: (response) => {
        this.surse = response.content || [];
      },
      error: () => this.surse = []
    });
  }

  openAddSursaForm() {
    this.isAddingSursa = true;
    this.editingSursa = null;
    this.newSursa = { tipSursa: '', stareFunctionare: true };
  }

  openEditSursa(sursa: SursaApa) {
    this.isAddingSursa = true;
    this.editingSursa = sursa;
    this.newSursa = { ...sursa };
  }

  cancelAddSursa() {
    this.isAddingSursa = false;
    this.editingSursa = null;
    this.newSursa = { stareFunctionare: true };
  }

  saveSursa() {
    if (!this.viewingParcela?.id) return;

    this.saving = true;
    const payload = this.newSursa as SursaApa;

    if (this.editingSursa && this.editingSursa.id) {
      this.sursaApaService.updateSursa(this.viewingParcela.id, this.editingSursa.id, payload).subscribe({
        next: updated => {
          this.saving = false;
          const idx = this.surse.findIndex(s => s.id === updated.id);
          if (idx >= 0) this.surse[idx] = updated;
          this.cancelAddSursa();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare sursă apă.'); }
      });
    } else {
      this.sursaApaService.createSursa(this.viewingParcela.id, payload).subscribe({
        next: saved => {
          this.saving = false;
          this.surse.push(saved);
          this.cancelAddSursa();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare sursă apă.'); }
      });
    }
  }

  deleteSursa(sursa: SursaApa) {
    if (!sursa.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți sursa de apă "${sursa.tipSursa || 'fără tip'}"?`)) return;
    this.sursaApaService.deleteSursa(this.viewingParcela.id, sursa.id).subscribe({
      next: () => {
        this.surse = this.surse.filter(s => s.id !== sursa.id);
      },
      error: () => alert('Eroare la ștergere sursă apă.')
    });
  }

  goBack() {
    this.router.navigate(['/gospodarii', this.gospodarieId]);
  }

  showCulturi(p: Parcela | null): boolean {
    if (!p || !p.categorieFolosinta) return false;
    const val = this.normalizeString(p.categorieFolosinta);
    return val === 'arabil';
  }

  showPomi(p: Parcela | null): boolean {
    if (!p || !p.categorieFolosinta) return false;
    const val = this.normalizeString(p.categorieFolosinta);
    return val === 'livada';
  }

  showVita(p: Parcela | null): boolean {
    if (!p || !p.categorieFolosinta) return false;
    const val = this.normalizeString(p.categorieFolosinta);
    return val === 'vii';
  }

  showPasuneFaneata(p: Parcela | null): boolean {
    if (!p || !p.categorieFolosinta) return false;
    const val = this.normalizeString(p.categorieFolosinta);
    return val === 'pasune' || val === 'fanete';
  }

  showPadure(p: Parcela | null): boolean {
    if (!p || !p.categorieFolosinta) return false;
    const val = this.normalizeString(p.categorieFolosinta);
    return val === 'padure' || val === 'paduri' || val === 'vegetatie forestiera';
  }

  loadPomi(parcelaId: number) {
    this.pomiService.getPomi(parcelaId, 0, 1000).subscribe({
      next: (response) => { this.pomi = response.content || []; },
      error: () => this.pomi = []
    });
  }

  openAddPomForm() {
    this.isAddingPom = true;
    this.editingPom = null;
    this.newPom = { tipInregistrare: TipInregistrarePom.IZOLAT, specie: '' };
  }

  openEditPom(pom: Pom) {
    this.isAddingPom = true;
    this.editingPom = pom;
    this.newPom = { ...pom };
  }

  cancelAddPom() {
    this.isAddingPom = false;
    this.editingPom = null;
    this.newPom = { tipInregistrare: TipInregistrarePom.IZOLAT };
  }

  savePom() {
    if (!this.newPom.specie?.trim() || !this.newPom.tipInregistrare) {
      alert('Completați specia și tipul de înregistrare.');
      return;
    }
    if (!this.viewingParcela?.id) return;

    this.saving = true;
    if (this.editingPom && this.editingPom.id) {
      this.pomiService.updatePom(this.viewingParcela.id, this.editingPom.id, this.newPom as Pom).subscribe({
        next: updated => {
          this.saving = false;
          const idx = this.pomi.findIndex(x => x.id === updated.id);
          if (idx >= 0) this.pomi[idx] = updated;
          this.cancelAddPom();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare pom.'); }
      });
    } else {
      this.pomiService.createPom(this.viewingParcela.id, this.newPom as Pom).subscribe({
        next: saved => {
          this.saving = false;
          this.pomi.push(saved);
          this.cancelAddPom();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare pom.'); }
      });
    }
  }

  deletePom(pom: Pom) {
    if (!pom.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți înregistrarea "${pom.specie}"?`)) return;
    this.pomiService.deletePom(this.viewingParcela.id, pom.id).subscribe({
      next: () => { this.pomi = this.pomi.filter(x => x.id !== pom.id); },
      error: () => alert('Eroare la ștergere pom.')
    });
  }

  loadVita(parcelaId: number) {
    this.vitaDeVieService.getVitaDeVie(parcelaId, 0, 1000).subscribe({
      next: (response) => { this.vitaDeVie = response.content || []; },
      error: () => this.vitaDeVie = []
    });
  }

  openAddVitaForm() {
    this.isAddingVita = true;
    this.editingVita = null;
    this.newVita = { tipInregistrare: TipInregistrareVita.IZOLAT, specie: 'Viță de vie' };
  }

  openEditVita(vita: VitaDeVie) {
    this.isAddingVita = true;
    this.editingVita = vita;
    this.newVita = { ...vita };
  }

  cancelAddVita() {
    this.isAddingVita = false;
    this.editingVita = null;
    this.newVita = { tipInregistrare: TipInregistrareVita.IZOLAT };
  }

  saveVita() {
    if (!this.newVita.specie?.trim() || !this.newVita.tipInregistrare) {
      alert('Completați specia și tipul de înregistrare.');
      return;
    }
    if (!this.viewingParcela?.id) return;

    this.saving = true;
    if (this.editingVita && this.editingVita.id) {
      this.vitaDeVieService.updateVita(this.viewingParcela.id, this.editingVita.id, this.newVita as VitaDeVie).subscribe({
        next: updated => {
          this.saving = false;
          const idx = this.vitaDeVie.findIndex(x => x.id === updated.id);
          if (idx >= 0) this.vitaDeVie[idx] = updated;
          this.cancelAddVita();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare viță de vie.'); }
      });
    } else {
      this.vitaDeVieService.createVita(this.viewingParcela.id, this.newVita as VitaDeVie).subscribe({
        next: saved => {
          this.saving = false;
          this.vitaDeVie.push(saved);
          this.cancelAddVita();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare viță de vie.'); }
      });
    }
  }

  deleteVita(vita: VitaDeVie) {
    if (!vita.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți înregistrarea "${vita.soi || vita.specie}"?`)) return;
    this.vitaDeVieService.deleteVita(this.viewingParcela.id, vita.id).subscribe({
      next: () => { this.vitaDeVie = this.vitaDeVie.filter(x => x.id !== vita.id); },
      error: () => alert('Eroare la ștergere viță de vie.')
    });
  }

  loadPasuneFaneata(parcelaId: number) {
    this.pasuneFaneataService.getPasuneFaneata(parcelaId, 0, 1000).subscribe({
      next: (response) => { this.pasuniFanete = response.content || []; },
      error: () => this.pasuniFanete = []
    });
  }

  openAddPasuneForm() {
    this.isAddingPasune = true;
    this.editingPasune = null;
    this.newPasune = { tipFolosinta: TipFolosintaPasune.PASUNAT };
  }

  openEditPasune(pasune: PasuneFaneata) {
    this.isAddingPasune = true;
    this.editingPasune = pasune;
    this.newPasune = { ...pasune };
  }

  cancelAddPasune() {
    this.isAddingPasune = false;
    this.editingPasune = null;
    this.newPasune = { tipFolosinta: TipFolosintaPasune.PASUNAT };
  }

  savePasune() {
    if (!this.newPasune.suprafataHa || !this.newPasune.tipFolosinta) {
      alert('Completați suprafața și tipul de folosință.');
      return;
    }
    if (!this.viewingParcela?.id) return;

    this.saving = true;
    if (this.editingPasune && this.editingPasune.id) {
      this.pasuneFaneataService.update(this.viewingParcela.id, this.editingPasune.id, this.newPasune as PasuneFaneata).subscribe({
        next: updated => {
          this.saving = false;
          const idx = this.pasuniFanete.findIndex(x => x.id === updated.id);
          if (idx >= 0) this.pasuniFanete[idx] = updated;
          this.cancelAddPasune();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare pășune/fânețe.'); }
      });
    } else {
      this.pasuneFaneataService.create(this.viewingParcela.id, this.newPasune as PasuneFaneata).subscribe({
        next: saved => {
          this.saving = false;
          this.pasuniFanete.push(saved);
          this.cancelAddPasune();
        },
        error: err => { this.saving = false; console.error(err); alert('Eroare la salvare pășune/fânețe.'); }
      });
    }
  }

  deletePasune(pasune: PasuneFaneata) {
    if (!pasune.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți înregistrarea de ${pasune.suprafataHa} ha?`)) return;
    this.pasuneFaneataService.delete(this.viewingParcela.id, pasune.id).subscribe({
      next: () => { this.pasuniFanete = this.pasuniFanete.filter(x => x.id !== pasune.id); },
      error: () => alert('Eroare la ștergere pășune/fânețe.')
    });
  }

  normalizeString(str: string): string {
    if (!str) return '';
    let res = str
      .trim()
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "");
    if (res === 'vie') res = 'vii';
    return res;
  }

  get speciiPomiFiltrate(): string[] {
    const cat = this.normalizeString(this.viewingParcela?.categorieFolosinta || '');
    if (!cat) return [];
    return this.speciiPomiToate
      .filter(s => this.normalizeString(s.categorieFolosinta || '') === cat)
      .map(s => s.nume);
  }

  // --- CICLURI DE PRODUCTIE METHODS ---
  loadCicluri(parcelaId: number) {
    this.cicluService.getCicluri(parcelaId, 0, 100).subscribe({
      next: (response) => { this.cicluri = response.content || []; },
      error: () => this.cicluri = []
    });
  }

  openAddCicluForm() {
    this.isAddingCiclu = true;
    this.editingCiclu = null;
    this.newCiclu = { 
      parcelaId: this.viewingParcela?.id,
      cultura: '', 
      dataInfiintare: new Date().toISOString().substring(0, 10), 
      status: 'ACTIV', 
      programSprijin: false 
    };
  }

  openEditCiclu(ciclu: CicluProductie) {
    this.isAddingCiclu = true;
    this.editingCiclu = ciclu;
    this.newCiclu = { ...ciclu };
  }

  cancelAddCiclu() {
    this.isAddingCiclu = false;
    this.editingCiclu = null;
    this.newCiclu = { status: 'ACTIV', programSprijin: false };
  }

  saveCiclu() {
    if (!this.newCiclu.cultura?.trim() || !this.newCiclu.dataInfiintare) {
      alert('Introduceți cultura și data înființării.');
      return;
    }

    this.saving = true;
    const payload = { ...this.newCiclu, parcelaId: this.viewingParcela?.id } as CicluProductie;

    const action = this.editingCiclu && this.editingCiclu.id
      ? this.cicluService.updateCiclu(this.editingCiclu.id, payload)
      : this.cicluService.createCiclu(payload);

    action.subscribe({
      next: (saved) => {
        this.saving = false;
        if (saved.warning) {
          alert(saved.warning);
        }
        this.cancelAddCiclu();
        if (this.viewingParcela?.id) this.loadCicluri(this.viewingParcela.id);
      },
      error: (err) => {
        this.saving = false;
        console.error(err);
        const msg = err.error?.message || err.error || 'Eroare la salvare ciclu.';
        alert(msg);
      }
    });
  }

  deleteCiclu(ciclu: CicluProductie) {
    if (!ciclu.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți ciclul de "${ciclu.cultura}"?`)) return;
    this.cicluService.deleteCiclu(ciclu.id).subscribe({
      next: () => { this.cicluri = this.cicluri.filter(c => c.id !== ciclu.id); },
      error: () => alert('Eroare la ștergere ciclu.')
    });
  }

  // --- RECOLTARI METHODS ---
  loadRecoltari(parcelaId: number) {
    this.recoltareService.getRecoltari(parcelaId, 0, 100).subscribe({
      next: (response) => { this.recoltari = response.content || []; },
      error: () => this.recoltari = []
    });
  }

  openAddRecoltareForm() {
    this.isAddingRecoltare = true;
    this.editingRecoltare = null;
    this.newRecoltare = {
      parcelaId: this.viewingParcela?.id,
      cultura: '',
      dataRecoltare: new Date().toISOString().substring(0, 10),
      cantitateKg: 0
    };
  }

  openEditRecoltare(rec: Recoltare) {
    this.isAddingRecoltare = true;
    this.editingRecoltare = rec;
    this.newRecoltare = { ...rec };
  }

  cancelAddRecoltare() {
    this.isAddingRecoltare = false;
    this.editingRecoltare = null;
    this.newRecoltare = { cantitateKg: 0 };
  }

  saveRecoltare() {
    if (!this.newRecoltare.dataRecoltare || !this.newRecoltare.cantitateKg) {
      alert('Introduceți data recoltării și cantitatea.');
      return;
    }

    this.saving = true;
    const payload = { ...this.newRecoltare, parcelaId: this.viewingParcela?.id } as Recoltare;

    const action = this.editingRecoltare && this.editingRecoltare.id
      ? this.recoltareService.updateRecoltare(this.editingRecoltare.id, payload)
      : this.recoltareService.createRecoltare(payload);

    action.subscribe({
      next: (saved) => {
        this.saving = false;
        this.cancelAddRecoltare();
        if (this.viewingParcela?.id) this.loadRecoltari(this.viewingParcela.id);
      },
      error: (err) => {
        this.saving = false;
        console.error(err);
        const msg = err.error?.message || err.error || 'Eroare la salvare recoltare.';
        alert(msg);
      }
    });
  }

  deleteRecoltare(rec: Recoltare) {
    if (!rec.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți recoltarea de ${rec.cantitateKg} kg?`)) return;
    this.recoltareService.deleteRecoltare(rec.id).subscribe({
      next: () => { this.recoltari = this.recoltari.filter(r => r.id !== rec.id); },
      error: () => alert('Eroare la ștergere recoltare.')
    });
  }

  // --- PADURI METHODS ---
  loadPaduri(parcelaId: number) {
    this.padureService.getPaduri(parcelaId, 0, 100).subscribe({
      next: (response) => { this.paduri = response.content || []; },
      error: () => this.paduri = []
    });
  }

  openAddPadureForm() {
    this.isAddingPadure = true;
    this.editingPadure = null;
    this.newPadure = {
      parcelaId: this.viewingParcela?.id,
      tipVegetatie: 'Pădure',
      suprafataHa: this.viewingParcela?.suprafata || 0
    };
  }

  openEditPadure(padure: Padure) {
    this.isAddingPadure = true;
    this.editingPadure = padure;
    this.newPadure = { ...padure };
  }

  cancelAddPadure() {
    this.isAddingPadure = false;
    this.editingPadure = null;
    this.newPadure = { tipVegetatie: 'Pădure' };
  }

  savePadure() {
    if (!this.newPadure.tipVegetatie || !this.newPadure.suprafataHa) {
      alert('Introduceți tipul de vegetație și suprafața.');
      return;
    }
    if (!this.viewingParcela?.id) return;

    this.saving = true;
    const payload = { ...this.newPadure, parcelaId: this.viewingParcela.id } as Padure;

    const action = this.editingPadure && this.editingPadure.id
      ? this.padureService.updatePadure(this.viewingParcela.id, this.editingPadure.id, payload)
      : this.padureService.createPadure(this.viewingParcela.id, payload);

    action.subscribe({
      next: (saved) => {
        this.saving = false;
        this.cancelAddPadure();
        if (this.viewingParcela?.id) this.loadPaduri(this.viewingParcela.id);
      },
      error: (err) => {
        this.saving = false;
        console.error(err);
        const msg = err.error?.message || err.error || 'Eroare la salvare evidență pădure.';
        alert(msg);
      }
    });
  }

  deletePadure(padure: Padure) {
    if (!padure.id || !this.viewingParcela?.id) return;
    if (!confirm(`Ștergeți evidența forestieră de ${padure.suprafataHa} ha?`)) return;
    this.padureService.deletePadure(this.viewingParcela.id, padure.id).subscribe({
      next: () => { this.paduri = this.paduri.filter(p => p.id !== padure.id); },
      error: () => alert('Eroare la ștergere evidență pădure.')
    });
  }
}
