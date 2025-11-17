import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainComponent } from './main-component/main-component';
import { ProfileComponent } from './profile-component/profile-component';
import { RideDetail } from './ride-detail/ride-detail';
import { ProposeRide } from './propose-ride/propose-ride';
import { AppRoutingModule } from '../app-routing-module';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { HomeComponent } from './home-component/home-component';
import { NavBar } from './nav-bar/nav-bar';
import { HomeRouterModule } from './home-router/home-router-module';
import { SearchComponent } from './search-component/search-component';
import { CarComponent } from './car-component/car-component';
import { MyRidesComponent } from './my-rides-component/my-rides-component';
import { ViewCarComponent } from './view-car-component/view-car-component';
import { DriverViewDetail } from './driver-view-detail/driver-view-detail';
import { RideItem } from './ride-item/ride-item';



@NgModule({
  
    declarations: [

    NavBar,
    MainComponent,
    ProfileComponent,
    RideDetail,
    ProposeRide,
    HomeComponent,
    SearchComponent,
    CarComponent,
    MyRidesComponent,
    ViewCarComponent,
    DriverViewDetail,
    RideItem

    
  ],
  imports: [
    HomeRouterModule,
    ReactiveFormsModule,
    HttpClientModule,
    MatSlideToggleModule,
    CommonModule
  ]
})
export class HomeModule { }
