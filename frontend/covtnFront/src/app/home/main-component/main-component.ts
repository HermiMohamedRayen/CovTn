import { Component } from '@angular/core';
import { ApiService } from '../../api-service';
import { NavigationExtras, Router } from '@angular/router';

@Component({
  selector: 'app-main-component',
  standalone: false,
  templateUrl: './main-component.html',
  styleUrl: './main-component.css'
})
export class MainComponent {
  protected user = ApiService.user;

  searchQuery: string = '';

  latestRides: any;;

  constructor(
    protected apiService: ApiService,
    private router: Router
  ) {
    this.apiService.getLatestRides().subscribe({
      next: (rides) => {
        this.latestRides = rides;
      },
      error: (error) => {
        console.error('Error fetching latest rides:', error);
      }
    });
   }

  onSearch(): void {
    // Minimal handler for the search form. Wire to ApiService as needed.
    console.log('Searching for:', this.searchQuery);
    // Example: this.apiService.searchRides(this.searchQuery).subscribe(...)
  }

  viewRide(ride: any): void {
    const extra : NavigationExtras = {state: { ride: ride } };
    this.router.navigate(['/ride-detail'], extra);
  }
  proposeRide(): void {
    if(!this.apiService.hasRole('ROLE_DRIVER')){
      this.apiService.becomeDriver().then(() => {
        this.router.navigate(['/car']);
      }).catch((error) => {
        if(error.status === 400){
          alert("please verify that you have added you phone number and profile image in your profile settings");
        }
      });
    }else{
      if(!this.user().car){
        this.router.navigate(['/car']);
      }else{
        this.router.navigate(['/propose-ride']);
      }
      
    }
  }
    
}
