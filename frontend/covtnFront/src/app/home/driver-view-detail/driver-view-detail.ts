import { Component, signal, Signal } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-driver-view-detail',
  standalone: false,
  templateUrl: './driver-view-detail.html',
  styleUrl: './driver-view-detail.css'
})
export class DriverViewDetail {
  driver = signal({} as any);
  constructor(private router: Router) {
    this.driver.set(this.router.currentNavigation()?.extras?.state?.['driver']);
    console.log(this.driver());
  }

}
