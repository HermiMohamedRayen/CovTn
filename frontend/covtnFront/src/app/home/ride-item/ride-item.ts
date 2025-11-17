import { Component, Input, OnInit, signal } from '@angular/core';
import { MapService } from '../../map-service';
import { NavigationExtras, Router } from '@angular/router';

@Component({
  selector: 'app-ride-item',
  standalone: false,
  templateUrl: './ride-item.html',
  styleUrl: './ride-item.css'
})
export class RideItem implements OnInit{
  @Input() ride: any;

  ride_item = signal({} as any);

  loading = true

  constructor(private mapService: MapService, private router: Router) {
    
   }

  async ngOnInit() {
    this.ride_item.set(this.ride);
      const adress : any = await this.mapService.reverseGeocode(this.ride.departure.latitude, this.ride.departure.longitude).toPromise();
 
        this.ride_item.update((ride) => {
          return {...ride, from: adress.address.suburb || adress.address.county || adress.address.city_district, fromAddress: adress.display_name};
        });
      
      const adress2 : any = await this.mapService.reverseGeocode(this.ride.destination.latitude, this.ride.destination.longitude).toPromise();
 
        this.ride_item.update((ride) => {
          console.log(adress2);
          return {...ride, to: adress2.address.suburb || adress2.address.county || adress2.address.city_district, toAddress: adress2.display_name};
        });
      this.loading = false;
  }
  viewRide(): void {
    const navigationExtras : NavigationExtras = {
      state: {
        ride: this.ride_item()
      }
    };
    this.router.navigate(['/ride-detail/' + this.ride_item().id], navigationExtras);
  }


}
