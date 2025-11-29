import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from '../main-component/main-component';
import { ProfileComponent } from '../profile-component/profile-component';
import { RideDetail } from '../ride-detail/ride-detail';
import { ProposeRide } from '../propose-ride/propose-ride';
import { SearchComponent } from '../search-component/search-component';
import { CarComponent } from '../car-component/car-component';
import { MyRidesComponent } from '../my-rides-component/my-rides-component';
import { ViewCarComponent } from '../view-car-component/view-car-component';
import { DriverViewDetail } from '../driver-view-detail/driver-view-detail';
import { MyParticipationComponent } from '../my-participation-component/my-participation-component';




const routes: Routes = [
  { path: '', component: MainComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'ride-detail/:id', component: RideDetail },
  { path: 'propose-ride', component: ProposeRide },
  { path: 'search', component: SearchComponent },
  { path: 'car', component: CarComponent },
  { path: 'my-rides', component: MyRidesComponent },
  { path: 'view-car', component: ViewCarComponent },
  { path: 'driver-view-detail', component: DriverViewDetail },
  { path: 'my-participations', component: MyParticipationComponent },
  


];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class HomeRouterModule { }
