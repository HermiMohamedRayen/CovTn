import { Component } from '@angular/core';
import { ApiService } from '../../api-service';
import { MapService } from '../../map-service';
import { NavigationExtras, Router } from '@angular/router';

@Component({
  selector: 'app-my-rides-component',
  standalone: false,
  templateUrl: './my-rides-component.html',
  styleUrl: './my-rides-component.css'
})
export class MyRidesComponent {

  rides = ApiService.user().rides

  constructor(protected apiService: ApiService, private router: Router) {
   }

  viewRide(ride: any): void {
    const navigationExtras : NavigationExtras = {
      state: {
        ride: ride
      }
    };
    this.router.navigate(['/ride-detail/' + ride.id], navigationExtras);
  }

}
