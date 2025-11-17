import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MapService } from '../../map-service';
import * as L from 'leaflet';
import { ApiService } from '../../api-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-propose-ride',
  standalone: false,
  templateUrl: './propose-ride.html',
  styleUrl: './propose-ride.css'
})
export class ProposeRide implements OnInit {

  mapdep : L.Map = null!! ;
  mapdest : L.Map = null!! ;

  departureMarker: L.Marker = null!!;
  destinationMarker: L.Marker = null!!;


  form: FormGroup;
  ride = {
    departure : { latitude: 0, longitude: 0 },
    destination : { latitude: 0, longitude: 0 },
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

  }


  async ngOnInit() {
    const cnt = await this.mapService.getUserLocation()

    this.mapdep =L.map("mapdep").setView([cnt.latitude, cnt.longitude], 10); 
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(this.mapdep);
    this.mapdest =L.map("mapdest").setView([cnt.latitude, cnt.longitude], 10); 
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(this.mapdest);


  this.mapdep.addEventListener('click', (e: any) => {
    if(!this.departureMarker){
      this.departureMarker = L.marker(e.latlng).addTo(this.mapdep);
    }else{
      this.ride.departure.longitude = e.latlng.lng;
      this.ride.departure.latitude = e.latlng.lat;
      this.departureMarker.setLatLng([e.latlng.lat, e.latlng.lng]);
    }
      
    });
  this.mapdest.addEventListener('click', (e: any) => {
    if(!this.destinationMarker){
      this.destinationMarker = L.marker(e.latlng).addTo(this.mapdest);
    }else{
      this.ride.destination.longitude = e.latlng.lng;
      this.ride.destination.latitude = e.latlng.lat;
      this.destinationMarker.setLatLng([e.latlng.lat, e.latlng.lng]);
    }
    });



  }







  onSubmit( depmap: HTMLElement, destmap: HTMLElement) {
    destmap.classList.remove('border-red-500');
    depmap.classList.remove('border-red-500');
    if (this.form.valid && this.departureMarker && this.destinationMarker) {
      this.ride.departureTime = this.form.value.departureTime;
      this.ride.arrivalTime = this.form.value.arrivalTime;
      this.ride.departure.latitude = this.departureMarker.getLatLng().lat;
      this.ride.departure.longitude = this.departureMarker.getLatLng().lng;
      this.ride.destination.latitude = this.destinationMarker.getLatLng().lat;
      this.ride.destination.longitude = this.destinationMarker.getLatLng().lng;
      this.apiService.proposeRide(this.ride).subscribe({
        next: (response) => {
          console.log('Ride proposed successfully', response);
          alert('Ride proposed successfully!');
          this.router.navigate(['/']);
        },
        error: (error) => {
          console.error('Error proposing ride', error);
        }
      });
      
    } else {
      destmap.classList.add(this.destinationMarker ? 'border-green-500' : 'border-red-500');
      depmap.classList.add(this.departureMarker ? 'border-green-500' : 'border-red-500');

      this.form.markAllAsTouched();
    }
    return;
  }






}
