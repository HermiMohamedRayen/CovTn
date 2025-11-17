import { Component, input, Input, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../api-service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-view-car-component',
  standalone: false,
  templateUrl: './view-car-component.html',
  styleUrl: './view-car-component.css'
})
export class ViewCarComponent implements OnInit{


  @Input({required:false}) inputcar!: any;
  car = signal({} as any);
  extra: any;
  constructor(private router: Router, private apiService: ApiService,private location: Location) {
          this.extra = this.router.currentNavigation()?.extras?.state?.['car'];
    
  }
  ngOnInit(): void {
    if(this.inputcar){
      this.car.set(this.inputcar);
    } else {
      this.car.set(this.extra);
    }
    if(!this.car()){
      this.location.back();

    }
    console.log(this.car());
    Array.from(this.car().photos).map((photo: any) => {
      this.apiService.getCarPhotoByName(photo,this.car().user).then((url) => {
        this.car.update((car) => {
          const photosUrl = car.photosUrl || [];
          photosUrl.push(url);
          return {...car, photosUrl: photosUrl};
        });
      }).catch((error) => {
        console.error("Error fetching car photo URL:", error);
      });
    });
  }
}
