import { Component, signal } from '@angular/core';
import { ApiService } from '../../api-service';
import { HttpStatusCode } from '@angular/common/http';

@Component({
  selector: 'app-my-participation-component',
  standalone: false,
  templateUrl: './my-participation-component.html',
  styleUrl: './my-participation-component.css'
})
export class MyParticipationComponent {

  protected isLoaded: boolean = false;

  protected participations = signal<Array<any>>([]);
  constructor(private apiService: ApiService) {
    this.apiService.getMyParticipatedRides().subscribe(
      {next: (participations) => {
        this.participations.set(participations);
        this.isLoaded = true;
      }}
    );
       
  }

  removepart(participation: any) {
    this.apiService.unparticipateFromRide(participation.id).subscribe({
      next: () => {
        const updatedParticipations = this.participations().filter(p => p.id !== participation.id);
        this.participations.set(updatedParticipations);
        alert('You have successfully unparticipated from the ride.');
      },
      error: (error) => {
        console.error('Error unparticipating from ride:', error);
        switch (error.status) {
          case 404:
            alert('Participation not found.');
            break;
          case 400:
            alert('Bad request. Please try again later.');
            break;
          case HttpStatusCode.Gone:
            alert('Connot unparticipate from a ride that has already departed.');
            break;
          default:
            alert('An error occurred while trying to unparticipate from the ride. Please try again later.');
        }
      }
    });
  }
}
