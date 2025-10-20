import { Component, signal,OnInit } from '@angular/core';
import { ApiService } from './api-service';

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
  protected userData: any;

  constructor(private apiService: ApiService) {
    if(localStorage.getItem('token')!==null){
      const token = localStorage.getItem('token')?.toString() || '';
      this.apiService.validateToken(token).subscribe({
        next: (response) => {
          this.auth = true;
          this.userData = response;
        },
        error: (error) => {
          this.auth = false;
          localStorage.removeItem('token');
          console.error('Token validation error:', error);
        }
      });
    }
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
    const user = {username: $event.email, password: $event.password};
    this.apiService.login(user).subscribe({
      next: (response) => {
        const token = response;
        localStorage.setItem('token', token.toString());
        this.auth = true;
      }
      ,
      error: (error) => {
        alert('Login failed. Please check your credentials.');
        console.error('Login error:', error);
      }
    });

     
  }
  signUpUser($event: { firstName: string; lastName: string; email: string; password: string }){
    this.apiService.signUp($event).subscribe({
      next: (response) => {
        alert('Registration successful! You can now log in.');
        this.perm();
      },
      error: (error) => {
        alert('Registration failed. Please try again.');
        console.error('Registration error:', error);
      }
    });
  }
}
