import { Component, Input, OnInit, signal } from '@angular/core';
import { MapService } from '../../map-service';
import { NavigationExtras, Router } from '@angular/router';
import { firstValueFrom, forkJoin } from 'rxjs';

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

    const depReq = this.mapService.reverseGeocode(this.ride.departure.latitude, this.ride.departure.longitude);
    const destReq = this.mapService.reverseGeocode(this.ride.destination.latitude, this.ride.destination.longitude);

    try {
      const [depAddress, destAddress]: any[] = await firstValueFrom(forkJoin([depReq, destReq]));

      this.ride_item.update((ride) => ({
        ...ride,
        from: depAddress.address.suburb || depAddress.address.county || depAddress.address.city_district,
        fromAddress: depAddress.display_name,
        to: destAddress.address.suburb || destAddress.address.county || destAddress.address.city_district,
        toAddress: destAddress.display_name
      }));
    } catch (err) {
      console.error('Error during reverse geocoding:', err);
    } finally {
      this.loading = false;
    }
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
