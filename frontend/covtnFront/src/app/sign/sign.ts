import { Component, EventEmitter, Output } from '@angular/core';
import { FormGroup,FormControl, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { matchPass } from './matchPass';
@Component({
  selector: 'app-sign',
  standalone: false,
  templateUrl: './sign.html',
  styleUrl: './sign.css'
})
export class Sign {
  form = new FormGroup({
    firstName: new FormControl('', [Validators.required, Validators.minLength(2), Validators.pattern('[a-zA-Z ]*')]),
    lastName: new FormControl('', [Validators.required, Validators.minLength(2), Validators.pattern('[a-zA-Z ]*')]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', [Validators.required])
  });

  constructor() {
    const confirmPasswordControl = this.form.get('confirmPassword');
    confirmPasswordControl?.setValidators([Validators.required, matchPass(this.form.get('password') as FormControl)]);
  }

  @Output() signUp = new EventEmitter<{ firstName: string; lastName: string; email: string; password: string }>();

  submit() {
    if (this.form.valid) {
      this.signUp.emit({
        firstName: this.form.get('firstName')?.value || '',
        lastName: this.form.get('lastName')?.value || '',
        email: this.form.get('email')?.value || '',
        password: this.form.get('password')?.value || ''
      });
    }else {
      alert("Form is invalid. Please check the entered data.");
    }
  }

}

