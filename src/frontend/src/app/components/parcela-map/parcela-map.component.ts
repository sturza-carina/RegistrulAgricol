import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { ParcelaService } from '../../services/parcela.service';
import { Parcela } from '../../models/parcela.model';
import { TerenService } from '../../services/teren.service';
import { Teren } from '../../models/teren.model';
import { GospodarieService } from '../../services/gospodarie.service';
import { Gospodarie } from '../../models/gospodarie.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';

@Component({
  selector: 'app-parcela-map',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './parcela-map.component.html',
  styleUrls: ['./parcela-map.component.css']
})
export class ParcelaMapComponent implements OnInit, OnDestroy {
  map!: L.Map;

  showDialog = false;
  currentLayer: any = null;

  gospodarii: Gospodarie[] = [];
  teren: Teren | null = null;
  parcele: Parcela[] = [];

  selectedGospodarieId: number | null = null;
  selectedTerenId: number | null = null;

  newParcela: Parcela = { denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null, stereo70Coordinates: '' };

  parcelaPolygons: L.Polygon[] = [];
  maskLayer: L.Polygon | null = null;

  isAddingParcela = false;
  viewingParcela: Parcela | null = null;
  points: { x: string; y: string }[] = [
    { x: '', y: '' },
    { x: '', y: '' },
    { x: '', y: '' }
  ];

  constructor(
    private parcelaService: ParcelaService,
    private terenService: TerenService,
    private gospodarieService: GospodarieService,
    private route: ActivatedRoute,
    private http: HttpClient,
    private zone: NgZone
  ) {}

  ngOnInit() {
    this.loadGospodarii();

    this.route.queryParams.subscribe(params => {
      if (params['gospodarieId']) {
        this.selectedGospodarieId = +params['gospodarieId'];
        this.onGospodarieChange();
      }
      if (params['addParcela'] === 'true') {
         this.isAddingParcela = true;
      }
      setTimeout(() => {
        this.initMap();
        if (!this.selectedGospodarieId) {
          this.loadAllParcele();
        }
      }, 100);
    });
  }

  ngOnDestroy() {
    this.parcelaPolygons.forEach(p => p.remove());
    if (this.maskLayer) this.maskLayer.remove();
    if (this.map) this.map.remove();
  }

  loadGospodarii() {
    this.gospodarieService.getAllGospodarii(undefined, 0, 1000).subscribe(response => this.gospodarii = response.content);
  }

  onGospodarieChange() {
    this.selectedTerenId = null;
    this.teren = null;
    this.parcele = [];
    this.isAddingParcela = false;

    this.parcelaPolygons.forEach(p => p.remove());
    this.parcelaPolygons = [];

    if (this.maskLayer) {
      this.maskLayer.remove();
      this.maskLayer = null;
    }

    if (this.selectedGospodarieId) {
      this.gospodarieService.getGospodarieById(this.selectedGospodarieId).subscribe(g => {
        if (g && g.uat) {
          this.loadUatBoundary(g.uat.denumire, g.uat.judet);
        }
      });

      this.terenService.getTerenByGospodarieId(this.selectedGospodarieId, 0, 1000).subscribe(response => {
        const terenuri = response.content;
        const firstTeren = terenuri && terenuri.length > 0 ? terenuri[0] : null;
        this.teren = firstTeren;
        if (firstTeren && firstTeren.id) {
          this.selectedTerenId = firstTeren.id;
          this.loadParceleForTeren(firstTeren.id);
        }
      });
    } else {
      this.loadAllParcele();
    }
  }

  loadUatBoundary(uatName: string, county: string) {
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

  applyMask(geometry: any) {
    if (!this.map) return;
    if (this.maskLayer) {
      this.maskLayer.remove();
      this.maskLayer = null;
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

    this.maskLayer = L.polygon([worldBox, ...holes], {
      stroke: false,
      fillColor: '#000',
      fillOpacity: 0.6,
      interactive: false
    }).addTo(this.map);

    const bounds = L.latLngBounds([]);
    holes.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
    if (bounds.isValid()) {
      this.map.fitBounds(bounds);
    }
  }

  mapInitialized = false;

  private initMap() {
    const el = document.getElementById('map');
    if (!el) return;

    const romaniaBounds = L.latLngBounds(
      [43.6, 20.2], // SouthWest
      [48.3, 29.7]  // NorthEast
    );

    this.map = L.map('map', {
      maxBounds: romaniaBounds,
      maxBoundsViscosity: 1.0,
      minZoom: 6
    }).setView([45.9432, 24.9668], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    this.mapInitialized = true;
    this.renderParcele();
  }

  openAddParcelaForm() {
    this.viewingParcela = null;
    this.newParcela = { denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null, stereo70Coordinates: '' };
    this.points = [
      { x: '', y: '' },
      { x: '', y: '' },
      { x: '', y: '' }
    ];
    this.isAddingParcela = true;
  }

  calculateArea() {
    const validPoints = this.points
      .map(p => ({ x: parseFloat(p.x), y: parseFloat(p.y) }))
      .filter(p => !isNaN(p.x) && !isNaN(p.y));

    if (validPoints.length >= 3) {
      let area = 0;
      for (let i = 0; i < validPoints.length; i++) {
        let j = (i + 1) % validPoints.length;
        area += validPoints[i].x * validPoints[j].y - validPoints[j].x * validPoints[i].y;
      }
      area = Math.abs(area) / 2.0; 
      let hectares = area / 10000.0;
      this.newParcela.suprafata = parseFloat(hectares.toFixed(4));
    }
  }

  closeView() {
    this.viewingParcela = null;
  }

  addPoint() {
    this.points.push({ x: '', y: '' });
  }

  removePoint(index: number) {
    if (this.points.length > 1) {
       this.points.splice(index, 1);
    }
  }

  cancelAdd() {
    this.isAddingParcela = false;
  }

  loadAllParcele() {
    this.parcelaService.getAllParcele(0, 1000).subscribe(response => {
      this.parcele = response.content;
      this.renderParcele();
    });
  }

  loadParceleForTeren(terenId: number) {
    this.parcelaService.getParcele(terenId, 0, 1000).subscribe(response => {
      this.parcele = response.content;
      this.renderParcele();
    });
  }

  renderParcele() {
    if (!this.mapInitialized || !this.map) return;

    this.parcelaPolygons.forEach(p => p.remove());
    this.parcelaPolygons = [];

    const bounds = L.latLngBounds([]);

    this.parcele.forEach(p => {
      if (!p.polygon) return;

      const geom = p.polygon.geometry ?? p.polygon;
      let paths: L.LatLngTuple[][] = [];
      if (geom.type === 'Polygon') {
        paths = geom.coordinates.map((ring: any[]) => ring.map((c: number[]) => [c[1], c[0]] as L.LatLngTuple));
      } else if (geom.type === 'MultiPolygon') {
        geom.coordinates.forEach((poly: any[]) => {
          poly.forEach((ring: any[]) => paths.push(ring.map((c: number[]) => [c[1], c[0]] as L.LatLngTuple)));
        });
      }
      if (!paths.length) return;

      const polygon = L.polygon(paths as any, {
        color: '#e74c3c', weight: 3, opacity: 1,
        fillColor: '#f1c40f', fillOpacity: 0.6
      }).addTo(this.map);

      const info = `<b>${p.denumire}</b><br>Suprafață: ${p.suprafata} ha<br>Cat: ${p.categorieFolosinta}<br><i>${p.gospodarieName || ''}</i><br><small>Click pentru detalii</small>`;
      polygon.bindTooltip(info, { sticky: true });

      polygon.on('click', () => {
        this.zone.run(() => {
          this.viewingParcela = p;
          this.isAddingParcela = false;
        });
      });

      this.parcelaPolygons.push(polygon);
      paths.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
    });

    if (bounds.isValid()) {
      this.map.fitBounds(bounds);
    }
  }

  saveCombined() {
    if (!this.selectedTerenId) return;

    this.newParcela.stereo70Coordinates = this.points
       .filter(p => p.x.trim() !== '' && p.y.trim() !== '')
       .map(p => `${p.x.trim()} ${p.y.trim()}`)
       .join('\n');

    this.parcelaService.createParcela(this.selectedTerenId, this.newParcela).subscribe({
      next: (saved) => {
        this.parcele.push(saved);
        this.renderParcele();
        this.isAddingParcela = false;
      },
      error: (err) => {
        console.error('Save error:', err);
        alert('Eroare la salvare!');
      }
    });
  }

  deleteParcela() {
    if (this.viewingParcela && this.viewingParcela.id) {
      if (confirm('Sunteți sigur că doriți să ștergeți această parcelă?')) {
        this.parcelaService.deleteParcela(this.viewingParcela.id).subscribe({
          next: () => {
            this.viewingParcela = null;
            if (this.selectedTerenId) {
              this.loadParceleForTeren(this.selectedTerenId);
            } else {
              this.loadAllParcele();
            }
          },
          error: (err) => {
            console.error('Delete error:', err);
            alert('Eroare la ștergere!');
          }
        });
      }
    }
  }
}
