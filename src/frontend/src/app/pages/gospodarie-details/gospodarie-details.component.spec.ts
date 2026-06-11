import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GospodarieDetailsComponent } from './gospodarie-details.component';

describe('GospodarieDetailsComponent', () => {
  let component: GospodarieDetailsComponent;
  let fixture: ComponentFixture<GospodarieDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GospodarieDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GospodarieDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
