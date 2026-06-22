import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { ParcelaService } from '../../services/parcela.service';
import { Parcela } from '../../models/parcela.model';
import { TerenService } from '../../services/teren.service';
import { Teren } from '../../models/teren.model';
import { GospodarieService } from '../../services/gospodarie.service';
import { Gospodarie } from '../../models/gospodarie.model';
import { GoogleMapsLoaderService } from '../../services/google-maps-loader.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-parcela-map',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './parcela-map.component.html',
  styleUrls: ['./parcela-map.component.css']
})
export class ParcelaMapComponent implements OnInit, OnDestroy {
  map!: google.maps.Map;
  infoWindow!: google.maps.InfoWindow;

  showDialog = false;
  currentLayer: any = null;


  gospodarii: Gospodarie[] = [];
  teren: Teren | null = null;
  parcele: Parcela[] = [];

  selectedGospodarieId: number | null = null;
  selectedTerenId: number | null = null;


  newParcela: Parcela = { denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null, stereo70Coordinates: '' };

  parcelaPolygons: google.maps.Polygon[] = [];
  maskLayer: google.maps.Polygon | null = null;

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
    private googleMapsLoader: GoogleMapsLoaderService,
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
    this.parcelaPolygons.forEach(p => p.setMap(null));
    if (this.maskLayer) this.maskLayer.setMap(null);
  }

  loadGospodarii() {
    this.gospodarieService.getAllGospodarii().subscribe(data => this.gospodarii = data);
  }

  onGospodarieChange() {
    this.selectedTerenId = null;
    this.teren = null;
    this.parcele = [];
    this.isAddingParcela = false;

    this.parcelaPolygons.forEach(p => p.setMap(null));
    this.parcelaPolygons = [];

    if (this.maskLayer) {
      this.maskLayer.setMap(null);
      this.maskLayer = null;
    }

    if (this.selectedGospodarieId) {
      // Fetch Gospodarie to get UAT details for the mask
      this.gospodarieService.getGospodarieById(this.selectedGospodarieId).subscribe(g => {
        if (g && g.uat) {
          this.loadUatBoundary(g.uat.denumire, g.uat.judet);
        }
      });

      this.terenService.getTerenByGospodarieId(this.selectedGospodarieId).subscribe(data => {
        // Use first teren if available (map shows one at a time)
        const firstTeren = data && data.length > 0 ? data[0] : null;
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
    // Basic formatting: "Cluj-Napoca", "Cluj"
    const url = `https://nominatim.openstreetmap.org/search?city=${encodeURIComponent(uatName)}&county=${encodeURIComponent(county)}&country=Romania&format=geojson&polygon_geojson=1&email=admin@registru.ro`;
    this.http.get(url).subscribe((res: any) => {
      if (res && res.features && res.features.length > 0) {
        // Find the first polygon feature
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
      this.maskLayer.setMap(null);
      this.maskLayer = null;
    }

    // World bounding box coordinates for the inverted polygon
    const worldBox: google.maps.LatLngLiteral[] = [
      { lat: -85, lng: -180 }, { lat: 85, lng: -180 }, { lat: 85, lng: 180 }, { lat: -85, lng: 180 }
    ];

    let holes: google.maps.LatLngLiteral[][] = [];
    if (geometry.type === 'Polygon') {
        holes.push(geometry.coordinates[0].map((coord: any) => ({ lat: coord[1], lng: coord[0] })));
    } else if (geometry.type === 'MultiPolygon') {
        geometry.coordinates.forEach((poly: any) => {
            holes.push(poly[0].map((coord: any) => ({ lat: coord[1], lng: coord[0] })));
        });
    }

    this.maskLayer = new google.maps.Polygon({
      paths: [worldBox, ...holes],
      strokeWeight: 0,
      fillColor: '#000',
      fillOpacity: 0.6,
      clickable: false,
      zIndex: 1,
      map: this.map
    });

    // Fit map to UAT bounds
    const bounds = new google.maps.LatLngBounds();
    holes.forEach(ring => ring.forEach(pt => bounds.extend(pt)));
    if (!bounds.isEmpty()) this.map.fitBounds(bounds);
  }



  mapInitialized = false;

  private async initMap() {
    const el = document.getElementById('map');
    if (!el) return;

    await this.googleMapsLoader.load();

    this.map = new google.maps.Map(el, {
      center: { lat: 45.9432, lng: 24.9668 },
      zoom: 6,
      mapTypeId: google.maps.MapTypeId.HYBRID,
      streetViewControl: false,
      fullscreenControl: true
    });
    this.infoWindow = new google.maps.InfoWindow();
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
      area = Math.abs(area) / 2.0; // Area in square meters
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
    this.parcelaService.getAllParcele().subscribe(data => {
      this.parcele = data;
      this.renderParcele();
    });
  }

  loadParceleForTeren(terenId: number) {
    this.parcelaService.getParcele(terenId).subscribe(data => {
      this.parcele = data;
      this.renderParcele();
    });
  }

  renderParcele() {
    if (!this.mapInitialized || !this.map) return;

    this.parcelaPolygons.forEach(p => p.setMap(null));
    this.parcelaPolygons = [];

    const bounds = new google.maps.LatLngBounds();
    let hasBounds = false;

    this.parcele.forEach(p => {
      if (!p.polygon) return;

      const geom = p.polygon.geometry ?? p.polygon;
      let paths: google.maps.LatLngLiteral[][] = [];
      if (geom.type === 'Polygon') {
        paths = geom.coordinates.map((ring: any[]) => ring.map((c: number[]) => ({ lat: c[1], lng: c[0] })));
      } else if (geom.type === 'MultiPolygon') {
        geom.coordinates.forEach((poly: any[]) => {
          poly.forEach((ring: any[]) => paths.push(ring.map((c: number[]) => ({ lat: c[1], lng: c[0] }))));
        });
      }
      if (!paths.length) return;

      const polygon = new google.maps.Polygon({
        paths,
        strokeColor: '#e74c3c', strokeWeight: 3, strokeOpacity: 1,
        fillColor: '#f1c40f', fillOpacity: 0.6,
        zIndex: 2,
        map: this.map
      });

      const info = `<b>${p.denumire}</b><br>Suprafață: ${p.suprafata} ha<br>Cat: ${p.categorieFolosinta}<br><i>${p.gospodarieName || ''}</i><br><small>Click pentru detalii</small>`;
      polygon.addListener('mouseover', (e: google.maps.PolyMouseEvent) => {
        if (!e.latLng) return;
        this.infoWindow.setContent(info);
        this.infoWindow.setPosition(e.latLng);
        this.infoWindow.open(this.map);
      });
      polygon.addListener('mouseout', () => this.infoWindow.close());
      polygon.addListener('click', () => {
        this.zone.run(() => {
          this.viewingParcela = p;
          this.isAddingParcela = false;
        });
      });

      this.parcelaPolygons.push(polygon);
      paths.forEach(ring => ring.forEach(pt => { bounds.extend(pt); hasBounds = true; }));
    });

    if (hasBounds) {
      this.map.fitBounds(bounds);
    }
  }

  saveCombined() {
    if (!this.selectedTerenId) return;

    // Construct stereo70Coordinates string from points
    this.newParcela.stereo70Coordinates = this.points
       .filter(p => p.x.trim() !== '' && p.y.trim() !== '')
       .map(p => `${p.x.trim()} ${p.y.trim()}`)
       .join('\n');

    this.parcelaService.createParcela(this.selectedTerenId, this.newParcela).subscribe({
      next: (saved) => {
        console.log('Saved parcela:', saved);
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
