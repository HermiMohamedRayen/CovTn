import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverViewDetail } from './driver-view-detail';

describe('DriverViewDetail', () => {
  let component: DriverViewDetail;
  let fixture: ComponentFixture<DriverViewDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DriverViewDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverViewDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
