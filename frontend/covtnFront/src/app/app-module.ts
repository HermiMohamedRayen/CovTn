import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Login } from './login/login';
import { Sign } from './sign/sign';
import { MainComponent } from './main-component/main-component';
import { NavBar } from './nav-bar/nav-bar';
import { AuthentificationComponent } from './authentification-component/authentification-component';


@NgModule({
  declarations: [
    App,
    Login,
    Sign,
    MainComponent,
    NavBar,
    AuthentificationComponent,
    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
