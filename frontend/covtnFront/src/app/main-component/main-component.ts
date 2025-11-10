import { Component,Input } from '@angular/core';
import { ApiService } from '../api-service';

@Component({
  selector: 'app-main-component',
  standalone: false,
  templateUrl: './main-component.html',
  styleUrl: './main-component.css'
})
export class MainComponent {
  @Input() userData: any;
  protected user = ApiService.user;

  constructor(protected apiService: ApiService) { }
}
