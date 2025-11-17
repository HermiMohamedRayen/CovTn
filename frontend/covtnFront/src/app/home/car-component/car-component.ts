import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../../api-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-car-component',
  standalone: false,
  templateUrl: './car-component.html',
  styleUrl: './car-component.css'
})
export class CarComponent {

  form: FormGroup;
  photosUrls: string[] = [];

  car : {matriculationNumber: string, model: string, seats: number, airConditioner: boolean, smoker: boolean, photos: File[]} ;

  constructor(private fb: FormBuilder, protected apiService: ApiService, private router: Router) {
    this.form = this.fb.group({
      matriculationNumber: ['', Validators.required],
      model: ['', Validators.required],
      seats: [1, [Validators.required, Validators.min(1)]],
      airConditioner: [false],
      smoker: [false],
      photos: this.fb.array([]) 
    });
    this.car = this.form.value;
  }

  get photos(): FormArray {
    return this.form.get('photos') as FormArray;
  }

  addPhoto(inp : any) {
    if(this.photos.length >= 5){
      alert("You can upload a maximum of 5 photos.");
      return;
    }
    if(inp.files && inp.files.length > 0){
        if(inp.files.length + this.photos.length > 5){  
          alert("You can upload a maximum of 5 photos.");
          return;
        }
        const fl = inp.files as FileList;
        Array.from(fl).map((file: any) => this.photos.push(this.fb.control(file)) );
        this.photosUrls = [];
        for ( let i = 0; i < this.photos.length; i++) {
          this.photosUrls.push(URL.createObjectURL( this.photos.at(i).value) );
        }
      }  

      
    
  }

  removePhoto(i: number) {
    this.photos.removeAt(i);
    this.photosUrls.splice(i, 1);
  }

  onSubmit() {

      this.car = this.form.value;
      console.log(this.car);
    if (this.form.valid) {
      
      
 
      this.apiService.addCar(this.car).subscribe({
        next: (response) => {
          console.log('Car added successfully', response);
          alert('Car added successfully!');
          this.router.navigate(['/propose-ride']);
        },
        error: (error) => {
          console.error('Error adding car', error);
        }
      });
    } else {
      this.form.markAllAsTouched();
    }
  }
}
