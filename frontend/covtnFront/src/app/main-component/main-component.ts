import { Component,Input } from '@angular/core';

@Component({
  selector: 'app-main-component',
  standalone: false,
  templateUrl: './main-component.html',
  styleUrl: './main-component.css'
})
export class MainComponent {
  @Input() userData: any;
}
