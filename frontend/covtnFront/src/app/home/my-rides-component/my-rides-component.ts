import { Component, signal } from '@angular/core';
import { ApiService } from '../../api-service';
import { MapService } from '../../map-service';
import { NavigationExtras, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ParticipantsDialog } from './participants-dialog/participants-dialog';

@Component({
  selector: 'app-my-rides-component',
  standalone: false,
  templateUrl: './my-rides-component.html',
  styleUrl: './my-rides-component.css'
})
export class MyRidesComponent {

  protected rides = signal<Array<any>>([]);

  constructor(protected apiService: ApiService, private router: Router, private dialog: MatDialog) {
    this.apiService.getMyRides().subscribe((rides) => {
      console.log(rides);
      this.rides.set(rides);
    });
   }

  viewRide(ride: any): void {
    const navigationExtras : NavigationExtras = {
      state: {
        ride: ride
      }
    };
    this.router.navigate(['/ride-detail/' + ride.id], navigationExtras);
  }

  openParticipantsDialog(ride: any, event: Event): void {
    event.stopPropagation(); // Prevent triggering viewRide or other parent clicks
    this.dialog.open(ParticipantsDialog, {
      width: '400px',
      data: { participants: ride.rideParticipations || [] } // Assuming 'passengers' is the field name
    });
  }

  remove(ride: any) {
    if(!confirm("Are you sure you want to remove this ride?")) {
      return;
    }
    this.apiService.removeRide(ride.id).subscribe({
      next: () => {
        this.apiService.getMyRides().subscribe((rides) => {
          this.rides.set(rides);
        });
        alert('Ride removed successfully.');
      }
    });
  }
}