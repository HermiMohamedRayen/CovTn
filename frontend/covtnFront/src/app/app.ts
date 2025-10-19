import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('covtnFront');
  protected auth = false;
  protected sw = false;
  protected swName = "sign in";
  protected signUp = false;

  perm(){
    this.sw = !this.sw
    switch(this.sw){
      case false: this.swName = "sign in"; break;
      case true: this.swName = "log in"; break;
    }
  }

  verifMail(){
    this.auth = true;
  }

  loginUser($event: { email: string; password: string }){
     alert(`Logged in with Email: ${$event.email}\nPassword: ${$event.password}`);
     this.auth = true;
  }
  signUpUser($event: { firstName: string; lastName: string; email: string; password: string }){
    alert(`Signed up with\nFirst Name: ${$event.firstName}\nLast Name: ${$event.lastName}\nEmail: ${$event.email}\nPassword: ${$event.password}`);
  }
}
