import { Component, input, Input, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../api-service';
import { Location } from '@angular/common';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-view-car-component',
  standalone: false,
  templateUrl: './view-car-component.html',
  styleUrl: './view-car-component.css'
})
export class ViewCarComponent implements OnInit{

  protected user = ApiService.user;

  @Input({required:false}) inputcar!: any;
  car = signal({ photos: [], photosUrl: [] } as any);
  extra: any;
  editMode = signal(false);
  editForm: FormGroup;
  newPhotosUrls: string[] = [];
  photosToRemove: string[] = [];
  isSaving = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  constructor(private router: Router, private apiService: ApiService, private location: Location, private fb: FormBuilder) {
    this.extra = this.router.currentNavigation()?.extras?.state?.['car'];
    this.editForm = this.fb.group({
      matriculationNumber: ['', [Validators.required, Validators.minLength(3)]],
      model: ['', [Validators.required, Validators.minLength(2)]],
      seats: [1, [Validators.required, Validators.min(1), Validators.max(9)]],
      airConditioner: [false],
      smoker: [false]
    });
  }

  ngOnInit(): void {
    if(this.inputcar){
      this.car.set({
        ...this.inputcar,
        photos: this.inputcar.photos || [],
        photosUrl: this.inputcar.photosUrl || []
      });
    } else {
      this.car.set({
        ...this.extra,
        photos: this.extra?.photos || [],
        photosUrl: this.extra?.photosUrl || []
      });
    }
    if(!this.car() || !this.car().id){
      this.location.back();
      return;
    }
    this.loadCarPhotos();
    this.initEditForm();
  }

  loadCarPhotos(): void {
    const photos = this.car().photos || [];
    const userEmail = this.car().user?.email || this.car().user;
    
    if (!userEmail) {
      console.warn("User email not found for loading car photos");
      return;
    }

    Array.from(photos).forEach((photo: any) => {
      if (typeof photo === 'string') {
        this.apiService.getCarPhotoByName(photo, userEmail).then((url) => {
          this.car.update((car) => {
            const photosUrl = car.photosUrl || [];
            if (!photosUrl.includes(url)) {
              photosUrl.push(url);
            }
            return {...car, photosUrl: photosUrl};
          });
        }).catch((error) => {
          console.error("Error fetching car photo URL:", error);
        });
      }
    });
  }

  initEditForm(): void {
    this.editForm.patchValue({
      matriculationNumber: this.car().matriculationNumber,
      model: this.car().model,
      seats: this.car().seats,
      airConditioner: this.car().airConditioner,
      smoker: this.car().smoker
    });
  }

  toggleEditMode(): void {
    if (this.editMode()) {
      this.editMode.set(false);
      this.newPhotosUrls = [];
      this.photosToRemove = [];
      this.initEditForm();
      this.clearMessages();
    } else {
      this.editMode.set(true);
      this.clearMessages();
    }
  }

  addPhotos(input: any): void {
    if (input.files && input.files.length > 0) {
      const currentPhotos = this.car().photosUrl?.length || 0;
      const newPhotos = input.files.length;
      
      if (currentPhotos + newPhotos > 5) {
        this.errorMessage.set('Maximum 5 photos allowed. You have ' + currentPhotos + ' photos.');
        setTimeout(() => this.clearMessages(), 3000);
        return;
      }

      this.car.update((car) => {
        const photos = car.photos || [];
        for (let file of input.files) {
          this.newPhotosUrls.push(URL.createObjectURL(file));
          photos.push(file);
        }
        return {...car, photos};
      });
      
      this.successMessage.set(`Added ${input.files.length} photo(s)`);
      setTimeout(() => this.clearMessages(), 2000);
    }
  }

  removePhoto(index: number): void {
    this.car.update((car) => {
      const photos = car.photos || [];
      const photosUrl = car.photosUrl || [];
      
      if (index < photosUrl.length) {
        const photoToRemove = photos[index];
        if (typeof photoToRemove === 'string') {
          this.photosToRemove.push(photoToRemove);
        }
      }
      
      if (index < photos.length) {
        photos.splice(index, 1);
      }
      if (index < photosUrl.length) {
        photosUrl.splice(index, 1);
      }
      
      return {...car, photos, photosUrl};
    });
    
    this.successMessage.set('Photo removed');
    setTimeout(() => this.clearMessages(), 2000);
  }

  onSave(): void {
    this.clearMessages();
    if (!this.editForm.valid) {
      this.errorMessage.set('Please check all required fields');
      return;
    }
    
    if (this.car().id) {
      this.isSaving.set(true);
      const updatedCar = {
        ...this.car(),
        ...this.editForm.value,
        photosToRemove: this.photosToRemove
      };

      this.apiService.updateCar(this.car().id, updatedCar).subscribe({
        next: (response) => {
          this.successMessage.set('Car updated successfully! 🎉');
          this.isSaving.set(false);
          setTimeout(() => {
            this.editMode.set(false);
            this.router.navigate(['/profile']);
          }, 1500);
        },
        error: (error) => {
          this.isSaving.set(false);
          console.error('Error updating car', error);
          this.errorMessage.set('Failed to update car. Please try again.');
        }
      });
    }
  }

  clearMessages(): void {
    this.successMessage.set('');
    this.errorMessage.set('');
  }

  getPhotoCount(): number {
    const existingPhotos = this.car()?.photosUrl?.length || 0;
    return existingPhotos + this.newPhotosUrls.length;
  }
}
