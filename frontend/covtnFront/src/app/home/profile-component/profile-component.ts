import { Component, effect, OnInit } from '@angular/core';
import { ApiService } from '../../api-service';
import { Navigation, NavigationExtras, Router } from '@angular/router';
import { Location } from '@angular/common';
import { App } from '../../app';



@Component({
  selector: 'app-profile-component',
  standalone: false,
  templateUrl: './profile-component.html',
  styleUrls: [
    './profile-component.css'
  ]
  
})
export class ProfileComponent {
  user = ApiService.user;

  noimg = -1;
  imgUrl = "";

  constructor(
    protected apiService: ApiService,
    private router: Router,
    private location: Location
  ) { 

      this.loadImage();

    

    }

    
  updateimg() {
    if(confirm("Do you want to update your profile image?")){
      
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/*';
      input.onchange = async (event: any) => {
        const file = event.target.files[0];
        if (file) {
          App.loading.set(true);
          this.apiService.updateProfileImage(file).then(() => {
            console.log("Profile image updated successfully");
            window.location.reload();
            App.loading.set(false);
            this.loadImage();
          }).catch((error) => {
            console.error("Error updating profile image:", error);
            App.loading.set(false);
          });
        }
      }
      input.click();
    }
  }

  

  logout() {
    this.apiService.logout();
    window.location.reload()
  }

  previousPage() {
    this.location.back();
  }

  viewCar() {
    const navigationExtras : NavigationExtras = {
      state: {
        car: this.user().car
      }
    };
    this.router.navigate(['/view-car'], navigationExtras);
  }

  updatePhoneNumber() {
    const newPhoneNumber = confirm("do you want to update your phone number?");
    if (newPhoneNumber) {
      const phoneNumber = prompt("Please enter your new phone number:", String(this.user().number || ''));
      if (phoneNumber !== null && phoneNumber.trim() !== '') {
        this.apiService.updatePhoneNumber(phoneNumber).subscribe({
          next: () => {
            alert("Phone number updated successfully.");
            this.user().number = phoneNumber;
          },
          error: (error) => {
            console.error("Error updating phone number:", error);
            alert("Failed to update phone number. Please try again.");
          }
        });
      }
    }
  }
  viewParticipations() {
    this.router.navigate(['/my-participations']);
  }

  getAverageRating() {
    if (!this.user().ratings || this.user().ratings.length === 0) {
      return "N/A";
    }
    const sum = this.user().ratings.reduce((acc:any, rating:any) => acc + rating.rating, 0);
    return Math.round((sum / this.user().ratings.length) * 10) / 10;
  }

  loadImage() {
    this.apiService.getProfileImage().then((imageData) => {
      this.imgUrl = imageData.toString();
      this.noimg = 1;
    }).catch((error) => {
      console.error("Error fetching profile image:", error);
      this.noimg = 0;
    });
  }
}