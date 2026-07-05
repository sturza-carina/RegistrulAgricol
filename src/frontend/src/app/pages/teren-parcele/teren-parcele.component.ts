import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { TerenService } from '../../services/teren.service';
import { ParcelaService } from '../../services/parcela.service';
import { CoordConversionService } from '../../services/coord-conversion.service';
import { CategorieFolosintaService } from '../../services/categorie-folosinta.service';
import { GoogleMapsLoaderService } from '../../services/google-maps-loader.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { Teren } from '../../models/teren.model';
import { Parcela } from '../../models/parcela.model';
import { CategorieFolosinta } from '../../models/categorie-folosinta.model';
import { CulturaParcela } from '../../models/cultura-parcela.model';
import { CulturaParcelaService } from '../../services/cultura-parcela.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { SursaApa } from '../../models/sursa-apa.model';
import { SursaApaService } from '../../services/sursa-apa.service';
import { LookupService } from '../../services/lookup.service';
import { Pom, TipInregistrarePom } from '../../models/pom.model';
import { PomService } from '../../services/pom.service';
import {SpecieRef} from '../../models/specie-ref.model';

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

  map!: google.maps.Map;
  mapInitialized = false;
  infoWindow!: google.maps.InfoWindow;

  // Map layers
  terenOutlineLayer: google.maps.Polygon | null = null;
  terenMaskLayer: google.maps.Polygon | null = null;
  judetOutlineLayer: google.maps.Polygon | null = null;
  parcelaPolygons: google.maps.Polygon[] = [];
  parcelaLayers: { parcela: Parcela; polygon: google.maps.Polygon }[] = [];
  previewMarkers: google.maps.Marker[] = [];
  previewPolygon: google.maps.Polygon | null = null;
  previewPolyline: google.maps.Polyline | null = null;

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
    private googleMapsLoader: GoogleMapsLoaderService,
    private gospodarieService: GospodarieService,
    private lookupService: LookupService,
    private pomiService: PomService,
    private http: HttpClient,
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
      this.updateBreadcrumbs();
      console.log('LOADED TEREN:', t);
      setTimeout(() => {
        this.initMap();
        this.loadParcele();
        this.loadCategorii();
      }, 150);
    });

    if (this.gospodarieId) {
      this.gospodarieService.getGospodarieById(this.gospodarieId).subscribe(g => {
        if (g?.uat?.judet) this.loadJudetBoundary(g.uat.judet);
      });
    }

    this.lookupService.getCategoriiFolosinta().subscribe(v => this.categoriiFolosinta = v);
    this.lookupService.getTipuriSol().subscribe(v => this.tipuriSol = v);
    this.lookupService.getTipuriSursaApa().subscribe(v => this.tipuriSursa = v);
    this.lookupService.getSpeciiPomi().subscribe(v => this.speciiPomiToate = v);
  }

  private pendingJudetPaths: google.maps.LatLngLiteral[][] | null = null;

  /** Fetches the outline of the county (județ) the gospodărie belongs to. */
  private loadJudetBoundary(judet: string) {
    const url = `https://nominatim.openstreetmap.org/search?county=${encodeURIComponent(judet)}&country=Romania&format=geojson&polygon_geojson=1&email=admin@registru.ro`;
    this.http.get(url).subscribe((res: any) => {
      const feature = res?.features?.find((f: any) => f.geometry && (f.geometry.type === 'Polygon' || f.geometry.type === 'MultiPolygon'));
      if (!feature) return;

      const paths = this.extractPaths(feature.geometry);
      if (!paths.length) return;

      if (this.mapInitialized) {
        this.drawJudetOutline(paths);
      } else {
        this.pendingJudetPaths = paths;
      }
    });
  }

  /** Draws the county outline on the map (no fill — just a contextual boundary line). */
  private drawJudetOutline(paths: google.maps.LatLngLiteral[][]) {
    if (this.judetOutlineLayer) this.judetOutlineLayer.setMap(null);
    this.judetOutlineLayer = new google.maps.Polygon({
      paths,
      strokeColor: '#d97706', strokeWeight: 2, strokeOpacity: 0.8,
      fillOpacity: 0,
      clickable: false,
      zIndex: 0,
      map: this.map
    });
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' },
      { label: 'Detalii Gospodărie', link: `/gospodarii/${this.gospodarieId}?tab=TERENURI` },
      { label: `Teren: ${this.teren?.denumire || ''}` }
    ];
  }

  ngOnDestroy() {
    this.parcelaPolygons.forEach(p => p.setMap(null));
    this.previewMarkers.forEach(m => m.setMap(null));
    if (this.terenOutlineLayer) this.terenOutlineLayer.setMap(null);
    if (this.terenMaskLayer) this.terenMaskLayer.setMap(null);
    if (this.judetOutlineLayer) this.judetOutlineLayer.setMap(null);
    if (this.previewPolygon) this.previewPolygon.setMap(null);
    if (this.previewPolyline) this.previewPolyline.setMap(null);
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
  private extractPaths(geom: any): google.maps.LatLngLiteral[][] {
    if (!geom) return [];
    if (geom.type === 'Polygon') {
      return geom.coordinates.map((ring: any[]) => ring.map((c: number[]) => ({ lat: c[1], lng: c[0] })));
    }
    if (geom.type === 'MultiPolygon') {
      const paths: google.maps.LatLngLiteral[][] = [];
      geom.coordinates.forEach((poly: any[]) => {
        poly.forEach((ring: any[]) => paths.push(ring.map((c: number[]) => ({ lat: c[1], lng: c[0] }))));
      });
      return paths;
    }
    return [];
  }

  private async initMap() {
    const el = document.getElementById('teren-parcele-map');
    if (!el || this.mapInitialized) return;

    await this.googleMapsLoader.load();

    this.map = new google.maps.Map(el, {
      center: { lat: 45.9432, lng: 24.9668 },
      zoom: 7,
      mapTypeId: google.maps.MapTypeId.HYBRID,
      streetViewControl: false,
      fullscreenControl: true
    });
    this.infoWindow = new google.maps.InfoWindow();
    this.mapInitialized = true;

    if (this.pendingJudetPaths) {
      this.drawJudetOutline(this.pendingJudetPaths);
      this.pendingJudetPaths = null;
    }

    // Draw teren boundary
    if (this.teren?.polygon) {
      const geom = this.parseGeoJson(this.teren.polygon);
      const paths = this.extractPaths(geom);
      if (paths.length) {
        this.terenOutlineLayer = new google.maps.Polygon({
          paths,
          strokeColor: '#1e40af', strokeWeight: 4, strokeOpacity: 0.9,
          fillColor: '#bfdbfe', fillOpacity: 0.15,
          zIndex: 2,
          map: this.map
        });

        const bounds = new google.maps.LatLngBounds();
        paths.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
        this.map.fitBounds(bounds, 25);
      }

      const ro = new ResizeObserver(() => {
        if (!this.map) return;
        google.maps.event.trigger(this.map, 'resize');
      });
      ro.observe(el);
    }

    this.renderParcele();
  }

  private applyTerenMask() {
    if (!this.map || !this.teren?.polygon) return;
    if (this.terenMaskLayer) { this.terenMaskLayer.setMap(null); this.terenMaskLayer = null; }

    const geom = this.parseGeoJson(this.teren.polygon);
    const holes = this.extractPaths(geom);
    if (!holes.length) return;

    const worldBox: google.maps.LatLngLiteral[] = [
      { lat: -85, lng: -180 }, { lat: 85, lng: -180 }, { lat: 85, lng: 180 }, { lat: -85, lng: 180 }
    ];

    this.terenMaskLayer = new google.maps.Polygon({
      paths: [worldBox, ...holes],
      strokeWeight: 0,
      fillColor: '#1e293b',
      fillOpacity: 0.55,
      clickable: false,
      zIndex: 1,
      map: this.map
    });
  }

  private removeTerenMask() {
    if (this.terenMaskLayer) {
      this.terenMaskLayer.setMap(null);
      this.terenMaskLayer = null;
    }
  }

  private clearPreviewLayers() {
    if (this.previewPolygon) { this.previewPolygon.setMap(null); this.previewPolygon = null; }
    if (this.previewPolyline) { this.previewPolyline.setMap(null); this.previewPolyline = null; }
    this.previewMarkers.forEach(m => m.setMap(null));
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
    this.parcelaPolygons.forEach(p => p.setMap(null));
    this.parcelaPolygons = [];
    this.parcelaLayers = [];

    this.parcele.forEach(p => {
      if (!p.polygon) return;
      const geom = this.parseGeoJson(p.polygon);
      const paths = this.extractPaths(geom);
      if (!paths.length) return;

      const polygon = new google.maps.Polygon({
        paths,
        strokeColor: '#dc2626', strokeWeight: 2, strokeOpacity: 1,
        fillColor: '#fca5a5', fillOpacity: 0.5,
        zIndex: 3,
        map: this.map
      });

      const content = `<b>${p.denumire}</b><br>${p.suprafata} ha<br>${p.categorieFolosinta}`;
      polygon.addListener('mouseover', (e: google.maps.PolyMouseEvent) => {
        if (!e.latLng) return;
        this.infoWindow.setContent(content);
        this.infoWindow.setPosition(e.latLng);
        this.infoWindow.open(this.map);
      });
      polygon.addListener('mouseout', () => this.infoWindow.close());
      polygon.addListener('click', () => {
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

    const bounds = new google.maps.LatLngBounds();
    layer.polygon.getPaths().forEach(path => path.forEach(latLng => bounds.extend(latLng)));
    if (!bounds.isEmpty()) this.map.fitBounds(bounds, 80);

    layer.polygon.setOptions({ strokeColor: '#2563eb', strokeWeight: 4, zIndex: 6 });
    setTimeout(() => layer.polygon.setOptions({ strokeColor: '#dc2626', strokeWeight: 2, zIndex: 3 }), 1500);
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
    this.previewMarkers.forEach(m => m.setMap(null));
    this.previewMarkers = [];

    const latlngs: google.maps.LatLngLiteral[] = valid.map((p, idx) => {
      const [lat, lng] = this.conv.stereo70ToWgs84(p.x, p.y);
      const marker = new google.maps.Marker({
        position: { lat, lng },
        map: this.map,
        icon: {
          path: google.maps.SymbolPath.CIRCLE,
          scale: 6,
          fillColor: '#3b82f6', fillOpacity: 1,
          strokeColor: '#1e40af', strokeWeight: 2
        },
        title: `Punct ${idx + 1}\nX: ${p.x}\nY: ${p.y}`,
        zIndex: 5
      });
      this.previewMarkers.push(marker);
      return { lat, lng };
    });

    // 2. Remove old preview shapes
    if (this.previewPolygon) { this.previewPolygon.setMap(null); this.previewPolygon = null; }
    if (this.previewPolyline) { this.previewPolyline.setMap(null); this.previewPolyline = null; }

    if (latlngs.length === 0) return;

    if (latlngs.length === 1) {
      this.map.setCenter(latlngs[0]);
      this.map.setZoom(Math.max(this.map.getZoom() ?? 0, 15));
    } else if (latlngs.length === 2) {
      this.previewPolyline = new google.maps.Polyline({
        path: latlngs, strokeColor: '#3b82f6', strokeWeight: 2, strokeOpacity: 0.8, map: this.map
      });
      const bounds = new google.maps.LatLngBounds();
      latlngs.forEach(pt => bounds.extend(pt));
      this.map.fitBounds(bounds, 40);
    } else {
      this.previewPolygon = new google.maps.Polygon({
        paths: latlngs,
        strokeColor: '#16a34a', strokeWeight: 2.5,
        fillColor: '#86efac', fillOpacity: 0.45,
        zIndex: 4,
        map: this.map
      });

      const bounds = new google.maps.LatLngBounds();
      latlngs.forEach(pt => bounds.extend(pt));
      if (!bounds.isEmpty()) this.map.fitBounds(bounds, 25);
    }
  }

  openAddParcelaForm() {
    this.viewingParcela = null;
    this.newParcela = { denumire: '', suprafata: 0, categorieFolosinta: '', polygon: null, stereo70Coordinates: '' };
    this.points = [{ x: '', y: '' }, { x: '', y: '' }, { x: '', y: '' }];
    this.calculatedArea = null;
    this.isAddingParcela = true;
    this.applyTerenMask();

    // Zoom to teren
    if (this.terenOutlineLayer) {
      const bounds = new google.maps.LatLngBounds();
      this.terenOutlineLayer.getPaths().forEach(path => path.forEach(latLng => bounds.extend(latLng)));
      if (!bounds.isEmpty()) this.map.fitBounds(bounds, 20);
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
    if (p.id) {
      if (this.showCulturi(p)) this.loadCulturi(p.id);
      this.loadSurse(p.id);
      if (this.showPomi(p)) this.loadPomi(p.id);
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
    const val = p.categorieFolosinta.trim().toLowerCase();
    return val === 'arabil' || val === 'pășune' || val === 'pasune' || val === 'fânețe' || val === 'fanețe' || val === 'fanete';
  }

  showPomi(p: Parcela | null): boolean {
    if (!p || !p.categorieFolosinta) return false;
    const val = p.categorieFolosinta.trim().toLowerCase();
    return val === 'livadă' || val === 'livada' || val === 'vii' || val === 'vie' || val === 'pădure' || val === 'padure';
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
}
