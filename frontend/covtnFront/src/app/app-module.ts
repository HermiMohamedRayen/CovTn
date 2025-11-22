import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Login } from './login/login';
import { Sign } from './sign/sign';
import { AuthentificationComponent } from './authentification-component/authentification-component';
import { MailVerifyComponent } from './mail-verify-component/mail-verify-component';

import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ViewCarComponent } from './home/view-car-component/view-car-component';
import { DriverViewDetail } from './home/driver-view-detail/driver-view-detail';
import { HttpAuthInterceptor } from './http.interceptor';


@NgModule({
  declarations: [
    App,
    Login,
    Sign,
    AuthentificationComponent,
    MailVerifyComponent,

    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    MatSlideToggleModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpAuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [App]
})
export class AppModule { }
