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
import { MailVerifyComponent } from './mail-verify-component/mail-verify-component';
import { ProfileComponent } from './profile-component/profile-component';

import { MatSlideToggleModule } from '@angular/material/slide-toggle';


@NgModule({
  declarations: [
    App,
    Login,
    Sign,
    MainComponent,
    NavBar,
    AuthentificationComponent,
    MailVerifyComponent,
    ProfileComponent,

    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    MatSlideToggleModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
