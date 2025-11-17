import { Component, OnInit, signal } from '@angular/core';
import * as L from 'leaflet';
import { MapService } from '../../map-service';
import { ApiService } from '../../api-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-search-component',
  standalone: false,
  templateUrl: './search-component.html',
  styleUrl: './search-component.css'
})
export class SearchComponent implements OnInit {

  mode: 'departure' | 'destination' = 'departure';

  map : L.Map = null!! ;

  rides = signal<any[]>([]);

  departureMarker : L.Marker = null!! ;
  destinationMarker : L.Marker = null!! ;
  private red = L.divIcon({
        className: 'custom-div-icon',
        html: "<i class='bi bi-geo-alt-fill' style='color:red;font-size: 40px; '></i>",
        iconSize: [30, 42],
  
      });
private blue = L.divIcon({
        className: 'custom-div-icon',
        html: "<i class='bi bi-geo-alt-fill' style='color:blue;font-size: 40px; '></i>",
        iconSize: [30, 42],
  
      });

  constructor(
    private mapService: MapService,
    private apiService: ApiService,
    private router: Router
  ) { 

  }

  async ngOnInit() {
    const centr = await this.mapService.getUserLocation();
    this.map = L.map('map').setView([centr.latitude, centr.longitude], 10);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      if(this.mode === 'departure'){
        if(!this.departureMarker){
          this.departureMarker = L.marker(e.latlng, {icon: this.red}).addTo(this.map);
        }else{
          this.departureMarker.setLatLng(e.latlng);
        }
      }else{
        if(!this.destinationMarker){
          this.destinationMarker = L.marker(e.latlng, {icon: this.blue}).addTo(this.map);
        }else{
          this.destinationMarker.setLatLng(e.latlng);
        }
      }
    });

  }

  setMode(value: 'departure' | 'destination') {
    this.mode = value;
  }

  onSearch() {
    if(this.departureMarker && this.destinationMarker){
      const deplat = this.departureMarker.getLatLng().lat;
      const delong = this.departureMarker.getLatLng().lng;
      const destlat = this.destinationMarker.getLatLng().lat;
      const destlong = this.destinationMarker.getLatLng().lng;
      this.apiService.searchRides(deplat, delong, destlat, destlong).subscribe((rides) => {
        console.log('Searching rides with coordinates:', deplat, delong, destlat, destlong);
        console.log('Rides found:', rides);
        this.rides.set(rides);
      }, (error) => {
        console.error('Error searching rides:', error);
      });
    } else {
      alert('Please select both departure and destination points on the map.');
    }
  }

  viewRide(ride: any): void {
    const navigationExtras = {
      state: {
        ride: ride
      }
    };
    this.router.navigate(['/ride-detail/' + ride.id], navigationExtras);
  }
}
