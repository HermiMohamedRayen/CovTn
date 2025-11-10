import { Component, signal,OnInit, ViewChild } from '@angular/core';
import { ApiService } from './api-service';
import { GuardsCheckEnd, GuardsCheckStart, NavigationCancel, Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App {
  public static loading = signal(false);
  public loading = App.loading;


  constructor(private apiService: ApiService,private router: Router) {
    
    this.router.events.subscribe(event => {
      if (event instanceof GuardsCheckStart) {
        App.loading.set(true);
      }     
      if (event instanceof GuardsCheckEnd || event instanceof NavigationCancel) {
        App.loading.set(false);
      } 
    });
    this.apiService.refreshToken();

  }

    
  

}