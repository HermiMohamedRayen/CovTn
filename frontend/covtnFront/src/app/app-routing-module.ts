import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from './home/main-component/main-component';
import { AuthGuard } from './auth-gard-guard';
import { AuthentificationComponent } from './authentification-component/authentification-component';
import { MailVerifyComponent } from './mail-verify-component/mail-verify-component';
import { AdminGuard } from './admin-guard-guard';
import { ProfileComponent } from './home/profile-component/profile-component';
import { RideDetail } from './home/ride-detail/ride-detail';
import { ProposeRide } from './home/propose-ride/propose-ride';
import { HomeComponent } from './home/home-component/home-component';

const routes: Routes = [
  { path: '', component: HomeComponent , loadChildren: () => import('./home/home-module').then(m => m.HomeModule) , canActivate: [AuthGuard] , canActivateChild: [AuthGuard] },
  { path: 'auth', component: AuthentificationComponent },
  { path: 'admin', loadChildren: () => import('./admin/admin-module').then(m => m.AdminModule), canActivate: [AuthGuard, AdminGuard], canActivateChild: [AdminGuard] },
  { path: 'mail-verify', component: MailVerifyComponent },
  


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
