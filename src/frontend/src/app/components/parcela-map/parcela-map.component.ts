import { Component, OnInit, OnDestroy } from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-draw';
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

@Component({
  selector: 'app-parcela-map',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './parcela-map.component.html',
  styleUrls: ['./parcela-map.component.css']
})
export class ParcelaMapComponent implements OnInit, OnDestroy {
  map!: L.Map;
  drawnItems!: L.FeatureGroup;
  drawControl!: L.Control.Draw;
  isDrawControlAdded = false;
  
  showDialog = false;
  currentLayer: any = null;


  gospodarii: Gospodarie[] = [];
  teren: Teren | null = null;
  parcele: Parcela[] = [];
  
  selectedGospodarieId: number | null = null;
  selectedTerenId: number | null = null;


  newParcela: Parcela = { denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null };

  layerToParcelaId = new Map<number, number>();
  maskLayer: L.Polygon | null = null;

  constructor(
    private parcelaService: ParcelaService,
    private terenService: TerenService,
    private gospodarieService: GospodarieService,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadGospodarii();

    this.route.queryParams.subscribe(params => {
      if (params['gospodarieId']) {
        this.selectedGospodarieId = +params['gospodarieId'];
        this.onGospodarieChange();
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
    if (this.map) this.map.remove();
  }

  loadGospodarii() {
    this.gospodarieService.getAllGospodarii().subscribe(data => this.gospodarii = data);
  }

  onGospodarieChange() {
    this.selectedTerenId = null;
    this.teren = null;
    this.parcele = [];
    this.updateDrawControl();
    if (this.drawnItems) this.drawnItems.clearLayers();
    if (this.maskLayer && this.map) {
      this.map.removeLayer(this.maskLayer);
      this.maskLayer = null;
      this.map.setMaxBounds(null as any); // remove bounds restriction
    }

    if (this.selectedGospodarieId) {
      // Fetch Gospodarie to get UAT details for the mask
      this.gospodarieService.getGospodarieById(this.selectedGospodarieId).subscribe(g => {
        if (g && g.uat) {
          this.loadUatBoundary(g.uat.denumire, g.uat.judet);
        }
      });

      this.terenService.getTerenByGospodarieId(this.selectedGospodarieId).subscribe(data => {
        this.teren = data;
        if (data && data.id) {
          this.selectedTerenId = data.id;
          this.updateDrawControl();
          this.loadParceleForTeren(this.selectedTerenId);
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
      this.map.removeLayer(this.maskLayer);
    }

    // World bounding box coordinates for the inverted polygon
    const worldBox = [
      [-90, -180], [90, -180], [90, 180], [-90, 180], [-90, -180]
    ];

    let holes: any[] = [];
    if (geometry.type === 'Polygon') {
        holes.push(geometry.coordinates[0].map((coord: any) => [coord[1], coord[0]]));
    } else if (geometry.type === 'MultiPolygon') {
        geometry.coordinates.forEach((poly: any) => {
            holes.push(poly[0].map((coord: any) => [coord[1], coord[0]]));
        });
    }

    const maskedPolygonCoordinates = [worldBox, ...holes];

    this.maskLayer = L.polygon(maskedPolygonCoordinates as any, {
        color: '#000',
        fillColor: '#000',
        fillOpacity: 0.6,
        weight: 1,
        stroke: false
    }).addTo(this.map);

    // Fit map to UAT bounds and restrict panning
    const innerBounds = L.polygon(holes as any).getBounds();
    this.map.fitBounds(innerBounds);
    this.map.setMaxBounds(innerBounds.pad(0.5));
  }



  private initMap() {
    this.map = L.map('map').setView([45.9432, 24.9668], 6);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    this.drawnItems = new L.FeatureGroup();
    this.map.addLayer(this.drawnItems);

    this.setupDrawControl();

    this.map.on('draw:created', (e: any) => {
      const layer = e.layer;
      this.currentLayer = layer;
      this.newParcela.polygon = layer.toGeoJSON().geometry;
      this.showDialog = true;
    });

    this.map.on('draw:edited', (e: any) => {
      const layers = e.layers;
      layers.eachLayer((layer: any) => {
        const pId = this.layerToParcelaId.get(layer._leaflet_id);
        if (pId) {
           const p = this.parcele.find(x => x.id === pId);
           if (p) {
               p.polygon = layer.toGeoJSON().geometry;
               this.parcelaService.updateParcela(p.id!, p).subscribe();
           }
        }
      });
    });

    this.map.on('draw:deleted', (e: any) => {
      const layers = e.layers;
      layers.eachLayer((layer: any) => {
        const pId = this.layerToParcelaId.get(layer._leaflet_id);
        if (pId) {
           this.parcelaService.deleteParcela(pId).subscribe(() => {
              this.parcele = this.parcele.filter(x => x.id !== pId);
              this.layerToParcelaId.delete(layer._leaflet_id);
           });
        }
      });
    });
  }

  private setupDrawControl() {
    this.drawControl = new L.Control.Draw({
      edit: { featureGroup: this.drawnItems, remove: true },
      draw: { polygon: {}, polyline: false, rectangle: false, circle: false, circlemarker: false, marker: false }
    });
    this.updateDrawControl();
  }

  private updateDrawControl() {
    if (!this.map || !this.drawControl) return;
    
    if (this.selectedTerenId) {
      if (!this.isDrawControlAdded) {
        this.map.addControl(this.drawControl);
        this.isDrawControlAdded = true;
      }
    } else {
      if (this.isDrawControlAdded) {
        this.map.removeControl(this.drawControl);
        this.isDrawControlAdded = false;
      }
    }
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
    if (this.drawnItems) this.drawnItems.clearLayers();
    this.layerToParcelaId.clear();
    this.parcele.forEach(p => {
      if (p.polygon) {
        const layer = L.geoJSON(p.polygon as any).getLayers()[0] as any;
        const info = `<b>${p.denumire}</b><br>Suprafață: ${p.suprafata} ha<br>Cat: ${p.categorieFolosinta}<br><i>${p.gospodarieName || ''}</i>`;
        layer.bindPopup(info);
        layer.bindTooltip(info);
        this.drawnItems.addLayer(layer);
        this.layerToParcelaId.set(layer._leaflet_id, p.id!);
      }
    });
    if (this.drawnItems.getLayers().length > 0) {
      this.map.fitBounds(this.drawnItems.getBounds());
    }
  }

  saveCombined() {
    if (!this.currentLayer || !this.selectedTerenId) return;
    
    this.parcelaService.createParcela(this.selectedTerenId, this.newParcela).subscribe({
      next: (saved) => {
        this.parcele.push(saved);
        const info = `<b>${saved.denumire}</b><br>Suprafață: ${saved.suprafata} ha<br>Cat: ${saved.categorieFolosinta}<br><i>${saved.gospodarieName || ''}</i>`;
        this.currentLayer.bindPopup(info);
        this.currentLayer.bindTooltip(info);
        this.drawnItems.addLayer(this.currentLayer);
        this.layerToParcelaId.set(this.currentLayer._leaflet_id, saved.id!);
        this.closeDialog();
      },
      error: (err) => alert('Eroare la salvare!')
    });
  }

  closeDialog() {
    this.showDialog = false;
    this.currentLayer = null;
    this.newParcela = { denumire: '', suprafata: 0, categorieFolosinta: 'Arabil', polygon: null };
  }
  
  cancelDraw() {
    if (this.currentLayer) this.map.removeLayer(this.currentLayer);
    this.closeDialog();
  }
}
