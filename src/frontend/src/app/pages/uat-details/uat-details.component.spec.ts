import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UatDetailsComponent } from './uat-details.component';

describe('UatDetailsComponent', () => {
  let component: UatDetailsComponent;
  let fixture: ComponentFixture<UatDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UatDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UatDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
