import { Component, Output, signal } from '@angular/core';
import { ApiService } from '../api-service';
import { NavigationExtras, Router } from '@angular/router';
import { App } from '../app';

@Component({
  selector: 'app-authentification-component',
  standalone: false,
  templateUrl: './authentification-component.html',
  styleUrl: './authentification-component.css'
})
export class AuthentificationComponent {
  protected readonly title = signal('covtnFront');
  protected auth = false;
  protected sw = false;
  protected swName = "sign in";
  protected signUp = false;
  protected userData: any;

  constructor(private apiService: ApiService,private router: Router) {
    this.apiService.isAuthenticated().then((authenticated => {
      this.auth = authenticated;
      this.router.navigate(['/']);
    }));
  }

 

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
    App.loading.set(true);
    const user = {username: $event.email, password: $event.password};
    this.apiService.login(user).subscribe({
      next: (elem: String) => {
        const val = JSON.parse(elem.toString());
        const toVerify : NavigationExtras = {state: { elem: val } };
        this.router.navigate(['/mail-verify'], toVerify);
        App.loading.set(false);
      },
      error: (error) => {
        alert("Login failed: " + error.error);
        App.loading.set(false);
      }
    });

     
  }
  signUpUser($event: { firstName: string; lastName: string; email: string; password: string }){
    App.loading.set(true);
    this.apiService.signUp($event).subscribe({
      next: (response) => {
        alert('Registration successful! You can now log in.');
        this.perm();
      },
      error: (error) => {
        alert('Registration failed. Please try again.');
        console.error('Registration error:', error);
        App.loading.set(false);
      }
    });
  }


}


