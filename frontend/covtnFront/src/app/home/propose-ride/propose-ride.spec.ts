import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProposeRide } from './propose-ride';

describe('ProposeRide', () => {
  let component: ProposeRide;
  let fixture: ComponentFixture<ProposeRide>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProposeRide]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProposeRide);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
