import { Component, EventEmitter } from '@angular/core';
import { Output } from '@angular/core';
import { FormGroup,FormControl, Validators } from '@angular/forms';
@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css'

})
export class Login {
  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
  });
  @Output() login = new EventEmitter<{ email: string; password: string }>();

  protected submit(){
    this.login.emit({ email: this.form.get('email')?.value || '', password: this.form.get('password')?.value || '' });
  }

}
