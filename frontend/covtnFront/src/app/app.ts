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
  protected loading = true;

  constructor(private apiService: ApiService,private router: Router) {
    
    this.router.events.subscribe(event => {
      if (event instanceof GuardsCheckStart) {
        this.loading = true;
      }     
      if (event instanceof GuardsCheckEnd || event instanceof NavigationCancel) {
        this.loading = false;
      } 
    });
    this.apiService.refreshToken();

  }

    
  

}