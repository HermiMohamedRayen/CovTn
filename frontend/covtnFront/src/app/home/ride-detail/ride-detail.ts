import { Component, effect, ElementRef, OnInit, signal, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import { MapService } from '../../map-service';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { ApiService } from '../../api-service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpStatusCode } from '@angular/common/http';

@Component({
  selector: 'app-ride-detail',
  standalone: false,
  templateUrl: './ride-detail.html',
  styleUrl: './ride-detail.css'
})
export class RideDetail implements OnInit{

  protected ride = signal({} as any);
  protected comments = signal<any[]>([]);
  commentForm: FormGroup;

  map : L.Map = null!! ;
  private isinit: boolean = false;
  protected depart = signal('');
  protected dest = signal('');
  effected = false;
  protected user = ApiService.user;

  @ViewChild('participateButton') participateButton!: ElementRef<HTMLButtonElement> ;

  constructor(private mapService: MapService,
    private router : Router,private location: Location,
     private apiService: ApiService,
     private route: ActivatedRoute,
     private fb: FormBuilder
    ) {
      
    this.commentForm = this.fb.group({
      comment: ['', [Validators.required, Validators.minLength(5)]],
      rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]]
    });
    const extra = this.router.currentNavigation()?.extras.state;
    
      const id = this.route.paramMap.subscribe(params => {
        let rideId: any = params.get('id');
        try {
          rideId = Number(rideId)
        }catch(e) {
        }
          this.apiService.getRideById(rideId).subscribe((rideData) => {
            console.log(rideData);
            this.ride.set(rideData);
            this.checkParticipation();
            this.mapService.reverseGeocode(rideData.departure.latitude, rideData.departure.longitude).subscribe((address : any) => {
              this.ride.update((ride) => {
                
                return {...ride, from: address.name, fromAddress: address.display_name};
                
              });
            }); 
            this.mapService.reverseGeocode(rideData.destination.latitude, rideData.destination.longitude).subscribe((address : any) => {
              this.ride.update((ride) => {
                return {...ride, to: address.name, toAddress: address.display_name};
              });
            });
          
        }, (e) => {
          console.error("Error fetching ride data:", e);

        });
      });
    
    effect(() => {
      if (this.isinit) {
        const latcenter = this.ride()?.departure?.latitude + (this.ride()?.destination?.latitude - this.ride()?.departure?.latitude) / 2;
        const loncenter = this.ride()?.departure?.longitude + (this.ride()?.destination?.longitude - this.ride()?.departure?.longitude) / 2;
        const distance = this.mapService.geoLocDistance(this.ride()?.departure?.latitude, this.ride()?.departure?.longitude, this.ride()?.destination?.latitude, this.ride()?.destination?.longitude)
        const zm = this.mapService.calculateZoomLevel(distance);
        this.map.setView([latcenter, loncenter], zm  ); 
        this.setmarker();
      }

    });
    effect(() => {
          

            if(this.ride()?.driver?.email && ! this.ride()?.driver?.photoUrl){    this.apiService.getImage(this.ride()?.driver?.email).then((imageData) => {
          this.ride.update((ride) => {
            this.effected = true;
            return {...ride, driver: {...ride.driver, photoUrl: imageData}};
          });
        }).catch((error) => {
          console.error("Error fetching profile image:", error);
        });}
       
  }
    );

    
       



  }
  
  
  ngOnInit(): void {
    console.log(this.user())
    this.map =L.map("map").setView([35.8065, 10.1815], 6); 
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(this.map);
    this.isinit = true;
  }


  setmarker() {
    const red = L.divIcon({
      className: 'custom-div-icon',
      html: "<i class='bi bi-geo-alt-fill' style='color:red;font-size: 40px; '></i>",
      iconSize: [30, 42],

    });
    const blue = L.divIcon({
      className: 'custom-div-icon',
      html: "<i class='bi bi-geo-alt-fill' style='color:blue;font-size: 40px; '></i>",
      iconSize: [30, 42],

    });

    const departureMarker = L.marker([this.ride()?.departure?.latitude, this.ride()?.departure?.longitude],{icon:red}).addTo(this.map)
      this.depart.set(this.ride()?.fromAddress);
      departureMarker.bindPopup(`Departure: ${this.ride()?.fromAddress}`).openPopup();
    const destinationMarker = L.marker([this.ride()?.destination?.latitude, this.ride()?.destination?.longitude],{icon:blue}).addTo(this.map)
      this.dest.set(this.ride()?.toAddress);
      destinationMarker.bindPopup(`Destination: ${this.ride()?.toAddress}`).openPopup();
  }
  
  viewDriverDetail() {
    const navigationExtras = {
      state: {
        driver: this.ride().driver
      }
    };
    this.router.navigate(['/driver-view-detail'], navigationExtras);
  }


  submitComment(): void {
    if (this.commentForm.valid && this.ride()?.id) {
      if (!this.commentForm.get('comment')?.value?.trim()) {
        alert('Please enter a comment');
        return;
      }
      
      this.apiService.addComment(this.ride().driver.email as string, this.commentForm.value).subscribe({
        next: (response) => {
          console.log('Comment added successfully', response);
          this.commentForm.reset({ comment: '', rating: 5 });
          location.reload();
        },
        error: (error) => {
          console.error('Error adding comment', error);
          alert('Failed to add comment. Please try again.');
        }
      });
    }
  }

  getAverageRating(): number {
    if (this.ride().driver.ratings.length === 0) return 0;
    const sum = this.ride().driver.ratings.reduce((acc : any, comment : any) => acc + (comment.rating || 0), 0);
    return Math.round((sum / this.ride().driver.ratings.length) * 10) / 10;
  }

  participateInRide($event: Event): void {
    const button = $event.target as HTMLButtonElement;
    button.disabled = true;
    (button.lastChild as HTMLElement).textContent = 'Processing...';
    this.apiService.participateInRide(this.ride().id).subscribe({
      next: (response) => {
        alert('You have successfully participated in the ride!');
        (button.lastChild as HTMLElement).textContent = 'Participated';
      },
      error: (error) => {
        switch (error.status) {
          case 400:
            alert('Bad Request: ' + (error.error || 'Unable to process your request.'));
            break;
          case 409:
            alert('Conflict: You have already participated in this ride.');
            (button.lastChild as HTMLElement).textContent = 'Participated';
            break;
          case HttpStatusCode.NotAcceptable:
            alert('Not Acceptable: You cannot participate in your own ride.');
            break;
          case HttpStatusCode.ImUsed:
            alert('this ride is full.');
            break;
          case HttpStatusCode.Gone:
            alert('This ride has already departed.');
            break;
          default:
            alert('An error occurred while trying to participate in the ride. Please try again later.');
        }
      }
    });
  }

  checkParticipation() {
    this.apiService.isParticipant(this.ride().id).subscribe({
      next: (data) => {
        if(data.participated){
          (this.participateButton.nativeElement.firstChild as HTMLElement).remove();
          (this.participateButton.nativeElement.lastChild as HTMLElement).textContent = 'Participated';
        }else{
          this.participateButton.nativeElement.disabled = false;
          (this.participateButton.nativeElement.lastChild as HTMLElement).textContent = 'Participate';
        }
      },
      error: (error) => {
        console.error("Error checking participation status:", error);
        this.participateButton.nativeElement.disabled = false;
      }
    });
  }

  getUser(Obj : any) {
    this.apiService.getUserInfo(Obj).subscribe({
      next: (data) => {
        Obj = data;
      },
      error: (error) => {
        console.error("Error fetching user data:", error);
      }
    });
  }

    


  

}
