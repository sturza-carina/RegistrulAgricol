import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GoogleMapsModule } from '@angular/google-maps';
import { GoogleMapsLoaderService } from '../../services/google-maps-loader.service';

@Component({
  selector: 'app-google-map',
  standalone: true,
  imports: [CommonModule, GoogleMapsModule],
  templateUrl: './google-map.component.html',
  styleUrls: ['./google-map.component.css']
})
export class GoogleMapComponent implements OnInit {
  center: google.maps.LatLngLiteral = { lat: 46.7712, lng: 23.6236 };
  zoom = 13;
  apiLoaded = false;

  constructor(private googleMapsLoader: GoogleMapsLoaderService) {}

  ngOnInit(): void {
    this.googleMapsLoader.load().then(() => {
      this.apiLoaded = true;
    });
  }
}
