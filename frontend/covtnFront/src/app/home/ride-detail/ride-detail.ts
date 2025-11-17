import { Component, effect, OnInit, signal } from '@angular/core';
import * as L from 'leaflet';
import { MapService } from '../../map-service';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { ApiService } from '../../api-service';

@Component({
  selector: 'app-ride-detail',
  standalone: false,
  templateUrl: './ride-detail.html',
  styleUrl: './ride-detail.css'
})
export class RideDetail implements OnInit{

  protected ride = signal({} as any);

  map : L.Map = null!! ;
  private isinit: boolean = false;
  protected depart = signal('');
  protected dest = signal('');
  effected = false;

  constructor(private mapService: MapService,
    private router : Router,private location: Location,
     private apiService: ApiService,
     private route: ActivatedRoute
    ) {
    const extra = this.router.currentNavigation()?.extras.state;
    if (extra && extra['ride']) {
      this.ride.set(extra['ride']);
    }else {
      const id = this.route.paramMap.subscribe(params => {
        let rideId: any = params.get('id');
        try {
          rideId = Number(rideId)
        }catch(e) {
        }
          this.apiService.getRideById(rideId).subscribe((rideData) => {
            this.ride.set(rideData);
            this.mapService.reverseGeocode(rideData.departure.latitude, rideData.departure.longitude).subscribe((address : any) => {
              this.ride.update((ride) => {
                
                return {...ride, from: address.name, fromAddress: address.display_name};
                
              });
            }); 
            this.mapService.reverseGeocode(rideData.destination.latitude, rideData.destination.longitude).subscribe((address : any) => {
              this.ride.update((ride) => {
                return {...ride, to: address.name, toAddress: address.display_name};
              });
            });
          
        }, (e) => {
          console.error("Error fetching ride data:", e);

        });
      });
    }
    effect(() => {
      if (this.isinit) {
        const latcenter = this.ride()?.departure?.latitude + (this.ride()?.destination?.latitude - this.ride()?.departure?.latitude) / 2;
        const loncenter = this.ride()?.departure?.longitude + (this.ride()?.destination?.longitude - this.ride()?.departure?.longitude) / 2;
        const distance = this.mapService.geoLocDistance(this.ride()?.departure?.latitude, this.ride()?.departure?.longitude, this.ride()?.destination?.latitude, this.ride()?.destination?.longitude)
        const zm = this.mapService.calculateZoomLevel(distance);
        this.map.setView([latcenter, loncenter], zm  ); 
        this.setmarker();
      }

    });
    effect(() => {
            if(this.ride()?.driver?.email && ! this.ride()?.driver?.photoUrl){    this.apiService.getImage(this.ride()?.driver?.email).then((imageData) => {
          this.ride.update((ride) => {
            this.effected = true;
            return {...ride, driver: {...ride.driver, photoUrl: imageData}};
          });
        }).catch((error) => {
          console.error("Error fetching profile image:", error);
        });}
  }
    );
  }
  
  
  ngOnInit(): void {
    this.map =L.map("map").setView([35.8065, 10.1815], 6); 
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(this.map);
    this.isinit = true;

  }


  setmarker() {
    const red = L.divIcon({
      className: 'custom-div-icon',
      html: "<i class='bi bi-geo-alt-fill' style='color:red;font-size: 40px; '></i>",
      iconSize: [30, 42],

    });
    const blue = L.divIcon({
      className: 'custom-div-icon',
      html: "<i class='bi bi-geo-alt-fill' style='color:blue;font-size: 40px; '></i>",
      iconSize: [30, 42],

    });

    const departureMarker = L.marker([this.ride()?.departure?.latitude, this.ride()?.departure?.longitude],{icon:red}).addTo(this.map)
      this.depart.set(this.ride()?.fromAddress);
      departureMarker.bindPopup(`Departure: ${this.ride()?.fromAddress}`).openPopup();
    const destinationMarker = L.marker([this.ride()?.destination?.latitude, this.ride()?.destination?.longitude],{icon:blue}).addTo(this.map)
      this.dest.set(this.ride()?.toAddress);
      destinationMarker.bindPopup(`Destination: ${this.ride()?.toAddress}`).openPopup();
  }
  
  viewDriverDetail() {
    const navigationExtras = {
      state: {
        driver: this.ride().driver
      }
    };
    this.router.navigate(['/driver-view-detail'], navigationExtras);
  }

}
