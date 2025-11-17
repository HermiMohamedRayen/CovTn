import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideItem } from './ride-item';

describe('RideItem', () => {
  let component: RideItem;
  let fixture: ComponentFixture<RideItem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RideItem]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideItem);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
