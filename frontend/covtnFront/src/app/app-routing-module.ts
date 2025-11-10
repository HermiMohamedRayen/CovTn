import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from './main-component/main-component';
import { AuthGuard } from './auth-gard-guard';
import { AuthentificationComponent } from './authentification-component/authentification-component';
import { MailVerifyComponent } from './mail-verify-component/mail-verify-component';
import { AdminGuard } from './admin-guard-guard';
import { ProfileComponent } from './profile-component/profile-component';

const routes: Routes = [
  { path: '', component: MainComponent, canActivate: [AuthGuard] },
  { path: 'auth', component: AuthentificationComponent },
  { path: 'admin', loadChildren: () => import('./admin/admin-module').then(m => m.AdminModule), canActivate: [AuthGuard, AdminGuard], canActivateChild: [AdminGuard] },
  { path: 'mail-verify', component: MailVerifyComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] }


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
