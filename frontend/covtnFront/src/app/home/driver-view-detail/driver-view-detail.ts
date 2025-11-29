import { Location } from '@angular/common';
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
  constructor(private router: Router, private location: Location) {
    const st = this.router.currentNavigation()?.extras?.state?.['driver'];
    if (st) {
      this.driver.set(st);
    }else {
      this.location.back();
    }
    
  }
  getavgrating(): number | null {
    const ratings: number[] = this.driver().ratings || [];
    if (ratings.length === 0) {
      return null;
    }
    const sum = ratings.reduce((acc:any, rating:any) => acc + rating.rating, 0);
    return Math.round((sum / ratings.length) * 10) / 10;
  } 

}
