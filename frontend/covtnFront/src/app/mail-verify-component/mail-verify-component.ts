import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../api-service';

@Component({
  selector: 'app-mail-verify-component',
  standalone: false,
  templateUrl: './mail-verify-component.html',
  styleUrl: './mail-verify-component.css'
})
export class MailVerifyComponent {
  constructor(
    private router: Router,
    private apiService: ApiService
  ) {
      const nav = this.router.getCurrentNavigation();
      this.elem = nav?.extras.state?.['elem'];
      if(!this.elem){
        this.router.navigate(['/auth']);
      }
  }
  form = new FormGroup({
    verificationCode: new FormControl('', [Validators.required])

  });

  private elem : any;

  onSubmit(){
    let code = this.form.get('verificationCode')?.value || '';
    code = String(code).trim();
    if(code === ''){
      alert("Please enter the verification code.");
      return;
    }
    this.elem.code = code;
    console.log(this.elem);
    this.apiService.verifyEmail(this.elem).then((isValid) => {
      if (isValid) {
        alert('Email verified successfully! You can now access your account.');
        this.router.navigate(['/']);
      } else {
        alert('Invalid verification code. Please try again or request a new code.');
      }
      return
    });

  }



}
