import { Component, effect, ElementRef, OnInit, signal, ViewChild, viewChild } from '@angular/core';
import * as L from 'leaflet';
import { MapService } from '../../map-service';
import { ApiService } from '../../api-service';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-search-component',
  standalone: false,
  templateUrl: './search-component.html',
  styleUrl: './search-component.css'
})
export class SearchComponent implements OnInit {

  mode: 'departure' | 'destination' = 'departure';

  map: L.Map = null!!;

  rides = signal<any[]>([]);

  minDepartureTime: string = new Date().toISOString().slice(0,16);

  @ViewChild('searchResults') searchResults!: ElementRef<HTMLDivElement>;

  departureMarker: L.Marker = null!!;
  destinationMarker: L.Marker = null!!;
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


  form: FormGroup;
  ride = {
    departureTime: '',
    arrivalTime: '',
  };



  constructor(private fb: FormBuilder, private mapService: MapService, private apiService: ApiService, private router: Router) {
    this.form = this.fb.group({

      departureTime: ['', Validators.required],
      arrivalTime: ['', Validators.required],

    });
    this.form.get('arrivalTime')?.setValidators([
      Validators.required,
      control => {
        const departureTime = this.form.get('departureTime')?.value;
        const arrivalTime = control.value;
        if (departureTime && arrivalTime && new Date(arrivalTime) <= new Date(departureTime)) {
          return { arrivalBeforeDeparture: true };
        }
        return null;
      }
    ]);
    this.form.get('arrivalTime')?.updateValueAndValidity();
    this.form.get('arrivalTime')?.disable();
    this.form.get('departureTime')?.valueChanges.subscribe(value => {
      if (this.form.get('departureTime')?.valid) {
        this.form.get('arrivalTime')?.enable();
      } else {
        this.form.get('arrivalTime')?.disable();
        this.form.get('arrivalTime')?.setValue('');
      }
      this.form.get('arrivalTime')?.updateValueAndValidity();
    });

    effect(() => {
      if (this.rides().length > 0) {
        setTimeout(() => {
          this.searchResults.nativeElement.scrollIntoView({ behavior: 'smooth' });
        }, 10);
      }
    });

  }




  async ngOnInit() {
    let cnt ;
    await this.mapService.getUserLocation().then((location) => {
      cnt = location;
    },(error) => {
      console.error('Error getting user location:', error);
    });

    if(!cnt){
      cnt = {latitude: 36.8065, longitude: 10.1815 , accuracy: 0  };
    }
    this.map = L.map('map').setView([cnt.latitude, cnt.longitude], 10);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      if (this.mode === 'departure') {
        if (!this.departureMarker) {
          this.departureMarker = L.marker(e.latlng, { icon: this.red }).addTo(this.map);
        } else {
          this.departureMarker.setLatLng(e.latlng);
        }
      } else {
        if (!this.destinationMarker) {
          this.destinationMarker = L.marker(e.latlng, { icon: this.blue }).addTo(this.map);
        } else {
          this.destinationMarker.setLatLng(e.latlng);
        }
      }
    });

  }

  setMode(value: 'departure' | 'destination') {
    this.mode = value;
  }

  onSearch() {
    if (this.departureMarker && this.destinationMarker && this.form.valid) {
      const deplat = this.departureMarker.getLatLng().lat;
      const delong = this.departureMarker.getLatLng().lng;
      const destlat = this.destinationMarker.getLatLng().lat;
      const destlong = this.destinationMarker.getLatLng().lng;
      this.apiService.searchRides(deplat, delong, destlat, destlong, this.form.value.departureTime, this.form.value.arrivalTime).subscribe((rides) => {
        this.rides.set(rides);
      }, (error) => {
        console.error('Error searching rides:', error);
      });
    } else {
      if(!this.form.valid){
        this.form.markAllAsTouched();
        alert('Please provide valid departure and arrival times.');
        return;
      }
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
